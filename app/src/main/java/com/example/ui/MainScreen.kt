package com.example.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.AiToolsPanel
import com.example.ui.components.BeatboxRecorderPanel
import com.example.ui.components.DjScratchPanel
import com.example.ui.components.MasteringSuitePanel
import com.example.ui.components.MixerEffectsPanel
import com.example.ui.components.OledOscilloscope
import com.example.ui.components.PerformancePadsPanel
import com.example.ui.components.PresetManagerDialog
import com.example.ui.components.StartScreen
import com.example.ui.components.StepSequencerPanel
import com.example.ui.components.SynthTweakerPanel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.FlOrange
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OnDarkSubtext
import com.example.ui.theme.OnDarkText

@Composable
fun MainScreen(viewModel: MainStudioViewModel) {
    var showStartScreen by remember { mutableStateOf(true) }
    val activeTab by viewModel.activeTab.collectAsState()
    val projectName by viewModel.currentProjectName.collectAsState()
    val showPresetDialog by viewModel.showPresetDialog.collectAsState()
    val activePresetName by viewModel.activePresetName.collectAsState()
    val audioEngine = viewModel.audioEngine

    if (showStartScreen) {
        StartScreen(onEnterStudio = { showStartScreen = false })
        return
    }

    if (showPresetDialog) {
        PresetManagerDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closePresetDialog() }
        )
    }

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Top App Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Logo", tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BEATBOX & SYNTH STUDIO",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$projectName • ${activePresetName ?: "Custom Patch"}",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Qamelot Media Start Screen Launcher Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FlOrange)
                                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                                .clickable { showStartScreen = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "QAMELOT",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Presets Launcher Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent)
                                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                                .clickable { viewModel.openPresetDialog() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Presets",
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PRESETS",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Quick Save Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricGreen)
                                .clickable { viewModel.saveCurrentProject() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "QUICK SAVE",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time OLED Oscilloscope
                OledOscilloscope(visualizerEngine = audioEngine.visualizerEngine)

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Studio Tab Selector Bar
                val tabScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tabScrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudioTab.values().forEach { tab ->
                        val isSelected = (activeTab == tab)
                        val tabColor = when (tab) {
                            StudioTab.SEQUENCER -> NeonCyan
                            StudioTab.PERFORMANCE_PADS -> NeonMagenta
                            StudioTab.BEATBOX_RECORDER -> ElectricGreen
                            StudioTab.SYNTH_TWEAKER -> NeonPurple
                            StudioTab.MIXER_EFFECTS -> GoldAccent
                            StudioTab.DJ_TOOLS -> NeonCyan
                            StudioTab.AI_TOOLS -> NeonMagenta
                            StudioTab.MASTERING_SUITE -> ElectricGreen
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) tabColor else DarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White else GlassBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectTab(tab) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                StudioTab.SEQUENCER -> StepSequencerPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.PERFORMANCE_PADS -> PerformancePadsPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.BEATBOX_RECORDER -> BeatboxRecorderPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.SYNTH_TWEAKER -> SynthTweakerPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.MIXER_EFFECTS -> MixerEffectsPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.DJ_TOOLS -> DjScratchPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.AI_TOOLS -> AiToolsPanel(viewModel = viewModel, audioEngine = audioEngine)
                StudioTab.MASTERING_SUITE -> MasteringSuitePanel(viewModel = viewModel, audioEngine = audioEngine)
            }
        }
    }
}
