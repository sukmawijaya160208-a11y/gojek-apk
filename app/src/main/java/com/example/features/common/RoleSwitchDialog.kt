package com.example.features.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.UserRole
import com.example.ui.theme.ControlShape
import com.example.ui.theme.SukmaEmeraldPrimary

@Composable
fun RoleSwitchDialog(
  currentRole: UserRole,
  onRoleSelected: (UserRole) -> Unit,
  onDismiss: () -> Unit
) {
  val roles = listOf(
    UserRole.CUSTOMER to "Pelanggan: Booking ride, tracking, wallet & review",
    UserRole.DRIVER to "Driver Mitra: Online/offline, order fulfillment & earnings",
    UserRole.MERCHANT to "Mitra Merchant: Manajemen menu & order makanan",
    UserRole.OPS to "Dispatcher / Ops: Monitoring live fleet & insiden",
    UserRole.ADMIN to "Admin Super: Kontrol penuh sistem, tarif & ledger"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Pilih Persona / Role",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      LazyColumn {
        items(roles) { (role, desc) ->
          val isSelected = role == currentRole
          Card(
            shape = ControlShape,
            border = BorderStroke(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) SukmaEmeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable {
                onRoleSelected(role)
                onDismiss()
              }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(12.dp)
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = role.label,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = desc,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Selected",
                  tint = SukmaEmeraldPrimary
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup", color = SukmaEmeraldPrimary, fontWeight = FontWeight.Bold)
      }
    }
  )
}
