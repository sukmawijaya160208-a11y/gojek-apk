package com.example.core.model

enum class UserRole(val label: String) {
  CUSTOMER("Pelanggan"),
  DRIVER("Driver Mitra"),
  MERCHANT("Mitra Merchant"),
  OPS("Operasional / Dispatcher"),
  SUPPORT("Customer Support"),
  FINANCE("Keuangan & Settlement"),
  ADMIN("Administrator"),
  SUPER_ADMIN("Super Admin")
}

enum class UserStatus {
  ACTIVE,
  SUSPENDED,
  RESTRICTED
}

enum class ServiceType(val displayName: String, val vehicleType: String, val iconDescription: String) {
  RIDE_BIKE("Sukma Ride", "Motor", "Transportasi motor cepat & gesit"),
  RIDE_CAR("Sukma Car", "Mobil", "Perjalanan mobil nyaman ber-AC"),
  DELIVERY_BIKE("Sukma Send", "Motor", "Pengantaran dokumen & paket instan"),
  FOOD("Sukma Food", "Merchant", "Pesan antar makanan favorit"),
  SHOP("Sukma Shop", "Toko", "Belanja kebutuhan sehari-hari")
}

enum class OrderStatus(val label: String, val isTerminal: Boolean) {
  CREATED("Dibuat", false),
  MATCHING("Mencari Driver", false),
  DRIVER_ASSIGNED("Driver Ditemukan", false),
  ARRIVING("Driver Menuju Titik Jemput", false),
  ARRIVED("Driver Sudah Tiba", false),
  PICKED_UP("Penumpang Dijemput", false),
  IN_TRIP("Dalam Perjalanan", false),
  COMPLETING("Menyelesaikan Pesanan", false),
  COMPLETED("Selesai", true),
  CANCELLED("Dibatalkan", true),
  EXPIRED("Kedaluwarsa", true),
  PAYMENT_FAILED("Pembayaran Gagal", true)
}

enum class DriverOnlineState(val label: String) {
  OFFLINE("Offline"),
  ONLINE("Online & Siap"),
  OFFERED("Penawaran Masuk"),
  ACCEPTED("Tugas Diterima"),
  ARRIVING("Menuju Jemput"),
  ARRIVED("Tiba di Lokasi"),
  IN_TRIP("Mengantar"),
  COMPLETING("Selesai")
}

enum class PaymentStatus {
  CREATED,
  PENDING,
  AUTHORIZED,
  CAPTURED,
  FAILED,
  CANCELLED,
  REFUNDED
}

enum class PaymentMethod(val label: String) {
  WALLET("GoSukma Pay"),
  CASH("Tunai"),
  QRIS("QRIS")
}

enum class LedgerDirection {
  CREDIT,
  DEBIT
}

enum class LedgerReason(val label: String) {
  TRIP_FARE("Ongkos Perjalanan"),
  PLATFORM_FEE("Biaya Layanan Aplikasi"),
  DRIVER_EARNING("Pendapatan Driver"),
  TOP_UP("Isi Ulang Saldo"),
  REFUND("Pengembalian Dana"),
  TIP("Tip Apresiasi Driver"),
  CANCELLATION_FEE("Biaya Kompensasi Pembatalan"),
  MERCHANT_SALE("Penjualan Makanan Merchant"),
  MERCHANT_SETTLEMENT("Pencairan Saldo Toko")
}

enum class MerchantOrderStatus(val label: String) {
  RECEIVED("Pesanan Masuk"),
  CONFIRMED("Dikonfirmasi"),
  PREPARING("Sedang Dimasak"),
  READY_FOR_PICKUP("Siap Diambil Driver"),
  PICKED_UP("Diantar Driver"),
  COMPLETED("Selesai"),
  CANCELLED("Dibatalkan")
}

enum class IncidentSeverity(val label: String) {
  LOW("Rendah"),
  MEDIUM("Sedang"),
  HIGH("Tinggi"),
  CRITICAL("KRITIS / SOS")
}

enum class IncidentType(val label: String) {
  WRONG_DRIVER("Driver / Kendaraan Tidak Sesuai"),
  ACCIDENT("Kecelakaan Lalu Lintas"),
  HARASSMENT("Perilaku Tidak Pantas"),
  UNSAFE_DRIVING("Berkendara Membahayakan"),
  LOST_ITEM("Barang Tertinggal"),
  FRAUD("Kecurangan / Masalah Pembayaran")
}
