package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.AuditLogEntity
import com.example.core.database.entity.DriverProfileEntity
import com.example.core.database.entity.IncidentEntity
import com.example.core.database.entity.LedgerEntryEntity
import com.example.core.database.entity.MerchantCategoryEntity
import com.example.core.database.entity.MerchantOrderEntity
import com.example.core.database.entity.MerchantProductEntity
import com.example.core.database.entity.OrderEntity
import com.example.core.database.entity.PricingPolicyEntity
import com.example.core.database.entity.PromoEntity
import com.example.core.database.entity.StoreEntity
import com.example.core.database.entity.SupportTicketEntity
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.WalletEntity
import com.example.core.database.entity.ZoneEntity
import com.example.core.model.DriverOnlineState
import com.example.core.model.MerchantOrderStatus
import com.example.core.model.OrderStatus
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE id = :userId")
  fun getUserById(userId: String): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE role = :role")
  fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

  @Query("SELECT * FROM users ORDER BY createdAt DESC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface OrderDao {
  @Query("SELECT * FROM orders WHERE id = :orderId")
  fun getOrderById(orderId: String): Flow<OrderEntity?>

  @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
  fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE driverId = :driverId ORDER BY createdAt DESC")
  fun getOrdersByDriver(driverId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE customerId = :customerId AND status NOT IN ('COMPLETED', 'CANCELLED', 'EXPIRED', 'PAYMENT_FAILED') LIMIT 1")
  fun getActiveOrderByCustomer(customerId: String): Flow<OrderEntity?>

  @Query("SELECT * FROM orders WHERE driverId = :driverId AND status NOT IN ('COMPLETED', 'CANCELLED', 'EXPIRED', 'PAYMENT_FAILED') LIMIT 1")
  fun getActiveOrderByDriver(driverId: String): Flow<OrderEntity?>

  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE status = 'MATCHING' ORDER BY createdAt DESC LIMIT 1")
  fun getLatestMatchingOrder(): Flow<OrderEntity?>

  @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
  fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrders(orders: List<OrderEntity>)

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
  suspend fun updateOrderStatus(orderId: String, status: OrderStatus, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE orders SET driverId = :driverId, driverName = :driverName, driverPlate = :plate, driverVehicle = :vehicle, status = :status, updatedAt = :updatedAt WHERE id = :orderId")
  suspend fun assignDriver(orderId: String, driverId: String, driverName: String, plate: String, vehicle: String, status: OrderStatus = OrderStatus.DRIVER_ASSIGNED, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE orders SET rating = :rating, tipAmount = :tipAmount, updatedAt = :updatedAt WHERE id = :orderId")
  suspend fun submitRating(orderId: String, rating: Int, tipAmount: Long, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE orders SET status = :status, cancellationReason = :reason, cancelledByActor = :cancelledBy, updatedAt = :updatedAt WHERE id = :orderId")
  suspend fun cancelOrder(orderId: String, status: OrderStatus = OrderStatus.CANCELLED, reason: String, cancelledBy: String, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface DriverDao {
  @Query("SELECT * FROM driver_profiles WHERE userId = :driverId")
  fun getDriverById(driverId: String): Flow<DriverProfileEntity?>

  @Query("SELECT * FROM driver_profiles WHERE onlineState != 'OFFLINE'")
  fun getOnlineDrivers(): Flow<List<DriverProfileEntity>>

  @Query("SELECT * FROM driver_profiles")
  fun getAllDrivers(): Flow<List<DriverProfileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDriver(driver: DriverProfileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDrivers(drivers: List<DriverProfileEntity>)

  @Query("UPDATE driver_profiles SET onlineState = :onlineState WHERE userId = :driverId")
  suspend fun updateOnlineState(driverId: String, onlineState: DriverOnlineState)

  @Query("UPDATE driver_profiles SET verificationState = :state WHERE userId = :driverId")
  suspend fun updateVerificationState(driverId: String, state: String)

  @Query("UPDATE driver_profiles SET todayCompletedTrips = todayCompletedTrips + 1 WHERE userId = :driverId")
  suspend fun incrementCompletedTrips(driverId: String)

  @Query("UPDATE driver_profiles SET currentLat = :lat, currentLng = :lng, lastPingAt = :timestamp WHERE userId = :driverId")
  suspend fun updateLocation(driverId: String, lat: Double, lng: Double, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface WalletDao {
  @Query("SELECT * FROM wallets WHERE ownerId = :ownerId LIMIT 1")
  fun getWalletByOwner(ownerId: String): Flow<WalletEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWallet(wallet: WalletEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWallets(wallets: List<WalletEntity>)

  @Query("SELECT * FROM ledger_entries WHERE walletId = :walletId ORDER BY createdAt DESC")
  fun getLedgerEntries(walletId: String): Flow<List<LedgerEntryEntity>>

  @Query("SELECT * FROM ledger_entries ORDER BY createdAt DESC")
  fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLedgerEntry(entry: LedgerEntryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLedgerEntries(entries: List<LedgerEntryEntity>)
}

@Dao
interface PricingDao {
  @Query("SELECT * FROM pricing_policies WHERE serviceType = :serviceType AND zoneId = :zoneId LIMIT 1")
  fun getPolicy(serviceType: ServiceType, zoneId: String): Flow<PricingPolicyEntity?>

  @Query("SELECT * FROM pricing_policies")
  fun getAllPolicies(): Flow<List<PricingPolicyEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPolicies(policies: List<PricingPolicyEntity>)

  @Update
  suspend fun updatePolicy(policy: PricingPolicyEntity)

  @Query("UPDATE pricing_policies SET surgeMultiplier = :surge WHERE zoneId = :zoneId")
  suspend fun updateZoneSurge(zoneId: String, surge: Double)

  @Query("SELECT * FROM pricing_policies WHERE zoneId = :zoneId AND serviceType = :serviceType LIMIT 1")
  suspend fun getPolicySync(zoneId: String, serviceType: ServiceType): PricingPolicyEntity?

  @Query("SELECT * FROM zones")
  fun getAllZones(): Flow<List<ZoneEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertZones(zones: List<ZoneEntity>)

  @Query("SELECT * FROM promos WHERE isActive = 1")
  fun getActivePromos(): Flow<List<PromoEntity>>

  @Query("SELECT * FROM promos WHERE code = :code AND isActive = 1 LIMIT 1")
  suspend fun getPromoByCode(code: String): PromoEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPromos(promos: List<PromoEntity>)
}

@Dao
interface SupportDao {
  @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
  fun getAllTickets(): Flow<List<SupportTicketEntity>>

  @Query("SELECT * FROM support_tickets WHERE requesterId = :userId ORDER BY createdAt DESC")
  fun getTicketsByUser(userId: String): Flow<List<SupportTicketEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTicket(ticket: SupportTicketEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTickets(tickets: List<SupportTicketEntity>)

  @Query("UPDATE support_tickets SET aiDraftReply = :draft WHERE id = :ticketId")
  suspend fun updateAiDraft(ticketId: String, draft: String)

  @Query("UPDATE support_tickets SET status = :status WHERE id = :ticketId")
  suspend fun updateTicketStatus(ticketId: String, status: String)
}

@Dao
interface IncidentDao {
  @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
  fun getAllIncidents(): Flow<List<IncidentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncident(incident: IncidentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncidents(incidents: List<IncidentEntity>)
}

@Dao
interface AuditDao {
  @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC LIMIT 100")
  fun getRecentLogs(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface MerchantDao {

  // Store Management
  @Query("SELECT * FROM stores WHERE merchantUserId = :userId LIMIT 1")
  fun getStoreByOwner(userId: String): Flow<StoreEntity?>

  @Query("SELECT * FROM stores WHERE id = :storeId LIMIT 1")
  fun getStoreById(storeId: String): Flow<StoreEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStore(store: StoreEntity)

  @Query("UPDATE stores SET isOpen = :isOpen WHERE id = :storeId")
  suspend fun updateStoreOpenStatus(storeId: String, isOpen: Boolean)

  @Query("UPDATE stores SET name = :name, description = :description, address = :address, phone = :phone, bankName = :bankName, bankAccountNumber = :bankAccount, bankAccountHolder = :bankHolder, npwpNumber = :npwp, nibNumber = :nib WHERE id = :storeId")
  suspend fun updateStoreKyc(
    storeId: String,
    name: String,
    description: String,
    address: String,
    phone: String,
    bankName: String,
    bankAccount: String,
    bankHolder: String,
    npwp: String,
    nib: String
  )

  // Categories
  @Query("SELECT * FROM merchant_categories WHERE storeId = :storeId ORDER BY sortOrder ASC")
  fun getCategoriesByStore(storeId: String): Flow<List<MerchantCategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: MerchantCategoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<MerchantCategoryEntity>)

  @Query("DELETE FROM merchant_categories WHERE id = :categoryId")
  suspend fun deleteCategory(categoryId: String)

  // Products & Catalog
  @Query("SELECT * FROM merchant_products WHERE storeId = :storeId ORDER BY name ASC")
  fun getProductsByStore(storeId: String): Flow<List<MerchantProductEntity>>

  @Query("SELECT * FROM merchant_products WHERE categoryId = :categoryId ORDER BY name ASC")
  fun getProductsByCategory(categoryId: String): Flow<List<MerchantProductEntity>>

  @Query("SELECT * FROM merchant_products WHERE id = :productId LIMIT 1")
  fun getProductById(productId: String): Flow<MerchantProductEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: MerchantProductEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<MerchantProductEntity>)

  @Update
  suspend fun updateProduct(product: MerchantProductEntity)

  @Query("UPDATE merchant_products SET stockQuantity = :stock WHERE id = :productId")
  suspend fun updateStock(productId: String, stock: Int)

  @Query("UPDATE merchant_products SET isAvailable = :isAvailable WHERE id = :productId")
  suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean)

  @Query("DELETE FROM merchant_products WHERE id = :productId")
  suspend fun deleteProduct(productId: String)

  // Food Orders
  @Query("SELECT * FROM merchant_orders WHERE storeId = :storeId ORDER BY createdAt DESC")
  fun getOrdersByStore(storeId: String): Flow<List<MerchantOrderEntity>>

  @Query("SELECT * FROM merchant_orders WHERE storeId = :storeId AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt ASC")
  fun getActiveOrdersByStore(storeId: String): Flow<List<MerchantOrderEntity>>

  @Query("SELECT * FROM merchant_orders WHERE id = :orderId LIMIT 1")
  fun getOrderById(orderId: String): Flow<MerchantOrderEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: MerchantOrderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrders(orders: List<MerchantOrderEntity>)

  @Query("UPDATE merchant_orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
  suspend fun updateOrderStatus(
    orderId: String,
    status: MerchantOrderStatus,
    updatedAt: Long = System.currentTimeMillis()
  )

  @Query("UPDATE merchant_orders SET estimatedPreparationMin = :minutes, updatedAt = :updatedAt WHERE id = :orderId")
  suspend fun updatePreparationTime(
    orderId: String,
    minutes: Int,
    updatedAt: Long = System.currentTimeMillis()
  )
}
