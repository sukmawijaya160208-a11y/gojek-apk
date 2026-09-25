package com.example.features.ops

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.IncidentSeverity
import com.example.core.ui.components.SukmaButton
import com.example.core.ui.components.SukmaCard
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmergencyLight
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSurgeAmber
import com.example.ui.theme.SukmaSurgeLight

data class MockIncident(
  val id: String,
  val orderId: String,
  val type: String,
  val severity: IncidentSeverity,
  val reporter: String,
  val notes: String,
  val time: String
)

@Composable
fun OpsHomeScreen() {
  val mockIncidents = listOf(
    MockIncident("INC-102", "ORD-2026-0901", "Barang Tertinggal", IncidentSeverity.MEDIUM, "Sukma Wijaya (Pelanggan)", "Kacamata hitam tertinggal di helm driver", "1 jam lalu"),
    MockIncident("INC-103", "ORD-2026-0904", "Kendaraan Berbeda", IncidentSeverity.HIGH, "Budi (Pelanggan)", "Driver datang dengan motor plat berbeda", "15 menit lalu")
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Column {
        Text(
          text = "Antrean Penanganan Insiden & Safety",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Dispatcher & Emergency Response Center",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    items(mockIncidents) { inc ->
      SukmaCard {
        Column {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "${inc.id} • ${inc.type}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            val (bgColor, textColor) = if (inc.severity == IncidentSeverity.CRITICAL || inc.severity == IncidentSeverity.HIGH) {
              SukmaEmergencyLight to SukmaEmergencyRed
            } else {
              SukmaSurgeLight to SukmaSurgeAmber
            }
            Surface(shape = PillShape, color = bgColor) {
              Text(
                text = inc.severity.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = "Order: #${inc.orderId} • Pelapor: ${inc.reporter}", fontSize = 12.sp, color = SukmaSlateSubtle)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = inc.notes, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SukmaButton(
              text = "Hubungi Pelapor",
              onClick = {},
              modifier = Modifier.weight(1f)
            )
            SukmaButton(
              text = "Tutup Insiden",
              onClick = {},
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}
