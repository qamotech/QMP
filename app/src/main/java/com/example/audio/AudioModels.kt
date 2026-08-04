package com.example.audio

enum class TrackType(val displayName: String, val defaultColor: Long) {
    KICK("Kick Drum", 0xFF00E5FF),
    SNARE("Snare / Rim", 0xFFFF007F),
    HIHAT("Hi-Hat", 0xFFFFD700),
    PERC("Percussion", 0xFF00E676),
    BASS("Sub Bass", 0xFFA855F7),
    SYNTH("Lead Synth", 0xFFFF6D00),
    PADS("Chord Pads", 0xFF3B82F6),
    BEATBOX_VOICE("Beatbox Voice", 0xFFEC4899)
}

data class StepNote(
    var active: Boolean = false,
    var velocity: Float = 0.8f,
    var pitchOffset: Int = 0, // -12 to +12 semitones
    var probability: Float = 1.0f, // 0.0 to 1.0 probability
    var gateLength: Float = 1.0f, // 0.1 to 2.0 beat steps gate
    var microShift: Float = 0.0f  // -0.5 to +0.5 tick swing microtiming
)

data class SequencerTrack(
    val id: Int,
    var name: String,
    val type: TrackType,
    val steps: MutableList<StepNote> = MutableList(32) { StepNote() },
    var volume: Float = 0.8f,
    var pan: Float = 0.0f,
    var muted: Boolean = false,
    var soloed: Boolean = false,
    val colorHex: Long = type.defaultColor
)

enum class PatternBank(val label: String, val badgeColorHex: Long) {
    PATTERN_A("Pattern A", 0xFFFF6E00),
    PATTERN_B("Pattern B", 0xFF00E5FF),
    PATTERN_C("Pattern C", 0xFF00E676),
    PATTERN_D("Pattern D", 0xFFA855F7)
}

data class SynthParams(
    var attack: Float = 0.02f,    // seconds
    var decay: Float = 0.25f,     // seconds
    var sustain: Float = 0.6f,    // 0.0 - 1.0
    var release: Float = 0.4f,    // seconds
    var lfoRate: Float = 2.0f,    // Hz
    var lfoShape: String = "Sine", // Sine, Triangle, Saw, Square, Random
    var lfoTarget: String = "Filter Cutoff", // Filter Cutoff, Pitch, Amplitude
    var pitchScale: String = "Minor", // Chromatic, Minor, Major, Pentatonic, Dorian, Japanese
    var microTuningCents: MutableList<Float> = MutableList(12) { 0f },
    var formantVowel: String = "Off", // Off, A (Ah), E (Eh), I (Ee), O (Oh), U (Oo)
    var cutoff: Float = 4000f,    // Hz
    var resonance: Float = 1.5f
)

data class MixerFxParams(
    var eqLow: Float = 0f,       // dB
    var eqLowMid: Float = 0f,
    var eqMid: Float = 0f,
    var eqHighMid: Float = 0f,
    var eqHigh: Float = 0f,
    var panner3dX: Float = 0f,   // -1.0 to 1.0
    var panner3dY: Float = 0f,   // -1.0 to 1.0
    var reverbWet: Float = 0.2f,
    var reverbSize: Float = 0.5f,
    var delayWet: Float = 0.15f,
    var delayTime: Float = 0.25f,// seconds
    var delayFeedback: Float = 0.35f,
    var saturatorDrive: Float = 0.1f,
    var bitcrusherDepth: Int = 16, // 2 to 16
    var sidechainDucking: Float = 0.25f,
    var masterLimiterThreshold: Float = -0.5f,
    var stereoWidth: Float = 1.0f,
    var harmonicExciter: Float = 0.2f,
    var transientPunch: Float = 0.3f
)

data class PerformancePad(
    val id: Int,
    val name: String,
    val note: Int, // MIDI Note
    var chordType: String = "Single", // Single, Major, Minor, 7th, Sus2, Stab
    val colorHex: Long,
    val trackType: TrackType
)

