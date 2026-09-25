package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(12.dp),
  large = RoundedCornerShape(16.dp),
  extraLarge = RoundedCornerShape(24.dp)
)

val PillShape = RoundedCornerShape(999.dp)
val CardShape = RoundedCornerShape(16.dp)
val ControlShape = RoundedCornerShape(12.dp)
val SheetTopShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
