package com.example.core.database.converter

import androidx.room.TypeConverter
import com.example.core.model.DriverOnlineState
import com.example.core.model.IncidentSeverity
import com.example.core.model.IncidentType
import com.example.core.model.LedgerDirection
import com.example.core.model.LedgerReason
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceType
import com.example.core.model.UserRole
import com.example.core.model.UserStatus

class RoomConverters {

  @TypeConverter
  fun fromUserRole(value: UserRole): String = value.name

  @TypeConverter
  fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.CUSTOMER)

  @TypeConverter
  fun fromUserStatus(value: UserStatus): String = value.name

  @TypeConverter
  fun toUserStatus(value: String): UserStatus = runCatching { UserStatus.valueOf(value) }.getOrDefault(UserStatus.ACTIVE)

  @TypeConverter
  fun fromServiceType(value: ServiceType): String = value.name

  @TypeConverter
  fun toServiceType(value: String): ServiceType = runCatching { ServiceType.valueOf(value) }.getOrDefault(ServiceType.RIDE_BIKE)

  @TypeConverter
  fun fromOrderStatus(value: OrderStatus): String = value.name

  @TypeConverter
  fun toOrderStatus(value: String): OrderStatus = runCatching { OrderStatus.valueOf(value) }.getOrDefault(OrderStatus.CREATED)

  @TypeConverter
  fun fromDriverOnlineState(value: DriverOnlineState): String = value.name

  @TypeConverter
  fun toDriverOnlineState(value: String): DriverOnlineState = runCatching { DriverOnlineState.valueOf(value) }.getOrDefault(DriverOnlineState.OFFLINE)

  @TypeConverter
  fun fromPaymentMethod(value: PaymentMethod): String = value.name

  @TypeConverter
  fun toPaymentMethod(value: String): PaymentMethod = runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.WALLET)

  @TypeConverter
  fun fromPaymentStatus(value: PaymentStatus): String = value.name

  @TypeConverter
  fun toPaymentStatus(value: String): PaymentStatus = runCatching { PaymentStatus.valueOf(value) }.getOrDefault(PaymentStatus.CAPTURED)

  @TypeConverter
  fun fromLedgerDirection(value: LedgerDirection): String = value.name

  @TypeConverter
  fun toLedgerDirection(value: String): LedgerDirection = runCatching { LedgerDirection.valueOf(value) }.getOrDefault(LedgerDirection.CREDIT)

  @TypeConverter
  fun fromLedgerReason(value: LedgerReason): String = value.name

  @TypeConverter
  fun toLedgerReason(value: String): LedgerReason = runCatching { LedgerReason.valueOf(value) }.getOrDefault(LedgerReason.TRIP_FARE)

  @TypeConverter
  fun fromIncidentSeverity(value: IncidentSeverity): String = value.name

  @TypeConverter
  fun toIncidentSeverity(value: String): IncidentSeverity = runCatching { IncidentSeverity.valueOf(value) }.getOrDefault(IncidentSeverity.MEDIUM)

  @TypeConverter
  fun fromIncidentType(value: IncidentType): String = value.name

  @TypeConverter
  fun toIncidentType(value: String): IncidentType = runCatching { IncidentType.valueOf(value) }.getOrDefault(IncidentType.UNSAFE_DRIVING)

  @TypeConverter
  fun fromMerchantOrderStatus(value: com.example.core.model.MerchantOrderStatus): String = value.name

  @TypeConverter
  fun toMerchantOrderStatus(value: String): com.example.core.model.MerchantOrderStatus =
    runCatching { com.example.core.model.MerchantOrderStatus.valueOf(value) }.getOrDefault(com.example.core.model.MerchantOrderStatus.RECEIVED)
}
