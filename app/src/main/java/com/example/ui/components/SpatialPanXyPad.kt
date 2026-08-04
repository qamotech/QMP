package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.FlOrange
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OnDarkSubtext

@Composable
fun SpatialPanXyPad(
    pannerX: Float, // -1.0f to 1.0f
    pannerY: Float, // -1.0f to 1.0f
    onPanChange: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCanvas)
            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3D SPATIAL PANNER XY PAD",
                color = ElectricGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "L/R: ${(pannerX * 100).toInt()}% | Depth: ${(pannerY * 100).toInt()}%",
                color = NeonCyan,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F1118))
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            val currentPxX = ((pannerX + 1f) / 2f) * width
                            val currentPxY = ((1f - pannerY) / 2f) * height

                            val newPxX = (currentPxX + dragAmount.x).coerceIn(0f, width)
                            val newPxY = (currentPxY + dragAmount.y).coerceIn(0f, height)

                            val normX = (newPxX / width) * 2f - 1f
                            val normY = 1f - (newPxY / height) * 2f

                            onPanChange(normX.coerceIn(-1f, 1f), normY.coerceIn(-1f, 1f))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val centerX = w / 2f
                val centerY = h / 2f

                val targetX = ((pannerX + 1f) / 2f) * w
                val targetY = ((1f - pannerY) / 2f) * h

                // Center Grid Lines
                val dashPattern = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(0f, centerY),
                    end = Offset(w, centerY),
                    pathEffect = dashPattern
                )
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, h),
                    pathEffect = dashPattern
                )

                // Concentric Depth Rings
                drawCircle(color = Color(0xFF1E2333), radius = w * 0.2f, center = Offset(centerX, centerY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
                drawCircle(color = Color(0xFF1E2333), radius = w * 0.4f, center = Offset(centerX, centerY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))

                // Line connecting center to Target Ball
                drawLine(
                    color = FlOrange.copy(alpha = 0.5f),
                    start = Offset(centerX, centerY),
                    end = Offset(targetX, targetY),
                    strokeWidth = 2f
                )

                // Target Sound Position Ball
                drawCircle(color = FlOrange.copy(alpha = 0.3f), radius = 16.dp.toPx(), center = Offset(targetX, targetY))
                drawCircle(color = FlOrange, radius = 8.dp.toPx(), center = Offset(targetX, targetY))
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(targetX, targetY))
            }
        }
    }
}
