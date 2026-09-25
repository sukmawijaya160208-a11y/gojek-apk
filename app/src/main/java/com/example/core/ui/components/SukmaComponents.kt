package com.example.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.OrderStatus
import com.example.core.model.UserRole
import com.example.ui.theme.CardShape
import com.example.ui.theme.ControlShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmergencyLight
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaInfoBlue
import com.example.ui.theme.SukmaInfoLight
import com.example.ui.theme.SukmaSlateDark
import com.example.ui.theme.SukmaSlateMedium
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSuccessGreen
import com.example.ui.theme.SukmaSuccessLight
import com.example.ui.theme.SukmaSurgeAmber
import com.example.ui.theme.SukmaSurgeLight

enum class SukmaButtonVariant {
  PRIMARY,
  SECONDARY,
  OUTLINED,
  DANGER
}

@Composable
fun SukmaButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: SukmaButtonVariant = SukmaButtonVariant.PRIMARY,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  icon: ImageVector? = null,
  testTag: String = "sukma_button"
) {
  val shape = ControlShape
  val minHeight = 48.dp

  when (variant) {
    SukmaButtonVariant.PRIMARY -> {
      Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = SukmaEmeraldPrimary,
          contentColor = Color.White,
          disabledContainerColor = Color(0xFFCBD5E1),
          disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
          .defaultMinSize(minHeight = minHeight)
          .testTag(testTag)
      ) {
        ButtonInnerContent(text, isLoading, icon, Color.White)
      }
    }
    SukmaButtonVariant.SECONDARY -> {
      Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = SukmaSlateMedium,
          contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
          .defaultMinSize(minHeight = minHeight)
          .testTag(testTag)
      ) {
        ButtonInnerContent(text, isLoading, icon, Color.White)
      }
    }
    SukmaButtonVariant.OUTLINED -> {
      OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        border = BorderStroke(1.dp, SukmaEmeraldPrimary),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = SukmaEmeraldPrimary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
          .defaultMinSize(minHeight = minHeight)
          .testTag(testTag)
      ) {
        ButtonInnerContent(text, isLoading, icon, SukmaEmeraldPrimary)
      }
    }
    SukmaButtonVariant.DANGER -> {
      Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = SukmaEmergencyRed,
          contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
          .defaultMinSize(minHeight = minHeight)
          .testTag(testTag)
      ) {
        ButtonInnerContent(text, isLoading, icon, Color.White)
      }
    }
  }
}

@Composable
private fun ButtonInnerContent(
  text: String,
  isLoading: Boolean,
  icon: ImageVector?,
  contentColor: Color
) {
  if (isLoading) {
    CircularProgressIndicator(
      color = contentColor,
      strokeWidth = 2.5.dp,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
  } else if (icon != null) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
  }
  Text(
    text = text,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
  )
}

@Composable
fun SukmaCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .clip(CardShape)
      .clickable(onClick = onClick)
  } else {
    modifier
  }

  Card(
    shape = CardShape,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = cardModifier
  ) {
    Box(modifier = Modifier.padding(16.dp)) {
      content()
    }
  }
}

@Composable
fun SukmaStatusBadge(status: OrderStatus) {
  val (bgColor, textColor, label) = when (status) {
    OrderStatus.CREATED -> Triple(SukmaInfoLight, SukmaInfoBlue, status.label)
    OrderStatus.MATCHING -> Triple(SukmaSurgeLight, SukmaSurgeAmber, status.label)
    OrderStatus.DRIVER_ASSIGNED -> Triple(SukmaInfoLight, SukmaInfoBlue, status.label)
    OrderStatus.ARRIVING -> Triple(SukmaSurgeLight, SukmaSurgeAmber, status.label)
    OrderStatus.ARRIVED -> Triple(SukmaSuccessLight, SukmaSuccessGreen, status.label)
    OrderStatus.PICKED_UP -> Triple(SukmaSuccessLight, SukmaSuccessGreen, status.label)
    OrderStatus.IN_TRIP -> Triple(SukmaSuccessLight, SukmaSuccessGreen, status.label)
    OrderStatus.COMPLETING -> Triple(SukmaInfoLight, SukmaInfoBlue, status.label)
    OrderStatus.COMPLETED -> Triple(SukmaSuccessLight, SukmaSuccessGreen, status.label)
    OrderStatus.CANCELLED -> Triple(SukmaEmergencyLight, SukmaEmergencyRed, status.label)
    OrderStatus.EXPIRED -> Triple(SukmaEmergencyLight, SukmaEmergencyRed, status.label)
    OrderStatus.PAYMENT_FAILED -> Triple(SukmaEmergencyLight, SukmaEmergencyRed, status.label)
  }

  Surface(
    shape = PillShape,
    color = bgColor,
    modifier = Modifier.padding(2.dp)
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SukmaTopBar(
  title: String,
  subtitle: String? = null,
  currentRole: UserRole,
  onSwitchRoleClick: () -> Unit,
  onBackClick: (() -> Unit)? = null
) {
  TopAppBar(
    title = {
      Column {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    navigationIcon = {
      if (onBackClick != null) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Kembali",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    actions = {
      // Role Switcher pill
      Surface(
        shape = PillShape,
        color = SukmaEmeraldPrimary.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, SukmaEmeraldPrimary.copy(alpha = 0.4f)),
        modifier = Modifier
          .padding(end = 12.dp)
          .clip(PillShape)
          .clickable(onClick = onSwitchRoleClick)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(SukmaEmeraldPrimary)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = currentRole.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SukmaEmeraldPrimary
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )
}

@Composable
fun SukmaLoadingState(
  message: String = "Memuat data...",
  modifier: Modifier = Modifier
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator(
        color = SukmaEmeraldPrimary,
        modifier = Modifier.size(36.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = message,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun SukmaEmptyState(
  title: String,
  description: String,
  icon: ImageVector = Icons.Default.Info,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp)
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
      modifier = Modifier.size(64.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = SukmaEmeraldPrimary,
          modifier = Modifier.size(32.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = description,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 18.sp
    )
    if (actionText != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(20.dp))
      SukmaButton(
        text = actionText,
        onClick = onActionClick,
        variant = SukmaButtonVariant.PRIMARY
      )
    }
  }
}

@Composable
fun SukmaErrorState(
  message: String,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .fillMaxWidth()
      .padding(24.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Warning,
      contentDescription = "Error",
      tint = SukmaEmergencyRed,
      modifier = Modifier.size(40.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Terjadi Kendala",
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = SukmaEmergencyRed
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = message,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    SukmaButton(
      text = "Coba Lagi",
      onClick = onRetry,
      icon = Icons.Default.Refresh,
      variant = SukmaButtonVariant.OUTLINED
    )
  }
}

@Composable
fun PriceRow(
  label: String,
  amount: String,
  color: Color = MaterialTheme.colorScheme.onSurface,
  weight: FontWeight = FontWeight.Normal,
  fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(text = label, fontSize = fontSize, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = amount, fontSize = fontSize, fontWeight = weight, color = color)
  }
}
