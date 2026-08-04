package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VisualizerEngine
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

@Composable
fun OledOscilloscope(
    visualizerEngine: VisualizerEngine,
    modifier: Modifier = Modifier
) {
    val waveform by visualizerEngine.waveformState.collectAsState()
    val spectrum by visualizerEngine.spectrumState.collectAsState()
    val vectorScope by visualizerEngine.vectorScopeState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCanvas)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "60-FPS OLED OSCILLOSCOPE & SPECTROGRAM",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "44.1kHz • TRUE PEAK",
                    color = ElectricGreen,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxSize()) {
                // 1. Oscilloscope Waveform & Spectrum Overlay
                Canvas(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06070B))
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    // Grid Lines
                    val gridColor = Color(0xFF1B2030)
                    drawLine(gridColor, Offset(0f, centerY), Offset(w, centerY), strokeWidth = 1f)
                    drawLine(gridColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 1f)
                    drawLine(gridColor, Offset(w / 4f, 0f), Offset(w / 4f, h), strokeWidth = 1f)
                    drawLine(gridColor, Offset(3 * w / 4f, 0f), Offset(3 * w / 4f, h), strokeWidth = 1f)

                    // RTA Spectrum Bars
                    if (spectrum.isNotEmpty()) {
                        val barWidth = w / spectrum.size
                        for (i in spectrum.indices) {
                            val barH = spectrum[i] * h * 0.8f
                            drawRect(
                                color = NeonMagenta.copy(alpha = 0.35f),
                                topLeft = Offset(i * barWidth + 2f, h - barH),
                                size = androidx.compose.ui.geometry.Size(barWidth - 4f, barH)
                            )
                        }
                    }

                    // Oscilloscope Waveform Path
                    if (waveform.isNotEmpty()) {
                        val path = Path()
                        val stepX = w / (waveform.size - 1)
                        for (i in waveform.indices) {
                            val x = i * stepX
                            val y = centerY - (waveform[i] * (h / 2f) * 0.9f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, NeonCyan, style = Stroke(width = 2.5f))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 2. Vector Scope Goniometer
                Canvas(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06070B))
                ) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // Goniometer Crosshair
                    drawLine(Color(0xFF1B2030), Offset(cx - 20f, cy), Offset(cx + 20f, cy), strokeWidth = 1f)
                    drawLine(Color(0xFF1B2030), Offset(cx, cy - 20f), Offset(cx, cy + 20f), strokeWidth = 1f)

                    // Goniometer Dots
                    for (pt in vectorScope) {
                        val px = cx + (pt.first * (w / 2f) * 0.8f)
                        val py = cy - (pt.second * (h / 2f) * 0.8f)
                        drawCircle(ElectricGreen, radius = 1.5f, center = Offset(px, py))
                    }
                }
            }
        }
    }
}
