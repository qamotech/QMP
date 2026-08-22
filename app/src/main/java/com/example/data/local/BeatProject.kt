package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beat_projects")
data class BeatProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val bpm: Int = 120,
    val swing: Float = 0f,
    val stepsCount: Int = 16,
    val tracksJson: String = "",
    val synthConfigJson: String = "",
    val mixerConfigJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_audio_stems")
data class SavedAudioStem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val stemName: String,
    val filePath: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
