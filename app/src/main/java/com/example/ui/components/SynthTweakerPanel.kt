package com.example.ui.components

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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon

@Composable
fun SynthTweakerPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val synthParams by audioEngine.synthParams.collectAsState()
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
        // Preset Quick Switcher Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = "Presets", tint = GoldAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SYNTH PRESETS:",
                    color = GoldAccent,
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
                    text = "MANAGE ALL (${presets.size})",
                    color = FlOrange,
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
                        .background(if (isSelected) FlOrange else MetallicSteel)
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

        // ADSR Envelope Interactive Graphic Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = "ADSR ENVELOPE CURVE",
                    color = FlOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val pA = (synthParams.attack / 2.0f) * (w * 0.25f)
                    val pD = (synthParams.decay / 3.0f) * (w * 0.25f)
                    val pS = h * (1.0f - synthParams.sustain)
                    val pR = (synthParams.release / 4.0f) * (w * 0.25f)

                    val path = Path()
                    path.moveTo(0f, h)
                    path.lineTo(pA, 0f)
                    path.lineTo(pA + pD, pS)
                    path.lineTo(w - pR, pS)
                    path.lineTo(w, h)

                    drawPath(path, FlOrange, style = Stroke(width = 3f))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metallic Analog Rotary Knobs Section for ADSR & Cutoff
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "ANALOG ADSR & FILTER KNOBS",
                color = ElectricGreen,
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
                // ATTACK
                RotaryKnob(
                    value = (synthParams.attack / 2.0f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(attack = it * 2.0f) },
                    label = "ATTACK",
                    valueDisplay = "${String.format("%.2f", synthParams.attack)}s",
                    accentColor = FlOrange,
                    defaultValue = 0.01f
                )

                // DECAY
                RotaryKnob(
                    value = (synthParams.decay / 3.0f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(decay = it * 3.0f) },
                    label = "DECAY",
                    valueDisplay = "${String.format("%.2f", synthParams.decay)}s",
                    accentColor = FlOrange,
                    defaultValue = 0.25f
                )

                // SUSTAIN
                RotaryKnob(
                    value = synthParams.sustain.coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(sustain = it) },
                    label = "SUSTAIN",
                    valueDisplay = "${(synthParams.sustain * 100).toInt()}%",
                    accentColor = FlOrange,
                    defaultValue = 0.6f
                )

                // RELEASE
                RotaryKnob(
                    value = (synthParams.release / 4.0f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(release = it * 4.0f) },
                    label = "RELEASE",
                    valueDisplay = "${String.format("%.2f", synthParams.release)}s",
                    accentColor = FlOrange,
                    defaultValue = 0.4f
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CUTOFF
                RotaryKnob(
                    value = (synthParams.cutoff / 10000f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(cutoff = it * 10000f) },
                    label = "CUTOFF",
                    valueDisplay = "${synthParams.cutoff.toInt()}Hz",
                    accentColor = NeonCyan,
                    defaultValue = 0.4f
                )

                // RESONANCE
                RotaryKnob(
                    value = (synthParams.resonance / 10f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(resonance = it * 10f) },
                    label = "RESONANCE",
                    valueDisplay = String.format("%.1f", synthParams.resonance),
                    accentColor = NeonCyan,
                    defaultValue = 0.15f
                )

                // LFO RATE
                RotaryKnob(
                    value = (synthParams.lfoRate / 10f).coerceIn(0f, 1f),
                    onValueChange = { audioEngine.synthParams.value = synthParams.copy(lfoRate = it * 10f) },
                    label = "LFO RATE",
                    valueDisplay = "${String.format("%.1f", synthParams.lfoRate)}Hz",
                    accentColor = NeonMagenta,
                    defaultValue = 0.2f
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LFO Routing & Formant Vowel Filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text("FORMANT VOWEL FILTER (VOCAL SYNTH)", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Off", "A (Ah)", "E (Eh)", "I (Ee)", "O (Oh)", "U (Oo)").forEach { vowel ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (synthParams.formantVowel == vowel) ElectricGreen else MetallicSteel)
                            .clickable { audioEngine.synthParams.value = synthParams.copy(formantVowel = vowel) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(vowel, color = if (synthParams.formantVowel == vowel) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("PITCH QUANTIZE SCALE", color = NeonPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Chromatic", "Minor", "Major", "Pentatonic", "Dorian", "Japanese", "Blues").forEach { scale ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (synthParams.pitchScale == scale) NeonPurple else MetallicSteel)
                            .clickable { audioEngine.synthParams.value = synthParams.copy(pitchScale = scale) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(scale, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

