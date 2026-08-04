package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlOrange
import com.example.ui.theme.MetallicBorder
import com.example.ui.theme.MetallicSteel
import com.example.ui.theme.MetallicSteelDark
import com.example.ui.theme.OnDarkSubtext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotaryKnob(
    value: Float, // 0.0f to 1.0f range normalized
    onValueChange: (Float) -> Unit,
    label: String,
    valueDisplay: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    accentColor: Color = FlOrange,
    defaultValue: Float = 0.5f
) {
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .pointerInput(value) {
                    detectDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Vertical drag adjusts knob value
                            val delta = -dragAmount.y / 200f
                            val newValue = (value + delta).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    )
                }
                .pointerInput(defaultValue) {
                    detectTapGestures(
                        onDoubleTap = {
                            onValueChange(defaultValue)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasSize = size.toPx()
                val radius = canvasSize / 2f
                val strokeWidth = 5.dp.toPx()
                val arcRadius = radius - strokeWidth / 2f

                val startAngle = 135f
                val totalSweep = 270f
                val currentSweep = (value.coerceIn(0f, 1f)) * totalSweep

                // Outer Track
                drawArc(
                    color = MetallicBorder,
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Arc
                drawArc(
                    color = accentColor,
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Knob Metallic Center Circle
                val knobRadius = radius - strokeWidth - 2.dp.toPx()
                drawCircle(
                    color = MetallicSteelDark,
                    radius = knobRadius,
                    center = Offset(radius, radius)
                )
                drawCircle(
                    color = MetallicSteel,
                    radius = knobRadius * 0.85f,
                    center = Offset(radius, radius)
                )

                // Rotating Needle Line
                val currentAngleRad = (startAngle + currentSweep) * (PI / 180f)
                val innerOffsetRadius = knobRadius * 0.2f
                val outerOffsetRadius = knobRadius * 0.8f

                val startX = radius + (innerOffsetRadius * cos(currentAngleRad)).toFloat()
                val startY = radius + (innerOffsetRadius * sin(currentAngleRad)).toFloat()
                val endX = radius + (outerOffsetRadius * cos(currentAngleRad)).toFloat()
                val endY = radius + (outerOffsetRadius * sin(currentAngleRad)).toFloat()

                drawLine(
                    color = accentColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = OnDarkSubtext,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = valueDisplay,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
