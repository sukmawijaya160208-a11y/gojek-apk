package com.example

import com.example.core.model.OrderStatus
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoundationUnitTest {

  @Test
  fun testServiceCatalogDefinitions() {
    assertEquals("Sukma Ride", ServiceType.RIDE_BIKE.displayName)
    assertEquals("Sukma Car", ServiceType.RIDE_CAR.displayName)
    assertEquals("Sukma Send", ServiceType.DELIVERY_BIKE.displayName)
    assertEquals("Sukma Food", ServiceType.FOOD.displayName)
  }

  @Test
  fun testOrderStatusTerminalStates() {
    assertTrue(OrderStatus.COMPLETED.isTerminal)
    assertTrue(OrderStatus.CANCELLED.isTerminal)
    assertTrue(OrderStatus.EXPIRED.isTerminal)
    assertFalse(OrderStatus.MATCHING.isTerminal)
    assertFalse(OrderStatus.IN_TRIP.isTerminal)
  }

  @Test
  fun testUserRolesDefined() {
    val roles = UserRole.values().map { it.name }
    assertTrue(roles.contains("CUSTOMER"))
    assertTrue(roles.contains("DRIVER"))
    assertTrue(roles.contains("MERCHANT"))
    assertTrue(roles.contains("OPS"))
    assertTrue(roles.contains("ADMIN"))
  }
}
