package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.DriverOnlineState
import com.example.core.model.IncidentSeverity
import com.example.core.model.IncidentType
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import com.example.core.model.MerchantOrderStatus
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import com.example.core.model.UserStatus

@Entity(
  tableName = "users",
  indices = [Index("phone"), Index("role")]
)
data class UserEntity(
  @PrimaryKey val id: String,
  val role: UserRole,
  val fullName: String,
  val phone: String,
  val email: String,
  val status: UserStatus = UserStatus.ACTIVE,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "driver_profiles",
  indices = [Index("userId"), Index("onlineState"), Index("zoneId")]
)
data class DriverProfileEntity(
  @PrimaryKey val userId: String,
  val fullName: String,
  val phone: String,
  val verificationState: String = "VERIFIED", // VERIFIED, PENDING, REJECTED
  val onlineState: DriverOnlineState = DriverOnlineState.ONLINE,
  val rating: Double = 4.9,
  val totalTrips: Int = 128,
  val acceptanceRate: Double = 98.5,
  val vehicleType: String = "Motorcycle",
  val vehicleModel: String = "Honda Vario 160",
  val plateNumber: String = "B 4821 SKM",
  val currentLat: Double = -6.2088,
  val currentLng: Double = 106.8456,
  val zoneId: String = "ZONE_CENTRAL",
  val lastPingAt: Long = System.currentTimeMillis(),
  val ktpNumber: String = "3171028374920001",
  val simNumber: String = "002938491823",
  val stnkNumber: String = "19283746",
  val skckStatus: String = "VERIFIED",
  val bankName: String = "Bank Central Asia (BCA)",
  val bankAccountNumber: String = "8271928374",
  val bankAccountHolder: String = "Ahmad Fauzi",
  val dailyTripTarget: Int = 8,
  val todayCompletedTrips: Int = 5,
  val activeIncentiveBonus: Long = 35000L
)

@Entity(
  tableName = "zones",
  indices = [Index("code", unique = true)]
)
data class ZoneEntity(
  @PrimaryKey val id: String,
  val code: String,
  val name: String,
  val centerLat: Double,
  val centerLng: Double,
  val radiusKm: Double,
  val isActive: Boolean = true
)

@Entity(
  tableName = "pricing_policies",
  indices = [Index("zoneId"), Index("serviceType")]
)
data class PricingPolicyEntity(
  @PrimaryKey val id: String,
  val zoneId: String,
  val serviceType: ServiceType,
  val baseFare: Long,
  val distanceRateKm: Long,
  val timeRateMin: Long,
  val minimumFare: Long,
  val platformFee: Long,
  val surgeMultiplier: Double = 1.0,
  val policyVersion: String = "v1.0"
)

@Entity(
  tableName = "orders",
  indices = [
    Index("customerId"),
    Index("driverId"),
    Index("status"),
    Index("createdAt")
  ]
)
data class OrderEntity(
  @PrimaryKey val id: String,
  val serviceType: ServiceType,
  val customerId: String,
  val customerName: String,
  val driverId: String?,
  val driverName: String?,
  val driverPlate: String?,
  val driverVehicle: String?,
  val status: OrderStatus,
  val pickupAddress: String,
  val pickupLat: Double,
  val pickupLng: Double,
  val destAddress: String,
  val destLat: Double,
  val destLng: Double,
  val distanceKm: Double,
  val durationMin: Int,
  val subtotalFare: Long,
  val platformFee: Long,
  val surgeAmount: Long = 0,
  val discountAmount: Long = 0,
  val totalFare: Long,
  val paymentMethod: PaymentMethod = PaymentMethod.WALLET,
  val paymentStatus: PaymentStatus = PaymentStatus.CAPTURED,
  val promoCode: String? = null,
  val idempotencyKey: String,
  val cancellationReason: String? = null,
  val cancelledByActor: String? = null,
  val rating: Int? = null,
  val tipAmount: Long = 0,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "wallets",
  indices = [Index("ownerId"), Index("ownerType")]
)
data class WalletEntity(
  @PrimaryKey val id: String,
  val ownerId: String,
  val ownerType: String, // "CUSTOMER", "DRIVER", "PLATFORM"
  val currency: String = "IDR",
  val status: String = "ACTIVE"
)

@Entity(
  tableName = "ledger_entries",
  indices = [Index("walletId"), Index("referenceId"), Index("createdAt")]
)
data class LedgerEntryEntity(
  @PrimaryKey val id: String,
  val walletId: String,
  val direction: LedgerDirection, // CREDIT, DEBIT
  val amount: Long,
  val reason: LedgerReason,
  val description: String,
  val referenceId: String, // orderId or topUpId
  val idempotencyKey: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "promos",
  indices = [Index("code", unique = true)]
)
data class PromoEntity(
  @PrimaryKey val id: String,
  val code: String,
  val title: String,
  val description: String,
  val discountPercentage: Int,
  val maxDiscountAmount: Long,
  val minOrderAmount: Long,
  val budgetTotal: Long,
  val budgetRemaining: Long,
  val validFrom: Long,
  val validTo: Long,
  val isActive: Boolean = true
)

@Entity(
  tableName = "support_tickets",
  indices = [Index("requesterId"), Index("orderId"), Index("status")]
)
data class SupportTicketEntity(
  @PrimaryKey val id: String,
  val requesterId: String,
  val requesterName: String,
  val orderId: String?,
  val severity: IncidentSeverity,
  val category: String,
  val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
  val subject: String,
  val description: String,
  val aiDraftReply: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "incidents",
  indices = [Index("orderId"), Index("reporterId"), Index("severity")]
)
data class IncidentEntity(
  @PrimaryKey val id: String,
  val orderId: String,
  val reporterId: String,
  val reporterRole: String,
  val type: IncidentType,
  val severity: IncidentSeverity,
  val status: String = "REPORTED", // REPORTED, INVESTIGATING, RESOLVED
  val notes: String,
  val locationDesc: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "audit_logs",
  indices = [Index("actorId"), Index("entityType"), Index("entityId")]
)
data class AuditLogEntity(
  @PrimaryKey val id: String,
  val actorId: String,
  val actorRole: String,
  val action: String,
  val entityType: String,
  val entityId: String,
  val details: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "stores",
  indices = [Index("merchantUserId"), Index("zoneId"), Index("isOpen")]
)
data class StoreEntity(
  @PrimaryKey val id: String,
  val merchantUserId: String,
  val name: String,
  val description: String,
  val address: String,
  val latitude: Double = -6.1950,
  val longitude: Double = 106.8230,
  val zoneId: String = "ZONE_CENTRAL",
  val phone: String,
  val category: String = "Kuliner Nusantara, Aneka Nasi, Minuman",
  val isOpen: Boolean = true,
  val rating: Double = 4.88,
  val totalReviews: Int = 186,
  val verificationState: String = "VERIFIED", // VERIFIED, PENDING_REVIEW
  val ktpNumber: String = "3171092837480001",
  val npwpNumber: String = "09.827.182.9-021.000",
  val nibNumber: String = "NIB-1209384756",
  val bankName: String = "Bank Central Asia (BCA)",
  val bankAccountNumber: String = "0182938475",
  val bankAccountHolder: String = "Dapur Nusantara Resto",
  val commissionRatePercent: Double = 15.0,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "merchant_categories",
  indices = [Index("storeId")]
)
data class MerchantCategoryEntity(
  @PrimaryKey val id: String,
  val storeId: String,
  val name: String,
  val sortOrder: Int = 0,
  val isActive: Boolean = true
)

@Entity(
  tableName = "merchant_products",
  indices = [Index("storeId"), Index("categoryId"), Index("isAvailable")]
)
data class MerchantProductEntity(
  @PrimaryKey val id: String,
  val storeId: String,
  val categoryId: String,
  val name: String,
  val description: String,
  val price: Long,
  val imageUrl: String? = null,
  val stockQuantity: Int = 50,
  val isAvailable: Boolean = true,
  val soldCount: Int = 0,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "merchant_orders",
  indices = [Index("storeId"), Index("customerId"), Index("status"), Index("createdAt")]
)
data class MerchantOrderEntity(
  @PrimaryKey val id: String,
  val storeId: String,
  val customerId: String,
  val customerName: String,
  val customerPhone: String,
  val driverId: String? = null,
  val driverName: String? = null,
  val status: MerchantOrderStatus = MerchantOrderStatus.RECEIVED,
  val itemsSummary: String, // e.g. "2x Nasi Goreng Spesial, 2x Es Teh Manis"
  val subtotalAmount: Long,
  val platformCommission: Long, // 15% platform commission
  val netMerchantAmount: Long, // subtotal - commission
  val paymentMethod: PaymentMethod = PaymentMethod.WALLET,
  val deliveryAddress: String,
  val customerNotes: String? = null,
  val estimatedPreparationMin: Int = 15,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
