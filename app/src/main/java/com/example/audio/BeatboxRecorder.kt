package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

class BeatboxRecorder {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordJob: Job? = null

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(2048)

    private val _recordingState = MutableStateFlow(false)
    val recordingState: StateFlow<Boolean> = _recordingState

    private val _currentRms = MutableStateFlow(0f)
    val currentRms: StateFlow<Float> = _currentRms

    private val recordedBuffer = mutableListOf<Short>()

    @SuppressLint("MissingPermission")
    fun startRecording(scope: CoroutineScope, onAudioChunk: (ShortArray, Float) -> Unit) {
        if (isRecording) return
        recordedBuffer.clear()

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioRecord?.startRecording()
            isRecording = true
            _recordingState.value = true

            recordJob = scope.launch(Dispatchers.IO) {
                val tempBuffer = ShortArray(1024)
                while (isRecording) {
                    val readSamples = audioRecord?.read(tempBuffer, 0, tempBuffer.size) ?: 0
                    if (readSamples > 0) {
                        var sumSq = 0.0
                        for (i in 0 until readSamples) {
                            val sample = tempBuffer[i]
                            recordedBuffer.add(sample)
                            sumSq += (sample * sample).toDouble()
                        }
                        val rms = sqrt(sumSq / readSamples).toFloat() / 32768f
                        _currentRms.value = rms
                        onAudioChunk(tempBuffer.copyOf(readSamples), rms)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            _recordingState.value = false
        }
    }

    fun stopRecording(): ShortArray {
        isRecording = false
        _recordingState.value = false
        recordJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
        return recordedBuffer.toShortArray()
    }

    /**
     * Auto-transcribe beatboxing audio into Kick, Snare, Hi-Hat, and Vocal Bass step pattern.
     */
    fun analyzeBeatboxToTracks(
        pcmAudio: ShortArray,
        bpm: Int,
        stepsCount: Int = 16
    ): Map<TrackType, List<Boolean>> {
        val kickSteps = MutableList(stepsCount) { false }
        val snareSteps = MutableList(stepsCount) { false }
        val hihatSteps = MutableList(stepsCount) { false }
        val bassSteps = MutableList(stepsCount) { false }

        if (pcmAudio.isEmpty()) return mapOf(
            TrackType.KICK to kickSteps,
            TrackType.SNARE to snareSteps,
            TrackType.HIHAT to hihatSteps,
            TrackType.BASS to bassSteps
        )

        // Calculate step duration in samples
        val beatSec = 60.0 / bpm
        val stepSec = beatSec / 4.0 // 16th note step duration
        val samplesPerStep = (stepSec * sampleRate).toInt().coerceAtLeast(100)

        // Window each step and analyze energy / frequency content
        for (step in 0 until stepsCount) {
            val startIdx = step * samplesPerStep
            val endIdx = ((step + 1) * samplesPerStep).coerceAtMost(pcmAudio.size)
            if (startIdx >= pcmAudio.size) break

            var lowEnergy = 0.0
            var midEnergy = 0.0
            var highEnergy = 0.0
            var zeroCrossings = 0

            var prevSample = 0
            for (i in startIdx until endIdx) {
                val sample = pcmAudio[i].toInt()
                val absVal = abs(sample).toDouble()

                // Simple band approximation via sample diffs and zero crossings
                if ((sample >= 0 && prevSample < 0) || (sample < 0 && prevSample >= 0)) {
                    zeroCrossings++
                }

                val diff = abs(sample - prevSample)
                if (diff > 8000) {
                    highEnergy += absVal
                } else if (diff in 2000..8000) {
                    midEnergy += absVal
                } else {
                    lowEnergy += absVal
                }
                prevSample = sample
            }

            val totalSamples = (endIdx - startIdx).coerceAtLeast(1)
            val avgLow = lowEnergy / totalSamples
            val avgMid = midEnergy / totalSamples
            val avgHigh = highEnergy / totalSamples
            val zcr = zeroCrossings.toFloat() / totalSamples

            // Threshold classification for beatbox sounds:
            // "Boom / Puh" (Kick) -> High low-freq energy, lower ZCR
            // "Kah / Ts" (Snare / Rim) -> High mid/high energy & mid ZCR
            // "Tss / Ssh" (Hi-Hat) -> Very high ZCR & high freq energy
            // "Hum / Bass" -> Sustained low freq, low ZCR
            if (avgLow > 2500 && zcr < 0.12) {
                kickSteps[step] = true
            } else if (avgHigh > 3000 || zcr > 0.22) {
                hihatSteps[step] = true
            } else if (avgMid > 2000 || (avgLow > 1500 && zcr in 0.12..0.22)) {
                snareSteps[step] = true
            }

            if (avgLow > 1800 && !kickSteps[step]) {
                bassSteps[step] = true
            }
        }

        // Fallback default rhythm if recording was silent/very quiet
        val totalActive = kickSteps.count { it } + snareSteps.count { it } + hihatSteps.count { it }
        if (totalActive == 0) {
            for (s in 0 until stepsCount) {
                if (s % 4 == 0) kickSteps[s] = true
                if (s % 4 == 2) snareSteps[s] = true
                if (s % 2 == 1) hihatSteps[s] = true
            }
        }

        return mapOf(
            TrackType.KICK to kickSteps,
            TrackType.SNARE to snareSteps,
            TrackType.HIHAT to hihatSteps,
            TrackType.BASS to bassSteps
        )
    }
}
