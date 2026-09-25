package com.example.features.customer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.OrderEntity
import com.example.core.database.entity.UserEntity
import com.example.core.model.LedgerDirection
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceType
import com.example.core.repository.OrderRepository
import com.example.core.repository.WalletRepository
import com.example.core.ui.components.SukmaButton
import com.example.core.ui.components.SukmaButtonVariant
import com.example.core.ui.components.SukmaCard
import com.example.core.ui.components.SukmaEmptyState
import com.example.core.ui.components.SukmaStatusBadge
import com.example.features.customer.components.JAKARTA_PLACES
import com.example.features.customer.components.LiveTripTrackingView
import com.example.features.customer.components.LocationPickerSheet
import com.example.features.customer.components.MatchingRadarView
import com.example.features.customer.components.PlaceLocation
import com.example.features.customer.components.ReceiptDetailDialog
import com.example.features.customer.components.ServiceSelectionView
import com.example.features.customer.components.TripCompletionView
import com.example.ui.theme.CardShape
import com.example.ui.theme.ControlShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaMintLight
import com.example.ui.theme.SukmaSlateDark
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSuccessGreen
import com.example.ui.theme.SukmaSurgeAmber
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Long): String {
  val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
  return format.format(amount).replace("Rp", "Rp ").substringBefore(",00")
}

enum class CustomerScreenMode {
  HOME_DASHBOARD,
  LOCATION_PICKER,
  SERVICE_SELECTION,
  MATCHING,
  LIVE_TRIP,
  COMPLETION_REVIEW
}

@Composable
fun CustomerHomeScreen(
  currentUser: UserEntity,
  orderRepository: OrderRepository,
  walletRepository: WalletRepository,
  onOpenWallet: () -> Unit,
  onOpenActivity: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val activeOrder by orderRepository.getActiveOrderByCustomer(currentUser.id).collectAsState(initial = null)
  val balance by walletRepository.getCalculatedBalance("w_cust_01").collectAsState(initial = 150000L)

  var currentMode by remember { mutableStateOf(CustomerScreenMode.HOME_DASHBOARD) }
  var selectedPickup by remember { mutableStateOf(JAKARTA_PLACES[0]) }
  var selectedDest by remember { mutableStateOf(JAKARTA_PLACES[1]) }
  var latestCompletedOrder by remember { mutableStateOf<OrderEntity?>(null) }

  // Auto-switch mode based on active order status
  if (activeOrder != null) {
    if (activeOrder!!.status == OrderStatus.MATCHING && currentMode == CustomerScreenMode.HOME_DASHBOARD) {
      currentMode = CustomerScreenMode.MATCHING
    } else if (activeOrder!!.status in listOf(OrderStatus.DRIVER_ASSIGNED, OrderStatus.ARRIVING, OrderStatus.ARRIVED, OrderStatus.IN_TRIP) && currentMode == CustomerScreenMode.HOME_DASHBOARD) {
      currentMode = CustomerScreenMode.LIVE_TRIP
    }
  }

  // Handle Android Back Navigation
  BackHandler(enabled = currentMode != CustomerScreenMode.HOME_DASHBOARD) {
    currentMode = CustomerScreenMode.HOME_DASHBOARD
  }

  when (currentMode) {
    CustomerScreenMode.LOCATION_PICKER -> {
      LocationPickerSheet(
        initialPickup = selectedPickup.address,
        initialDest = selectedDest.address,
        onConfirmRoute = { pickup, dest ->
          selectedPickup = pickup
          selectedDest = dest
          currentMode = CustomerScreenMode.SERVICE_SELECTION
        },
        onDismiss = { currentMode = CustomerScreenMode.HOME_DASHBOARD }
      )
    }

    CustomerScreenMode.SERVICE_SELECTION -> {
      ServiceSelectionView(
        pickup = selectedPickup,
        dest = selectedDest,
        currentUser = currentUser,
        orderRepository = orderRepository,
        walletRepository = walletRepository,
        onOrderConfirmed = { order ->
          currentMode = CustomerScreenMode.MATCHING
        },
        onChangeRouteClick = { currentMode = CustomerScreenMode.LOCATION_PICKER }
      )
    }

    CustomerScreenMode.MATCHING -> {
      if (activeOrder != null) {
        MatchingRadarView(
          order = activeOrder!!,
          onDriverAssigned = {
            scope.launch {
              orderRepository.assignDriverSimulated(activeOrder!!.id)
              currentMode = CustomerScreenMode.LIVE_TRIP
            }
          },
          onCancelOrder = {
            scope.launch {
              orderRepository.cancelOrder(activeOrder!!.id, "Dibatalkan saat pencarian driver", "CUSTOMER")
              currentMode = CustomerScreenMode.HOME_DASHBOARD
            }
          }
        )
      } else {
        currentMode = CustomerScreenMode.HOME_DASHBOARD
      }
    }

    CustomerScreenMode.LIVE_TRIP -> {
      if (activeOrder != null) {
        LiveTripTrackingView(
          order = activeOrder!!,
          onAdvanceMilestone = { nextStatus ->
            scope.launch {
              orderRepository.updateOrderStatus(activeOrder!!.id, nextStatus)
            }
          },
          onCancelTrip = {
            scope.launch {
              orderRepository.cancelOrder(activeOrder!!.id, "Dibatalkan oleh Pelanggan", "CUSTOMER")
              currentMode = CustomerScreenMode.HOME_DASHBOARD
            }
          },
          onTripCompleted = {
            scope.launch {
              val finishedOrder = activeOrder!!
              orderRepository.completeTrip(finishedOrder)
              latestCompletedOrder = finishedOrder
              currentMode = CustomerScreenMode.COMPLETION_REVIEW
            }
          }
        )
      } else {
        currentMode = CustomerScreenMode.HOME_DASHBOARD
      }
    }

    CustomerScreenMode.COMPLETION_REVIEW -> {
      if (latestCompletedOrder != null) {
        TripCompletionView(
          order = latestCompletedOrder!!,
          onSubmitReview = { rating, tipAmount, feedback ->
            scope.launch {
              orderRepository.submitRatingAndTip(latestCompletedOrder!!.id, rating, tipAmount)
              latestCompletedOrder = null
              currentMode = CustomerScreenMode.HOME_DASHBOARD
            }
          }
        )
      } else {
        currentMode = CustomerScreenMode.HOME_DASHBOARD
      }
    }

    CustomerScreenMode.HOME_DASHBOARD -> {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(4.dp))
          // Greeting & Header
          Column {
            Text(
              text = "Halo, ${currentUser.fullName} 👋",
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Mau bepergian atau kirim barang ke mana hari ini?",
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Active Order Live Tracker Banner (If Order Active)
        if (activeOrder != null) {
          item {
            ActiveOrderBanner(
              order = activeOrder!!,
              onOpenTracking = {
                currentMode = if (activeOrder!!.status == OrderStatus.MATCHING) {
                  CustomerScreenMode.MATCHING
                } else {
                  CustomerScreenMode.LIVE_TRIP
                }
              },
              onAdvanceTrip = {
                scope.launch {
                  val current = activeOrder!!.status
                  when (current) {
                    OrderStatus.MATCHING -> orderRepository.assignDriverSimulated(activeOrder!!.id)
                    OrderStatus.DRIVER_ASSIGNED -> orderRepository.updateOrderStatus(activeOrder!!.id, OrderStatus.ARRIVING)
                    OrderStatus.ARRIVING -> orderRepository.updateOrderStatus(activeOrder!!.id, OrderStatus.ARRIVED)
                    OrderStatus.ARRIVED -> orderRepository.updateOrderStatus(activeOrder!!.id, OrderStatus.IN_TRIP)
                    OrderStatus.IN_TRIP -> {
                      val finishedOrder = activeOrder!!
                      orderRepository.completeTrip(finishedOrder)
                      latestCompletedOrder = finishedOrder
                      currentMode = CustomerScreenMode.COMPLETION_REVIEW
                    }
                    else -> Unit
                  }
                }
              },
              onCancelTrip = {
                scope.launch {
                  orderRepository.cancelOrder(activeOrder!!.id, "Dibatalkan oleh Pelanggan", "CUSTOMER")
                }
              }
            )
          }
        }

        // Quick "Mau ke mana?" Search Box (Taps directly into Location Picker)
        item {
          Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, SukmaEmeraldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
              .fillMaxWidth()
              .clip(PillShape)
              .clickable { currentMode = CustomerScreenMode.LOCATION_PICKER }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SukmaEmeraldPrimary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Mau ke mana hari ini? Cari tujuan...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Wallet Summary Strip
        item {
          SukmaCard(onClick = onOpenWallet) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = SukmaMintLight,
                  modifier = Modifier.size(44.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.AccountBalanceWallet,
                      contentDescription = null,
                      tint = SukmaEmeraldPrimary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Saldo GoSukma Pay",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = formatRupiah(balance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
              SukmaButton(
                text = "Isi Saldo",
                onClick = onOpenWallet,
                variant = SukmaButtonVariant.OUTLINED,
                modifier = Modifier.height(38.dp)
              )
            }
          }
        }

        // Service Selector Grid
        item {
          Text(
            text = "Layanan Transportasi & Pesanan",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ServiceTileItem(
              title = "Sukma Ride",
              icon = Icons.Default.TwoWheeler,
              color = SukmaEmeraldPrimary,
              onClick = {
                selectedPickup = JAKARTA_PLACES[0]
                selectedDest = JAKARTA_PLACES[1]
                currentMode = CustomerScreenMode.SERVICE_SELECTION
              }
            )
            ServiceTileItem(
              title = "Sukma Car",
              icon = Icons.Default.DirectionsCar,
              color = SukmaEmeraldPrimary,
              onClick = {
                selectedPickup = JAKARTA_PLACES[0]
                selectedDest = JAKARTA_PLACES[2]
                currentMode = CustomerScreenMode.SERVICE_SELECTION
              }
            )
            ServiceTileItem(
              title = "Sukma Send",
              icon = Icons.Default.Navigation,
              color = SukmaEmeraldPrimary,
              onClick = {
                selectedPickup = JAKARTA_PLACES[0]
                selectedDest = JAKARTA_PLACES[4]
                currentMode = CustomerScreenMode.SERVICE_SELECTION
              }
            )
            ServiceTileItem(
              title = "Sukma Food",
              icon = Icons.Default.Restaurant,
              color = SukmaEmeraldPrimary,
              onClick = {
                selectedPickup = JAKARTA_PLACES[0]
                selectedDest = JAKARTA_PLACES[5]
                currentMode = CustomerScreenMode.SERVICE_SELECTION
              }
            )
          }
        }

        // Quick Popular Destinations Picker
        item {
          Text(
            text = "Destinasi Cepat & Populer",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
          ) {
            items(JAKARTA_PLACES) { place ->
              Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                  .clip(PillShape)
                  .clickable {
                    selectedPickup = JAKARTA_PLACES[0]
                    selectedDest = place
                    currentMode = CustomerScreenMode.SERVICE_SELECTION
                  }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SukmaEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = place.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }

        // Promo Voucher Banner
        item {
          SukmaCard {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Surface(
                  shape = PillShape,
                  color = SukmaSurgeAmber.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "VOUCHER AKTIF: SUKMABARU",
                    color = SukmaSurgeAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Diskon 50% hingga Rp 15.000",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Berlaku untuk semua armada Sukma Ride dan Sukma Car",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
fun ServiceTileItem(
  title: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(ControlShape)
      .clickable(onClick = onClick)
      .padding(4.dp)
  ) {
    Surface(
      shape = CardShape,
      color = color.copy(alpha = 0.12f),
      border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
      modifier = Modifier.size(60.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = color,
          modifier = Modifier.size(30.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun ActiveOrderBanner(
  order: OrderEntity,
  onOpenTracking: () -> Unit,
  onAdvanceTrip: () -> Unit,
  onCancelTrip: () -> Unit
) {
  Card(
    shape = CardShape,
    colors = CardDefaults.cardColors(
      containerColor = SukmaMintLight
    ),
    border = BorderStroke(1.5.dp, SukmaEmeraldPrimary),
    modifier = Modifier
      .fillMaxWidth()
      .clip(CardShape)
      .clickable(onClick = onOpenTracking)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = order.serviceType.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = SukmaSlateDark
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "#${order.id}",
            fontSize = 12.sp,
            color = SukmaSlateSubtle
          )
        }
        SukmaStatusBadge(status = order.status)
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Tujuan: ${order.destAddress}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = SukmaSlateDark
      )

      if (order.driverName != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Driver: ${order.driverName} • ${order.driverPlate} (${order.driverVehicle})",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = SukmaEmeraldPrimary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val nextActionLabel = when (order.status) {
          OrderStatus.MATCHING -> "Buka Radar / Cari"
          OrderStatus.DRIVER_ASSIGNED -> "Lihat Peta Penjemputan"
          OrderStatus.ARRIVING -> "Driver Tiba di Lokasi"
          OrderStatus.ARRIVED -> "Mulai Perjalanan"
          OrderStatus.IN_TRIP -> "Lihat Peta Perjalanan"
          else -> "Buka Pelacakan"
        }

        SukmaButton(
          text = nextActionLabel,
          onClick = onOpenTracking,
          variant = SukmaButtonVariant.PRIMARY,
          modifier = Modifier.weight(1f)
        )

        if (order.status != OrderStatus.IN_TRIP) {
          SukmaButton(
            text = "Batal",
            onClick = onCancelTrip,
            variant = SukmaButtonVariant.DANGER
          )
        }
      }
    }
  }
}

@Composable
fun CustomerActivityScreen(
  currentUser: UserEntity,
  orderRepository: OrderRepository,
  onRebookOrder: ((OrderEntity) -> Unit)? = null
) {
  val orders by orderRepository.getOrdersByCustomer(currentUser.id).collectAsState(initial = emptyList())
  var selectedOrderForReceipt by remember { mutableStateOf<OrderEntity?>(null) }

  if (orders.isEmpty()) {
    SukmaEmptyState(
      title = "Belum Ada Riwayat Pesanan",
      description = "Pesanan perjalanan atau pengiriman Anda akan tercatat secara otomatis di sini.",
      icon = Icons.Default.History
    )
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Riwayat Aktivitas & Struk Resmi",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      items(orders) { order ->
        SukmaCard(onClick = { selectedOrderForReceipt = order }) {
          Column {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(
                  text = order.serviceType.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Order ID: #${order.id}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              SukmaStatusBadge(status = order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Dari: ${order.pickupAddress}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Ke: ${order.destAddress}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Metode: ${order.paymentMethod.label}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = formatRupiah(order.totalFare),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SukmaEmeraldPrimary
              )
            }
          }
        }
      }
      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Digital Receipt Detail Modal
  if (selectedOrderForReceipt != null) {
    ReceiptDetailDialog(
      order = selectedOrderForReceipt!!,
      onRebook = {
        val target = selectedOrderForReceipt!!
        selectedOrderForReceipt = null
        onRebookOrder?.invoke(target)
      },
      onDismiss = { selectedOrderForReceipt = null }
    )
  }
}

@Composable
fun CustomerWalletScreen(
  walletRepository: WalletRepository
) {
  val scope = rememberCoroutineScope()
  val balance by walletRepository.getCalculatedBalance("w_cust_01").collectAsState(initial = 0L)
  val ledgerEntries by walletRepository.getLedgerEntries("w_cust_01").collectAsState(initial = emptyList())

  var showTopUpDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    // Wallet Hero Card
    Card(
      shape = CardShape,
      colors = CardDefaults.cardColors(
        containerColor = SukmaSlateDark
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "GoSukma Pay • Saldo Aktif",
          fontSize = 13.sp,
          color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = formatRupiah(balance),
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          SukmaButton(
            text = "Top Up Saldo",
            icon = Icons.Default.Add,
            onClick = { showTopUpDialog = true },
            variant = SukmaButtonVariant.PRIMARY
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = "Buku Besar Transaksi (Immutable Ledger)",
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (ledgerEntries.isEmpty()) {
      SukmaEmptyState(
        title = "Belum Ada Transaksi",
        description = "Setiap mutasi kredit atau debit akan tercatat permanen di ledger ini.",
        icon = Icons.Default.AccountBalanceWallet
      )
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
      ) {
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
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
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
                color = if (isCredit) SukmaSuccessGreen else Color(0xFFDC2626)
              )
            }
          }
        }
      }
    }
  }

  // Top Up Action Modal
  if (showTopUpDialog) {
    val presets = listOf(20000L, 50000L, 100000L, 200000L)
    var selectedAmount by remember { mutableStateOf(50000L) }

    AlertDialog(
      onDismissRequest = { showTopUpDialog = false },
      title = { Text("Top Up Saldo GoSukma Pay", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Pilih nominal pengisian saldo:", fontSize = 13.sp)
          presets.forEach { amount ->
            Surface(
              shape = ControlShape,
              border = BorderStroke(
                1.dp,
                if (selectedAmount == amount) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline
              ),
              color = if (selectedAmount == amount) SukmaEmeraldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .fillMaxWidth()
                .clip(ControlShape)
                .clickable { selectedAmount = amount }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(12.dp)
              ) {
                Text(text = formatRupiah(amount), fontWeight = FontWeight.Bold)
                if (selectedAmount == amount) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SukmaEmeraldPrimary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Konfirmasi Top Up",
          onClick = {
            scope.launch {
              walletRepository.topUpWallet("w_cust_01", selectedAmount, "BCA Virtual Account")
              showTopUpDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showTopUpDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}
