package com.example.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

class SynthEngine {
    companion object {
        const val SAMPLE_RATE = 44100

        // Scale note offsets from root note (0 = C)
        val SCALE_MAP = mapOf(
            "Chromatic" to intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
            "Minor" to intArrayOf(0, 2, 3, 5, 7, 8, 10),
            "Major" to intArrayOf(0, 2, 4, 5, 7, 9, 11),
            "Pentatonic" to intArrayOf(0, 3, 5, 7, 10),
            "Dorian" to intArrayOf(0, 2, 3, 5, 7, 9, 10),
            "Japanese" to intArrayOf(0, 1, 5, 7, 8),
            "Blues" to intArrayOf(0, 3, 5, 6, 7, 10)
        )

        fun midiToFreq(midiNote: Int, microCents: Float = 0f): Float {
            val noteWithCents = midiNote + (microCents / 100f)
            return (440.0 * 2.0.pow((noteWithCents - 69.0) / 12.0)).toFloat()
        }

        fun quantizeToScale(midiNote: Int, scaleName: String, rootNote: Int = 60): Int {
            val scale = SCALE_MAP[scaleName] ?: SCALE_MAP["Minor"]!!
            val relativeNote = (midiNote - rootNote) % 12
            val octave = (midiNote - rootNote) / 12
            val normalizedRelative = if (relativeNote < 0) relativeNote + 12 else relativeNote

            var nearest = scale[0]
            var minDiff = abs(normalizedRelative - scale[0])
            for (s in scale) {
                val diff = abs(normalizedRelative - s)
                if (diff < minDiff) {
                    minDiff = diff
                    nearest = s
                }
            }
            return rootNote + (octave * 12) + nearest
        }
    }

    /**
     * ADSR Envelope Generator
     */
    fun calculateAdsrEnvelope(
        timeSec: Float,
        durationSec: Float,
        attack: Float,
        decay: Float,
        sustain: Float,
        release: Float
    ): Float {
        if (timeSec < 0f) return 0f
        return if (timeSec <= durationSec) {
            // Note is held
            if (timeSec < attack) {
                (timeSec / attack.coerceAtLeast(0.001f)).coerceIn(0f, 1f)
            } else if (timeSec < attack + decay) {
                val tDecay = (timeSec - attack) / decay.coerceAtLeast(0.001f)
                1.0f - (1.0f - sustain) * tDecay
            } else {
                sustain
            }
        } else {
            // Release phase
            val relTime = timeSec - durationSec
            if (relTime >= release) {
                0f
            } else {
                sustain * (1.0f - relTime / release.coerceAtLeast(0.001f)).coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Synthesize a buffer of sound for a specific oscillator type & parameters
     */
    fun renderSynthBuffer(
        freqHz: Float,
        numSamples: Int,
        params: SynthParams,
        type: TrackType,
        timeOffsetSec: Float = 0f,
        lfoPhase: Float = 0f
    ): FloatArray {
        val buffer = FloatArray(numSamples)
        val phaseIncrement = (2.0 * PI * freqHz / SAMPLE_RATE).toFloat()
        var currentLfoPhase = lfoPhase

        val lfoInc = (2.0 * PI * params.lfoRate / SAMPLE_RATE).toFloat()

        for (i in 0 until numSamples) {
            val tSec = timeOffsetSec + (i.toFloat() / SAMPLE_RATE)

            // LFO Calculation
            currentLfoPhase = (currentLfoPhase + lfoInc) % (2f * PI.toFloat())
            val lfoVal = when (params.lfoShape) {
                "Triangle" -> (abs(currentLfoPhase / PI.toFloat() - 1f) * 2f - 1f)
                "Square" -> if (currentLfoPhase < PI.toFloat()) 1f else -1f
                "Saw" -> (currentLfoPhase / (2f * PI.toFloat()) * 2f - 1f)
                "Random" -> Random.nextFloat() * 2f - 1f
                else -> sin(currentLfoPhase) // Sine
            }

            // Target Modulation
            var modFreq = freqHz
            var modCutoff = params.cutoff
            if (params.lfoTarget == "Pitch") {
                modFreq *= (1f + 0.1f * lfoVal)
            } else if (params.lfoTarget == "Filter Cutoff") {
                modCutoff = (modCutoff + lfoVal * 1000f).coerceIn(100f, 18000f)
            }

            // Raw Oscillator Waveform
            val curPhaseInc = (2.0 * PI * modFreq / SAMPLE_RATE).toFloat()
            val phase = (curPhaseInc * i) % (2f * PI.toFloat())

            var sample = when (type) {
                TrackType.KICK -> {
                    // Premastered Punchy Kick: Pitch drop sine + Sub harmonic + Transient click + Soft tape drive
                    val pitchEnv = exp(-tSec * 35.0).toFloat()
                    val kickFreq = 48.0f + 190.0f * pitchEnv
                    val sine = sin(2.0 * PI * kickFreq * tSec).toFloat()
                    val subHarmonic = sin(2.0 * PI * (kickFreq * 0.5f) * tSec).toFloat() * 0.35f
                    val clickTransient = (Random.nextFloat() * 2f - 1f) * exp(-tSec * 160.0).toFloat() * 0.45f
                    val rawKick = (sine + subHarmonic + clickTransient) * exp(-tSec * 9.5).toFloat()
                    // Studio Soft Clipper for premastered density & sub-bass punch
                    (rawKick * 1.45f).coerceIn(-0.95f, 0.95f)
                }
                TrackType.SNARE -> {
                    // Premastered Snare/Clap: Tone body + Crisp noise tail + Transient slap + Tape saturation
                    val toneEnv = exp(-tSec * 22.0).toFloat()
                    val tone = sin(2.0 * PI * 185.0 * tSec).toFloat() * toneEnv
                    val noiseEnv = exp(-tSec * 14.0).toFloat()
                    val noise = (Random.nextFloat() * 2f - 1f) * noiseEnv
                    val snapTransient = (Random.nextFloat() * 2f - 1f) * exp(-tSec * 200.0).toFloat() * 0.5f
                    val rawSnare = tone * 0.5f + noise * 0.75f + snapTransient
                    (rawSnare * 1.35f).coerceIn(-0.95f, 0.95f)
                }
                TrackType.HIHAT -> {
                    // Premastered Sizzle Hi-Hat: Metallic ring harmonics + Bandpass noise burst
                    val noise = Random.nextFloat() * 2f - 1f
                    val ring = (sin(2.0 * PI * 7800.0 * tSec) + sin(2.0 * PI * 9400.0 * tSec) * 0.5f).toFloat()
                    val env = exp(-tSec * 42.0).toFloat()
                    ((noise * 0.65f + ring * 0.35f) * env * 1.25f).coerceIn(-0.95f, 0.95f)
                }
                TrackType.PERC -> {
                    // Premastered Rim / Clap / Percussion
                    val env = exp(-tSec * 25.0).toFloat()
                    val clapTransient = if (tSec < 0.025f) ((sin(2.0 * PI * 900.0 * tSec).toFloat() + Random.nextFloat()) * 0.3f) else 0f
                    val tone = sin(2.0 * PI * 850.0 * tSec).toFloat() * env
                    ((tone * 0.55f + clapTransient * 0.65f) * 1.2f).coerceIn(-0.95f, 0.95f)
                }
                TrackType.BASS -> {
                    // Sub Bass Saw / Square + Sine sub
                    val sub = sin(phase)
                    val saw = (phase / PI.toFloat()) - 1f
                    sub * 0.6f + saw * 0.4f
                }
                TrackType.SYNTH -> {
                    // Dual Saw / Square Lead
                    val saw = (phase / PI.toFloat()) - 1f
                    val sqr = if (phase < PI.toFloat()) 1f else -1f
                    saw * 0.5f + sqr * 0.5f
                }
                TrackType.PADS -> {
                    // Warm Sine + Triangle Chorus Pad
                    val tri = abs(phase / PI.toFloat() - 1f) * 2f - 1f
                    val sine = sin(phase)
                    sine * 0.6f + tri * 0.4f
                }
                TrackType.BEATBOX_VOICE -> {
                    // Formant Vowel Synth
                    applyFormantVowel(phase, params.formantVowel)
                }
            }

            // ADSR Envelope
            val env = calculateAdsrEnvelope(tSec, 0.3f, params.attack, params.decay, params.sustain, params.release)
            sample *= env

            // Simple Lowpass Filter
            sample = applyLowpass(sample, modCutoff, params.resonance)

            buffer[i] = sample.coerceIn(-1.0f, 1.0f)
        }
        return buffer
    }

    private var filterPrev = 0f
    private fun applyLowpass(input: Float, cutoff: Float, resonance: Float): Float {
        val rc = 1.0f / (2.0f * PI.toFloat() * cutoff.coerceIn(50f, 18000f))
        val dt = 1.0f / SAMPLE_RATE
        val alpha = dt / (rc + dt)
        filterPrev += alpha * (input - filterPrev)
        return filterPrev
    }

    private fun applyFormantVowel(phase: Float, vowel: String): Float {
        val rawSine = sin(phase)
        return when (vowel) {
            "A (Ah)" -> (rawSine + sin(phase * 1.5f) * 0.5f + sin(phase * 2.2f) * 0.3f) * 0.7f
            "E (Eh)" -> (rawSine + sin(phase * 2.0f) * 0.6f + sin(phase * 3.5f) * 0.2f) * 0.7f
            "I (Ee)" -> (rawSine + sin(phase * 3.2f) * 0.8f + sin(phase * 4.5f) * 0.4f) * 0.6f
            "O (Oh)" -> (rawSine + sin(phase * 1.2f) * 0.7f + sin(phase * 1.8f) * 0.2f) * 0.7f
            "U (Oo)" -> (rawSine * 0.9f + sin(phase * 2.8f) * 0.2f) * 0.8f
            else -> rawSine
        }
    }
}
