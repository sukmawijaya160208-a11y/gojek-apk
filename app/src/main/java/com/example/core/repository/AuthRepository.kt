package com.example.core.repository

import com.example.core.database.AppDatabase
import com.example.core.database.entity.UserEntity
import com.example.core.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val db: AppDatabase) {

  private val _currentUser = MutableStateFlow(
    UserEntity(
      id = "user_cust_01",
      role = UserRole.CUSTOMER,
      fullName = "Sukma Wijaya",
      phone = "+6281298765432",
      email = "sukmawijaya160208@gmail.com"
    )
  )
  val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

  fun getAllUsers(): Flow<List<UserEntity>> = db.userDao().getAllUsers()

  suspend fun switchUserRole(role: UserRole) {
    // Find pre-seeded user with the target role
    val targetUser = when (role) {
      UserRole.CUSTOMER -> UserEntity("user_cust_01", UserRole.CUSTOMER, "Sukma Wijaya", "+6281298765432", "sukmawijaya160208@gmail.com")
      UserRole.DRIVER -> UserEntity("user_driver_01", UserRole.DRIVER, "Ahmad Fauzi (Mitra Motor)", "+6281987654321", "ahmad.fauzi@driver.gosukma.id")
      UserRole.MERCHANT -> UserEntity("user_merchant_01", UserRole.MERCHANT, "Dapur Nusantara Resto", "+6282133445566", "resto@dapurnusantara.id")
      UserRole.OPS -> UserEntity("user_ops_01", UserRole.OPS, "Doni Pratama (Dispatcher)", "+6285611223344", "doni.ops@gosukma.id")
      UserRole.SUPPORT -> UserEntity("user_support_01", UserRole.SUPPORT, "Maya Indah (Support)", "+6285722334455", "maya.support@gosukma.id")
      UserRole.FINANCE -> UserEntity("user_finance_01", UserRole.FINANCE, "Hendra Saputra (Finance)", "+6285833445566", "hendra.finance@gosukma.id")
      UserRole.ADMIN -> UserEntity("user_admin_01", UserRole.ADMIN, "Super Admin Go Sukma", "+6281100112233", "admin@gosukma.id")
      UserRole.SUPER_ADMIN -> UserEntity("user_admin_01", UserRole.SUPER_ADMIN, "Super Admin Go Sukma", "+6281100112233", "admin@gosukma.id")
    }
    _currentUser.value = targetUser
  }

  suspend fun loginWithOtp(phone: String, otp: String): Boolean {
    // In Phase 1 foundation, valid 6-digit OTP logs in as Customer
    if (otp.length == 6) {
      _currentUser.value = UserEntity(
        id = "user_cust_01",
        role = UserRole.CUSTOMER,
        fullName = "Sukma Wijaya",
        phone = phone.ifBlank { "+6281298765432" },
        email = "sukmawijaya160208@gmail.com"
      )
      return true
    }
    return false
  }
}
