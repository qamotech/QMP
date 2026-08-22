package com.example.ui.components

import com.example.audio.PatternBank
import com.example.audio.StepNote

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
fun StepSequencerPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val bpm by audioEngine.bpm.collectAsState()
    val swing by audioEngine.swing.collectAsState()
    val stepsCount by audioEngine.stepsCount.collectAsState()
    val currentStep by audioEngine.currentStep.collectAsState()
    val polyrhythm by audioEngine.polyrhythmRatio.collectAsState()
    val tracks by audioEngine.tracks.collectAsState()
    val activePattern by audioEngine.activePattern.collectAsState()
    val pitchBend by audioEngine.masterPitchBend.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()

    val vuLeftPeak by audioEngine.visualizerEngine.vuLeftPeak.collectAsState()
    val vuRightPeak by audioEngine.visualizerEngine.vuRightPeak.collectAsState()
    val isClipping by audioEngine.visualizerEngine.isClipping.collectAsState()

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    var showAdvancedLane by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(6.dp)
    ) {
        // FL Studio Dark Metallic Control Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // FL Transport Play/Pause
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FlOrange)
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { audioEngine.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Tap Tempo Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MetallicSteel)
                            .border(1.dp, FlOrange, RoundedCornerShape(8.dp))
                            .clickable { audioEngine.tapTempo() }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "TAP BPM",
                            color = FlOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Stereo VU Meter
                    VuMeterBar(
                        leftLevel = vuLeftPeak,
                        rightLevel = vuRightPeak,
                        isClipping = isClipping
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Master Pitch Bend Wheel
                    MasterPitchWheel(
                        pitchSemitones = pitchBend,
                        onPitchChange = { audioEngine.setMasterPitchBend(it) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Fullscreen Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleFullscreen() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MetallicSteel)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = NeonCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tempo & Swing Sliders Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TEMPO: $bpm BPM", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("SWING: ${(swing * 100).toInt()}%", color = GoldAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { audioEngine.setBpm(it.toInt()) },
                        valueRange = 40f..220f,
                        colors = SliderDefaults.colors(thumbColor = FlOrange, activeTrackColor = FlOrange)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 16 / 32 Steps Selector
                Button(
                    onClick = { audioEngine.setStepsCount(if (stepsCount == 16) 32 else 16) },
                    colors = ButtonDefaults.buttonColors(containerColor = MetallicSteel),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("${stepsCount} STEPS", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Pattern Bank Switching Tabs (Pattern A, B, C, D) & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PatternBank.values().forEach { bank ->
                    val isSelected = (activePattern == bank)
                    val badgeColor = Color(bank.badgeColorHex)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) badgeColor else MetallicSteel)
                            .border(1.dp, if (isSelected) Color.White else GlassBorder, RoundedCornerShape(6.dp))
                            .clickable { audioEngine.selectPattern(bank) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = bank.label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Toggle Advanced Lane (Velocity / Gate / Probability)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (showAdvancedLane) ElectricGreen else MetallicSteel)
                        .clickable { showAdvancedLane = !showAdvancedLane }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (showAdvancedLane) "EXPRESSION ON" else "EXPRESSION OFF",
                        color = if (showAdvancedLane) Color.Black else OnDarkSubtext,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.saveCurrentProject() },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAVE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Multi-Track Sequencer Grid with FL Studio High-Contrast Metallic Look
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                tracks.forEachIndexed { trackIdx, track ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Track Header Badge & Mute/Solo
                            Column(modifier = Modifier.width(80.dp)) {
                                Text(
                                    text = track.name,
                                    color = Color(track.colorHex),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (track.muted) Color.Red else MetallicSteel)
                                            .clickable {
                                                val updated = tracks.toMutableList()
                                                updated[trackIdx] = track.copy(muted = !track.muted)
                                                audioEngine.tracks.value = updated
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("M", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (track.soloed) GoldAccent else MetallicSteel)
                                            .clickable {
                                                val updated = tracks.toMutableList()
                                                updated[trackIdx] = track.copy(soloed = !track.soloed)
                                                audioEngine.tracks.value = updated
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("S", color = if (track.soloed) Color.Black else Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Scrollable Step Buttons Row
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                for (stepIdx in 0 until stepsCount) {
                                    val stepObj = if (stepIdx < track.steps.size) track.steps[stepIdx] else StepNote()
                                    val isActive = stepObj.active
                                    val isCurrent = (stepIdx == currentStep)

                                    val isBeatGroup = ((stepIdx / 4) % 2 == 0)
                                    val padColor = when {
                                        isCurrent && isActive -> ElectricGreen
                                        isCurrent -> FlOrange
                                        isActive -> Color(track.colorHex)
                                        isBeatGroup -> MetallicSteel
                                        else -> Color(0xFF12141A)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(1.5.dp)
                                            .size(width = 28.dp, height = 34.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(padColor)
                                            .border(
                                                width = if (isCurrent) 1.5.dp else 0.5.dp,
                                                color = if (isCurrent) Color.White else GlassBorder,
                                                shape = RoundedCornerShape(5.dp)
                                            )
                                            .clickable { viewModel.toggleStep(trackIdx, stepIdx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Advanced Expression Control Lane (Gate / Probability / Velocity)
                        if (showAdvancedLane) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 84.dp, top = 2.dp)
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                for (stepIdx in 0 until stepsCount) {
                                    val stepObj = if (stepIdx < track.steps.size) track.steps[stepIdx] else StepNote()
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .padding(horizontal = 1.5.dp)
                                            .width(28.dp)
                                    ) {
                                        // Velocity Indicator Bar
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(DarkCanvas)
                                                .clickable {
                                                    val nextVel = if (stepObj.velocity >= 1.0f) 0.3f else stepObj.velocity + 0.35f
                                                    viewModel.updateStepVelocity(trackIdx, stepIdx, nextVel)
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(stepObj.velocity)
                                                    .fillMaxSize()
                                                    .background(NeonCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

