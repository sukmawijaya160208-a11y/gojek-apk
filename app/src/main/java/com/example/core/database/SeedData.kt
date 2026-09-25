package com.example.core.database

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeedData {

  suspend fun populateDatabase(db: AppDatabase) = withContext(Dispatchers.IO) {
    // 1. Zones
    val zones = listOf(
      ZoneEntity(id = "ZONE_CENTRAL", code = "CENTRAL", name = "Jakarta Pusat (Thamrin-Sudirman)", centerLat = -6.2088, centerLng = 106.8456, radiusKm = 12.0),
      ZoneEntity(id = "ZONE_SOUTH", code = "SOUTH", name = "Jakarta Selatan (SCBD-Senopati)", centerLat = -6.2250, centerLng = 106.8090, radiusKm = 10.0),
      ZoneEntity(id = "ZONE_NORTH", code = "NORTH", name = "Jakarta Utara (Kelapa Gading-PIK)", centerLat = -6.1550, centerLng = 106.9020, radiusKm = 14.0)
    )
    db.pricingDao().insertZones(zones)

    // 2. Pricing Policies (Deterministic rules per PRD)
    val policies = listOf(
      PricingPolicyEntity(id = "POL_BIKE_CEN", zoneId = "ZONE_CENTRAL", serviceType = ServiceType.RIDE_BIKE, baseFare = 8000, distanceRateKm = 2500, timeRateMin = 300, minimumFare = 12000, platformFee = 2000, surgeMultiplier = 1.0),
      PricingPolicyEntity(id = "POL_CAR_CEN", zoneId = "ZONE_CENTRAL", serviceType = ServiceType.RIDE_CAR, baseFare = 15000, distanceRateKm = 5000, timeRateMin = 600, minimumFare = 20000, platformFee = 4000, surgeMultiplier = 1.0),
      PricingPolicyEntity(id = "POL_SEND_CEN", zoneId = "ZONE_CENTRAL", serviceType = ServiceType.DELIVERY_BIKE, baseFare = 9000, distanceRateKm = 2800, timeRateMin = 200, minimumFare = 13000, platformFee = 2500, surgeMultiplier = 1.0),
      PricingPolicyEntity(id = "POL_FOOD_CEN", zoneId = "ZONE_CENTRAL", serviceType = ServiceType.FOOD, baseFare = 7000, distanceRateKm = 2200, timeRateMin = 200, minimumFare = 10000, platformFee = 3000, surgeMultiplier = 1.0),
      PricingPolicyEntity(id = "POL_SHOP_CEN", zoneId = "ZONE_CENTRAL", serviceType = ServiceType.SHOP, baseFare = 8000, distanceRateKm = 2400, timeRateMin = 200, minimumFare = 12000, platformFee = 3000, surgeMultiplier = 1.0)
    )
    db.pricingDao().insertPolicies(policies)

    // 3. Promos
    val promos = listOf(
      PromoEntity(
        id = "PROMO_1",
        code = "SUKMABARU",
        title = "Diskon Pengguna Baru 50%",
        description = "Hemat 50% hingga Rp 15.000 untuk perjalanan pertama Anda",
        discountPercentage = 50,
        maxDiscountAmount = 15000,
        minOrderAmount = 15000,
        budgetTotal = 10000000,
        budgetRemaining = 8500000,
        validFrom = System.currentTimeMillis() - 86400000L * 7,
        validTo = System.currentTimeMillis() + 86400000L * 30
      ),
      PromoEntity(
        id = "PROMO_2",
        code = "HEMATSENIN",
        title = "Senin Semangat Hemat 25%",
        description = "Potongan 25% maksimal Rp 10.000 untuk semua layanan mobilitas",
        discountPercentage = 25,
        maxDiscountAmount = 10000,
        minOrderAmount = 20000,
        budgetTotal = 5000000,
        budgetRemaining = 4200000,
        validFrom = System.currentTimeMillis() - 86400000L * 3,
        validTo = System.currentTimeMillis() + 86400000L * 14
      ),
      PromoEntity(
        id = "PROMO_3",
        code = "DRIVEHEMAT",
        title = "Voucher Perjalanan Rp 5.000",
        description = "Potongan langsung Rp 5.000 tanpa minimum transaksi tinggi",
        discountPercentage = 20,
        maxDiscountAmount = 5000,
        minOrderAmount = 12000,
        budgetTotal = 3000000,
        budgetRemaining = 2100000,
        validFrom = System.currentTimeMillis() - 86400000L,
        validTo = System.currentTimeMillis() + 86400000L * 60
      )
    )
    db.pricingDao().insertPromos(promos)

    // 4. Seed Users for each Persona / Role
    val users = listOf(
      UserEntity(id = "user_cust_01", role = UserRole.CUSTOMER, fullName = "Sukma Wijaya", phone = "+6281298765432", email = "sukmawijaya160208@gmail.com", status = UserStatus.ACTIVE),
      UserEntity(id = "user_cust_02", role = UserRole.CUSTOMER, fullName = "Siti Nurhaliza", phone = "+6281311223344", email = "siti@gmail.com", status = UserStatus.ACTIVE),
      UserEntity(id = "user_driver_01", role = UserRole.DRIVER, fullName = "Ahmad Fauzi (Mitra Motor)", phone = "+6281987654321", email = "ahmad.fauzi@driver.gosukma.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_driver_02", role = UserRole.DRIVER, fullName = "Dimas Prasetyo (Mitra Mobil)", phone = "+6281755443322", email = "dimas.prasetyo@driver.gosukma.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_driver_03", role = UserRole.DRIVER, fullName = "Rian Hidayat (Mitra Motor)", phone = "+6281899887766", email = "rian.h@driver.gosukma.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_merchant_01", role = UserRole.MERCHANT, fullName = "Dapur Nusantara Resto", phone = "+6282133445566", email = "resto@dapurnusantara.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_ops_01", role = UserRole.OPS, fullName = "Doni Pratama (Dispatcher)", phone = "+6285611223344", email = "doni.ops@gosukma.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_support_01", role = UserRole.SUPPORT, fullName = "Maya Indah (Support Specialist)", phone = "+6285722334455", email = "maya.support@gosukma.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_finance_01", role = UserRole.FINANCE, fullName = "Hendra Saputra (Finance Officer)", phone = "+6285833445566", email = "hendra.finance@gosukma.id", status = UserStatus.ACTIVE),
      UserEntity(id = "user_admin_01", role = UserRole.ADMIN, fullName = "Super Admin Go Sukma", phone = "+6281100112233", email = "admin@gosukma.id", status = UserStatus.ACTIVE)
    )
    db.userDao().insertUsers(users)

    // 5. Driver Profiles
    val drivers = listOf(
      DriverProfileEntity(
        userId = "user_driver_01",
        fullName = "Ahmad Fauzi",
        phone = "+6281987654321",
        verificationState = "VERIFIED",
        onlineState = DriverOnlineState.ONLINE,
        rating = 4.92,
        totalTrips = 342,
        acceptanceRate = 98.2,
        vehicleType = "Motor",
        vehicleModel = "Honda Vario 160 Hitam",
        plateNumber = "B 4821 SKM",
        currentLat = -6.2095,
        currentLng = 106.8460,
        zoneId = "ZONE_CENTRAL"
      ),
      DriverProfileEntity(
        userId = "user_driver_02",
        fullName = "Dimas Prasetyo",
        phone = "+6281755443322",
        verificationState = "VERIFIED",
        onlineState = DriverOnlineState.ONLINE,
        rating = 4.88,
        totalTrips = 215,
        acceptanceRate = 96.5,
        vehicleType = "Mobil",
        vehicleModel = "Toyota Avanza Veloz Putih",
        plateNumber = "B 1290 SKM",
        currentLat = -6.2240,
        currentLng = 106.8105,
        zoneId = "ZONE_SOUTH"
      ),
      DriverProfileEntity(
        userId = "user_driver_03",
        fullName = "Rian Hidayat",
        phone = "+6281899887766",
        verificationState = "VERIFIED",
        onlineState = DriverOnlineState.OFFLINE,
        rating = 4.95,
        totalTrips = 520,
        acceptanceRate = 99.1,
        vehicleType = "Motor",
        vehicleModel = "Yamaha NMAX Abu-abu",
        plateNumber = "B 6712 SKM",
        currentLat = -6.1580,
        currentLng = 106.9050,
        zoneId = "ZONE_NORTH"
      )
    )
    db.driverDao().insertDrivers(drivers)

    // 6. Wallets & Initial Ledgers
    val wallets = listOf(
      WalletEntity(id = "w_cust_01", ownerId = "user_cust_01", ownerType = "CUSTOMER"),
      WalletEntity(id = "w_driver_01", ownerId = "user_driver_01", ownerType = "DRIVER"),
      WalletEntity(id = "w_driver_02", ownerId = "user_driver_02", ownerType = "DRIVER"),
      WalletEntity(id = "w_merch_01", ownerId = "user_merch_01", ownerType = "MERCHANT"),
      WalletEntity(id = "w_platform", ownerId = "user_admin_01", ownerType = "PLATFORM")
    )
    db.walletDao().insertWallets(wallets)

    val ledgerEntries = listOf(
      LedgerEntryEntity(
        id = "led_01",
        walletId = "w_cust_01",
        direction = LedgerDirection.CREDIT,
        amount = 150000,
        reason = LedgerReason.TOP_UP,
        description = "Isi Ulang Saldo via BCA Virtual Account",
        referenceId = "topup_initial_01",
        idempotencyKey = "idem_topup_01",
        createdAt = System.currentTimeMillis() - 86400000L * 2
      ),
      LedgerEntryEntity(
        id = "led_02",
        walletId = "w_driver_01",
        direction = LedgerDirection.CREDIT,
        amount = 320000,
        reason = LedgerReason.DRIVER_EARNING,
        description = "Pendapatan kumulatif minggu ini",
        referenceId = "earning_summary_01",
        idempotencyKey = "idem_earn_01",
        createdAt = System.currentTimeMillis() - 86400000L
      ),
      LedgerEntryEntity(
        id = "led_03",
        walletId = "w_merch_01",
        direction = LedgerDirection.CREDIT,
        amount = 450000,
        reason = LedgerReason.MERCHANT_SALE,
        description = "Hasil penjualan makanan kumulatif minggu ini",
        referenceId = "merch_sale_initial",
        idempotencyKey = "idem_merch_sale_01",
        createdAt = System.currentTimeMillis() - 86400000L
      )
    )
    db.walletDao().insertLedgerEntries(ledgerEntries)

    // 7. Seed Orders
    val orders = listOf(
      OrderEntity(
        id = "ORD-2026-0901",
        serviceType = ServiceType.RIDE_BIKE,
        customerId = "user_cust_01",
        customerName = "Sukma Wijaya",
        driverId = "user_driver_01",
        driverName = "Ahmad Fauzi",
        driverPlate = "B 4821 SKM",
        driverVehicle = "Honda Vario 160 Hitam",
        status = OrderStatus.COMPLETED,
        pickupAddress = "Grand Indonesia Mall, Jl. M.H. Thamrin No.1",
        pickupLat = -6.1950,
        pickupLng = 106.8230,
        destAddress = "Stasiun Sudirman, Menteng, Jakarta Pusat",
        destLat = -6.2025,
        destLng = 106.8235,
        distanceKm = 2.4,
        durationMin = 10,
        subtotalFare = 14000,
        platformFee = 2000,
        discountAmount = 5000,
        totalFare = 11000,
        paymentMethod = PaymentMethod.WALLET,
        paymentStatus = PaymentStatus.CAPTURED,
        promoCode = "DRIVEHEMAT",
        idempotencyKey = "idem_ord_01",
        rating = 5,
        tipAmount = 2000,
        createdAt = System.currentTimeMillis() - 3600000L * 4
      )
    )
    db.orderDao().insertOrders(orders)

    // 8. Seed Support Tickets & Incidents
    val tickets = listOf(
      SupportTicketEntity(
        id = "TCK-881",
        requesterId = "user_cust_01",
        requesterName = "Sukma Wijaya",
        orderId = "ORD-2026-0901",
        severity = IncidentSeverity.LOW,
        category = "Pertanyaan Tagihan",
        status = "RESOLVED",
        subject = "Konfirmasi promo voucher DRIVEHEMAT",
        description = "Apakah promo voucher sudah terpotong otomatis di struk? Terima kasih.",
        aiDraftReply = "Halo Bapak Sukma, promo voucher DRIVEHEMAT sebesar Rp 5.000 telah sukses dipotong pada struk resmi transaksi ORD-2026-0901 Anda.",
        createdAt = System.currentTimeMillis() - 7200000L
      )
    )
    db.supportDao().insertTickets(tickets)

    val incidents = listOf(
      IncidentEntity(
        id = "INC-102",
        orderId = "ORD-2026-0901",
        reporterId = "user_cust_01",
        reporterRole = "CUSTOMER",
        type = IncidentType.LOST_ITEM,
        severity = IncidentSeverity.MEDIUM,
        status = "INVESTIGATING",
        notes = "Kacamata hitam tertinggal di kantong helm driver saat turun.",
        locationDesc = "Stasiun Sudirman",
        createdAt = System.currentTimeMillis() - 3600000L
      )
    )
    db.incidentDao().insertIncidents(incidents)

    // 9. Seed Merchant Store, Categories, Catalog Products & Incoming Orders
    val store = StoreEntity(
      id = "store_01",
      merchantUserId = "user_merch_01",
      name = "Dapur Nusantara Resto - Thamrin",
      description = "Spesialis masakan nusantara autentik, aneka nasi olahan, lauk bakar, dan minuman segar tradisional.",
      address = "Jl. M.H. Thamrin No. 28, Menteng, Jakarta Pusat",
      latitude = -6.1950,
      longitude = 106.8230,
      zoneId = "ZONE_CENTRAL",
      phone = "+6281399887766",
      category = "Masakan Nusantara, Nasi Goreng, Ayam Bakar, Aneka Minuman",
      isOpen = true,
      rating = 4.88,
      totalReviews = 214,
      verificationState = "VERIFIED",
      ktpNumber = "3171092837480001",
      npwpNumber = "09.827.182.9-021.000",
      nibNumber = "NIB-1209384756",
      bankName = "Bank Central Asia (BCA)",
      bankAccountNumber = "0182938475",
      bankAccountHolder = "Dapur Nusantara Resto",
      commissionRatePercent = 15.0
    )
    db.merchantDao().insertStore(store)

    val categories = listOf(
      MerchantCategoryEntity(id = "cat_main", storeId = "store_01", name = "Makanan Utama", sortOrder = 1),
      MerchantCategoryEntity(id = "cat_drink", storeId = "store_01", name = "Minuman Segar", sortOrder = 2),
      MerchantCategoryEntity(id = "cat_snack", storeId = "store_01", name = "Camilan & Tambahan", sortOrder = 3)
    )
    db.merchantDao().insertCategories(categories)

    val products = listOf(
      MerchantProductEntity(
        id = "prod_01",
        storeId = "store_01",
        categoryId = "cat_main",
        name = "Nasi Goreng Spesial Sukma",
        description = "Nasi goreng bumbu racik istimewa dengan suwiran ayam, sosis, bakso, dan telur mata sapi.",
        price = 28000L,
        stockQuantity = 45,
        isAvailable = true,
        soldCount = 412
      ),
      MerchantProductEntity(
        id = "prod_02",
        storeId = "store_01",
        categoryId = "cat_main",
        name = "Ayam Bakar Madu Pedas",
        description = "Ayam pejantan empuk dibakar dengan olesan madu hutan dan bumbu pedas manis, disajikan dengan lalapan & sambal terasi.",
        price = 35000L,
        stockQuantity = 28,
        isAvailable = true,
        soldCount = 289
      ),
      MerchantProductEntity(
        id = "prod_03",
        storeId = "store_01",
        categoryId = "cat_main",
        name = "Sate Ayam Bumbu Kacang (10 Tusuk)",
        description = "Daging ayam fillet lembut bakar arang dengan siraman bumbu kacang kental dan taburan bawang goreng gurih.",
        price = 32000L,
        stockQuantity = 35,
        isAvailable = true,
        soldCount = 345
      ),
      MerchantProductEntity(
        id = "prod_04",
        storeId = "store_01",
        categoryId = "cat_main",
        name = "Mie Goreng Jawa Seafood",
        description = "Mie telur kenyal dimasak dengan udang, cumi segar, sayuran hijau dan bumbu kemiri khas Jawa.",
        price = 30000L,
        stockQuantity = 20,
        isAvailable = true,
        soldCount = 198
      ),
      MerchantProductEntity(
        id = "prod_05",
        storeId = "store_01",
        categoryId = "cat_drink",
        name = "Es Teh Manis Melati",
        description = "Seduhan daun teh melati pilihan yang harum wangi dengan gula tebu asli dan es batu dingin segar.",
        price = 8000L,
        stockQuantity = 120,
        isAvailable = true,
        soldCount = 890
      ),
      MerchantProductEntity(
        id = "prod_06",
        storeId = "store_01",
        categoryId = "cat_drink",
        name = "Jus Alpukat Kocok Kental",
        description = "Alpukat mentega matang pohon dengan siraman kental manis cokelat dan serutan es kristal.",
        price = 18000L,
        stockQuantity = 30,
        isAvailable = true,
        soldCount = 275
      ),
      MerchantProductEntity(
        id = "prod_07",
        storeId = "store_01",
        categoryId = "cat_snack",
        name = "Tahu & Tempe Goreng Krispi",
        description = "Tahu susu lembut dan tempe kedelai gurih dibalut tepung bumbu renyah dengan sambal kecap rawit.",
        price = 14000L,
        stockQuantity = 50,
        isAvailable = true,
        soldCount = 180
      ),
      MerchantProductEntity(
        id = "prod_08",
        storeId = "store_01",
        categoryId = "cat_snack",
        name = "Pisang Goreng Keju Cokelat",
        description = "Pisang kepok manis digoreng krispi dengan taburan keju cheddar parut dan susu cokelat manis.",
        price = 18000L,
        stockQuantity = 25,
        isAvailable = true,
        soldCount = 145
      )
    )
    db.merchantDao().insertProducts(products)

    val merchantOrders = listOf(
      MerchantOrderEntity(
        id = "FOOD-ORD-01",
        storeId = "store_01",
        customerId = "user_cust_01",
        customerName = "Sukma Wijaya",
        customerPhone = "+6281234567890",
        driverId = "user_driver_01",
        driverName = "Ahmad Fauzi",
        status = MerchantOrderStatus.RECEIVED,
        itemsSummary = "2x Nasi Goreng Spesial, 2x Es Teh Manis Melati",
        subtotalAmount = 72000L,
        platformCommission = 10800L,
        netMerchantAmount = 61200L,
        paymentMethod = PaymentMethod.WALLET,
        deliveryAddress = "Grand Indonesia Tower lt. 15, Jl. M.H. Thamrin No.1",
        customerNotes = "Nasi goreng pedas sedang, es teh jangan terlalu manis.",
        estimatedPreparationMin = 15,
        createdAt = System.currentTimeMillis() - 300000L
      ),
      MerchantOrderEntity(
        id = "FOOD-ORD-02",
        storeId = "store_01",
        customerId = "user_cust_02",
        customerName = "Dewi Lestari",
        customerPhone = "+6281298765432",
        driverId = null,
        driverName = null,
        status = MerchantOrderStatus.PREPARING,
        itemsSummary = "1x Ayam Bakar Madu Pedas, 1x Jus Alpukat Kocok",
        subtotalAmount = 53000L,
        platformCommission = 7950L,
        netMerchantAmount = 45050L,
        paymentMethod = PaymentMethod.WALLET,
        deliveryAddress = "Apartemen Thamrin Residence Tower C-1802",
        customerNotes = "Sambal dipisah, ayam bakar matang merata ya min.",
        estimatedPreparationMin = 20,
        createdAt = System.currentTimeMillis() - 900000L
      ),
      MerchantOrderEntity(
        id = "FOOD-ORD-03",
        storeId = "store_01",
        customerId = "user_cust_03",
        customerName = "Budi Hartono",
        customerPhone = "+6281311223344",
        driverId = "user_driver_01",
        driverName = "Ahmad Fauzi",
        status = MerchantOrderStatus.READY_FOR_PICKUP,
        itemsSummary = "1x Sate Ayam Bumbu Kacang, 1x Pisang Goreng Keju",
        subtotalAmount = 50000L,
        platformCommission = 7500L,
        netMerchantAmount = 42500L,
        paymentMethod = PaymentMethod.WALLET,
        deliveryAddress = "Gedung UOB Plaza lt. 8, Jl. M.H. Thamrin",
        customerNotes = "Driver mohon bawa tas delivery makanan.",
        estimatedPreparationMin = 12,
        createdAt = System.currentTimeMillis() - 1500000L
      )
    )
    db.merchantDao().insertOrders(merchantOrders)

    // 10. Initial Audit Log
    db.auditDao().insertLog(
      AuditLogEntity(
        id = "LOG-001",
        actorId = "SYSTEM",
        actorRole = "SUPER_ADMIN",
        action = "DATABASE_BOOTSTRAP",
        entityType = "SYSTEM",
        entityId = "INITIAL_SEED",
        details = "Go Sukma Drive database seeded with 3 zones, 10 users, merchant store, catalog, policies, and ledger",
        createdAt = System.currentTimeMillis()
      )
    )
  }
}
