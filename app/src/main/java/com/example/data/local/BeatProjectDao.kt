package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeatProjectDao {
    @Query("SELECT * FROM beat_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<BeatProject>>

    @Query("SELECT * FROM beat_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): BeatProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: BeatProject): Long

    @Query("DELETE FROM beat_projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("SELECT * FROM saved_audio_stems WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getStemsForProject(projectId: Long): Flow<List<SavedAudioStem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStem(stem: SavedAudioStem): Long
}
