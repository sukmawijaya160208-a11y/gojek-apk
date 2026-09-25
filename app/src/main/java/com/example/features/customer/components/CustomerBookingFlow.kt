package com.example.features.customer.components

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.core.database.entity.OrderEntity
import com.example.core.database.entity.UserEntity
import com.example.core.model.OrderStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceType
import com.example.core.repository.OrderRepository
import com.example.core.repository.PricingQuote
import com.example.core.repository.WalletRepository
import com.example.core.ui.components.PriceRow
import com.example.core.ui.components.SukmaButton
import com.example.core.ui.components.SukmaButtonVariant
import com.example.core.ui.components.SukmaCard
import com.example.core.ui.components.SukmaStatusBadge
import com.example.features.customer.formatRupiah
import com.example.ui.theme.CardShape
import com.example.ui.theme.ControlShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmergencyLight
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaMintLight
import com.example.ui.theme.SukmaSlateDark
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSuccessGreen
import com.example.ui.theme.SukmaSuccessLight
import com.example.ui.theme.SukmaSurgeAmber
import com.example.ui.theme.SukmaSurgeLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PlaceLocation(
  val name: String,
  val address: String,
  val lat: Double,
  val lng: Double,
  val distanceKm: Double
)

val JAKARTA_PLACES = listOf(
  PlaceLocation("Grand Indonesia Mall", "Jl. M.H. Thamrin No.1, Menteng, Jakarta Pusat", -6.1950, 106.8230, 2.5),
  PlaceLocation("Stasiun Sudirman", "Jl. Kendal No.1, Dukuh Atas, Jakarta Pusat", -6.2025, 106.8235, 3.2),
  PlaceLocation("Senayan City", "Jl. Asia Afrika Lot 19, Gelora, Jakarta Pusat", -6.2272, 106.7974, 5.8),
  PlaceLocation("Stasiun Gambir", "Jl. Medan Merdeka Timur No.1, Gambir, Jakarta Pusat", -6.1767, 106.8306, 4.1),
  PlaceLocation("SCBD District 8", "Jl. Jend. Sudirman Kav. 52-53, Jakarta Selatan", -6.2250, 106.8090, 4.6),
  PlaceLocation("Blok M Square", "Jl. Melawai 5, Kebayoran Baru, Jakarta Selatan", -6.2443, 106.8001, 7.3),
  PlaceLocation("Sarinah Thamrin", "Jl. M.H. Thamrin No.11, Gondangdia, Jakarta Pusat", -6.1873, 106.8241, 1.8),
  PlaceLocation("Bandara Soekarno-Hatta (CGT)", "Tangerang, Banten", -6.1275, 106.6537, 28.5)
)

/**
 * Fullscreen / Sheet Location Picker & Route Setup
 */
@Composable
fun LocationPickerSheet(
  initialPickup: String = "Grand Indonesia Mall, Jl. M.H. Thamrin No.1",
  initialDest: String = "Stasiun Sudirman, Dukuh Atas",
  onConfirmRoute: (pickup: PlaceLocation, dest: PlaceLocation) -> Unit,
  onDismiss: () -> Unit
) {
  var pickupText by remember { mutableStateOf(initialPickup) }
  var destText by remember { mutableStateOf(initialDest) }
  var isSelectingPickup by remember { mutableStateOf(false) }

  val defaultPickup = remember {
    JAKARTA_PLACES[0]
  }
  var selectedDest by remember {
    mutableStateOf(JAKARTA_PLACES[1])
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Tentukan Rute Perjalanan",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      IconButton(onClick = onDismiss) {
        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Pickup & Destination Inputs
    Card(
      shape = CardShape,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Pickup row
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = null,
            tint = SukmaEmeraldPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Titik Jemput", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = pickupText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Destination row
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = SukmaEmergencyRed,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Titik Tujuan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = destText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Saved Places Fast Shortcuts
    Text(
      text = "Tempat Tersimpan",
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      SavedPlaceChip("🏠 Rumah", "Jl. Kemang Raya") {
        destText = "Jl. Kemang Raya No. 12, Jakarta Selatan"
        selectedDest = PlaceLocation("Rumah", destText, -6.2615, 106.8155, 6.2)
      }
      SavedPlaceChip("🏢 Kantor", "SCBD Menara") {
        destText = "SCBD District 8, Senopati, Jakarta Selatan"
        selectedDest = JAKARTA_PLACES[4]
      }
      SavedPlaceChip("🛍️ Mall", "Grand Indonesia") {
        destText = JAKARTA_PLACES[0].address
        selectedDest = JAKARTA_PLACES[0]
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Suggested Destinations List
    Text(
      text = "Pilih Destinasi di Sekitar Anda",
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = false)
    ) {
      items(JAKARTA_PLACES) { place ->
        Surface(
          shape = ControlShape,
          color = if (destText == place.address) SukmaEmeraldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
          border = BorderStroke(
            1.dp,
            if (destText == place.address) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .clickable {
              destText = place.address
              selectedDest = place
            }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = SukmaEmeraldPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = place.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(text = place.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
              text = "${place.distanceKm} km",
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp,
              color = SukmaEmeraldPrimary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    SukmaButton(
      text = "Lanjut Pilih Kendaraan (${selectedDest.distanceKm} km)",
      onClick = {
        onConfirmRoute(defaultPickup, selectedDest)
      },
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun SavedPlaceChip(
  label: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Surface(
    shape = PillShape,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    modifier = Modifier
      .clip(PillShape)
      .clickable(onClick = onClick)
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
  }
}

/**
 * Service Selection, Fare Quote Breakdown & Booking confirmation
 */
@Composable
fun ServiceSelectionView(
  pickup: PlaceLocation,
  dest: PlaceLocation,
  currentUser: UserEntity,
  orderRepository: OrderRepository,
  walletRepository: WalletRepository,
  onOrderConfirmed: (OrderEntity) -> Unit,
  onChangeRouteClick: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var selectedService by remember { mutableStateOf(ServiceType.RIDE_BIKE) }
  var promoCodeInput by remember { mutableStateOf("SUKMABARU") }
  var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.WALLET) }

  val walletBalance by walletRepository.getCalculatedBalance("w_cust_01").collectAsState(initial = 150000L)

  var quoteBike by remember {
    mutableStateOf(
      PricingQuote(
        serviceType = ServiceType.RIDE_BIKE,
        distanceKm = dest.distanceKm,
        durationMin = maxOf(5, (dest.distanceKm * 3.2).toInt()),
        baseFare = 8000,
        distanceFare = (dest.distanceKm * 2500).toLong(),
        timeFare = 1500,
        subtotal = 16000,
        platformFee = 2000,
        surgeAmount = 0,
        discountAmount = 8000,
        totalFare = 10000,
        policyVersion = "v1.1"
      )
    )
  }

  var quoteCar by remember {
    mutableStateOf(
      PricingQuote(
        serviceType = ServiceType.RIDE_CAR,
        distanceKm = dest.distanceKm,
        durationMin = maxOf(8, (dest.distanceKm * 4.0).toInt()),
        baseFare = 16000,
        distanceFare = (dest.distanceKm * 5000).toLong(),
        timeFare = 3000,
        subtotal = 32000,
        platformFee = 4000,
        surgeAmount = 0,
        discountAmount = 15000,
        totalFare = 21000,
        policyVersion = "v1.1"
      )
    )
  }

  var isSubmitting by remember { mutableStateOf(false) }

  // Recalculate quotes when dest or promo changes
  LaunchedEffect(dest, promoCodeInput) {
    quoteBike = orderRepository.calculateQuote(ServiceType.RIDE_BIKE, dest.distanceKm, promoCodeInput)
    quoteCar = orderRepository.calculateQuote(ServiceType.RIDE_CAR, dest.distanceKm, promoCodeInput)
  }

  val activeQuote = if (selectedService == ServiceType.RIDE_BIKE) quoteBike else quoteCar

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Map Canvas Preview with simulated route
    MapRouteCanvas(
      status = OrderStatus.CREATED,
      serviceType = selectedService,
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
    )

    // Route summary strip
    Card(
      shape = ControlShape,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(12.dp)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Ke: ${dest.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "${dest.distanceKm} km • Est. ${activeQuote.durationMin} mnt perjalanan",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        TextButton(onClick = onChangeRouteClick) {
          Text("Ubah", color = SukmaEmeraldPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Vehicle Service Cards (Sukma Ride vs Sukma Car)
    Text(
      text = "Pilih Jenis Layanan",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = MaterialTheme.colorScheme.onSurface
    )

    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      // Sukma Ride Card
      VehicleOptionCard(
        title = "Sukma Ride",
        subtitle = "Motor cepat & gesit (1 orang)",
        icon = Icons.Default.TwoWheeler,
        price = formatRupiah(quoteBike.totalFare),
        duration = "${quoteBike.durationMin} mnt",
        isSelected = selectedService == ServiceType.RIDE_BIKE,
        onClick = { selectedService = ServiceType.RIDE_BIKE },
        modifier = Modifier.weight(1f)
      )

      // Sukma Car Card
      VehicleOptionCard(
        title = "Sukma Car",
        subtitle = "Mobil ber-AC (4-6 orang)",
        icon = Icons.Default.DirectionsCar,
        price = formatRupiah(quoteCar.totalFare),
        duration = "${quoteCar.durationMin} mnt",
        isSelected = selectedService == ServiceType.RIDE_CAR,
        onClick = { selectedService = ServiceType.RIDE_CAR },
        modifier = Modifier.weight(1f)
      )
    }

    // Promo Code Field with chip recommendations
    OutlinedTextField(
      value = promoCodeInput,
      onValueChange = { promoCodeInput = it },
      label = { Text("Kode Promo / Voucher Diskon") },
      trailingIcon = {
        if (promoCodeInput.isNotBlank()) {
          IconButton(onClick = { promoCodeInput = "" }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
          }
        }
      },
      modifier = Modifier.fillMaxWidth()
    )

    // Payment Method Options
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Metode Pembayaran",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        PaymentMethodChip(
          title = "GoSukma Pay (${formatRupiah(walletBalance)})",
          isSelected = selectedPaymentMethod == PaymentMethod.WALLET,
          onClick = { selectedPaymentMethod = PaymentMethod.WALLET }
        )
        PaymentMethodChip(
          title = "Tunai",
          isSelected = selectedPaymentMethod == PaymentMethod.CASH,
          onClick = { selectedPaymentMethod = PaymentMethod.CASH }
        )
        PaymentMethodChip(
          title = "QRIS",
          isSelected = selectedPaymentMethod == PaymentMethod.QRIS,
          onClick = { selectedPaymentMethod = PaymentMethod.QRIS }
        )
      }
    }

    // Itemized Pricing Breakdown
    SukmaCard {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PriceRow("Tarif Dasar & Jarak (${activeQuote.distanceKm} km)", formatRupiah(activeQuote.subtotal))
        PriceRow("Biaya Layanan Aplikasi", formatRupiah(activeQuote.platformFee))
        if (activeQuote.discountAmount > 0) {
          PriceRow("Diskon Promo (${promoCodeInput.trim().uppercase()})", "- ${formatRupiah(activeQuote.discountAmount)}", SukmaSuccessGreen)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        PriceRow("Total Pembayaran", formatRupiah(activeQuote.totalFare), SukmaEmeraldPrimary, FontWeight.Bold, 16.sp)
      }
    }

    // Primary Booking Action Button (Idempotent)
    SukmaButton(
      text = "Pesan ${selectedService.displayName} • ${formatRupiah(activeQuote.totalFare)}",
      isLoading = isSubmitting,
      onClick = {
        isSubmitting = true
        scope.launch {
          val order = orderRepository.createOrder(
            serviceType = selectedService,
            customerId = currentUser.id,
            customerName = currentUser.fullName,
            pickupAddress = pickup.address,
            pickupLat = pickup.lat,
            pickupLng = pickup.lng,
            destAddress = dest.address,
            destLat = dest.lat,
            destLng = dest.lng,
            quote = activeQuote,
            paymentMethod = selectedPaymentMethod,
            promoCode = promoCodeInput.ifBlank { null },
            idempotencyKey = "cust-order-${UUID.randomUUID()}"
          )
          isSubmitting = false
          onOrderConfirmed(order)
        }
      },
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun VehicleOptionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  price: String,
  duration: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = CardShape,
    color = if (isSelected) SukmaEmeraldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    ),
    modifier = modifier
      .clip(CardShape)
      .clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = duration,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = price,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = SukmaEmeraldPrimary
      )
    }
  }
}

@Composable
fun PaymentMethodChip(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = PillShape,
    color = if (isSelected) SukmaEmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
    border = BorderStroke(
      1.dp,
      if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    ),
    modifier = Modifier
      .clip(PillShape)
      .clickable(onClick = onClick)
  ) {
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
  }
}

/**
 * Driver Matching Radar View
 */
@Composable
fun MatchingRadarView(
  order: OrderEntity,
  onDriverAssigned: () -> Unit,
  onCancelOrder: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var countdownSeconds by remember { mutableStateOf(15) }

  // Auto-countdown to simulated driver assignment
  LaunchedEffect(order.id) {
    while (countdownSeconds > 0) {
      delay(1000L)
      countdownSeconds--
    }
    onDriverAssigned()
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Map with animated radar waves
    MapRouteCanvas(
      status = OrderStatus.MATCHING,
      serviceType = order.serviceType,
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
    )

    Card(
      shape = CardShape,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(20.dp)
      ) {
        Surface(
          shape = PillShape,
          color = SukmaSurgeAmber.copy(alpha = 0.15f)
        ) {
          Text(
            text = "Mencari Driver Terdekat di Sekitar Anda ($countdownSeconds dtk)",
            color = SukmaSurgeAmber,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Menghubungkan ke Mitra Driver ${order.serviceType.displayName}...",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Pencarian radius 3 km di Jakarta Pusat (Thamrin-Sudirman). Harap tunggu sejenak.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          SukmaButton(
            text = "Simulasikan Driver Menerima",
            onClick = onDriverAssigned,
            variant = SukmaButtonVariant.PRIMARY,
            modifier = Modifier.weight(1f)
          )
          SukmaButton(
            text = "Batal",
            onClick = onCancelOrder,
            variant = SukmaButtonVariant.DANGER
          )
        }
      }
    }
  }
}

/**
 * Full Live Trip Tracking Screen with Driver Telemetry, Calling/Chatting Mask, SOS
 */
@Composable
fun LiveTripTrackingView(
  order: OrderEntity,
  onAdvanceMilestone: (OrderStatus) -> Unit,
  onCancelTrip: () -> Unit,
  onTripCompleted: () -> Unit
) {
  var showSosDialog by remember { mutableStateOf(false) }
  var showChatDialog by remember { mutableStateOf(false) }
  var showShareDialog by remember { mutableStateOf(false) }

  val milestoneTitle = when (order.status) {
    OrderStatus.DRIVER_ASSIGNED -> "Driver Ditemukan • Menuju Titik Jemput"
    OrderStatus.ARRIVING -> "Driver Sedang Menuju Titik Jemput (Est. 2 mnt)"
    OrderStatus.ARRIVED -> "Driver Sudah Tiba di Titik Jemput! 👋"
    OrderStatus.PICKED_UP -> "Penumpang Sudah Naik • Siap Meluncur"
    OrderStatus.IN_TRIP -> "Dalam Perjalanan ke ${order.destAddress}"
    else -> "Perjalanan Aktif"
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Map Route with moving vehicle
    MapRouteCanvas(
      status = order.status,
      serviceType = order.serviceType,
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
    )

    // Milestone Status Banner
    Card(
      shape = CardShape,
      colors = CardDefaults.cardColors(
        containerColor = if (order.status == OrderStatus.ARRIVED) SukmaSuccessLight else SukmaMintLight
      ),
      border = BorderStroke(1.dp, SukmaEmeraldPrimary),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(14.dp)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = milestoneTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SukmaSlateDark
          )
          Text(
            text = "Order #${order.id} • ${formatRupiah(order.totalFare)}",
            fontSize = 12.sp,
            color = SukmaSlateSubtle
          )
        }
        SukmaStatusBadge(status = order.status)
      }
    }

    // Driver Profile Card with Masked Call & Chat
    SukmaCard {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = CircleShape,
            color = SukmaEmeraldPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(50.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (order.serviceType == ServiceType.RIDE_CAR) Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                contentDescription = null,
                tint = SukmaEmeraldPrimary,
                modifier = Modifier.size(28.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = order.driverName ?: "Ahmad Fauzi",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "${order.driverPlate ?: "B 4821 SKM"} • ${order.driverVehicle ?: "Honda Vario 160"}",
              fontSize = 12.sp,
              color = SukmaSlateSubtle
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SukmaSurgeAmber, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(text = "4.92 (342 trip)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          // Masked Call Button
          IconButton(
            onClick = { showChatDialog = true },
            modifier = Modifier
              .background(SukmaEmeraldPrimary.copy(alpha = 0.12f), CircleShape)
              .size(40.dp)
          ) {
            Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat Driver", tint = SukmaEmeraldPrimary, modifier = Modifier.size(20.dp))
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Masked Phone Button
          IconButton(
            onClick = { /* Masked Voice Call */ },
            modifier = Modifier
              .background(SukmaEmeraldPrimary.copy(alpha = 0.12f), CircleShape)
              .size(40.dp)
          ) {
            Icon(imageVector = Icons.Default.Phone, contentDescription = "Panggil Masked", tint = SukmaEmeraldPrimary, modifier = Modifier.size(20.dp))
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Safety Tools: Share Trip & SOS
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          TextButton(onClick = { showShareDialog = true }) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = SukmaEmeraldPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bagikan Rute", color = SukmaEmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }

          TextButton(onClick = { showSosDialog = true }) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SukmaEmergencyRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bantuan Darurat (SOS)", color = SukmaEmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Step Simulator Actions
    val (actionLabel, nextStatus) = when (order.status) {
      OrderStatus.DRIVER_ASSIGNED -> "Driver Berangkat Jemput" to OrderStatus.ARRIVING
      OrderStatus.ARRIVING -> "Driver Tiba di Titik Jemput" to OrderStatus.ARRIVED
      OrderStatus.ARRIVED -> "Mulai Perjalanan" to OrderStatus.IN_TRIP
      OrderStatus.IN_TRIP -> "Selesaikan Perjalanan" to OrderStatus.COMPLETED
      else -> "Lanjut" to OrderStatus.COMPLETED
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      SukmaButton(
        text = actionLabel,
        onClick = {
          if (nextStatus == OrderStatus.COMPLETED) {
            onTripCompleted()
          } else {
            onAdvanceMilestone(nextStatus)
          }
        },
        variant = SukmaButtonVariant.PRIMARY,
        modifier = Modifier.weight(1f)
      )

      if (order.status != OrderStatus.IN_TRIP) {
        SukmaButton(
          text = "Batalkan",
          onClick = onCancelTrip,
          variant = SukmaButtonVariant.DANGER
        )
      }
    }
  }

  // SOS Emergency Dialog
  if (showSosDialog) {
    AlertDialog(
      onDismissRequest = { showSosDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SukmaEmergencyRed)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Panggilan Darurat / SOS", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Text("Tombol ini akan segera mengirimkan koordinat lokasi Anda, data driver ${order.driverName}, dan nomor polisi ${order.driverPlate} ke Tim Dispatcher Pusat & Polisi 110.")
      },
      confirmButton = {
        SukmaButton(
          text = "Panggil Bantuan Sekarang",
          variant = SukmaButtonVariant.DANGER,
          onClick = {
            showSosDialog = false
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showSosDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  // Masked Chat Dialog
  if (showChatDialog) {
    var messageText by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showChatDialog = false },
      title = { Text("Pesan ke Mitra Driver (Masked)") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Nomor telepon Anda disamarkan untuk privasi.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("Contoh: Saya tunggu di lobi barat mall...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Kirim Pesan",
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

  // Share Trip Dialog
  if (showShareDialog) {
    AlertDialog(
      onDismissRequest = { showShareDialog = false },
      title = { Text("Bagikan Rute Perjalanan") },
      text = {
        Text("Link pelacakan langsung aman telah dibuat:\nhttps://gosukma.id/trip/${order.id}?token=sec_${UUID.randomUUID().toString().take(8)}\n\nKeluarga atau kerabat dapat memantau posisi Anda secara realtime.")
      },
      confirmButton = {
        SukmaButton(
          text = "Salin Link",
          onClick = { showShareDialog = false }
        )
      }
    )
  }
}

/**
 * Trip Completion, Digital Receipt & Rating Screen
 */
@Composable
fun TripCompletionView(
  order: OrderEntity,
  onSubmitReview: (rating: Int, tipAmount: Long, feedback: String?) -> Unit
) {
  var selectedRating by remember { mutableStateOf(5) }
  var selectedTip by remember { mutableStateOf(2000L) }
  var feedbackTags by remember { mutableStateOf(setOf("Driver Ramah", "Tepat Waktu")) }

  val tagOptions = listOf("Driver Ramah", "Tepat Waktu", "Mengemudi Aman", "Kendaraan Bersih")
  val tipOptions = listOf(0L, 2000L, 5000L, 10000L)

  val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
  val formattedDate = dateFormat.format(Date(order.createdAt))

  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Success Check Icon
      Surface(
        shape = CircleShape,
        color = SukmaSuccessLight,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SukmaSuccessGreen,
            modifier = Modifier.size(44.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Perjalanan Telah Selesai!",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Terima kasih telah bepergian dengan ${order.serviceType.displayName}",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Official Digital Receipt Card
    item {
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Struk Pembayaran Resmi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "#${order.id}", fontSize = 12.sp, color = SukmaSlateSubtle)
          }
          Text(text = formattedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          PriceRow("Dari", order.pickupAddress)
          PriceRow("Ke", order.destAddress)
          PriceRow("Jarak & Durasi", "${order.distanceKm} km • ${order.durationMin} mnt")
          PriceRow("Mitra Driver", "${order.driverName ?: "Ahmad Fauzi"} (${order.driverPlate ?: "B 4821 SKM"})")

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          PriceRow("Tarif Subtotal", formatRupiah(order.subtotalFare))
          PriceRow("Biaya Layanan Aplikasi", formatRupiah(order.platformFee))
          if (order.discountAmount > 0) {
            PriceRow("Diskon Promo (${order.promoCode ?: ""})", "- ${formatRupiah(order.discountAmount)}", SukmaSuccessGreen)
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
          PriceRow("Total Dibayar", formatRupiah(order.totalFare), SukmaEmeraldPrimary, FontWeight.Bold, 16.sp)
          PriceRow("Metode Pembayaran", order.paymentMethod.label)
        }
      }
    }

    // Rating Stars
    item {
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(16.dp)
        ) {
          Text(
            text = "Bagaimana Perjalanan Anda?",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { star ->
              Icon(
                imageVector = if (star <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "$star bintang",
                tint = if (star <= selectedRating) SukmaSurgeAmber else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                  .size(36.dp)
                  .clickable { selectedRating = star }
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          // Feedback Chips
          Text(text = "Beri apresiasi kepada driver:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            tagOptions.forEach { tag ->
              val isSelected = feedbackTags.contains(tag)
              Surface(
                shape = PillShape,
                color = if (isSelected) SukmaEmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                  .clip(PillShape)
                  .clickable {
                    feedbackTags = if (isSelected) feedbackTags - tag else feedbackTags + tag
                  }
              ) {
                Text(
                  text = tag,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Driver Tip Options
          Text(text = "Beri Tip untuk Driver Mitra:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            tipOptions.forEach { tip ->
              val isSelected = selectedTip == tip
              val label = if (tip == 0L) "Tanpa Tip" else "+${formatRupiah(tip)}"
              Surface(
                shape = ControlShape,
                color = if (isSelected) SukmaEmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                  .weight(1f)
                  .clip(ControlShape)
                  .clickable { selectedTip = tip }
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.padding(vertical = 8.dp)
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }
    }

    // Submit Review Action
    item {
      SukmaButton(
        text = "Kirim Penilaian & Selesai",
        onClick = {
          onSubmitReview(
            selectedRating,
            selectedTip,
            feedbackTags.joinToString(", ")
          )
        },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

/**
 * Detailed Receipt Modal for past orders in Activity screen
 */
@Composable
fun ReceiptDetailDialog(
  order: OrderEntity,
  onRebook: () -> Unit,
  onDismiss: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
  val formattedDate = dateFormat.format(Date(order.createdAt))

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Struk Transaksi", fontWeight = FontWeight.Bold)
        SukmaStatusBadge(status = order.status)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = "Order ID: #${order.id}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(text = formattedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        HorizontalDivider()

        PriceRow("Layanan", order.serviceType.displayName)
        PriceRow("Titik Jemput", order.pickupAddress)
        PriceRow("Titik Antar", order.destAddress)
        PriceRow("Jarak", "${order.distanceKm} km")
        if (order.driverName != null) {
          PriceRow("Driver", "${order.driverName} (${order.driverPlate})")
        }

        HorizontalDivider()

        PriceRow("Tarif Subtotal", formatRupiah(order.subtotalFare))
        PriceRow("Biaya Layanan", formatRupiah(order.platformFee))
        if (order.discountAmount > 0) {
          PriceRow("Diskon Promo", "- ${formatRupiah(order.discountAmount)}", SukmaSuccessGreen)
        }
        if (order.tipAmount > 0) {
          PriceRow("Tip Driver", "+ ${formatRupiah(order.tipAmount)}", SukmaEmeraldPrimary)
        }
        HorizontalDivider()
        PriceRow("Total Bayar", formatRupiah(order.totalFare + order.tipAmount), SukmaEmeraldPrimary, FontWeight.Bold, 15.sp)
        PriceRow("Metode", order.paymentMethod.label)
        if (order.rating != null) {
          PriceRow("Penilaian Anda", "${order.rating} ★", SukmaSurgeAmber, FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      SukmaButton(
        text = "Pesan Lagi",
        onClick = onRebook
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}
