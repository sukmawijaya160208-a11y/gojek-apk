package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.core.database.converter.RoomConverters
import com.example.core.database.dao.AuditDao
import com.example.core.database.dao.DriverDao
import com.example.core.database.dao.IncidentDao
import com.example.core.database.dao.MerchantDao
import com.example.core.database.dao.OrderDao
import com.example.core.database.dao.PricingDao
import com.example.core.database.dao.SupportDao
import com.example.core.database.dao.UserDao
import com.example.core.database.dao.WalletDao
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

@Database(
  entities = [
    UserEntity::class,
    DriverProfileEntity::class,
    ZoneEntity::class,
    PricingPolicyEntity::class,
    OrderEntity::class,
    WalletEntity::class,
    LedgerEntryEntity::class,
    PromoEntity::class,
    SupportTicketEntity::class,
    IncidentEntity::class,
    AuditLogEntity::class,
    StoreEntity::class,
    MerchantCategoryEntity::class,
    MerchantProductEntity::class,
    MerchantOrderEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun orderDao(): OrderDao
  abstract fun driverDao(): DriverDao
  abstract fun walletDao(): WalletDao
  abstract fun pricingDao(): PricingDao
  abstract fun supportDao(): SupportDao
  abstract fun incidentDao(): IncidentDao
  abstract fun auditDao(): AuditDao
  abstract fun merchantDao(): MerchantDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "go_sukma_drive.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
