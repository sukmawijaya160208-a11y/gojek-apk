package com.example.core.repository

import com.example.core.database.AppDatabase
import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.LedgerEntryEntity
import com.example.core.database.entity.MerchantCategoryEntity
import com.example.core.database.entity.MerchantOrderEntity
import com.example.core.database.entity.MerchantProductEntity
import com.example.core.database.entity.StoreEntity
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import com.example.core.model.MerchantOrderStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MerchantRepository(private val db: AppDatabase) {

  fun getStore(merchantUserId: String = "user_merch_01"): Flow<StoreEntity?> =
    db.merchantDao().getStoreByOwner(merchantUserId)

  fun getStoreById(storeId: String): Flow<StoreEntity?> =
    db.merchantDao().getStoreById(storeId)

  suspend fun updateStoreOpenStatus(storeId: String, isOpen: Boolean) {
    db.merchantDao().updateStoreOpenStatus(storeId, isOpen)
    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = "user_merch_01",
        actorRole = "MERCHANT",
        action = if (isOpen) "STORE_OPENED" else "STORE_CLOSED",
        entityType = "STORE",
        entityId = storeId,
        details = "Status buka toko diubah menjadi: ${if (isOpen) "BUKA" else "TUTUP"}"
      )
    )
  }

  suspend fun updateStoreKyc(
    storeId: String,
    name: String,
    desc: String,
    address: String,
    phone: String,
    bankName: String,
    accountNum: String,
    accountHolder: String,
    npwp: String,
    nib: String
  ) {
    db.merchantDao().updateStoreKyc(
      storeId = storeId,
      name = name,
      description = desc,
      address = address,
      phone = phone,
      bankName = bankName,
      bankAccount = accountNum,
      bankHolder = accountHolder,
      npwp = npwp,
      nib = nib
    )

    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = "user_merch_01",
        actorRole = "MERCHANT",
        action = "STORE_KYC_UPDATED",
        entityType = "STORE",
        entityId = storeId,
        details = "Data legal & rekening bank diperbarui: $bankName $accountNum a.n $accountHolder"
      )
    )
  }

  // Categories
  fun getCategories(storeId: String = "store_01"): Flow<List<MerchantCategoryEntity>> =
    db.merchantDao().getCategoriesByStore(storeId)

  suspend fun addCategory(storeId: String = "store_01", name: String) {
    val categoryId = "cat_${System.currentTimeMillis()}"
    db.merchantDao().insertCategory(
      MerchantCategoryEntity(
        id = categoryId,
        storeId = storeId,
        name = name,
        sortOrder = 99
      )
    )
  }

  suspend fun deleteCategory(categoryId: String) {
    db.merchantDao().deleteCategory(categoryId)
  }

  // Products
  fun getProducts(storeId: String = "store_01"): Flow<List<MerchantProductEntity>> =
    db.merchantDao().getProductsByStore(storeId)

  fun getProductsByCategory(categoryId: String): Flow<List<MerchantProductEntity>> =
    db.merchantDao().getProductsByCategory(categoryId)

  suspend fun addProduct(
    storeId: String = "store_01",
    categoryId: String,
    name: String,
    description: String,
    price: Long,
    stockQuantity: Int = 50
  ) {
    val productId = "prod_${System.currentTimeMillis()}"
    db.merchantDao().insertProduct(
      MerchantProductEntity(
        id = productId,
        storeId = storeId,
        categoryId = categoryId,
        name = name,
        description = description,
        price = price,
        stockQuantity = stockQuantity,
        isAvailable = stockQuantity > 0
      )
    )
  }

  suspend fun updateProduct(product: MerchantProductEntity) {
    db.merchantDao().updateProduct(product)
  }

  suspend fun updateStock(productId: String, stockQuantity: Int) {
    db.merchantDao().updateStock(productId, stockQuantity)
    if (stockQuantity <= 0) {
      db.merchantDao().toggleProductAvailability(productId, false)
    }
  }

  suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
    db.merchantDao().toggleProductAvailability(productId, isAvailable)
  }

  suspend fun deleteProduct(productId: String) {
    db.merchantDao().deleteProduct(productId)
  }

  // Food Orders
  fun getOrders(storeId: String = "store_01"): Flow<List<MerchantOrderEntity>> =
    db.merchantDao().getOrdersByStore(storeId)

  fun getActiveOrders(storeId: String = "store_01"): Flow<List<MerchantOrderEntity>> =
    db.merchantDao().getActiveOrdersByStore(storeId)

  suspend fun confirmOrder(orderId: String) {
    db.merchantDao().updateOrderStatus(orderId, MerchantOrderStatus.CONFIRMED)
  }

  suspend fun startCooking(orderId: String, estimatedMinutes: Int = 15) {
    db.merchantDao().updatePreparationTime(orderId, estimatedMinutes)
    db.merchantDao().updateOrderStatus(orderId, MerchantOrderStatus.PREPARING)
  }

  suspend fun markReadyForPickup(orderId: String) {
    db.merchantDao().updateOrderStatus(orderId, MerchantOrderStatus.READY_FOR_PICKUP)
  }

  suspend fun markPickedUpByDriver(orderId: String) {
    db.merchantDao().updateOrderStatus(orderId, MerchantOrderStatus.PICKED_UP)
  }

  suspend fun completeOrder(order: MerchantOrderEntity) {
    db.merchantDao().updateOrderStatus(order.id, MerchantOrderStatus.COMPLETED)

    // Settle merchant earnings via double-entry ledger
    db.walletDao().insertLedgerEntry(
      LedgerEntryEntity(
        id = UUID.randomUUID().toString(),
        walletId = "w_merch_01",
        direction = LedgerDirection.CREDIT,
        amount = order.netMerchantAmount,
        reason = LedgerReason.MERCHANT_SALE,
        description = "Penjualan pesanan #${order.id} (${order.itemsSummary.take(25)}...)",
        referenceId = order.id,
        idempotencyKey = "merch_sale_${order.id}"
      )
    )
  }

  suspend fun cancelOrder(orderId: String, reason: String) {
    db.merchantDao().updateOrderStatus(orderId, MerchantOrderStatus.CANCELLED)
    db.auditDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = "user_merch_01",
        actorRole = "MERCHANT",
        action = "MERCHANT_ORDER_CANCELLED",
        entityType = "MERCHANT_ORDER",
        entityId = orderId,
        details = "Pesanan dibatalkan resto dengan alasan: $reason"
      )
    )
  }

  suspend fun requestSettlement(merchantId: String, amount: Long, bankInfo: String): Boolean {
    db.walletDao().insertLedgerEntry(
      LedgerEntryEntity(
        id = UUID.randomUUID().toString(),
        walletId = "w_merch_01",
        direction = LedgerDirection.DEBIT,
        amount = amount,
        reason = LedgerReason.MERCHANT_SETTLEMENT,
        description = "Pencairan Saldo Pendapatan Toko ke $bankInfo",
        referenceId = "SETTLE-${System.currentTimeMillis()}",
        idempotencyKey = "settle_${System.currentTimeMillis()}"
      )
    )
    return true
  }
}
