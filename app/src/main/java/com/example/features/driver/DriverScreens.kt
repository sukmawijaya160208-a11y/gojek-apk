package com.example.features.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.DriverProfileEntity
import com.example.core.database.entity.OrderEntity
import com.example.core.database.entity.UserEntity
import com.example.core.model.DriverOnlineState
import com.example.core.model.LedgerDirection
import com.example.core.model.OrderStatus
import com.example.core.model.ServiceType
import com.example.core.repository.DriverRepository
import com.example.core.repository.OrderRepository
import com.example.core.repository.WalletRepository
import com.example.core.ui.components.PriceRow
import com.example.core.ui.components.SukmaButton
import com.example.core.ui.components.SukmaButtonVariant
import com.example.core.ui.components.SukmaCard
import com.example.core.ui.components.SukmaEmptyState
import com.example.core.ui.components.SukmaStatusBadge
import com.example.features.customer.components.MapRouteCanvas
import com.example.features.customer.formatRupiah
import com.example.ui.theme.CardShape
import com.example.ui.theme.ControlShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmergencyLight
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaMintAccent
import com.example.ui.theme.SukmaMintLight
import com.example.ui.theme.SukmaSlateDark
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSuccessGreen
import com.example.ui.theme.SukmaSuccessLight
import com.example.ui.theme.SukmaSurgeAmber
import com.example.ui.theme.SukmaSurgeLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DriverTab(val label: String, val icon: ImageVector) {
  HOME("Beranda", Icons.Default.Home),
  NAVIGATION("Navigasi", Icons.Default.Navigation),
  EARNINGS("Pendapatan", Icons.Default.Payments),
  KYC_PROFILE("Profil & KYC", Icons.Default.Badge)
}

@Composable
fun DriverHomeScreen(
  currentUser: UserEntity,
  driverRepository: DriverRepository,
  orderRepository: OrderRepository,
  walletRepository: WalletRepository
) {
  DriverMainScreen(
    currentUser = currentUser,
    driverRepository = driverRepository,
    orderRepository = orderRepository,
    walletRepository = walletRepository
  )
}

@Composable
fun StatPill(label: String, value: String, valueColor: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
  }
}

@Composable
fun DriverMainScreen(
  currentUser: UserEntity,
  driverRepository: DriverRepository,
  orderRepository: OrderRepository,
  walletRepository: WalletRepository
) {
  val scope = rememberCoroutineScope()
  var currentTab by remember { mutableStateOf(DriverTab.HOME) }

  val driverProfile by driverRepository.getDriverProfile("user_driver_01").collectAsState(
    initial = DriverProfileEntity(
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
      dailyTripTarget = 8,
      todayCompletedTrips = 5,
      activeIncentiveBonus = 35000L
    )
  )

  val activeOrder by orderRepository.getActiveOrderByDriver("user_driver_01").collectAsState(initial = null)
  val pendingMatchingOrder by driverRepository.getPendingMatchingOrder().collectAsState(initial = null)

  var dismissedOrderId by remember { mutableStateOf<String?>(null) }

  // Auto-switch to navigation tab if active order is present
  LaunchedEffect(activeOrder?.id) {
    if (activeOrder != null && currentTab == DriverTab.HOME) {
      currentTab = DriverTab.NAVIGATION
    }
  }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
      ) {
        DriverTab.values().forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.label
              )
            },
            label = {
              Text(
                text = tab.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SukmaEmeraldPrimary,
              selectedTextColor = SukmaEmeraldPrimary,
              indicatorColor = SukmaEmeraldPrimary.copy(alpha = 0.15f),
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        DriverTab.HOME -> {
          DriverHomeTab(
            driverProfile = driverProfile ?: DriverProfileEntity(
              userId = "user_driver_01",
              fullName = "Ahmad Fauzi",
              phone = "+6281987654321"
            ),
            activeOrder = activeOrder,
            driverRepository = driverRepository,
            walletRepository = walletRepository,
            onOpenActiveNavigation = { currentTab = DriverTab.NAVIGATION },
            onOpenEarnings = { currentTab = DriverTab.EARNINGS },
            onOpenKyc = { currentTab = DriverTab.KYC_PROFILE }
          )
        }

        DriverTab.NAVIGATION -> {
          DriverNavigationTab(
            driverProfile = driverProfile ?: DriverProfileEntity(
              userId = "user_driver_01",
              fullName = "Ahmad Fauzi",
              phone = "+6281987654321"
            ),
            activeOrder = activeOrder,
            orderRepository = orderRepository,
            onTripFinished = { currentTab = DriverTab.EARNINGS }
          )
        }

        DriverTab.EARNINGS -> {
          DriverEarningsTab(
            driverProfile = driverProfile ?: DriverProfileEntity(
              userId = "user_driver_01",
              fullName = "Ahmad Fauzi",
              phone = "+6281987654321"
            ),
            driverRepository = driverRepository,
            walletRepository = walletRepository
          )
        }

        DriverTab.KYC_PROFILE -> {
          DriverKycProfileTab(
            driverProfile = driverProfile ?: DriverProfileEntity(
              userId = "user_driver_01",
              fullName = "Ahmad Fauzi",
              phone = "+6281987654321"
            ),
            driverRepository = driverRepository
          )
        }
      }

      // Incoming Order Offer Modal (Full Card)
      if (pendingMatchingOrder != null &&
        pendingMatchingOrder!!.id != dismissedOrderId &&
        activeOrder == null &&
        driverProfile?.onlineState != DriverOnlineState.OFFLINE
      ) {
        IncomingOrderOfferDialog(
          order = pendingMatchingOrder!!,
          onAccept = {
            scope.launch {
              driverProfile?.let { prof ->
                driverRepository.acceptOffer(pendingMatchingOrder!!.id, prof)
                currentTab = DriverTab.NAVIGATION
              }
            }
          },
          onReject = {
            scope.launch {
              dismissedOrderId = pendingMatchingOrder!!.id
              driverRepository.rejectOffer(pendingMatchingOrder!!.id, "user_driver_01")
            }
          }
        )
      }
    }
  }
}

/**
 * Tab 1: Driver Home (Online/Offline, Summary Stats, Active Job Banner)
 */
@Composable
fun DriverHomeTab(
  driverProfile: DriverProfileEntity,
  activeOrder: OrderEntity?,
  driverRepository: DriverRepository,
  walletRepository: WalletRepository,
  onOpenActiveNavigation: () -> Unit,
  onOpenEarnings: () -> Unit,
  onOpenKyc: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val earnings by walletRepository.getCalculatedBalance("w_driver_01").collectAsState(initial = 320000L)
  val isOnline = driverProfile.onlineState != DriverOnlineState.OFFLINE

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Online / Offline Master Switch Card
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(
          containerColor = if (isOnline) SukmaEmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
          width = 1.5.dp,
          color = if (isOnline) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .background(if (isOnline) SukmaEmeraldPrimary else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isOnline) "Driver ONLINE & Siap" else "Driver OFFLINE",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isOnline) "GPS Aktif • Menerima orderan di radius 3 km" else "Aktifkan sakelar untuk mulai menerima order",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Switch(
            checked = isOnline,
            onCheckedChange = { online ->
              scope.launch {
                driverRepository.updateOnlineState(
                  driverProfile.userId,
                  if (online) DriverOnlineState.ONLINE else DriverOnlineState.OFFLINE
                )
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = SukmaEmeraldPrimary
            )
          )
        }
      }
    }

    // Daily Earnings Card with quick Cashout link
    item {
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(
          containerColor = SukmaSlateDark
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clip(CardShape)
          .clickable(onClick = onOpenEarnings)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Pendapatan Hari Ini",
              fontSize = 13.sp,
              color = Color(0xFF94A3B8)
            )
            Surface(
              shape = PillShape,
              color = SukmaEmeraldPrimary.copy(alpha = 0.25f)
            ) {
              Text(
                text = "Lihat Rincian ➔",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SukmaMintAccent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = formatRupiah(earnings),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(14.dp))
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            StatPill("Rating", "${driverProfile.rating} ★", SukmaSurgeAmber)
            StatPill("Penerimaan", "${driverProfile.acceptanceRate}%", SukmaEmeraldPrimary)
            StatPill("Trip Hari Ini", "${driverProfile.todayCompletedTrips} / ${driverProfile.dailyTripTarget}", Color.White)
          }
        }
      }
    }

    // Active Order Banner if on-trip
    if (activeOrder != null) {
      item {
        Text(
          text = "Tugas Aktif Berjalan",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        SukmaCard(onClick = onOpenActiveNavigation) {
          Column {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "${activeOrder.serviceType.displayName} #${activeOrder.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              SukmaStatusBadge(status = activeOrder.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Penumpang: ${activeOrder.customerName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = "Jemput: ${activeOrder.pickupAddress}", fontSize = 12.sp, color = SukmaSlateSubtle)
            Text(text = "Tujuan: ${activeOrder.destAddress}", fontSize = 12.sp, color = SukmaSlateSubtle)
            Spacer(modifier = Modifier.height(12.dp))
            SukmaButton(
              text = "Buka Layar Navigasi Penuh",
              icon = Icons.Default.Navigation,
              onClick = onOpenActiveNavigation,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    } else {
      item {
        SukmaCard {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = SukmaEmeraldPrimary,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isOnline) "Menunggu Orderan Baru..." else "Driver Sedang Offline",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isOnline) "Pesanan dari pelanggan terdekat akan langsung muncul secara realtime di layar ini." else "Nyalakan sakelar di atas agar sistem dispatch dapat mengirim order.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    // Incentive Target Progress Bar
    item {
      Text(
        text = "Misi & Target Insentif Harian",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      SukmaCard(onClick = onOpenEarnings) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = SukmaSurgeAmber)
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Bonus Rp ${formatRupiah(driverProfile.activeIncentiveBonus)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(text = "${driverProfile.todayCompletedTrips} / ${driverProfile.dailyTripTarget} Trip", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }
          val progress = (driverProfile.todayCompletedTrips.toFloat() / driverProfile.dailyTripTarget.toFloat()).coerceIn(0f, 1f)
          LinearProgressIndicator(
            progress = { progress },
            color = SukmaEmeraldPrimary,
            trackColor = SukmaEmeraldPrimary.copy(alpha = 0.2f),
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(PillShape)
          )
          Text(
            text = if (progress >= 1f) "Selamat! Target insentif tercapai. Klaim bonus Anda." else "Selesaikan ${driverProfile.dailyTripTarget - driverProfile.todayCompletedTrips} perjalanan lagi untuk mendapat bonus.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Vehicle & KYC Status Card
    item {
      Text(
        text = "Profil Mitra & Status Dokumen",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      SukmaCard(onClick = onOpenKyc) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = CircleShape,
            color = SukmaEmeraldPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(48.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = SukmaEmeraldPrimary)
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "${driverProfile.vehicleModel} (${driverProfile.vehicleType})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "Plat: ${driverProfile.plateNumber} • ${driverProfile.verificationState}", fontSize = 12.sp, color = SukmaSlateSubtle)
          }
          Surface(
            shape = PillShape,
            color = SukmaSuccessLight
          ) {
            Text(
              text = "TERVERIFIKASI",
              color = SukmaSuccessGreen,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

/**
 * Tab 2: Full Driver Navigation Screen with Milestones & Turn-by-Turn Guidance
 */
@Composable
fun DriverNavigationTab(
  driverProfile: DriverProfileEntity,
  activeOrder: OrderEntity?,
  orderRepository: OrderRepository,
  onTripFinished: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var showSosDialog by remember { mutableStateOf(false) }
  var showChatDialog by remember { mutableStateOf(false) }

  if (activeOrder == null) {
    SukmaEmptyState(
      title = "Tidak Ada Tugas Aktif",
      description = "Anda belum memiliki pesanan aktif saat ini. Buka Beranda dan pastikan status Anda ONLINE untuk menerima tawaran order.",
      icon = Icons.Default.Navigation
    )
  } else {
    val navInstruction = when (activeOrder.status) {
      OrderStatus.DRIVER_ASSIGNED -> "Menuju Titik Penjemputan" to "Belok kanan 150m ke lobi utama mall"
      OrderStatus.ARRIVING -> "Hampir Sampai di Titik Jemput" to "Posisi penumpang berada di depan lobby Starbucks"
      OrderStatus.ARRIVED -> "Driver Tiba di Titik Jemput" to "Menunggu penumpang masuk ke kendaraan"
      OrderStatus.IN_TRIP -> "Dalam Perjalanan ke Tujuan" to "Lurus 2.1 km melalui Jl. Sudirman menuju Stasiun"
      else -> "Perjalanan Selesai" to "Konfirmasi pembayaran diterima"
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        // Map Route Canvas
        MapRouteCanvas(
          status = activeOrder.status,
          serviceType = activeOrder.serviceType,
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
        )
      }

      // Turn-by-turn banner
      item {
        Card(
          shape = ControlShape,
          colors = CardDefaults.cardColors(
            containerColor = SukmaSlateDark
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Navigation,
              contentDescription = null,
              tint = SukmaMintAccent,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = navInstruction.first,
                fontSize = 12.sp,
                color = SukmaMintAccent,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = navInstruction.second,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // Customer Contact & Order Snapshot Card
      item {
        SukmaCard {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(
                  text = activeOrder.customerName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
                Text(
                  text = "Order #${activeOrder.id} • ${activeOrder.serviceType.displayName}",
                  fontSize = 12.sp,
                  color = SukmaSlateSubtle
                )
              }
              SukmaStatusBadge(status = activeOrder.status)
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = SukmaEmeraldPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Jemput: ${activeOrder.pickupAddress}", fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = SukmaEmergencyRed, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Tujuan: ${activeOrder.destAddress}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            HorizontalDivider()

            // Masked Communication & SOS Buttons
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              TextButton(onClick = { showChatDialog = true }) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = SukmaEmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chat Penumpang", color = SukmaEmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }

              TextButton(onClick = { showSosDialog = true }) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SukmaEmergencyRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bantuan SOS", color = SukmaEmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Progression Action Buttons
      item {
        val (actionLabel, nextStatus) = when (activeOrder.status) {
          OrderStatus.DRIVER_ASSIGNED -> "Menuju Titik Jemput" to OrderStatus.ARRIVING
          OrderStatus.ARRIVING -> "Saya Sudah Sampai di Titik Jemput" to OrderStatus.ARRIVED
          OrderStatus.ARRIVED -> "Penumpang Sudah Naik • Mulai Perjalanan" to OrderStatus.IN_TRIP
          OrderStatus.IN_TRIP -> "Selesaikan Perjalanan & Terima Ongkos (${formatRupiah(activeOrder.totalFare)})" to OrderStatus.COMPLETED
          else -> "Selesaikan Pesanan" to OrderStatus.COMPLETED
        }

        SukmaButton(
          text = actionLabel,
          onClick = {
            scope.launch {
              if (nextStatus == OrderStatus.COMPLETED) {
                orderRepository.completeTrip(activeOrder)
                onTripFinished()
              } else {
                orderRepository.updateOrderStatus(activeOrder.id, nextStatus)
              }
            }
          },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    if (showChatDialog) {
      var msg by remember { mutableStateOf("") }
      AlertDialog(
        onDismissRequest = { showChatDialog = false },
        title = { Text("Kirim Pesan ke Penumpang", fontWeight = FontWeight.Bold) },
        text = {
          OutlinedTextField(
            value = msg,
            onValueChange = { msg = it },
            placeholder = { Text("Contoh: Saya sudah di depan lobi dengan jaket hijau...") },
            modifier = Modifier.fillMaxWidth()
          )
        },
        confirmButton = {
          SukmaButton(
            text = "Kirim",
            onClick = { showChatDialog = false }
          )
        },
        dismissButton = {
          TextButton(onClick = { showChatDialog = false }) {
            Text("Tutup")
          }
        }
      )
    }

    if (showSosDialog) {
      AlertDialog(
        onDismissRequest = { showSosDialog = false },
        title = { Text("Panggilan Darurat Driver (SOS)", fontWeight = FontWeight.Bold, color = SukmaEmergencyRed) },
        text = {
          Text("Sistem akan langsung mengirimkan peringatan darurat ke tim Dispatcher Operasional Go Sukma dengan telemetri lokasi kendaraan Anda.")
        },
        confirmButton = {
          SukmaButton(
            text = "Kirim Sinyal Darurat",
            variant = SukmaButtonVariant.DANGER,
            onClick = { showSosDialog = false }
          )
        },
        dismissButton = {
          TextButton(onClick = { showSosDialog = false }) {
            Text("Batal")
          }
        }
      )
    }
  }
}

/**
 * Tab 3: Driver Earnings & Double-entry Ledger & Payout
 */
@Composable
fun DriverEarningsTab(
  driverProfile: DriverProfileEntity,
  driverRepository: DriverRepository,
  walletRepository: WalletRepository
) {
  val scope = rememberCoroutineScope()
  val earnings by walletRepository.getCalculatedBalance("w_driver_01").collectAsState(initial = 320000L)
  val ledgerEntries by walletRepository.getLedgerEntries("w_driver_01").collectAsState(initial = emptyList())

  var showPayoutDialog by remember { mutableStateOf(false) }
  var isClaimingBonus by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Earnings Hero Card
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(
          containerColor = SukmaSlateDark
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Total Saldo Pendapatan Mitra",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = formatRupiah(earnings),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(14.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SukmaButton(
              text = "Tarik Saldo (Payout)",
              icon = Icons.Default.AccountBalance,
              onClick = { showPayoutDialog = true },
              variant = SukmaButtonVariant.PRIMARY
            )
          }
        }
      }
    }

    // Incentive Target Quest & Claim Card
    item {
      SukmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Target Insentif Harian", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Surface(shape = PillShape, color = SukmaSurgeLight) {
              Text(
                text = "BONUS: ${formatRupiah(driverProfile.activeIncentiveBonus)}",
                color = SukmaSurgeAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          val progress = (driverProfile.todayCompletedTrips.toFloat() / driverProfile.dailyTripTarget.toFloat()).coerceIn(0f, 1f)
          LinearProgressIndicator(
            progress = { progress },
            color = SukmaEmeraldPrimary,
            trackColor = SukmaEmeraldPrimary.copy(alpha = 0.2f),
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(PillShape)
          )

          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Progres Hari Ini", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "${driverProfile.todayCompletedTrips} dari ${driverProfile.dailyTripTarget} Trip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          SukmaButton(
            text = if (driverProfile.todayCompletedTrips >= driverProfile.dailyTripTarget) "Klaim Bonus ${formatRupiah(driverProfile.activeIncentiveBonus)}" else "Simulasikan Klaim Bonus Insentif",
            icon = Icons.Default.EmojiEvents,
            isLoading = isClaimingBonus,
            onClick = {
              isClaimingBonus = true
              scope.launch {
                driverRepository.claimIncentiveBonus(driverProfile.userId, driverProfile.activeIncentiveBonus)
                isClaimingBonus = false
              }
            },
            variant = SukmaButtonVariant.OUTLINED,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Double-Entry Ledger History
    item {
      Text(
        text = "Riwayat Buku Besar Pendapatan",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    if (ledgerEntries.isEmpty()) {
      item {
        SukmaEmptyState(
          title = "Belum Ada Mutasi",
          description = "Setiap komisi perjalanan, tip, dan penarikan saldo akan dicatat di buku besar ini.",
          icon = Icons.Default.History
        )
      }
    } else {
      items(ledgerEntries) { entry ->
        SukmaCard {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = entry.description,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
              Text(
                text = "Ref: ${entry.referenceId} • ${entry.reason.label}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            val isCredit = entry.direction == LedgerDirection.CREDIT
            Text(
              text = "${if (isCredit) "+" else "-"} ${formatRupiah(entry.amount)}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (isCredit) SukmaSuccessGreen else SukmaEmergencyRed
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Payout Dialog
  if (showPayoutDialog) {
    var payoutAmount by remember { mutableStateOf(100000L) }
    var isSubmittingPayout by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showPayoutDialog = false },
      title = { Text("Tarik Saldo ke Rekening Bank", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Rekening Tujuan Pencairan:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${driverProfile.bankName} - ${driverProfile.bankAccountNumber}\na.n ${driverProfile.bankAccountHolder}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
          Text("Pilih Nominal Penarikan:", fontSize = 12.sp)
          listOf(50000L, 100000L, 200000L).forEach { amt ->
            Surface(
              shape = ControlShape,
              color = if (payoutAmount == amt) SukmaEmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, if (payoutAmount == amt) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline),
              modifier = Modifier
                .fillMaxWidth()
                .clip(ControlShape)
                .clickable { payoutAmount = amt }
            ) {
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(12.dp)
              ) {
                Text(text = formatRupiah(amt), fontWeight = FontWeight.Bold)
                if (payoutAmount == amt) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SukmaEmeraldPrimary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Konfirmasi Tarik (${formatRupiah(payoutAmount)})",
          isLoading = isSubmittingPayout,
          onClick = {
            isSubmittingPayout = true
            scope.launch {
              driverRepository.requestPayout(
                driverProfile.userId,
                payoutAmount,
                "${driverProfile.bankName} ${driverProfile.bankAccountNumber}"
              )
              isSubmittingPayout = false
              showPayoutDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showPayoutDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

/**
 * Tab 4: Driver Onboarding & KYC Document Verification
 */
@Composable
fun DriverKycProfileTab(
  driverProfile: DriverProfileEntity,
  driverRepository: DriverRepository
) {
  val scope = rememberCoroutineScope()
  var showEditKycDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Profile Hero Card
      SukmaCard {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = CircleShape,
            color = SukmaEmeraldPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(60.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SukmaEmeraldPrimary, modifier = Modifier.size(32.dp))
            }
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = driverProfile.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = driverProfile.phone, fontSize = 13.sp, color = SukmaSlateSubtle)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = PillShape, color = SukmaSuccessLight) {
              Text(
                text = "MITRA TERVERIFIKASI",
                color = SukmaSuccessGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    item {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Kelengkapan Dokumen KYC",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = { showEditKycDialog = true }) {
          Text("Perbarui Data", color = SukmaEmeraldPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Document Cards
    item {
      KycDocItem(title = "Kartu Tanda Penduduk (KTP)", value = driverProfile.ktpNumber, status = "Terverifikasi Dukcapil")
    }
    item {
      KycDocItem(title = "Surat Izin Mengemudi (SIM C/A)", value = driverProfile.simNumber, status = "Valid s/d 2029")
    }
    item {
      KycDocItem(title = "Surat Tanda Nomor Kendaraan (STNK)", value = "${driverProfile.plateNumber} (No: ${driverProfile.stnkNumber})", status = "Pajak Aktif")
    }
    item {
      KycDocItem(title = "Surat Keterangan Catatan Kepolisian (SKCK)", value = "Nomor Reg: SKCK-POLRES-2026", status = driverProfile.skckStatus)
    }
    item {
      KycDocItem(title = "Rekening Bank Pencairan Saldo", value = "${driverProfile.bankName}\n${driverProfile.bankAccountNumber} a.n ${driverProfile.bankAccountHolder}", status = "Rekening Utama")
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Edit / Re-submit KYC Dialog
  if (showEditKycDialog) {
    var ktpInput by remember { mutableStateOf(driverProfile.ktpNumber) }
    var simInput by remember { mutableStateOf(driverProfile.simNumber) }
    var stnkInput by remember { mutableStateOf(driverProfile.stnkNumber) }
    var bankInput by remember { mutableStateOf(driverProfile.bankName) }
    var accountNumInput by remember { mutableStateOf(driverProfile.bankAccountNumber) }

    AlertDialog(
      onDismissRequest = { showEditKycDialog = false },
      title = { Text("Perbarui Dokumen KYC", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = ktpInput, onValueChange = { ktpInput = it }, label = { Text("Nomor NIK KTP") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = simInput, onValueChange = { simInput = it }, label = { Text("Nomor SIM") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = stnkInput, onValueChange = { stnkInput = it }, label = { Text("Nomor STNK") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = bankInput, onValueChange = { bankInput = it }, label = { Text("Nama Bank") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = accountNumInput, onValueChange = { accountNumInput = it }, label = { Text("Nomor Rekening") }, modifier = Modifier.fillMaxWidth())
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Simpan & Verifikasi",
          onClick = {
            scope.launch {
              driverRepository.submitKycDocuments(
                driverId = driverProfile.userId,
                ktp = ktpInput,
                sim = simInput,
                stnk = stnkInput,
                bankName = bankInput,
                accountNum = accountNumInput,
                accountHolder = driverProfile.fullName
              )
              showEditKycDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showEditKycDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun KycDocItem(
  title: String,
  value: String,
  status: String
) {
  SukmaCard {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = status, fontSize = 11.sp, color = SukmaSuccessGreen, fontWeight = FontWeight.SemiBold)
      }
      Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = SukmaSuccessGreen)
    }
  }
}

/**
 * Incoming Job Offer Card with 20s Countdown Timer
 */
@Composable
fun IncomingOrderOfferDialog(
  order: OrderEntity,
  onAccept: () -> Unit,
  onReject: () -> Unit
) {
  var countdownSeconds by remember { mutableStateOf(20) }

  LaunchedEffect(order.id) {
    while (countdownSeconds > 0) {
      delay(1000L)
      countdownSeconds--
    }
    onReject()
  }

  val progress = (countdownSeconds.toFloat() / 20f).coerceIn(0f, 1f)

  AlertDialog(
    onDismissRequest = onReject,
    title = {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (order.serviceType == ServiceType.RIDE_CAR) Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
            contentDescription = null,
            tint = SukmaEmeraldPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Order Masuk: ${order.serviceType.displayName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Surface(
          shape = CircleShape,
          color = SukmaSurgeLight,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "$countdownSeconds",
              color = SukmaSurgeAmber,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
          progress = { progress },
          color = SukmaSurgeAmber,
          trackColor = SukmaSurgeAmber.copy(alpha = 0.2f),
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(PillShape)
        )

        Card(
          shape = ControlShape,
          colors = CardDefaults.cardColors(containerColor = SukmaMintLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Pendapatan Bersih Mitra", fontSize = 12.sp, color = SukmaSlateSubtle)
            val driverNet = maxOf(0L, order.totalFare - order.platformFee)
            Text(text = formatRupiah(driverNet), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SukmaEmeraldPrimary)
            Text(text = "Metode: ${order.paymentMethod.label}", fontSize = 11.sp, color = SukmaSlateSubtle)
          }
        }

        PriceRow("Jarak Rute", "${order.distanceKm} km (Est. ${order.durationMin} mnt)")
        PriceRow("Titik Jemput", order.pickupAddress)
        PriceRow("Tujuan", order.destAddress)
        PriceRow("Pelanggan", order.customerName)
      }
    },
    confirmButton = {
      SukmaButton(
        text = "Terima Order (${countdownSeconds}s)",
        onClick = onAccept,
        variant = SukmaButtonVariant.PRIMARY,
        modifier = Modifier.fillMaxWidth()
      )
    },
    dismissButton = {
      TextButton(onClick = onReject, modifier = Modifier.fillMaxWidth()) {
        Text("Tolak", color = SukmaEmergencyRed, fontWeight = FontWeight.Bold)
      }
    }
  )
}
