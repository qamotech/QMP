package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studio_presets")
data class StudioPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Synth Patch", "Mixer FX", "Full Studio Configuration"
    val description: String = "",
    val isBuiltIn: Boolean = false,
    val synthConfigJson: String = "",
    val mixerConfigJson: String = "",
    val bpm: Int = 120,
    val swing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
