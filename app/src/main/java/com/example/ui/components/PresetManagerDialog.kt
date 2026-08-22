package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PresetSerializer
import com.example.data.local.StudioPreset
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

@Composable
fun PresetManagerDialog(
    viewModel: MainStudioViewModel,
    onDismiss: () -> Unit
) {
    val presets by viewModel.presets.collectAsState()
    val activePresetName by viewModel.activePresetName.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var isSavingNewPreset by remember { mutableStateOf(false) }
    var isImportingJson by remember { mutableStateOf(false) }

    // Save Form Inputs
    var newPresetName by remember { mutableStateOf("") }
    var newPresetCategory by remember { mutableStateOf("Full Studio Configuration") }
    var newPresetDesc by remember { mutableStateOf("") }

    // JSON Import Input
    var jsonImportText by remember { mutableStateOf("") }

    val categories = listOf("All", "Full Studio Configuration", "Synth Patch", "Mixer FX", "Custom")

    val filteredPresets = remember(presets, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "All" -> presets
            "Custom" -> presets.filter { !it.isBuiltIn }
            else -> presets.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Presets", tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "STUDIO PRESET MANAGER",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Active Patch: ${activePresetName ?: "Custom Patch"}",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OnDarkSubtext)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Save Current Patch & Import JSON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isSavingNewPreset = !isSavingNewPreset; isImportingJson = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSavingNewPreset) ElectricGreen else DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (isSavingNewPreset) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSavingNewPreset) "CANCEL SAVE" else "SAVE CURRENT PATCH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSavingNewPreset) Color.Black else Color.White
                        )
                    }

                    Button(
                        onClick = { isImportingJson = !isImportingJson; isSavingNewPreset = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isImportingJson) NeonCyan else DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import",
                            tint = if (isImportingJson) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isImportingJson) "CLOSE IMPORT" else "IMPORT JSON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isImportingJson) Color.Black else Color.White
                        )
                    }
                }

                // Save Form Expandable
                if (isSavingNewPreset) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCanvas)
                            .border(1.dp, ElectricGreen, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CREATE NEW CUSTOM PRESET",
                            color = ElectricGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            label = { Text("Preset Name", color = OnDarkSubtext, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricGreen,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Category:", color = OnDarkSubtext, fontSize = 10.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            listOf("Full Studio Configuration", "Synth Patch", "Mixer FX").forEach { cat ->
                                val selected = (newPresetCategory == cat)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) ElectricGreen else DarkSurfaceVariant)
                                        .clickable { newPresetCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newPresetDesc,
                            onValueChange = { newPresetDesc = it },
                            label = { Text("Description / Notes (Optional)", color = OnDarkSubtext, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricGreen,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (newPresetName.isNotBlank()) {
                                    viewModel.saveCustomPreset(
                                        name = newPresetName,
                                        category = newPresetCategory,
                                        description = newPresetDesc
                                    )
                                    newPresetName = ""
                                    newPresetDesc = ""
                                    isSavingNewPreset = false
                                }
                            },
                            enabled = newPresetName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE PRESET TO ROOM DATABASE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Import JSON Expandable
                if (isImportingJson) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCanvas)
                            .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "IMPORT PRESET JSON",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = jsonImportText,
                            onValueChange = { jsonImportText = it },
                            label = { Text("Paste Raw Preset JSON String", color = OnDarkSubtext, fontSize = 11.sp) },
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val success = viewModel.importPresetFromJson(jsonImportText)
                                if (success) {
                                    jsonImportText = ""
                                    isImportingJson = false
                                    Toast.makeText(context, "Preset imported & applied!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Invalid JSON structure", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = jsonImportText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("PARSE, SAVE & LOAD PRESET", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = (selectedCategoryFilter == category)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GoldAccent else DarkSurfaceVariant)
                                .clickable { selectedCategoryFilter = category }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else OnDarkSubtext
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPresets, key = { it.id }) { preset ->
                        PresetCardItem(
                            preset = preset,
                            isActive = (preset.name == activePresetName),
                            onLoad = {
                                viewModel.loadPreset(preset)
                                Toast.makeText(context, "Applied ${preset.name}", Toast.LENGTH_SHORT).show()
                            },
                            onCopyJson = {
                                val jsonStr = viewModel.exportPresetToJson(preset)
                                clipboardManager.setText(AnnotatedString(jsonStr))
                                Toast.makeText(context, "Preset JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                viewModel.deletePreset(preset.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetCardItem(
    preset: StudioPreset,
    isActive: Boolean,
    onLoad: () -> Unit,
    onCopyJson: () -> Unit,
    onDelete: () -> Unit
) {
    val synthParams = remember(preset.synthConfigJson) {
        PresetSerializer.jsonToSynthParams(preset.synthConfigJson)
    }
    val mixerFx = remember(preset.mixerConfigJson) {
        PresetSerializer.jsonToMixerFxParams(preset.mixerConfigJson)
    }

    val cardBorderColor = if (isActive) ElectricGreen else GlassBorder
    val cardBg = if (isActive) DarkSurfaceVariant else DarkCanvas

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(if (isActive) 2.dp else 1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(preset.category, color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = if (preset.isBuiltIn) "Factory Built-in" else "User Custom",
                        color = OnDarkSubtext,
                        fontSize = 9.sp
                    )

                    Text("•", color = OnDarkSubtext, fontSize = 9.sp)
                    Text("${preset.bpm} BPM", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Copy JSON button
                IconButton(
                    onClick = onCopyJson,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurface)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy JSON", tint = NeonCyan, modifier = Modifier.size(16.dp))
                }

                // Delete custom preset button
                if (!preset.isBuiltIn) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonMagenta, modifier = Modifier.size(16.dp))
                    }
                }

                // Load Preset Button
                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isActive) ElectricGreen else GoldAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Check else Icons.Default.PlayArrow,
                        contentDescription = "Load",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isActive) "LOADED" else "LOAD",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (preset.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preset.description,
                color = OnDarkSubtext,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Patch Inspector Badges & ADSR Curve Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                SpecBadge("Cutoff", "${synthParams.cutoff.toInt()}Hz", NeonCyan)
                SpecBadge("Res", String.format("%.1f", synthParams.resonance), NeonMagenta)
                SpecBadge("Reverb", "${(mixerFx.reverbWet * 100).toInt()}%", GoldAccent)
                SpecBadge("Delay", "${(mixerFx.delayWet * 100).toInt()}%", ElectricGreen)
                if (mixerFx.saturatorDrive > 0.1f) {
                    SpecBadge("Drive", "${(mixerFx.saturatorDrive * 100).toInt()}%", NeonPurple)
                }
            }

            // Mini ADSR + Filter Visualizer
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val path = Path()

                    val a = (synthParams.attack / 1.0f).coerceIn(0.1f, 0.3f) * w
                    val d = (synthParams.decay / 2.0f).coerceIn(0.1f, 0.3f) * w
                    val sHeight = (1.0f - synthParams.sustain.coerceIn(0.1f, 0.9f)) * h
                    val r = (synthParams.release / 2.0f).coerceIn(0.1f, 0.3f) * w

                    path.moveTo(0f, h)
                    path.lineTo(a, 2f)
                    path.lineTo(a + d, sHeight)
                    path.lineTo(w - r, sHeight)
                    path.lineTo(w, h)

                    drawPath(
                        path = path,
                        color = if (isActive) ElectricGreen else NeonCyan,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpecBadge(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurface)
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label: $value",
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
