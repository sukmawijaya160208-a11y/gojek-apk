package com.example.core.repository

import com.example.core.database.AppDatabase
import com.example.core.database.SeedData
import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.LedgerEntryEntity
import com.example.core.database.entity.OrderEntity
import com.example.core.engine.CandidateDriver
import com.example.core.engine.CancellationResult
import com.example.core.engine.DispatchEngine
import com.example.core.engine.DispatchSession
import com.example.core.engine.GeoRoutingEngine
import com.example.core.engine.PricingEngine
import com.example.core.engine.PricingQuote
import com.example.core.engine.TimeoutAction
import com.example.core.engine.TripStateMachine
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

typealias PricingQuote = com.example.core.engine.PricingQuote

class OrderRepository(private val db: AppDatabase) {

  fun getActiveOrderByCustomer(customerId: String): Flow<OrderEntity?> =
    db.orderDao().getActiveOrderByCustomer(customerId)

  fun getActiveOrderByDriver(driverId: String): Flow<OrderEntity?> =
    db.orderDao().getActiveOrderByDriver(driverId)

  fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>> =
    db.orderDao().getOrdersByCustomer(customerId)

  fun getOrdersByDriver(driverId: String): Flow<List<OrderEntity>> =
    db.orderDao().getOrdersByDriver(driverId)

  fun getAllOrders(): Flow<List<OrderEntity>> =
    db.orderDao().getAllOrders()

  fun getOrderById(orderId: String): Flow<OrderEntity?> =
    db.orderDao().getOrderById(orderId)

  suspend fun calculateQuote(
    serviceType: ServiceType,
    distanceKm: Double,
    promoCode: String? = null,
    zoneId: String = "ZONE_CENTRAL"
  ): PricingQuote {
    return PricingEngine.calculateQuote(
      db = db,
      serviceType = serviceType,
      distanceKm = distanceKm,
      promoCode = promoCode,
      zoneId = zoneId
    )
  }

  suspend fun createOrder(
    serviceType: ServiceType,
    customerId: String,
    customerName: String,
    pickupAddress: String,
    pickupLat: Double,
    pickupLng: Double,
    destAddress: String,
    destLat: Double,
    destLng: Double,
    quote: PricingQuote,
    paymentMethod: PaymentMethod,
    promoCode: String?,
    idempotencyKey: String
  ): OrderEntity {
    val orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}"
    val order = OrderEntity(
      id = orderId,
      serviceType = serviceType,
      customerId = customerId,
      customerName = customerName,
      driverId = null,
      driverName = null,
      driverPlate = null,
      driverVehicle = null,
      status = OrderStatus.MATCHING,
      pickupAddress = pickupAddress,
      pickupLat = pickupLat,
      pickupLng = pickupLng,
      destAddress = destAddress,
      destLat = destLat,
      destLng = destLng,
      distanceKm = quote.distanceKm,
      durationMin = quote.durationMin,
      subtotalFare = quote.subtotal,
      platformFee = quote.platformFee,
      surgeAmount = quote.surgeAmount,
      discountAmount = quote.discountAmount,
      totalFare = quote.totalFare,
      paymentMethod = paymentMethod,
      paymentStatus = PaymentStatus.PENDING,
      promoCode = promoCode,
      idempotencyKey = idempotencyKey
    )
    db.orderDao().insertOrder(order)

    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = customerId,
        actorRole = "CUSTOMER",
        action = "ORDER_CREATED",
        entityType = "ORDER",
        entityId = orderId,
        details = "Created ${serviceType.name} from $pickupAddress to $destAddress, Total IDR ${quote.totalFare}",
        createdAt = System.currentTimeMillis()
      )
    )

    return order
  }

  suspend fun assignDriverSimulated(orderId: String, driverId: String = "user_driver_01") {
    val driverProfile = when (driverId) {
      "user_driver_02" -> Triple("Dimas Prasetyo", "B 1290 SKM", "Toyota Avanza Veloz Putih")
      else -> Triple("Ahmad Fauzi", "B 4821 SKM", "Honda Vario 160 Hitam")
    }
    db.orderDao().assignDriver(
      orderId = orderId,
      driverId = driverId,
      driverName = driverProfile.first,
      plate = driverProfile.second,
      vehicle = driverProfile.third,
      status = OrderStatus.DRIVER_ASSIGNED
    )
  }

  suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
    val currentOrder = db.orderDao().getOrderById(orderId).first()
    if (currentOrder != null) {
      val validation = TripStateMachine.validateTransition(currentOrder.status, status)
      if (validation.isFailure) {
        throw validation.exceptionOrNull() ?: IllegalStateException("Transisi status tidak valid")
      }
    }
    db.orderDao().updateOrderStatus(orderId, status)
  }

  suspend fun searchNearbyDrivers(
    pickupLat: Double,
    pickupLng: Double,
    serviceType: ServiceType,
    maxRadiusKm: Double = 6.0
  ): List<CandidateDriver> {
    return DispatchEngine.searchNearbyDrivers(db, pickupLat, pickupLng, serviceType, maxRadiusKm)
  }

  fun createDispatchSession(orderId: String, candidates: List<CandidateDriver>): DispatchSession {
    return DispatchEngine.createDispatchSession(orderId, candidates)
  }

  fun advanceDispatchToNextCandidate(session: DispatchSession): CandidateDriver? {
    return DispatchEngine.advanceToNextCandidate(session)
  }

  suspend fun handleOfferTimeout(session: DispatchSession): TimeoutAction {
    return DispatchEngine.handleOfferTimeout(db, session)
  }

  suspend fun cancelOrder(
    order: OrderEntity,
    cancelledBy: String,
    actorRole: UserRole,
    reason: String
  ): CancellationResult {
    return DispatchEngine.cancelOrder(db, order, cancelledBy, actorRole, reason)
  }

  suspend fun cancelOrder(orderId: String, reason: String, actor: String) {
    val order = db.orderDao().getOrderById(orderId).first()
    if (order != null) {
      DispatchEngine.cancelOrder(db, order, actor, UserRole.CUSTOMER, reason)
    } else {
      db.orderDao().updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }
  }

  suspend fun updateZoneSurge(zoneId: String, surgeMultiplier: Double) {
    DispatchEngine.updateZoneSurge(db, zoneId, surgeMultiplier)
  }

  suspend fun calculateDynamicSurgeMultiplier(zoneId: String): Double {
    return DispatchEngine.calculateDynamicSurgeMultiplier(db, zoneId)
  }

  suspend fun completeTrip(order: OrderEntity) {
    db.orderDao().updateOrderStatus(order.id, OrderStatus.COMPLETED)

    // Settle Ledger atomically:
    // 1. Debit Customer (Trip Fare)
    db.walletDao().insertLedgerEntry(
      LedgerEntryEntity(
        id = UUID.randomUUID().toString(),
        walletId = "w_cust_01",
        direction = LedgerDirection.DEBIT,
        amount = order.totalFare,
        reason = LedgerReason.TRIP_FARE,
        description = "Pembayaran ${order.serviceType.displayName} #${order.id}",
        referenceId = order.id,
        idempotencyKey = "pay_${order.id}"
      )
    )

    // 2. Credit Driver (Trip Fare - Platform fee)
    val driverEarning = maxOf(0L, order.totalFare - order.platformFee)
    db.walletDao().insertLedgerEntry(
      LedgerEntryEntity(
        id = UUID.randomUUID().toString(),
        walletId = "w_driver_01",
        direction = LedgerDirection.CREDIT,
        amount = driverEarning,
        reason = LedgerReason.DRIVER_EARNING,
        description = "Pendapatan trip ${order.serviceType.displayName} #${order.id}",
        referenceId = order.id,
        idempotencyKey = "earn_${order.id}"
      )
    )

    // 3. Update driver metrics
    val driverId = order.driverId ?: "user_driver_01"
    db.driverDao().incrementCompletedTrips(driverId)
    db.driverDao().updateOnlineState(driverId, com.example.core.model.DriverOnlineState.ONLINE)
  }

  suspend fun submitRatingAndTip(orderId: String, rating: Int, tipAmount: Long) {
    db.orderDao().submitRating(orderId, rating, tipAmount)
    if (tipAmount > 0) {
      db.walletDao().insertLedgerEntry(
        LedgerEntryEntity(
          id = UUID.randomUUID().toString(),
          walletId = "w_cust_01",
          direction = LedgerDirection.DEBIT,
          amount = tipAmount,
          reason = LedgerReason.TIP,
          description = "Tip apresiasi driver order #$orderId",
          referenceId = orderId,
          idempotencyKey = "tip_$orderId"
        )
      )
      db.walletDao().insertLedgerEntry(
        LedgerEntryEntity(
          id = UUID.randomUUID().toString(),
          walletId = "w_driver_01",
          direction = LedgerDirection.CREDIT,
          amount = tipAmount,
          reason = LedgerReason.TIP,
          description = "Penerimaan tip apresiasi order #$orderId",
          referenceId = orderId,
          idempotencyKey = "driver_tip_$orderId"
        )
      )
    }
  }

  suspend fun resetDatabase() {
    db.clearAllTables()
    SeedData.populateDatabase(db)
  }
}
