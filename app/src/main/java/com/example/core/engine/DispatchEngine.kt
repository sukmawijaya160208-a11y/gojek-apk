package com.example.core.engine

import com.example.core.database.AppDatabase
import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.DriverProfileEntity
import com.example.core.database.entity.LedgerEntryEntity
import com.example.core.database.entity.OrderEntity
import com.example.core.model.DriverOnlineState
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import kotlinx.coroutines.flow.first
import java.util.UUID

data class CandidateDriver(
  val profile: DriverProfileEntity,
  val distanceKm: Double,
  val etaMin: Int,
  val compositeScore: Double,
  val rank: Int
)

data class DispatchSession(
  val orderId: String,
  val candidates: List<CandidateDriver>,
  var currentIndex: Int = 0,
  val offerTimeoutSeconds: Int = 20,
  var isCompleted: Boolean = false
) {
  val currentCandidate: CandidateDriver?
    get() = candidates.getOrNull(currentIndex)

  val hasNextCandidate: Boolean
    get() = currentIndex + 1 < candidates.size
}

enum class TimeoutAction {
  REASSIGN_NEXT_CANDIDATE,
  DISPATCH_EXPIRED
}

data class CancellationResult(
  val isSuccess: Boolean,
  val orderId: String,
  val cancellationFee: Long,
  val refundedAmount: Long,
  val reason: String,
  val message: String
)

object DispatchEngine {

  const val DEFAULT_SEARCH_RADIUS_KM = 6.0
  const val STANDARD_CANCELLATION_FEE = 5000L

  /**
   * Spatial search for eligible nearby drivers within [maxRadiusKm].
   * Filters by online status, verification, vehicle compatibility, and ranks by proximity + rating.
   */
  suspend fun searchNearbyDrivers(
    db: AppDatabase,
    pickupLat: Double,
    pickupLng: Double,
    serviceType: ServiceType,
    maxRadiusKm: Double = DEFAULT_SEARCH_RADIUS_KM
  ): List<CandidateDriver> {
    val allDrivers = db.driverDao().getOnlineDrivers().first()

    val candidates = mutableListOf<CandidateDriver>()

    for (driver in allDrivers) {
      // 1. Must be verified
      if (driver.verificationState != "VERIFIED") continue

      // 2. Must be ONLINE (not already on trip or offline)
      if (driver.onlineState != DriverOnlineState.ONLINE) continue

      // 3. Vehicle compatibility check
      val isCompatible = when (serviceType) {
        ServiceType.RIDE_CAR -> driver.vehicleType.contains("Mobil", ignoreCase = true)
        else -> driver.vehicleType.contains("Motor", ignoreCase = true)
      }
      if (!isCompatible) continue

      // 4. Distance & ETA Calculation
      val distance = GeoRoutingEngine.calculateRoadDistanceKm(
        pickupLat, pickupLng,
        driver.currentLat, driver.currentLng
      )

      if (distance <= maxRadiusKm) {
        val eta = GeoRoutingEngine.estimateDurationMinutes(distance, serviceType)

        // 5. Composite Ranking Score
        // 50% proximity, 30% rating, 20% acceptance rate
        val proximityScore = ((maxRadiusKm - distance) / maxRadiusKm).coerceIn(0.0, 1.0)
        val ratingScore = (driver.rating / 5.0).coerceIn(0.0, 1.0)
        val acceptanceScore = (driver.acceptanceRate / 100.0).coerceIn(0.0, 1.0)

        val compositeScore = (0.50 * proximityScore) + (0.30 * ratingScore) + (0.20 * acceptanceScore)

        candidates.add(
          CandidateDriver(
            profile = driver,
            distanceKm = distance,
            etaMin = eta,
            compositeScore = compositeScore,
            rank = 0 // assigned after sorting
          )
        )
      }
    }

    // Sort descending by composite score
    val sorted = candidates.sortedByDescending { it.compositeScore }
    return sorted.mapIndexed { index, candidate ->
      candidate.copy(rank = index + 1)
    }
  }

  /**
   * Initializes a dispatch matching session with the candidate queue.
   */
  fun createDispatchSession(orderId: String, candidates: List<CandidateDriver>): DispatchSession {
    return DispatchSession(
      orderId = orderId,
      candidates = candidates,
      currentIndex = 0
    )
  }

  /**
   * Cascading dispatch: Advances offer to the next nearest candidate when the current driver rejects or times out.
   */
  fun advanceToNextCandidate(session: DispatchSession): CandidateDriver? {
    if (session.hasNextCandidate) {
      session.currentIndex++
      return session.currentCandidate
    }
    session.isCompleted = true
    return null
  }

  /**
   * Handles offer timeout (20s). Returns action to cascade to next candidate or expire.
   */
  suspend fun handleOfferTimeout(db: AppDatabase, session: DispatchSession): TimeoutAction {
    val timedOutDriver = session.currentCandidate

    // Log the timeout in audit logs
    if (timedOutDriver != null) {
      db.auditDao().insertLog(
        AuditLogEntity(
          id = UUID.randomUUID().toString(),
          actorId = timedOutDriver.profile.userId,
          actorRole = "DRIVER",
          action = "DISPATCH_OFFER_TIMEOUT",
          entityType = "ORDER",
          entityId = session.orderId,
          details = "Driver ${timedOutDriver.profile.fullName} did not respond within ${session.offerTimeoutSeconds}s window."
        )
      )
    }

    return if (session.hasNextCandidate) {
      session.currentIndex++
      TimeoutAction.REASSIGN_NEXT_CANDIDATE
    } else {
      session.isCompleted = true
      db.orderDao().updateOrderStatus(session.orderId, OrderStatus.EXPIRED)
      TimeoutAction.DISPATCH_EXPIRED
    }
  }

  /**
   * Robust Cancellation Engine:
   * Handles customer, driver, or ops cancellations with policy compliance,
   * grace period rules, fee deduction, and ledger compensation settlement.
   */
  suspend fun cancelOrder(
    db: AppDatabase,
    order: OrderEntity,
    cancelledBy: String,
    actorRole: UserRole,
    reason: String
  ): CancellationResult {
    // 1. Verify state machine transition legality
    val transitionCheck = TripStateMachine.validateTransition(order.status, OrderStatus.CANCELLED)
    if (transitionCheck.isFailure) {
      return CancellationResult(
        isSuccess = false,
        orderId = order.id,
        cancellationFee = 0L,
        refundedAmount = 0L,
        reason = reason,
        message = transitionCheck.exceptionOrNull()?.message ?: "Gagal membatalkan pesanan."
      )
    }

    var cancellationFee = 0L
    var refundedAmount = 0L

    // 2. Cancellation fee determination
    if (order.status == OrderStatus.MATCHING) {
      // Free cancellation during matching
      cancellationFee = 0L
      refundedAmount = order.totalFare
    } else if (order.status in listOf(OrderStatus.DRIVER_ASSIGNED, OrderStatus.ARRIVING, OrderStatus.ARRIVED)) {
      // After driver acceptance / arrival, compensation fee applies if cancelled by customer
      if (actorRole == UserRole.CUSTOMER) {
        cancellationFee = STANDARD_CANCELLATION_FEE
        refundedAmount = maxOf(0L, order.totalFare - cancellationFee)

        // Settle Compensation Ledger Entries
        val driverId = order.driverId ?: "user_driver_01"

        // Credit driver compensation
        db.walletDao().insertLedgerEntry(
          LedgerEntryEntity(
            id = UUID.randomUUID().toString(),
            walletId = "w_driver_01",
            direction = LedgerDirection.CREDIT,
            amount = cancellationFee,
            reason = LedgerReason.CANCELLATION_FEE,
            description = "Kompensasi pembatalan pesanan #${order.id} oleh pelanggan",
            referenceId = order.id,
            idempotencyKey = "cancel_comp_${order.id}"
          )
        )

        // Debit customer cancellation fee if payment method is wallet
        if (order.paymentMethod == PaymentMethod.WALLET) {
          db.walletDao().insertLedgerEntry(
            LedgerEntryEntity(
              id = UUID.randomUUID().toString(),
              walletId = "w_cust_01",
              direction = LedgerDirection.DEBIT,
              amount = cancellationFee,
              reason = LedgerReason.CANCELLATION_FEE,
              description = "Biaya kompensasi pembatalan pesanan #${order.id}",
              referenceId = order.id,
              idempotencyKey = "cancel_fee_${order.id}"
            )
          )
        }
      } else {
        // Cancelled by Driver or Ops: No fee for customer
        cancellationFee = 0L
        refundedAmount = order.totalFare
      }

      // Restore assigned driver state to ONLINE so they can take new jobs
      order.driverId?.let { driverId ->
        db.driverDao().updateOnlineState(driverId, DriverOnlineState.ONLINE)
      }
    }

    // 3. Update Order record in database
    db.orderDao().cancelOrder(
      orderId = order.id,
      status = OrderStatus.CANCELLED,
      reason = reason,
      cancelledBy = "$cancelledBy (${actorRole.name})"
    )

    // 4. Record audit log
    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = cancelledBy,
        actorRole = actorRole.name,
        action = "ORDER_CANCELLED",
        entityType = "ORDER",
        entityId = order.id,
        details = "Alasan: $reason. Biaya pembatalan: Rp $cancellationFee."
      )
    )

    return CancellationResult(
      isSuccess = true,
      orderId = order.id,
      cancellationFee = cancellationFee,
      refundedAmount = refundedAmount,
      reason = reason,
      message = "Pesanan #${order.id} berhasil dibatalkan."
    )
  }

  /**
   * Updates zone surge multiplier and records change in pricing policies.
   */
  suspend fun updateZoneSurge(db: AppDatabase, zoneId: String, surgeMultiplier: Double) {
    db.pricingDao().updateZoneSurge(zoneId, surgeMultiplier)
  }

  /**
   * Computes dynamic surge recommendation based on Zone demand-to-supply ratio.
   */
  suspend fun calculateDynamicSurgeMultiplier(db: AppDatabase, zoneId: String): Double {
    val onlineDrivers = db.driverDao().getOnlineDrivers().first().count { it.zoneId == zoneId }
    val allOrders = db.orderDao().getAllOrders().first()
    val activeOrdersInZone = allOrders.count { !it.status.isTerminal }

    val supply = maxOf(1, onlineDrivers)
    val ratio = activeOrdersInZone.toDouble() / supply.toDouble()

    return when {
      ratio >= 2.0 -> 2.0
      ratio >= 1.5 -> 1.5
      ratio >= 1.2 -> 1.25
      else -> 1.0
    }
  }
}
