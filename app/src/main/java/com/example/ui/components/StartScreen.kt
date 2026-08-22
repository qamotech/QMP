package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.FlOrange
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MetallicBorder
import com.example.ui.theme.MetallicSteel
import com.example.ui.theme.MetallicSteelDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

@Composable
fun StartScreen(
    onEnterStudio: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Pulse animation for Enter button
    val infiniteTransition = rememberInfiniteTransition(label = "StartScreenPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Metallic Diamond Plate Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val spacing = 32f

            // Background subtle gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1116), Color(0xFF1B1E26), Color(0xFF08090C))
                )
            )

            // Diamond Plate Mesh Drawing
            val diamondColor = Color(0xFF2A2E3D).copy(alpha = 0.35f)
            val highlightColor = Color(0xFF4A5268).copy(alpha = 0.25f)

            var y = 0f
            while (y < h) {
                var x = 0f
                var rowIdx = 0
                while (x < w) {
                    val offsetX = if (rowIdx % 2 == 0) 0f else spacing / 2f
                    val posX = x + offsetX

                    val diamondPath = Path().apply {
                        moveTo(posX, y - 6f)
                        lineTo(posX + 10f, y)
                        lineTo(posX, y + 6f)
                        lineTo(posX - 10f, y)
                        close()
                    }

                    drawPath(diamondPath, color = diamondColor)
                    drawPath(diamondPath, color = highlightColor, style = Stroke(width = 1f))

                    x += spacing
                    rowIdx++
                }
                y += spacing / 1.5f
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Diamond Plate Brand Banner - QAMELOT MEDIA
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Diamond Emblem Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(FlOrange, GoldAccent, Color.White)
                            )
                        )
                        .border(2.dp, MetallicBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Qamelot Emblem",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metallic Diamond Plate "QAMELOT MEDIA" Header Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MetallicSteelDark)
                        .border(2.dp, MetallicBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "QAMELOT MEDIA",
                            color = FlOrange,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "PREMASTERED AUDIO WORKSTATION",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Professional HipHop • Pop • RnB Music Production & AI Studio",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Studio Capabilities Highlights Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureBadgeRow(
                    icon = Icons.Default.MusicNote,
                    iconColor = FlOrange,
                    title = "HipHop, Pop & RnB Presets",
                    subtitle = "Premastered drums with transient punch & sub-bass density."
                )

                FeatureBadgeRow(
                    icon = Icons.Default.GraphicEq,
                    iconColor = ElectricGreen,
                    title = "60-FPS OLED Oscilloscope",
                    subtitle = "Real-time FFT audio spectrum & waveform mastering."
                )

                FeatureBadgeRow(
                    icon = Icons.Default.Mic,
                    iconColor = NeonMagenta,
                    title = "AI Beatbox-to-MIDI Engine",
                    subtitle = "Record vocal beatboxing to auto-synthesize drums & bass."
                )

                FeatureBadgeRow(
                    icon = Icons.Default.Tune,
                    iconColor = GoldAccent,
                    title = "Analog Metallic Rotary Knobs",
                    subtitle = "Tactile rotary controls for ADSR, 5-Band EQ & 3D Panner."
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Enter Studio Pulsing Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FlOrange, GoldAccent, ElectricGreen)
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
                    .clickable { onEnterStudio() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Enter Studio",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTER PRODUCTION STUDIO",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeatureBadgeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MetallicSteelDark)
            .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MetallicSteel),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
