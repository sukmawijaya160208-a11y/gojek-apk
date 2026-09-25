package com.example.features.customer.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.core.model.OrderStatus
import com.example.core.model.ServiceType
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaMintAccent
import com.example.ui.theme.SukmaSlateDark
import com.example.ui.theme.SukmaSurgeAmber

@Composable
fun MapRouteCanvas(
  status: OrderStatus,
  serviceType: ServiceType = ServiceType.RIDE_BIKE,
  modifier: Modifier = Modifier
    .fillMaxWidth()
    .height(240.dp)
) {
  val infiniteTransition = rememberInfiniteTransition(label = "map_anim")
  
  // Radar wave for MATCHING
  val radarRadius by infiniteTransition.animateFloat(
    initialValue = 10f,
    targetValue = 90f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar"
  )

  // Vehicle progress animation along polyline
  val progress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(5000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "progress"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFFE2E8F0))
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // 1. Draw Map Background Elements (Road grid, river, green parks)
      drawMapBackground(w, h)

      // 2. Define Route Points (Pickup -> Midpoint 1 -> Midpoint 2 -> Dropoff)
      val pPickup = Offset(w * 0.22f, h * 0.72f)
      val pMid1 = Offset(w * 0.38f, h * 0.45f)
      val pMid2 = Offset(w * 0.65f, h * 0.55f)
      val pDest = Offset(w * 0.82f, h * 0.28f)

      // 3. Draw Route Polyline
      val routePath = Path().apply {
        moveTo(pPickup.x, pPickup.y)
        lineTo(pMid1.x, pMid1.y)
        lineTo(pMid2.x, pMid2.y)
        lineTo(pDest.x, pDest.y)
      }

      // Route Outer Border
      drawPath(
        path = routePath,
        color = Color.White,
        style = Stroke(
          width = 12f,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round
        )
      )

      // Route Main Color
      drawPath(
        path = routePath,
        color = SukmaEmeraldPrimary,
        style = Stroke(
          width = 7f,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round
        )
      )

      // 4. Draw Pickup Pin (Green)
      drawCircle(
        color = SukmaEmeraldPrimary.copy(alpha = 0.25f),
        radius = 18f,
        center = pPickup
      )
      drawCircle(
        color = SukmaEmeraldPrimary,
        radius = 10f,
        center = pPickup
      )
      drawCircle(
        color = Color.White,
        radius = 4f,
        center = pPickup
      )

      // 5. Draw Destination Pin (Red)
      drawCircle(
        color = SukmaEmergencyRed.copy(alpha = 0.25f),
        radius = 18f,
        center = pDest
      )
      drawCircle(
        color = SukmaEmergencyRed,
        radius = 10f,
        center = pDest
      )
      drawCircle(
        color = Color.White,
        radius = 4f,
        center = pDest
      )

      // 6. Draw Radar Pulse if MATCHING
      if (status == OrderStatus.MATCHING) {
        drawCircle(
          color = SukmaSurgeAmber.copy(alpha = (1f - (radarRadius / 90f)).coerceIn(0f, 0.8f)),
          radius = radarRadius,
          center = pPickup,
          style = Stroke(width = 3f)
        )
        drawCircle(
          color = SukmaEmeraldPrimary.copy(alpha = (1f - (radarRadius / 90f)).coerceIn(0f, 0.6f)),
          radius = (radarRadius * 0.6f),
          center = pPickup,
          style = Stroke(width = 2.5f)
        )
      }

      // 7. Draw Vehicle Marker along route during active statuses
      if (status in listOf(OrderStatus.ARRIVING, OrderStatus.ARRIVED, OrderStatus.IN_TRIP)) {
        val currentVehiclePos = when (status) {
          OrderStatus.ARRIVING -> {
            // Vehicle moves from driver start to pickup
            Offset(
              x = pPickup.x - 50f + (50f * progress),
              y = pPickup.y + 40f - (40f * progress)
            )
          }
          OrderStatus.ARRIVED -> pPickup
          OrderStatus.IN_TRIP -> {
            // Vehicle interpolates along route
            if (progress < 0.33f) {
              val subT = progress / 0.33f
              Offset(
                pPickup.x + (pMid1.x - pPickup.x) * subT,
                pPickup.y + (pMid1.y - pPickup.y) * subT
              )
            } else if (progress < 0.66f) {
              val subT = (progress - 0.33f) / 0.33f
              Offset(
                pMid1.x + (pMid2.x - pMid1.x) * subT,
                pMid1.y + (pMid2.y - pMid1.y) * subT
              )
            } else {
              val subT = (progress - 0.66f) / 0.34f
              Offset(
                pMid2.x + (pDest.x - pMid2.x) * subT,
                pMid2.y + (pDest.y - pMid2.y) * subT
              )
            }
          }
          else -> pPickup
        }

        // Vehicle Halo
        drawCircle(
          color = SukmaMintAccent.copy(alpha = 0.35f),
          radius = 16f,
          center = currentVehiclePos
        )
        // Vehicle Body Pin
        drawCircle(
          color = SukmaSlateDark,
          radius = 10f,
          center = currentVehiclePos
        )
        // Vehicle Center Indicator
        drawCircle(
          color = if (serviceType == ServiceType.RIDE_CAR) Color(0xFF38BDF8) else SukmaMintAccent,
          radius = 5f,
          center = currentVehiclePos
        )
      }
    }
  }
}

private fun DrawScope.drawMapBackground(w: Float, h: Float) {
  // Park greenery
  drawRect(
    color = Color(0xFFDCFCE7),
    topLeft = Offset(w * 0.05f, h * 0.1f),
    size = androidx.compose.ui.geometry.Size(w * 0.25f, h * 0.3f)
  )

  // River water path
  val riverPath = Path().apply {
    moveTo(0f, h * 0.85f)
    cubicTo(w * 0.3f, h * 0.9f, w * 0.6f, h * 0.6f, w, h * 0.7f)
  }
  drawPath(
    path = riverPath,
    color = Color(0xFFBAE6FD),
    style = Stroke(width = 22f, cap = StrokeCap.Round)
  )

  // Street network lines
  val roadColor = Color(0xFFCBD5E1)
  val mainAvenueColor = Color.White

  // Grid roads
  drawLine(roadColor, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 5f)
  drawLine(roadColor, Offset(0f, h * 0.6f), Offset(w, h * 0.6f), strokeWidth = 5f)
  drawLine(roadColor, Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), strokeWidth = 5f)
  drawLine(roadColor, Offset(w * 0.7f, 0f), Offset(w * 0.7f, h), strokeWidth = 5f)

  // Diagonal avenues
  drawLine(mainAvenueColor, Offset(0f, h * 0.9f), Offset(w, h * 0.15f), strokeWidth = 10f)
  drawLine(mainAvenueColor, Offset(w * 0.15f, 0f), Offset(w * 0.85f, h), strokeWidth = 8f)
}
