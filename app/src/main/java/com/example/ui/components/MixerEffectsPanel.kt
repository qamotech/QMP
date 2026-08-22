package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.MainStudioViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
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
fun MixerEffectsPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val tracks by audioEngine.tracks.collectAsState()
    val mixerFx by audioEngine.mixerFxParams.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val activePresetName by viewModel.activePresetName.collectAsState()
    val scrollState = rememberScrollState()
    val chipScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        // Mixer & FX Preset Quick Switcher Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = "Presets", tint = ElectricGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "MIXER ROUTING PRESETS:",
                    color = ElectricGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MetallicSteel)
                    .clickable { viewModel.openPresetDialog() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "MANAGE PRESETS (${presets.size})",
                    color = GoldAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScrollState)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = (preset.name == activePresetName)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricGreen else MetallicSteel)
                        .border(1.dp, if (isSelected) Color.White else MetallicBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.loadPreset(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3D Spatial Panner XY Touch Pad
        SpatialPanXyPad(
            pannerX = mixerFx.panner3dX,
            pannerY = mixerFx.panner3dY,
            onPanChange = { x, y ->
                audioEngine.mixerFxParams.value = mixerFx.copy(panner3dX = x, panner3dY = y)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5-Band Parametric EQ with Analog Knobs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "5-BAND PARAMETRIC EQ",
                color = NeonCyan,
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
                    value = ((mixerFx.eqLow + 12f) / 24f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(eqLow = it * 24f - 12f) },
                    label = "LOW",
                    valueDisplay = "${mixerFx.eqLow.toInt()}dB",
                    accentColor = NeonCyan
                )

                RotaryKnob(
                    value = ((mixerFx.eqLowMid + 12f) / 24f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(eqLowMid = it * 24f - 12f) },
                    label = "LOW MID",
                    valueDisplay = "${mixerFx.eqLowMid.toInt()}dB",
                    accentColor = NeonCyan
                )

                RotaryKnob(
                    value = ((mixerFx.eqMid + 12f) / 24f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(eqMid = it * 24f - 12f) },
                    label = "MID",
                    valueDisplay = "${mixerFx.eqMid.toInt()}dB",
                    accentColor = NeonCyan
                )

                RotaryKnob(
                    value = ((mixerFx.eqHighMid + 12f) / 24f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(eqHighMid = it * 24f - 12f) },
                    label = "HIGH MID",
                    valueDisplay = "${mixerFx.eqHighMid.toInt()}dB",
                    accentColor = NeonCyan
                )

                RotaryKnob(
                    value = ((mixerFx.eqHigh + 12f) / 24f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(eqHigh = it * 24f - 12f) },
                    label = "HIGH",
                    valueDisplay = "${mixerFx.eqHigh.toInt()}dB",
                    accentColor = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Effects Rack with Metallic Knobs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "STUDIO EFFECTS RACK",
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
                    value = mixerFx.saturatorDrive.coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(saturatorDrive = it) },
                    label = "SATURATOR",
                    valueDisplay = "${(mixerFx.saturatorDrive * 100).toInt()}%",
                    accentColor = GoldAccent
                )

                RotaryKnob(
                    value = ((mixerFx.bitcrusherDepth - 2f) / 14f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(bitcrusherDepth = (it * 14f + 2f).toInt()) },
                    label = "BITCRUSH",
                    valueDisplay = "${mixerFx.bitcrusherDepth}-BIT",
                    accentColor = NeonMagenta
                )

                RotaryKnob(
                    value = mixerFx.sidechainDucking.coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(sidechainDucking = it) },
                    label = "SIDECHAIN",
                    valueDisplay = "${(mixerFx.sidechainDucking * 100).toInt()}%",
                    accentColor = ElectricGreen
                )

                RotaryKnob(
                    value = mixerFx.reverbWet.coerceIn(0f, 1f),
                    onValueChange = { audioEngine.mixerFxParams.value = mixerFx.copy(reverbWet = it) },
                    label = "REVERB",
                    valueDisplay = "${(mixerFx.reverbWet * 100).toInt()}%",
                    accentColor = NeonPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-Track Fader Console
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text("TRACK FADER CONSOLE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            tracks.forEachIndexed { idx, track ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.name,
                        color = Color(track.colorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(75.dp)
                    )
                    Slider(
                        value = track.volume,
                        onValueChange = { newVol ->
                            val updated = tracks.toMutableList()
                            updated[idx] = track.copy(volume = newVol)
                            audioEngine.tracks.value = updated
                        },
                        valueRange = 0f..1.2f,
                        colors = SliderDefaults.colors(thumbColor = Color(track.colorHex), activeTrackColor = Color(track.colorHex)),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(track.volume * 100).toInt()}%",
                        color = OnDarkSubtext,
                        fontSize = 9.sp,
                        modifier = Modifier.width(35.dp)
                    )
                }
            }
        }
    }
}

