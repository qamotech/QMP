package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiBeatResponse
import com.example.ai.GeminiBeatService
import com.example.audio.AudioEngine
import com.example.audio.PerformancePad
import com.example.audio.SequencerTrack
import com.example.audio.StepNote
import com.example.audio.TrackType
import com.example.data.PresetSerializer
import com.example.data.local.AppDatabase
import com.example.data.local.BeatProject
import com.example.data.local.ProjectRepository
import com.example.data.local.StudioPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class StudioTab(val title: String) {
    SEQUENCER("Sequencer"),
    PERFORMANCE_PADS("Pads & Chords"),
    BEATBOX_RECORDER("Beatbox Mic"),
    SYNTH_TWEAKER("Synth & ADSR"),
    MIXER_EFFECTS("Mixer & FX"),
    DJ_TOOLS("DJ Scratch & Roll"),
    AI_TOOLS("AI Beat & Remix"),
    MASTERING_SUITE("Mastering & Oscilloscope")
}

class MainStudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository
    val audioEngine = AudioEngine(viewModelScope)
    private val geminiService = GeminiBeatService()

    private val _activeTab = MutableStateFlow(StudioTab.SEQUENCER)
    val activeTab: StateFlow<StudioTab> = _activeTab.asStateFlow()

    val projects: StateFlow<List<BeatProject>>

    // Preset State
    val presets: StateFlow<List<StudioPreset>>
    private val _showPresetDialog = MutableStateFlow(false)
    val showPresetDialog: StateFlow<Boolean> = _showPresetDialog.asStateFlow()

    private val _activePresetName = MutableStateFlow<String?>("Default Init Patch")
    val activePresetName: StateFlow<String?> = _activePresetName.asStateFlow()

    private val _currentProjectName = MutableStateFlow("New Beatbox Session")
    val currentProjectName: StateFlow<String> = _currentProjectName.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiStatusMessage = MutableStateFlow<String?>(null)
    val aiStatusMessage: StateFlow<String?> = _aiStatusMessage.asStateFlow()

    // Performance Pads
    val performancePads = MutableStateFlow<List<PerformancePad>>(emptyList())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.beatProjectDao(), db.studioPresetDao())

        val flowProjects = MutableStateFlow<List<BeatProject>>(emptyList())
        projects = flowProjects
        viewModelScope.launch {
            repository.allProjects.collect { flowProjects.value = it }
        }

        val flowPresets = MutableStateFlow<List<StudioPreset>>(emptyList())
        presets = flowPresets
        viewModelScope.launch {
            repository.allPresets.collect { flowPresets.value = it }
        }

        // Initialize Factory Presets if DB is fresh
        viewModelScope.launch {
            if (repository.getPresetCount() == 0) {
                val factoryPresets = PresetSerializer.getFactoryPresets()
                repository.savePresets(factoryPresets)
            }
        }

        initPerformancePads()
    }

    private fun initPerformancePads() {
        val pads = listOf(
            PerformancePad(1, "Kick Punch", 36, "Single", 0xFF00E5FF, TrackType.KICK),
            PerformancePad(2, "Snare Snap", 38, "Single", 0xFFFF007F, TrackType.SNARE),
            PerformancePad(3, "Hat Closed", 42, "Single", 0xFFFFD700, TrackType.HIHAT),
            PerformancePad(4, "Hat Open", 46, "Single", 0xFFFFD700, TrackType.HIHAT),
            PerformancePad(5, "Sub Bass C1", 36, "Single", 0xFFA855F7, TrackType.BASS),
            PerformancePad(6, "Bass Drop E1", 40, "Single", 0xFFA855F7, TrackType.BASS),
            PerformancePad(7, "Synth Stab C4", 60, "Major", 0xFFFF6D00, TrackType.SYNTH),
            PerformancePad(8, "Synth Stab F4", 65, "Minor", 0xFFFF6D00, TrackType.SYNTH),
            PerformancePad(9, "Chord Pad Cm", 60, "Minor", 0xFF3B82F6, TrackType.PADS),
            PerformancePad(10, "Chord Pad Fm", 65, "Minor", 0xFF3B82F6, TrackType.PADS),
            PerformancePad(11, "Vocal Beat Puh", 60, "Single", 0xFFEC4899, TrackType.BEATBOX_VOICE),
            PerformancePad(12, "Vocal Beat Tss", 62, "Single", 0xFFEC4899, TrackType.BEATBOX_VOICE)
        )
        performancePads.value = pads
    }

    fun selectTab(tab: StudioTab) {
        _activeTab.value = tab
    }

    fun toggleStep(trackIndex: Int, stepIndex: Int) {
        val currentTracks = audioEngine.tracks.value.toMutableList()
        if (trackIndex in currentTracks.indices) {
            val track = currentTracks[trackIndex]
            val newSteps = track.steps.toMutableList()
            if (stepIndex in newSteps.indices) {
                newSteps[stepIndex] = newSteps[stepIndex].copy(active = !newSteps[stepIndex].active)
                currentTracks[trackIndex] = track.copy(steps = newSteps)
                audioEngine.tracks.value = currentTracks
            }
        }
    }

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun updateStepGate(trackIndex: Int, stepIndex: Int, gateLength: Float) {
        val currentTracks = audioEngine.tracks.value.toMutableList()
        if (trackIndex in currentTracks.indices) {
            val track = currentTracks[trackIndex]
            val newSteps = track.steps.toMutableList()
            if (stepIndex in newSteps.indices) {
                newSteps[stepIndex] = newSteps[stepIndex].copy(gateLength = gateLength.coerceIn(0.1f, 2.0f))
                currentTracks[trackIndex] = track.copy(steps = newSteps)
                audioEngine.tracks.value = currentTracks
            }
        }
    }

    fun updateStepVelocity(trackIndex: Int, stepIndex: Int, velocity: Float) {
        val currentTracks = audioEngine.tracks.value.toMutableList()
        if (trackIndex in currentTracks.indices) {
            val track = currentTracks[trackIndex]
            val newSteps = track.steps.toMutableList()
            if (stepIndex in newSteps.indices) {
                newSteps[stepIndex] = newSteps[stepIndex].copy(velocity = velocity.coerceIn(0.0f, 1.0f))
                currentTracks[trackIndex] = track.copy(steps = newSteps)
                audioEngine.tracks.value = currentTracks
            }
        }
    }

    fun updateStepProbability(trackIndex: Int, stepIndex: Int, probability: Float) {
        val currentTracks = audioEngine.tracks.value.toMutableList()
        if (trackIndex in currentTracks.indices) {
            val track = currentTracks[trackIndex]
            val newSteps = track.steps.toMutableList()
            if (stepIndex in newSteps.indices) {
                newSteps[stepIndex] = newSteps[stepIndex].copy(probability = probability.coerceIn(0.0f, 1.0f))
                currentTracks[trackIndex] = track.copy(steps = newSteps)
                audioEngine.tracks.value = currentTracks
            }
        }
    }

    fun updatePadChord(padIndex: Int, chordType: String) {
        val currentPads = performancePads.value.toMutableList()
        if (padIndex in currentPads.indices) {
            currentPads[padIndex] = currentPads[padIndex].copy(chordType = chordType)
            performancePads.value = currentPads
        }
    }


    fun startBeatboxRecording() {
        audioEngine.beatboxRecorder.startRecording(viewModelScope) { chunk, rms ->
            // Audio chunk callback
        }
    }

    fun stopBeatboxRecordingAndConvert() {
        val pcmAudio = audioEngine.beatboxRecorder.stopRecording()
        val detectedMap = audioEngine.beatboxRecorder.analyzeBeatboxToTracks(
            pcmAudio = pcmAudio,
            bpm = audioEngine.bpm.value,
            stepsCount = audioEngine.stepsCount.value
        )

        // Apply detected pattern to tracks
        val currentTracks = audioEngine.tracks.value.toMutableList()
        for (i in currentTracks.indices) {
            val t = currentTracks[i]
            val detectedSteps = detectedMap[t.type]
            if (detectedSteps != null) {
                val newStepList = t.steps.toMutableList()
                for (s in 0 until audioEngine.stepsCount.value) {
                    if (s < detectedSteps.size) {
                        newStepList[s] = newStepList[s].copy(active = detectedSteps[s])
                    }
                }
                currentTracks[i] = t.copy(steps = newStepList)
            }
        }
        audioEngine.tracks.value = currentTracks
        _aiStatusMessage.value = "Voice Beatbox converted to tracks!"
    }

    fun generateAiBeat(userPrompt: String) {
        _isAiGenerating.value = true
        _aiStatusMessage.value = "AI composing beat from prompt..."
        viewModelScope.launch {
            val aiResponse = geminiService.generateBeatFromPrompt(userPrompt)
            applyAiResponseToSession(aiResponse)
            _isAiGenerating.value = false
            _aiStatusMessage.value = "AI Generated: ${aiResponse.title} (${aiResponse.genre})"
        }
    }

    private fun applyAiResponseToSession(response: AiBeatResponse) {
        audioEngine.setBpm(response.bpm)
        _currentProjectName.value = response.title

        val currentTracks = audioEngine.tracks.value.toMutableList()
        for (aiTrack in response.tracks) {
            val matchedIndex = currentTracks.indexOfFirst { it.type.name.equals(aiTrack.trackType, ignoreCase = true) }
            if (matchedIndex >= 0) {
                val existingTrack = currentTracks[matchedIndex]
                val newSteps = existingTrack.steps.toMutableList()
                for (s in 0 until 16) {
                    if (s < aiTrack.activeSteps.size) {
                        newSteps[s] = newSteps[s].copy(active = aiTrack.activeSteps[s])
                    }
                }
                currentTracks[matchedIndex] = existingTrack.copy(
                    steps = newSteps,
                    volume = aiTrack.volume
                )
            }
        }
        audioEngine.tracks.value = currentTracks
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val project = BeatProject(
                title = _currentProjectName.value,
                bpm = audioEngine.bpm.value,
                swing = audioEngine.swing.value,
                stepsCount = audioEngine.stepsCount.value
            )
            repository.saveProject(project)
            _aiStatusMessage.value = "Project saved to Room database!"
        }
    }

    fun exportStems() {
        viewModelScope.launch {
            val cacheDir = getApplication<Application>().cacheDir
            val stemsDir = File(cacheDir, "bounced_stems")
            if (!stemsDir.exists()) stemsDir.mkdirs()

            val files = audioEngine.bounceStemsToWav(stemsDir, _currentProjectName.value)
            _aiStatusMessage.value = "Bounced ${files.size} WAV stems to storage!"
        }
    }

    fun clearStatusMessage() {
        _aiStatusMessage.value = null
    }

    // Preset Management Functions
    fun openPresetDialog() {
        _showPresetDialog.value = true
    }

    fun closePresetDialog() {
        _showPresetDialog.value = false
    }

    fun loadPreset(preset: StudioPreset) {
        if (preset.synthConfigJson.isNotBlank()) {
            val synthP = PresetSerializer.jsonToSynthParams(preset.synthConfigJson)
            audioEngine.synthParams.value = synthP
        }
        if (preset.mixerConfigJson.isNotBlank()) {
            val mixerP = PresetSerializer.jsonToMixerFxParams(preset.mixerConfigJson)
            audioEngine.mixerFxParams.value = mixerP
        }
        if (preset.bpm in 40..240) {
            audioEngine.setBpm(preset.bpm)
        }
        if (preset.swing in 0f..0.5f) {
            audioEngine.setSwing(preset.swing)
        }

        _activePresetName.value = preset.name
        _aiStatusMessage.value = "Loaded Preset: '${preset.name}'"
    }

    fun saveCustomPreset(
        name: String,
        category: String,
        description: String
    ) {
        if (name.isBlank()) return
        val currentSynth = audioEngine.synthParams.value
        val currentMixer = audioEngine.mixerFxParams.value

        val newPreset = StudioPreset(
            name = name.trim(),
            category = if (category.isBlank()) "Custom Preset" else category.trim(),
            description = description.trim(),
            isBuiltIn = false,
            synthConfigJson = PresetSerializer.synthParamsToJson(currentSynth),
            mixerConfigJson = PresetSerializer.mixerFxParamsToJson(currentMixer),
            bpm = audioEngine.bpm.value,
            swing = audioEngine.swing.value
        )

        viewModelScope.launch {
            repository.savePreset(newPreset)
            _activePresetName.value = name.trim()
            _aiStatusMessage.value = "Saved Preset: '$name'"
        }
    }

    fun deletePreset(presetId: Long) {
        viewModelScope.launch {
            repository.deletePreset(presetId)
            _aiStatusMessage.value = "Preset deleted."
        }
    }

    fun importPresetFromJson(jsonStr: String): Boolean {
        val imported = PresetSerializer.importPresetFromJson(jsonStr)
        if (imported != null) {
            viewModelScope.launch {
                repository.savePreset(imported)
                loadPreset(imported)
                _aiStatusMessage.value = "Imported & Loaded: '${imported.name}'"
            }
            return true
        }
        return false
    }

    fun exportPresetToJson(preset: StudioPreset): String {
        return PresetSerializer.exportPresetToJson(preset)
    }
}
