package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.core.database.SeedData
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceType
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
class CustomerFlowUnitTest {

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
  fun testFareQuoteCalculation() = runBlocking {
    val quoteBike = orderRepository.calculateQuote(ServiceType.RIDE_BIKE, 4.0, null)
    assertTrue("Total fare must be greater than base fare", quoteBike.totalFare > quoteBike.baseFare)
    assertEquals(ServiceType.RIDE_BIKE, quoteBike.serviceType)

    val quoteCar = orderRepository.calculateQuote(ServiceType.RIDE_CAR, 4.0, null)
    assertTrue("Car fare must be higher than bike fare", quoteCar.totalFare > quoteBike.totalFare)
  }

  @Test
  fun testFareQuotePromoDiscount() = runBlocking {
    val quoteNoPromo = orderRepository.calculateQuote(ServiceType.RIDE_CAR, 10.0, null)
    val quoteWithPromo = orderRepository.calculateQuote(ServiceType.RIDE_CAR, 10.0, "SUKMABARU")

    assertTrue("Promo discount must be applied", quoteWithPromo.discountAmount > 0)
    assertTrue("Total fare with promo must be lower", quoteWithPromo.totalFare < quoteNoPromo.totalFare)
  }

  @Test
  fun testOrderLifecycleAndLedgerSettlement() = runBlocking {
    val quote = orderRepository.calculateQuote(ServiceType.RIDE_BIKE, 3.5, "SUKMABARU")
    val order = orderRepository.createOrder(
      serviceType = ServiceType.RIDE_BIKE,
      customerId = "user_cust_01",
      customerName = "Sukma Wijaya",
      pickupAddress = "Grand Indonesia Mall",
      pickupLat = -6.1950,
      pickupLng = 106.8230,
      destAddress = "Stasiun Sudirman",
      destLat = -6.2025,
      destLng = 106.8235,
      quote = quote,
      paymentMethod = PaymentMethod.WALLET,
      promoCode = "SUKMABARU",
      idempotencyKey = "test-key-123"
    )

    assertEquals(OrderStatus.MATCHING, order.status)

    // Simulate driver matching
    orderRepository.assignDriverSimulated(order.id, "user_driver_01")
    val assignedOrder = orderRepository.getOrderById(order.id).first()
    assertNotNull(assignedOrder)
    assertEquals(OrderStatus.DRIVER_ASSIGNED, assignedOrder!!.status)
    assertEquals("Ahmad Fauzi", assignedOrder.driverName)

    // Advance to IN_TRIP
    orderRepository.updateOrderStatus(order.id, OrderStatus.IN_TRIP)
    val inTripOrder = orderRepository.getOrderById(order.id).first()
    assertEquals(OrderStatus.IN_TRIP, inTripOrder!!.status)

    // Complete Trip
    orderRepository.completeTrip(inTripOrder)
    val completedOrder = orderRepository.getOrderById(order.id).first()
    assertEquals(OrderStatus.COMPLETED, completedOrder!!.status)

    // Submit rating & tip
    orderRepository.submitRatingAndTip(order.id, 5, 5000L)
    val ratedOrder = orderRepository.getOrderById(order.id).first()
    assertEquals(5, ratedOrder!!.rating)
    assertEquals(5000L, ratedOrder.tipAmount)
  }
}
