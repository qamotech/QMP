package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val dao: BeatProjectDao,
    private val presetDao: StudioPresetDao
) {
    val allProjects: Flow<List<BeatProject>> = dao.getAllProjects()

    suspend fun getProjectById(id: Long): BeatProject? = dao.getProjectById(id)

    suspend fun saveProject(project: BeatProject): Long = dao.insertProject(project)

    suspend fun deleteProject(id: Long) = dao.deleteProject(id)

    fun getStems(projectId: Long): Flow<List<SavedAudioStem>> = dao.getStemsForProject(projectId)

    suspend fun saveStem(stem: SavedAudioStem): Long = dao.insertStem(stem)

    // Studio Preset operations
    val allPresets: Flow<List<StudioPreset>> = presetDao.getAllPresets()

    suspend fun savePreset(preset: StudioPreset): Long = presetDao.insertPreset(preset)

    suspend fun savePresets(presets: List<StudioPreset>) = presetDao.insertPresets(presets)

    suspend fun deletePreset(id: Long) = presetDao.deleteCustomPreset(id)

    suspend fun getPresetCount(): Int = presetDao.getPresetCount()
}
