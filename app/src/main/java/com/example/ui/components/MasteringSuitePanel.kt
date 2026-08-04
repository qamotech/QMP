package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEngine
import com.example.audio.VisualizerMode
import com.example.ui.MainStudioViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.FlOrange
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MetallicBorder
import com.example.ui.theme.MetallicSteel
import com.example.ui.theme.MetallicSteelDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OnDarkSubtext
import com.example.ui.theme.OnDarkText

@Composable
fun MasteringSuitePanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val mixerFx by audioEngine.mixerFxParams.collectAsState()
    val statusMsg by viewModel.aiStatusMessage.collectAsState()
    val activeVizMode by audioEngine.visualizerEngine.mode.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        // Visualizer Mode Selector Header Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VisualizerMode.values().forEach { mode ->
                val isSelected = (activeVizMode == mode)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) FlOrange else MetallicSteel)
                        .border(1.dp, if (isSelected) Color.White else MetallicBorder, RoundedCornerShape(8.dp))
                        .clickable { audioEngine.visualizerEngine.setVisualizerMode(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.displayName,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Live Oscilloscope Visualizer
        OledOscilloscope(visualizerEngine = audioEngine.visualizerEngine)

        Spacer(modifier = Modifier.height(10.dp))

        // Mastering Analog Dynamics Processors with Metallic Knobs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "MASTERING DYNAMICS PROCESSORS",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = ((mixerFx.masterLimiterThreshold + 6.0f) / 6.0f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(masterLimiterThreshold = it * 6.0f - 6.0f) },
                    label = "LIMITER",
                    valueDisplay = "${String.format("%.1f", mixerFx.masterLimiterThreshold)}dB",
                    accentColor = NeonCyan
                )

                RotaryKnob(
                    value = ((mixerFx.stereoWidth - 0.5f) / 2.0f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(stereoWidth = it * 2.0f + 0.5f) },
                    label = "STEREO WIDTH",
                    valueDisplay = "${String.format("%.1f", mixerFx.stereoWidth)}x",
                    accentColor = NeonMagenta
                )

                RotaryKnob(
                    value = mixerFx.harmonicExciter.coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(harmonicExciter = it) },
                    label = "EXCITER",
                    valueDisplay = "${(mixerFx.harmonicExciter * 100).toInt()}%",
                    accentColor = GoldAccent
                )

                RotaryKnob(
                    value = mixerFx.transientPunch.coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(transientPunch = it) },
                    label = "PUNCH",
                    valueDisplay = "${(mixerFx.transientPunch * 100).toInt()}%",
                    accentColor = ElectricGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.saveCurrentProject() },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("AUTOSAVE PROJECT", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.exportStems() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("BOUNCE WAV STEMS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (statusMsg != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusMsg!!,
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

