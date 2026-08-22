package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OnDarkSubtext
import com.example.ui.theme.OnDarkText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiToolsPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    var promptText by remember { mutableStateOf("") }
    val isGenerating by viewModel.isAiGenerating.collectAsState()
    val statusMsg by viewModel.aiStatusMessage.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(10.dp)
            .verticalScroll(scrollState)
    ) {
        // AI Beat Generator Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = NeonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GEMINI AI BEAT GENERATOR & INSTANT REMIX",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Describe any genre or rhythm style. Gemini AI will compose the 16/32 step constructor patterns, synth chords, sub bass drops, and tempo automatically!",
                    color = OnDarkSubtext,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prompt Input Field
        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = { Text("e.g. Cyberpunk Synthwave 128 BPM with heavy sub bass and crisp snare...", color = OnDarkSubtext) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = OnDarkText,
                unfocusedTextColor = OnDarkText,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Prompt Shortcuts
        Text("QUICK GENRE PRESETS:", color = OnDarkSubtext, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Cyberpunk Synthwave 128 BPM",
                "Lo-Fi Chillhop Beatbox 85 BPM",
                "Trap Banger Heavy Sub 140 BPM",
                "Afrobeat Percussion Groove",
                "Futuristic Techno 130 BPM",
                "Old School Boom Bap Beatbox"
            ).forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            promptText = preset
                            viewModel.generateAiBeat(preset)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(preset, color = NeonMagenta, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Action Button
        Button(
            onClick = {
                if (promptText.isNotBlank()) {
                    viewModel.generateAiBeat(promptText)
                }
            },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GEMINI COMPOSING...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Generate", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE BEAT WITH GEMINI AI", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
