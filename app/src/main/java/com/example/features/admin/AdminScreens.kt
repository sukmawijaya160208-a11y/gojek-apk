package com.example.features.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.CandidateDriver
import com.example.core.model.OrderStatus
import com.example.core.model.ServiceType
import com.example.core.repository.AuthRepository
import com.example.core.repository.OrderRepository
import com.example.core.ui.components.SukmaButton
import com.example.core.ui.components.SukmaButtonVariant
import com.example.core.ui.components.SukmaCard
import com.example.core.ui.components.SukmaStatusBadge
import com.example.features.customer.formatRupiah
import com.example.ui.theme.ControlShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaMintLight
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSuccessGreen
import com.example.ui.theme.SukmaSuccessLight
import com.example.ui.theme.SukmaSurgeAmber
import com.example.ui.theme.SukmaSurgeLight
import kotlinx.coroutines.launch

@Composable
fun AdminHomeScreen(
  orderRepository: OrderRepository,
  authRepository: AuthRepository
) {
  val scope = rememberCoroutineScope()
  val allOrders by orderRepository.getAllOrders().collectAsState(initial = emptyList())
  val allUsers by authRepository.getAllUsers().collectAsState(initial = emptyList())

  var showResetDialog by remember { mutableStateOf(false) }
  var isResetting by remember { mutableStateOf(false) }

  val activeOrdersCount = allOrders.count { !it.status.isTerminal }

  var selectedZone by remember { mutableStateOf("ZONE_CENTRAL") }
  var selectedSurge by remember { mutableStateOf(1.25) }
  var surgeFeedbackMsg by remember { mutableStateOf<String?>(null) }
  var isApplyingSurge by remember { mutableStateOf(false) }

  var testedCandidates by remember { mutableStateOf<List<CandidateDriver>?>(null) }
  var isSearchingCandidates by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(
            text = "Pusat Kontrol Operasional",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Monitoring Marketplace & Database Engine",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        SukmaButton(
          text = "Reset Demo",
          icon = Icons.Default.Refresh,
          onClick = { showResetDialog = true },
          variant = SukmaButtonVariant.OUTLINED
        )
      }
    }

    // KPI Metrics Strip
    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        KpiCard(title = "Pesanan Aktif", value = activeOrdersCount.toString(), color = SukmaSurgeAmber, modifier = Modifier.weight(1f))
        KpiCard(title = "Total User", value = allUsers.size.toString(), color = SukmaEmeraldPrimary, modifier = Modifier.weight(1f))
        KpiCard(title = "Total Order", value = allOrders.size.toString(), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
      }
    }

    // Surge Pricing Engine Configuration
    item {
      Text(
        text = "Konfigurasi Surge Pricing per Zona",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      SukmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = SukmaSurgeLight, modifier = Modifier.size(36.dp)) {
              androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(
                  imageVector = Icons.Default.Bolt,
                  contentDescription = null,
                  tint = SukmaSurgeAmber,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "Dynamic Surge Multiplier", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(text = "Sesuaikan pengali tarif berdasarkan permintaan & cuaca", fontSize = 11.sp, color = SukmaSlateSubtle)
            }
          }

          HorizontalDivider()

          Text(text = "Pilih Zona Operasional:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
              "ZONE_CENTRAL" to "Pusat (Sudirman)",
              "ZONE_SOUTH" to "Selatan (SCBD)",
              "ZONE_NORTH" to "Utara (Kelapa Gdg)"
            ).forEach { (zid, label) ->
              val isSelected = selectedZone == zid
              Surface(
                shape = PillShape,
                color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .clip(PillShape)
                  .clickable { selectedZone = zid }
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }

          Text(text = "Pilih Pengali Tarif (Surge):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(1.0, 1.25, 1.5, 2.0).forEach { s ->
              val isSelected = selectedSurge == s
              Surface(
                shape = ControlShape,
                color = if (isSelected) SukmaSurgeAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) SukmaSurgeAmber else MaterialTheme.colorScheme.outline),
                modifier = Modifier
                  .weight(1f)
                  .clip(ControlShape)
                  .clickable { selectedSurge = s }
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.padding(vertical = 8.dp)
                ) {
                  Text(text = "${s}x", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) SukmaSurgeAmber else MaterialTheme.colorScheme.onSurface)
                  Text(
                    text = when (s) {
                      1.0 -> "Normal"
                      1.25 -> "Sedang"
                      1.5 -> "Sibuk"
                      else -> "Badai"
                    },
                    fontSize = 10.sp,
                    color = SukmaSlateSubtle
                  )
                }
              }
            }
          }

          if (surgeFeedbackMsg != null) {
            Surface(shape = ControlShape, color = SukmaSuccessLight, modifier = Modifier.fillMaxWidth()) {
              Text(
                text = surgeFeedbackMsg!!,
                color = SukmaSuccessGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          SukmaButton(
            text = "Terapkan Surge ${selectedSurge}x ke $selectedZone",
            icon = Icons.Default.Bolt,
            isLoading = isApplyingSurge,
            onClick = {
              isApplyingSurge = true
              scope.launch {
                orderRepository.updateZoneSurge(selectedZone, selectedSurge)
                isApplyingSurge = false
                surgeFeedbackMsg = "Surge ${selectedSurge}x berhasil disimpan untuk zona $selectedZone!"
              }
            },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Dispatch Engine & Nearby Driver Diagnostics
    item {
      Text(
        text = "Diagnostik Dispatch & Pencarian Driver",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      SukmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = SukmaMintLight, modifier = Modifier.size(36.dp)) {
              androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(
                  imageVector = Icons.Default.NearMe,
                  contentDescription = null,
                  tint = SukmaEmeraldPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "Nearby Driver Spatial Search", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(text = "Formula Haversine + Bobot Peringkat (Proximity, Rating, Penerimaan)", fontSize = 11.sp, color = SukmaSlateSubtle)
            }
          }

          SukmaButton(
            text = "Uji Pencarian Mitra Driver Terdekat (Radius 6 km)",
            icon = Icons.Default.NearMe,
            isLoading = isSearchingCandidates,
            onClick = {
              isSearchingCandidates = true
              scope.launch {
                val results = orderRepository.searchNearbyDrivers(
                  pickupLat = -6.2088,
                  pickupLng = 106.8456,
                  serviceType = ServiceType.RIDE_BIKE,
                  maxRadiusKm = 6.0
                )
                testedCandidates = results
                isSearchingCandidates = false
              }
            },
            variant = SukmaButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
          )

          if (testedCandidates != null) {
            if (testedCandidates!!.isEmpty()) {
              Text(text = "Tidak ada driver online yang sesuai di radius 6 km saat ini.", fontSize = 12.sp, color = SukmaSlateSubtle)
            } else {
              Text(text = "Ditemukan ${testedCandidates!!.size} kandidat driver:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              testedCandidates!!.forEach { candidate ->
                Surface(
                  shape = ControlShape,
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                  ) {
                    Column {
                      Text(text = "#${candidate.rank} ${candidate.profile.fullName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text(text = "${candidate.profile.vehicleModel} (${candidate.profile.plateNumber})", fontSize = 11.sp, color = SukmaSlateSubtle)
                      Text(text = "Skor: ${String.format(java.util.Locale.US, "%.2f", candidate.compositeScore)} • Rating: ${candidate.profile.rating} ★", fontSize = 11.sp, color = SukmaEmeraldPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                      Text(text = "${candidate.distanceKm} km", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text(text = "ETA ${candidate.etaMin} mnt", fontSize = 11.sp, color = SukmaSlateSubtle)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Live Order Master Explorer
    item {
      Text(
        text = "Daftar Pesanan & Status Timeline",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    items(allOrders) { order ->
      SukmaCard {
        Column {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(
                text = "${order.serviceType.displayName} #${order.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Pelanggan: ${order.customerName}",
                fontSize = 12.sp,
                color = SukmaSlateSubtle
              )
            }
            SukmaStatusBadge(status = order.status)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Rute: ${order.pickupAddress} ➔ ${order.destAddress}", fontSize = 12.sp)
          if (order.driverName != null) {
            Text(text = "Driver: ${order.driverName} (${order.driverPlate})", fontSize = 12.sp, color = SukmaEmeraldPrimary)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Total: ${formatRupiah(order.totalFare)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "Metode: ${order.paymentMethod.label}", fontSize = 12.sp, color = SukmaSlateSubtle)
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset Database Demo?", fontWeight = FontWeight.Bold) },
      text = {
        Text("Tindakan ini akan mengosongkan seluruh data transaksi dan mengembalikan seluruh zona, 10 pengguna contoh, tarif dasar, promo, dan ledger awal yang bersih.")
      },
      confirmButton = {
        SukmaButton(
          text = "Ya, Reset Bersih",
          isLoading = isResetting,
          variant = SukmaButtonVariant.DANGER,
          onClick = {
            isResetting = true
            scope.launch {
              orderRepository.resetDatabase()
              isResetting = false
              showResetDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun KpiCard(
  title: String,
  value: String,
  color: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier
) {
  SukmaCard(modifier = modifier) {
    Column {
      Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
  }
}
