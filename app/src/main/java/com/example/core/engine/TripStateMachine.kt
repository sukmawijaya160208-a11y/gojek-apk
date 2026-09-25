package com.example.core.engine

import com.example.core.model.OrderStatus

/**
 * Deterministic State Machine for Order and Trip lifecycles.
 * Enforces valid forward progressions and prevents illegal transitions from terminal states.
 */
object TripStateMachine {

  /**
   * Adjacency list defining all legal status transitions.
   */
  private val validTransitions: Map<OrderStatus, Set<OrderStatus>> = mapOf(
    OrderStatus.CREATED to setOf(
      OrderStatus.MATCHING,
      OrderStatus.CANCELLED
    ),
    OrderStatus.MATCHING to setOf(
      OrderStatus.DRIVER_ASSIGNED,
      OrderStatus.CANCELLED,
      OrderStatus.EXPIRED
    ),
    OrderStatus.DRIVER_ASSIGNED to setOf(
      OrderStatus.ARRIVING,
      OrderStatus.ARRIVED,
      OrderStatus.IN_TRIP,
      OrderStatus.CANCELLED
    ),
    OrderStatus.ARRIVING to setOf(
      OrderStatus.ARRIVED,
      OrderStatus.IN_TRIP,
      OrderStatus.CANCELLED
    ),
    OrderStatus.ARRIVED to setOf(
      OrderStatus.PICKED_UP,
      OrderStatus.IN_TRIP,
      OrderStatus.CANCELLED
    ),
    OrderStatus.PICKED_UP to setOf(
      OrderStatus.IN_TRIP,
      OrderStatus.CANCELLED
    ),
    OrderStatus.IN_TRIP to setOf(
      OrderStatus.COMPLETING,
      OrderStatus.COMPLETED,
      OrderStatus.CANCELLED
    ),
    OrderStatus.COMPLETING to setOf(
      OrderStatus.COMPLETED
    ),
    // Terminal States (no further transitions allowed)
    OrderStatus.COMPLETED to emptySet(),
    OrderStatus.CANCELLED to emptySet(),
    OrderStatus.EXPIRED to emptySet(),
    OrderStatus.PAYMENT_FAILED to emptySet()
  )

  /**
   * Checks whether transitioning from [currentStatus] to [targetStatus] is legally allowed.
   */
  fun canTransition(currentStatus: OrderStatus, targetStatus: OrderStatus): Boolean {
    // Transition to the exact same status is a no-op
    if (currentStatus == targetStatus) return true

    // Terminal states cannot transition to anything
    if (currentStatus.isTerminal) return false

    val allowedNextStates = validTransitions[currentStatus] ?: return false
    return targetStatus in allowedNextStates
  }

  /**
   * Validates a state transition and returns a Result.
   * Fails with [IllegalStateException] if the transition violates the state machine rules.
   */
  fun validateTransition(currentStatus: OrderStatus, targetStatus: OrderStatus): Result<Unit> {
    if (canTransition(currentStatus, targetStatus)) {
      return Result.success(Unit)
    }

    val errorMessage = if (currentStatus.isTerminal) {
      "Tidak dapat mengubah status pesanan yang sudah berakhir (${currentStatus.label}). Status ini bersifat permanen."
    } else {
      "Transisi tidak valid: Tidak dapat berpindah dari '${currentStatus.label}' ke '${targetStatus.label}'."
    }

    return Result.failure(IllegalStateException(errorMessage))
  }

  /**
   * Returns list of reachable states from current status.
   */
  fun getAvailableNextStates(currentStatus: OrderStatus): List<OrderStatus> {
    return validTransitions[currentStatus]?.toList() ?: emptyList()
  }
}
