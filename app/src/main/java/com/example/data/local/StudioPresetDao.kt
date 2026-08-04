package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioPresetDao {
    @Query("SELECT * FROM studio_presets ORDER BY isBuiltIn DESC, name ASC")
    fun getAllPresets(): Flow<List<StudioPreset>>

    @Query("SELECT * FROM studio_presets WHERE category = :category ORDER BY isBuiltIn DESC, name ASC")
    fun getPresetsByCategory(category: String): Flow<List<StudioPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: StudioPreset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<StudioPreset>)

    @Query("DELETE FROM studio_presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomPreset(id: Long)

    @Query("SELECT COUNT(*) FROM studio_presets")
    suspend fun getPresetCount(): Int
}
