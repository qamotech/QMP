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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Transform
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
import com.example.ui.MainStudioViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.OnDarkSubtext
import com.example.ui.theme.OnDarkText

@Composable
fun BeatboxRecorderPanel(
    viewModel: MainStudioViewModel,
    audioEngine: AudioEngine
) {
    val isRecording by audioEngine.beatboxRecorder.recordingState.collectAsState()
    val rms by audioEngine.beatboxRecorder.currentRms.collectAsState()
    val statusMsg by viewModel.aiStatusMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status & Instruction Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "BEATBOX VOCAL TO MULTI-TRACK CONVERTER",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Record your mouth beatboxing ('Boom', 'Kah', 'Tss', 'Hum'). The AI DSP engine analyzes low/mid/high transients and auto-populates Kick, Snare, Hi-Hat, and Sub-Bass tracks!",
                    color = OnDarkSubtext,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RMS Mic Volume Meter Circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(if (isRecording) NeonMagenta.copy(alpha = 0.2f + rms * 0.8f) else DarkSurface)
                .border(
                    width = 3.dp,
                    color = if (isRecording) NeonMagenta else NeonCyan,
                    shape = CircleShape
                )
                .clickable {
                    if (isRecording) {
                        viewModel.stopBeatboxRecordingAndConvert()
                    } else {
                        viewModel.startBeatboxRecording()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Record Mic",
                    tint = if (isRecording) Color.Red else ElectricGreen,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRecording) "RECORDING..." else "TAP TO RECORD",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Convert Action Button
        Button(
            onClick = {
                if (isRecording) {
                    viewModel.stopBeatboxRecordingAndConvert()
                } else {
                    viewModel.stopBeatboxRecordingAndConvert()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Transform, contentDescription = "Convert", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONVERT BEATBOX TO SEQUENCER TRACKS",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
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
