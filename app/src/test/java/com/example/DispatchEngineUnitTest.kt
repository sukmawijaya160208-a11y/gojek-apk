package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.core.database.SeedData
import com.example.core.engine.DispatchEngine
import com.example.core.engine.GeoRoutingEngine
import com.example.core.engine.PricingEngine
import com.example.core.engine.TimeoutAction
import com.example.core.engine.TripStateMachine
import com.example.core.model.DriverOnlineState
import com.example.core.model.LedgerDirection
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import com.example.core.repository.OrderRepository
import com.example.core.repository.WalletRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DispatchEngineUnitTest {

  private lateinit var database: AppDatabase
  private lateinit var orderRepository: OrderRepository
  private lateinit var walletRepository: WalletRepository

  @Before
  fun setup() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    SeedData.populateDatabase(database)
    orderRepository = OrderRepository(database)
    walletRepository = WalletRepository(database)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testHaversineDistanceAndEtaCalculation() {
    // Jakarta Central: Grand Indonesia (-6.1950, 106.8230) to Stasiun Gambir (-6.1767, 106.8306)
    val distance = GeoRoutingEngine.calculateRoadDistanceKm(-6.1950, 106.8230, -6.1767, 106.8306)
    assertTrue("Distance between GI and Gambir should be roughly 2-4 km", distance in 1.8..4.5)

    // ETA computation for Motor vs Car
    val durationMotor = GeoRoutingEngine.estimateDurationMinutes(distance, ServiceType.RIDE_BIKE, isPeakHour = false)
    val durationCar = GeoRoutingEngine.estimateDurationMinutes(distance, ServiceType.RIDE_CAR, isPeakHour = false)
    assertTrue("Car ETA should be higher or equal to bike due to traffic speed profile", durationCar >= durationMotor)
    assertTrue("Minimum duration should be at least 5 minutes", durationMotor >= 5)
  }

  @Test
  fun testTripStateMachineValidAndInvalidTransitions() {
    // 1. Legal forward transitions
    assertTrue(TripStateMachine.canTransition(OrderStatus.CREATED, OrderStatus.MATCHING))
    assertTrue(TripStateMachine.canTransition(OrderStatus.MATCHING, OrderStatus.DRIVER_ASSIGNED))
    assertTrue(TripStateMachine.canTransition(OrderStatus.DRIVER_ASSIGNED, OrderStatus.ARRIVING))
    assertTrue(TripStateMachine.canTransition(OrderStatus.ARRIVING, OrderStatus.ARRIVED))
    assertTrue(TripStateMachine.canTransition(OrderStatus.ARRIVED, OrderStatus.IN_TRIP))
    assertTrue(TripStateMachine.canTransition(OrderStatus.IN_TRIP, OrderStatus.COMPLETED))

    // 2. Illegal jumps
    assertFalse("Cannot jump directly from CREATED to IN_TRIP", TripStateMachine.canTransition(OrderStatus.CREATED, OrderStatus.IN_TRIP))
    assertFalse("Cannot jump directly from MATCHING to COMPLETED", TripStateMachine.canTransition(OrderStatus.MATCHING, OrderStatus.COMPLETED))

    // 3. Terminal state protection
    assertFalse("COMPLETED is terminal and cannot transition to MATCHING", TripStateMachine.canTransition(OrderStatus.COMPLETED, OrderStatus.MATCHING))
    assertFalse("CANCELLED is terminal and cannot transition to IN_TRIP", TripStateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.IN_TRIP))

    val resultTerminal = TripStateMachine.validateTransition(OrderStatus.COMPLETED, OrderStatus.IN_TRIP)
    assertTrue("Must return failure on terminal transition attempt", resultTerminal.isFailure)
  }

  @Test
  fun testDynamicPricingAndSurgeConfiguration() = runBlocking {
    // Base quote with normal 1.0x surge
    val quoteNormal = orderRepository.calculateQuote(
      serviceType = ServiceType.RIDE_BIKE,
      distanceKm = 5.0,
      promoCode = null,
      zoneId = "ZONE_CENTRAL"
    )
    assertEquals(1.0, quoteNormal.surgeMultiplier, 0.01)
    assertEquals(0L, quoteNormal.surgeAmount)

    // Update surge multiplier to 1.5x in database for ZONE_CENTRAL
    orderRepository.updateZoneSurge("ZONE_CENTRAL", 1.5)

    val quoteSurge = orderRepository.calculateQuote(
      serviceType = ServiceType.RIDE_BIKE,
      distanceKm = 5.0,
      promoCode = null,
      zoneId = "ZONE_CENTRAL"
    )

    assertEquals(1.5, quoteSurge.surgeMultiplier, 0.01)
    assertTrue("Surge amount should be greater than 0", quoteSurge.surgeAmount > 0)
    assertTrue("Total fare with 1.5x surge must be higher than normal", quoteSurge.totalFare > quoteNormal.totalFare)
    assertTrue("Surge flag must be active", quoteSurge.isSurgeActive)

    // Reset surge back to 1.0x
    orderRepository.updateZoneSurge("ZONE_CENTRAL", 1.0)
  }

  @Test
  fun testNearbyDriverSpatialSearchAndRanking() = runBlocking {
    // Search near Thamrin (-6.2088, 106.8456) for Motor
    val candidates = orderRepository.searchNearbyDrivers(
      pickupLat = -6.2088,
      pickupLng = 106.8456,
      serviceType = ServiceType.RIDE_BIKE,
      maxRadiusKm = 8.0
    )

    assertNotNull(candidates)
    assertTrue("Should find at least 1 online bike driver", candidates.isNotEmpty())

    val firstCandidate = candidates.first()
    assertEquals(1, firstCandidate.rank)
    assertTrue("Composite score must be positive", firstCandidate.compositeScore > 0)
    assertTrue("First candidate should be vehicle motor", firstCandidate.profile.vehicleType.contains("Motor", ignoreCase = true))

    // Verify car search excludes motorbike drivers
    val carCandidates = orderRepository.searchNearbyDrivers(
      pickupLat = -6.2240,
      pickupLng = 106.8105,
      serviceType = ServiceType.RIDE_CAR,
      maxRadiusKm = 8.0
    )
    assertTrue("Car search should return car driver", carCandidates.all { it.profile.vehicleType.contains("Mobil", ignoreCase = true) })
  }

  @Test
  fun testCascadingDispatchReassignmentAndTimeout() = runBlocking {
    val candidates = orderRepository.searchNearbyDrivers(
      pickupLat = -6.2088,
      pickupLng = 106.8456,
      serviceType = ServiceType.RIDE_BIKE,
      maxRadiusKm = 15.0
    )

    val session = orderRepository.createDispatchSession("ORD-TEST-99", candidates)
    assertEquals(0, session.currentIndex)
    assertNotNull(session.currentCandidate)

    // Simulate rejection -> advance to next candidate
    val nextCandidate = orderRepository.advanceDispatchToNextCandidate(session)
    if (session.candidates.size > 1) {
      assertNotNull(nextCandidate)
      assertEquals(1, session.currentIndex)
    }

    // Simulate timeout action
    val timeoutAction = orderRepository.handleOfferTimeout(session)
    assertTrue(timeoutAction == TimeoutAction.REASSIGN_NEXT_CANDIDATE || timeoutAction == TimeoutAction.DISPATCH_EXPIRED)
  }

  @Test
  fun testCancellationEngineWithCompensationFee() = runBlocking {
    // 1. Create order
    val quote = orderRepository.calculateQuote(ServiceType.RIDE_BIKE, 4.0, null)
    val order = orderRepository.createOrder(
      serviceType = ServiceType.RIDE_BIKE,
      customerId = "user_cust_01",
      customerName = "Sukma Wijaya",
      pickupAddress = "Mall Grand Indonesia",
      pickupLat = -6.1950,
      pickupLng = 106.8230,
      destAddress = "Stasiun Gambir",
      destLat = -6.1767,
      destLng = 106.8306,
      quote = quote,
      paymentMethod = PaymentMethod.WALLET,
      promoCode = null,
      idempotencyKey = "cancel-test-1"
    )

    // Test free cancellation during MATCHING
    val cancelMatchingResult = orderRepository.cancelOrder(
      order = order,
      cancelledBy = "user_cust_01",
      actorRole = UserRole.CUSTOMER,
      reason = "Ingin ganti tujuan"
    )
    assertTrue(cancelMatchingResult.isSuccess)
    assertEquals("Cancellation during matching must be free", 0L, cancelMatchingResult.cancellationFee)

    // 2. Create another order and advance to DRIVER_ASSIGNED
    val order2 = orderRepository.createOrder(
      serviceType = ServiceType.RIDE_BIKE,
      customerId = "user_cust_01",
      customerName = "Sukma Wijaya",
      pickupAddress = "Mall Grand Indonesia",
      pickupLat = -6.1950,
      pickupLng = 106.8230,
      destAddress = "Stasiun Gambir",
      destLat = -6.1767,
      destLng = 106.8306,
      quote = quote,
      paymentMethod = PaymentMethod.WALLET,
      promoCode = null,
      idempotencyKey = "cancel-test-2"
    )

    orderRepository.assignDriverSimulated(order2.id, "user_driver_01")
    val assignedOrder = orderRepository.getOrderById(order2.id).first()!!
    assertEquals(OrderStatus.DRIVER_ASSIGNED, assignedOrder.status)

    val driverBalanceBefore = walletRepository.getCalculatedBalance("w_driver_01").first()

    // Test customer cancellation with compensation fee
    val cancelAssignedResult = orderRepository.cancelOrder(
      order = assignedOrder,
      cancelledBy = "user_cust_01",
      actorRole = UserRole.CUSTOMER,
      reason = "Salah pilih titik jemput"
    )

    assertTrue(cancelAssignedResult.isSuccess)
    assertEquals(DispatchEngine.STANDARD_CANCELLATION_FEE, cancelAssignedResult.cancellationFee)

    // Verify driver was compensated with Rp 5.000 in wallet ledger
    val driverBalanceAfter = walletRepository.getCalculatedBalance("w_driver_01").first()
    assertEquals(driverBalanceBefore + DispatchEngine.STANDARD_CANCELLATION_FEE, driverBalanceAfter)

    val driverLedger = walletRepository.getLedgerEntries("w_driver_01").first()
    assertTrue(driverLedger.any { it.referenceId == order2.id && it.reason == com.example.core.model.LedgerReason.CANCELLATION_FEE && it.direction == LedgerDirection.CREDIT })

    // Verify assigned driver online state was restored to ONLINE
    val driverProfile = database.driverDao().getDriverById("user_driver_01").first()!!
    assertEquals(DriverOnlineState.ONLINE, driverProfile.onlineState)
  }
}
