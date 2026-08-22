package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.FlOrange
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MetallicBorder
import com.example.ui.theme.MetallicSteel
import com.example.ui.theme.MetallicSteelDark
import com.example.ui.theme.OnDarkSubtext
import kotlin.math.roundToInt

@Composable
fun MasterPitchWheel(
    pitchSemitones: Float, // -12f to +12f
    onPitchChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var currentPitchOffset by remember { mutableFloatStateOf(0f) }

    val animatedPitch by animateFloatAsState(
        targetValue = if (isDragging) currentPitchOffset else 0f,
        animationSpec = spring(stiffness = 500f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "PITCH BEND",
            color = OnDarkSubtext,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(28.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkCanvas)
                .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            currentPitchOffset = 0f
                            onPitchChange(0f)
                        },
                        onDragCancel = {
                            isDragging = false
                            currentPitchOffset = 0f
                            onPitchChange(0f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount.y / 3f
                            currentPitchOffset = (currentPitchOffset + delta).coerceIn(-12f, 12f)
                            onPitchChange(currentPitchOffset)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                val w = size.width
                val h = size.height
                val center = h / 2f

                // Draw Center Line Notch
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, center),
                    end = Offset(w, center),
                    strokeWidth = 2f
                )

                // Wheel Thumb Position (-12 to +12 maps to Y range)
                val activePitch = if (isDragging) currentPitchOffset else animatedPitch
                val normalizedY = center - (activePitch / 12f) * (center - 12f)

                // Wheel Cylinder Ribs
                val ribHeight = 12.dp.toPx()
                drawRoundRect(
                    color = MetallicSteel,
                    topLeft = Offset(2.dp.toPx(), normalizedY - ribHeight / 2f),
                    size = Size(w - 4.dp.toPx(), ribHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawRoundRect(
                    color = FlOrange,
                    topLeft = Offset(4.dp.toPx(), normalizedY - 2.dp.toPx()),
                    size = Size(w - 8.dp.toPx(), 4.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        val displayVal = if (isDragging) "${pitchSemitones.roundToInt()}st" else "CENTER"
        Text(
            text = displayVal,
            color = if (isDragging) FlOrange else Color.Gray,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
