package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEngine
import com.example.ui.MainStudioViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.FlOrange
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MetallicBorder
import com.example.ui.theme.MetallicSteel
import com.example.ui.theme.MetallicSteelDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OnDarkSubtext

@Composable
fun PerformancePadsPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val pads by viewModel.performancePads.collectAsState()
    var selectedChordType by remember { mutableStateOf("Single") }
    var arpEnabled by remember { mutableStateOf(false) }
    var arpRate by remember { mutableStateOf("1/16") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(8.dp)
    ) {
        // Chord Memory Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MetallicSteelDark)
                .border(1.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "CHORD MEMORY & ARPEGGIATOR ENGINE",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CHORD:", color = GoldAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    listOf("Single", "Major", "Minor", "7th", "Sus2", "Stab").forEach { chord ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedChordType == chord) FlOrange else MetallicSteel)
                                .clickable { selectedChordType = chord }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = chord,
                                color = if (selectedChordType == chord) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { arpEnabled = !arpEnabled },
                        colors = ButtonDefaults.buttonColors(containerColor = if (arpEnabled) ElectricGreen else MetallicSteel),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (arpEnabled) "ARP ON" else "ARP OFF", color = if (arpEnabled) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text("RATE:", color = GoldAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    listOf("1/4", "1/8", "1/16", "1/32").forEach { rate ->
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (arpRate == rate) NeonPurple else MetallicSteel)
                                .clickable { arpRate = rate }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(rate, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 12-Performance Pads Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pads) { pad ->
                var isPressed by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPressed) Color(pad.colorHex) else MetallicSteelDark)
                        .border(
                            width = if (isPressed) 2.5.dp else 1.dp,
                            color = if (isPressed) Color.White else Color(pad.colorHex).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            isPressed = true
                            val modifiedPad = pad.copy(chordType = selectedChordType)
                            audioEngine.triggerPad(modifiedPad)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Text(
                            text = pad.name,
                            color = if (isPressed) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${pad.trackType.displayName} • ${selectedChordType}",
                            color = if (isPressed) Color.Black.copy(alpha = 0.8f) else Color(pad.colorHex),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

