package com.example.core.repository

import com.example.core.database.AppDatabase
import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.DriverProfileEntity
import com.example.core.database.entity.LedgerEntryEntity
import com.example.core.database.entity.OrderEntity
import com.example.core.model.DriverOnlineState
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import com.example.core.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DriverRepository(private val db: AppDatabase) {

  fun getDriverProfile(driverId: String): Flow<DriverProfileEntity?> =
    db.driverDao().getDriverById(driverId)

  fun getPendingMatchingOrder(): Flow<OrderEntity?> =
    db.orderDao().getLatestMatchingOrder()

  suspend fun updateOnlineState(driverId: String, onlineState: DriverOnlineState) {
    db.driverDao().updateOnlineState(driverId, onlineState)
  }

  suspend fun updateLocation(driverId: String, lat: Double, lng: Double) {
    db.driverDao().updateLocation(driverId, lat, lng)
  }

  suspend fun submitKycDocuments(
    driverId: String,
    ktp: String,
    sim: String,
    stnk: String,
    bankName: String,
    accountNum: String,
    accountHolder: String
  ) {
    val existing = db.driverDao().getDriverById(driverId)
    db.driverDao().insertDriver(
      DriverProfileEntity(
        userId = driverId,
        fullName = accountHolder.ifBlank { "Ahmad Fauzi" },
        phone = "+6281987654321",
        verificationState = "VERIFIED",
        onlineState = DriverOnlineState.ONLINE,
        rating = 4.92,
        totalTrips = 342,
        acceptanceRate = 98.2,
        vehicleType = "Motor",
        vehicleModel = "Honda Vario 160 Hitam",
        plateNumber = "B 4821 SKM",
        ktpNumber = ktp,
        simNumber = sim,
        stnkNumber = stnk,
        bankName = bankName,
        bankAccountNumber = accountNum,
        bankAccountHolder = accountHolder
      )
    )

    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = driverId,
        actorRole = "DRIVER",
        action = "KYC_SUBMITTED_AND_VERIFIED",
        entityType = "DRIVER_PROFILE",
        entityId = driverId,
        details = "KYC documents submitted: KTP $ktp, SIM $sim, STNK $stnk, Bank $bankName $accountNum",
        createdAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun acceptOffer(orderId: String, driver: DriverProfileEntity) {
    db.orderDao().assignDriver(
      orderId = orderId,
      driverId = driver.userId,
      driverName = driver.fullName,
      plate = driver.plateNumber,
      vehicle = "${driver.vehicleModel} (${driver.vehicleType})",
      status = OrderStatus.DRIVER_ASSIGNED
    )
    db.driverDao().updateOnlineState(driver.userId, DriverOnlineState.ACCEPTED)

    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = driver.userId,
        actorRole = "DRIVER",
        action = "OFFER_ACCEPTED",
        entityType = "ORDER",
        entityId = orderId,
        details = "Driver ${driver.fullName} accepted job $orderId",
        createdAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun rejectOffer(orderId: String, driverId: String) {
    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = driverId,
        actorRole = "DRIVER",
        action = "OFFER_REJECTED",
        entityType = "ORDER",
        entityId = orderId,
        details = "Driver rejected incoming job $orderId",
        createdAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun claimIncentiveBonus(driverId: String, bonusAmount: Long) {
    db.walletDao().insertLedgerEntry(
      LedgerEntryEntity(
        id = UUID.randomUUID().toString(),
        walletId = "w_driver_01",
        direction = LedgerDirection.CREDIT,
        amount = bonusAmount,
        reason = LedgerReason.DRIVER_EARNING,
        description = "Bonus Pencapaian Target Insentif Harian",
        referenceId = "INC-BONUS-${System.currentTimeMillis()}",
        idempotencyKey = "incentive_claim_${System.currentTimeMillis()}"
      )
    )
  }

  suspend fun requestPayout(driverId: String, amount: Long, bankInfo: String): Boolean {
    db.walletDao().insertLedgerEntry(
      LedgerEntryEntity(
        id = UUID.randomUUID().toString(),
        walletId = "w_driver_01",
        direction = LedgerDirection.DEBIT,
        amount = amount,
        reason = LedgerReason.DRIVER_EARNING,
        description = "Penarikan Saldo Driver ke $bankInfo",
        referenceId = "PAYOUT-${System.currentTimeMillis()}",
        idempotencyKey = "payout_${System.currentTimeMillis()}"
      )
    )
    return true
  }
}
