package com.example.core.repository

import com.example.core.database.AppDatabase
import com.example.core.database.entity.LedgerEntryEntity
import com.example.core.database.entity.WalletEntity
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WalletRepository(private val db: AppDatabase) {

  fun getWallet(ownerId: String): Flow<WalletEntity?> =
    db.walletDao().getWalletByOwner(ownerId)

  fun getLedgerEntries(walletId: String): Flow<List<LedgerEntryEntity>> =
    db.walletDao().getLedgerEntries(walletId)

  fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>> =
    db.walletDao().getAllLedgerEntries()

  fun getCalculatedBalance(walletId: String): Flow<Long> {
    return db.walletDao().getLedgerEntries(walletId).map { entries ->
      var total = 0L
      for (entry in entries) {
        if (entry.direction == LedgerDirection.CREDIT) {
          total += entry.amount
        } else {
          total -= entry.amount
        }
      }
      total
    }
  }

  suspend fun topUpWallet(walletId: String, amount: Long, methodLabel: String): LedgerEntryEntity {
    val entry = LedgerEntryEntity(
      id = "topup-${UUID.randomUUID().toString().take(8)}",
      walletId = walletId,
      direction = LedgerDirection.CREDIT,
      amount = amount,
      reason = LedgerReason.TOP_UP,
      description = "Top up saldo via $methodLabel",
      referenceId = "REF-${System.currentTimeMillis()}",
      idempotencyKey = "idem-topup-${System.currentTimeMillis()}"
    )
    db.walletDao().insertLedgerEntry(entry)
    return entry
  }
}
