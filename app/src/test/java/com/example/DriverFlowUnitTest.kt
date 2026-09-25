package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.core.database.SeedData
import com.example.core.model.DriverOnlineState
import com.example.core.model.LedgerDirection
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceType
import com.example.core.repository.DriverRepository
import com.example.core.repository.OrderRepository
import com.example.core.repository.WalletRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DriverFlowUnitTest {

  private lateinit var database: AppDatabase
  private lateinit var driverRepository: DriverRepository
  private lateinit var orderRepository: OrderRepository
  private lateinit var walletRepository: WalletRepository

  @Before
  fun setup() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    SeedData.populateDatabase(database)
    driverRepository = DriverRepository(database)
    orderRepository = OrderRepository(database)
    walletRepository = WalletRepository(database)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testDriverOnlineOfflineToggle() = runBlocking {
    val initialProfile = driverRepository.getDriverProfile("user_driver_01").first()
    assertNotNull(initialProfile)

    // Toggle Offline
    driverRepository.updateOnlineState("user_driver_01", DriverOnlineState.OFFLINE)
    val offlineProfile = driverRepository.getDriverProfile("user_driver_01").first()
    assertEquals(DriverOnlineState.OFFLINE, offlineProfile!!.onlineState)

    // Toggle Online
    driverRepository.updateOnlineState("user_driver_01", DriverOnlineState.ONLINE)
    val onlineProfile = driverRepository.getDriverProfile("user_driver_01").first()
    assertEquals(DriverOnlineState.ONLINE, onlineProfile!!.onlineState)
  }

  @Test
  fun testDriverKycDocumentSubmission() = runBlocking {
    driverRepository.submitKycDocuments(
      driverId = "user_driver_01",
      ktp = "3171012345670001",
      sim = "SIM-99887766",
      stnk = "STNK-11223344",
      bankName = "Bank Central Asia (BCA)",
      accountNum = "8899001122",
      accountHolder = "Ahmad Fauzi"
    )

    val updatedProfile = driverRepository.getDriverProfile("user_driver_01").first()
    assertNotNull(updatedProfile)
    assertEquals("3171012345670001", updatedProfile!!.ktpNumber)
    assertEquals("SIM-99887766", updatedProfile.simNumber)
    assertEquals("Bank Central Asia (BCA)", updatedProfile.bankName)
    assertEquals("VERIFIED", updatedProfile.verificationState)
  }

  @Test
  fun testDriverAcceptAndCompleteTripFlow() = runBlocking {
    // 1. Create a matching order
    val quote = orderRepository.calculateQuote(ServiceType.RIDE_BIKE, 5.0, null)
    val order = orderRepository.createOrder(
      serviceType = ServiceType.RIDE_BIKE,
      customerId = "user_cust_01",
      customerName = "Sukma Wijaya",
      pickupAddress = "Grand Indonesia Mall",
      pickupLat = -6.1950,
      pickupLng = 106.8230,
      destAddress = "Senayan City",
      destLat = -6.2270,
      destLng = 106.7975,
      quote = quote,
      paymentMethod = PaymentMethod.WALLET,
      promoCode = null,
      idempotencyKey = "driver-test-flow-1"
    )
    assertEquals(OrderStatus.MATCHING, order.status)

    // 2. Driver receives and accepts the offer
    val driverProfile = driverRepository.getDriverProfile("user_driver_01").first()!!
    driverRepository.acceptOffer(order.id, driverProfile)

    val acceptedOrder = orderRepository.getOrderById(order.id).first()!!
    assertEquals(OrderStatus.DRIVER_ASSIGNED, acceptedOrder.status)
    assertEquals("user_driver_01", acceptedOrder.driverId)
    assertEquals(driverProfile.fullName, acceptedOrder.driverName)

    // 3. Driver arrives at pickup
    orderRepository.updateOrderStatus(order.id, OrderStatus.ARRIVED)
    val arrivedOrder = orderRepository.getOrderById(order.id).first()!!
    assertEquals(OrderStatus.ARRIVED, arrivedOrder.status)

    // 4. Passenger on-board, trip starts
    orderRepository.updateOrderStatus(order.id, OrderStatus.IN_TRIP)
    val inTripOrder = orderRepository.getOrderById(order.id).first()!!
    assertEquals(OrderStatus.IN_TRIP, inTripOrder.status)

    // 5. Trip completion and ledger settlement
    val balanceBefore = walletRepository.getCalculatedBalance("w_driver_01").first()
    orderRepository.completeTrip(inTripOrder)

    val completedOrder = orderRepository.getOrderById(order.id).first()!!
    assertEquals(OrderStatus.COMPLETED, completedOrder.status)

    val balanceAfter = walletRepository.getCalculatedBalance("w_driver_01").first()
    val expectedNet = inTripOrder.totalFare - inTripOrder.platformFee
    assertEquals(balanceBefore + expectedNet, balanceAfter)
  }

  @Test
  fun testDriverIncentiveBonusClaimAndPayout() = runBlocking {
    val initialBalance = walletRepository.getCalculatedBalance("w_driver_01").first()

    // Claim bonus of Rp 35.000
    val bonusAmount = 35000L
    driverRepository.claimIncentiveBonus("user_driver_01", bonusAmount)

    val balanceAfterBonus = walletRepository.getCalculatedBalance("w_driver_01").first()
    assertEquals(initialBalance + bonusAmount, balanceAfterBonus)

    // Request Payout of Rp 50.000
    val payoutAmount = 50000L
    driverRepository.requestPayout("user_driver_01", payoutAmount, "BCA 12345678")

    val balanceAfterPayout = walletRepository.getCalculatedBalance("w_driver_01").first()
    assertEquals(balanceAfterBonus - payoutAmount, balanceAfterPayout)

    // Verify ledger entries recorded properly
    val ledger = walletRepository.getLedgerEntries("w_driver_01").first()
    assertTrue(ledger.any { it.description.contains("Bonus") && it.direction == LedgerDirection.CREDIT })
    assertTrue(ledger.any { it.description.contains("Penarikan") && it.direction == LedgerDirection.DEBIT })
  }
}
