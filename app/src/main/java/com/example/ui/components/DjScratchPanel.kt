package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEngine
import com.example.ui.MainStudioViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OnDarkSubtext
import com.example.ui.theme.OnDarkText
import kotlin.math.atan2

@Composable
fun DjScratchPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val djEngine = audioEngine.djScratchEngine
    val angle by djEngine.platterAngle.collectAsState()
    val speed by djEngine.scratchSpeed.collectAsState()
    val rollRate by djEngine.stutterRollRate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DJ VINYL SCRATCH PLATTER & STUTTER ROLL",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SPEED: ${String.format("%.1f", speed)}x",
                    color = ElectricGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Vinyl Platter
        Box(
            modifier = Modifier
                .size(210.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C0E14))
                .border(3.dp, NeonCyan, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val ang = Math.toDegrees(atan2((offset.y - cy).toDouble(), (offset.x - cx).toDouble())).toFloat()
                            djEngine.onTouchDown(ang)
                        },
                        onDrag = { change, _ ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val ang = Math.toDegrees(atan2((change.position.y - cy).toDouble(), (change.position.x - cx).toDouble())).toFloat()
                            djEngine.onTouchMove(ang)
                        },
                        onDragEnd = { djEngine.onTouchUp() },
                        onDragCancel = { djEngine.onTouchUp() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.width / 2f

                rotate(angle, pivot = Offset(cx, cy)) {
                    // Grooves
                    drawCircle(Color(0xFF181B26), radius = radius * 0.88f, center = Offset(cx, cy), style = Stroke(width = 8f))
                    drawCircle(Color(0xFF222636), radius = radius * 0.72f, center = Offset(cx, cy), style = Stroke(width = 6f))
                    drawCircle(Color(0xFF181B26), radius = radius * 0.55f, center = Offset(cx, cy), style = Stroke(width = 6f))

                    // Label Center
                    drawCircle(NeonMagenta, radius = radius * 0.32f, center = Offset(cx, cy))
                    drawCircle(Color.Black, radius = radius * 0.08f, center = Offset(cx, cy))

                    // White Vinyl Position Marker Line
                    drawLine(
                        color = Color.White,
                        start = Offset(cx, cy - radius * 0.28f),
                        end = Offset(cx, cy - radius * 0.88f),
                        strokeWidth = 5f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backspin & Stutter Roll Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { djEngine.triggerBackspin() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("BACKSPIN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Row {
                Text("ROLL:", color = OnDarkSubtext, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0 to "OFF", 8 to "1/8", 16 to "1/16", 32 to "1/32").forEach { (rate, label) ->
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (rollRate == rate) ElectricGreen else DarkSurfaceVariant)
                            .clickable { djEngine.setStutterRoll(rate) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(label, color = if (rollRate == rate) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
