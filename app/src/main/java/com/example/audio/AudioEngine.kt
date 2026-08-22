package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine(private val scope: CoroutineScope) {
    private var audioTrack: AudioTrack? = null
    private var audioThreadJob: Job? = null
    @Volatile private var isPlaying = false

    val synthEngine = SynthEngine()
    val effectsProcessor = EffectsProcessor()
    val djScratchEngine = DJScratchEngine()
    val visualizerEngine = VisualizerEngine()
    val beatboxRecorder = BeatboxRecorder()

    // Sequencer States
    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm

    private val _swing = MutableStateFlow(0.0f) // 0.0 to 0.5
    val swing: StateFlow<Float> = _swing

    private val _stepsCount = MutableStateFlow(16) // 16 or 32 steps
    val stepsCount: StateFlow<Int> = _stepsCount

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    private val _polyrhythmRatio = MutableStateFlow("4/4") // 4/4, 3/4, 5/4, 7/8
    val polyrhythmRatio: StateFlow<String> = _polyrhythmRatio

    private val _masterPitchBend = MutableStateFlow(0f) // -12f to +12f semitones
    val masterPitchBend: StateFlow<Float> = _masterPitchBend

    private val _activePattern = MutableStateFlow(PatternBank.PATTERN_A)
    val activePattern: StateFlow<PatternBank> = _activePattern

    private val patternStorage = mutableMapOf<PatternBank, List<List<StepNote>>>()

    val tracks = MutableStateFlow<List<SequencerTrack>>(emptyList())
    val synthParams = MutableStateFlow(SynthParams())
    val mixerFxParams = MutableStateFlow(MixerFxParams())

    // Tap tempo timestamp tracking
    private var lastTapTimeMs = 0L
    private val tapIntervals = mutableListOf<Long>()

    // Trigger queue for playable performance pads
    private val padTriggerQueue = mutableListOf<PerformancePad>()

    init {
        initDefaultTracks()
    }

    fun setMasterPitchBend(semitones: Float) {
        _masterPitchBend.value = semitones.coerceIn(-12f, 12f)
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        if (lastTapTimeMs > 0 && (now - lastTapTimeMs) < 2000) {
            val interval = now - lastTapTimeMs
            tapIntervals.add(interval)
            if (tapIntervals.size > 4) tapIntervals.removeAt(0)
            val avgInterval = tapIntervals.average()
            if (avgInterval > 0) {
                val calculatedBpm = (60000.0 / avgInterval).toInt().coerceIn(40, 240)
                _bpm.value = calculatedBpm
            }
        } else {
            tapIntervals.clear()
        }
        lastTapTimeMs = now
    }

    fun selectPattern(bank: PatternBank) {
        if (_activePattern.value == bank) return
        patternStorage[_activePattern.value] = tracks.value.map { tr -> tr.steps.map { it.copy() } }
        
        _activePattern.value = bank
        
        val saved = patternStorage[bank]
        if (saved != null && saved.size == tracks.value.size) {
            val current = tracks.value
            for (i in current.indices) {
                val savedSteps = saved[i]
                for (s in current[i].steps.indices) {
                    if (s < savedSteps.size) {
                        current[i].steps[s] = savedSteps[s].copy()
                    }
                }
            }
            tracks.value = current.toList()
        } else {
            val current = tracks.value
            for (tr in current) {
                for (st in tr.steps) {
                    st.active = false
                }
            }
            if (bank == PatternBank.PATTERN_B) {
                current[0].steps[0].active = true
                current[0].steps[4].active = true
                current[0].steps[8].active = true
                current[0].steps[12].active = true
                current[2].steps[2].active = true
                current[2].steps[6].active = true
                current[2].steps[10].active = true
                current[2].steps[14].active = true
            } else if (bank == PatternBank.PATTERN_C) {
                current[1].steps[4].active = true
                current[1].steps[12].active = true
                current[3].steps[0].active = true
                current[3].steps[3].active = true
                current[3].steps[6].active = true
                current[3].steps[9].active = true
            }
            tracks.value = current.toList()
        }
    }


    private fun initDefaultTracks() {
        val defaultList = listOf(
            SequencerTrack(id = 1, name = "Kick", type = TrackType.KICK),
            SequencerTrack(id = 2, name = "Snare", type = TrackType.SNARE),
            SequencerTrack(id = 3, name = "Hi-Hat", type = TrackType.HIHAT),
            SequencerTrack(id = 4, name = "Perc", type = TrackType.PERC),
            SequencerTrack(id = 5, name = "Sub Bass", type = TrackType.BASS),
            SequencerTrack(id = 6, name = "Lead Synth", type = TrackType.SYNTH),
            SequencerTrack(id = 7, name = "Chord Pads", type = TrackType.PADS),
            SequencerTrack(id = 8, name = "Beatbox Voice", type = TrackType.BEATBOX_VOICE)
        )

        // Pre-populate a standard groovy beat
        defaultList[0].steps[0].active = true
        defaultList[0].steps[4].active = true
        defaultList[0].steps[8].active = true
        defaultList[0].steps[12].active = true

        defaultList[1].steps[4].active = true
        defaultList[1].steps[12].active = true

        for (s in 0 until 16 step 2) {
            defaultList[2].steps[s].active = true
        }
        defaultList[2].steps[14].active = true

        defaultList[4].steps[0].active = true
        defaultList[4].steps[6].active = true
        defaultList[4].steps[10].active = true

        tracks.value = defaultList
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(40, 240)
    }

    fun setSwing(newSwing: Float) {
        _swing.value = newSwing.coerceIn(0f, 0.5f)
    }

    fun setStepsCount(count: Int) {
        _stepsCount.value = if (count >= 32) 32 else 16
    }

    fun setPolyrhythm(ratio: String) {
        _polyrhythmRatio.value = ratio
    }

    fun triggerPad(pad: PerformancePad) {
        synchronized(padTriggerQueue) {
            padTriggerQueue.add(pad)
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        if (isPlaying) return
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        audioThreadJob = scope.launch(Dispatchers.IO) {
            val chunkSize = 1024 // 1024 samples per chunk (~23ms)
            val leftPcm = FloatArray(chunkSize)
            val rightPcm = FloatArray(chunkSize)
            val outputShorts = ShortArray(chunkSize * 2)

            var stepSampleCounter = 0
            var activeStep = 0

            while (isPlaying) {
                // Calculate samples per 16th step at current BPM
                val bpmVal = _bpm.value
                val beatSec = 60.0 / bpmVal
                val baseStepSamples = ((beatSec / 4.0) * 44100).toInt()

                // Apply Swing offset to odd steps
                val currentSwing = if (activeStep % 2 != 0) _swing.value else 0f
                val stepSamples = (baseStepSamples * (1.0 + currentSwing)).toInt()

                djScratchEngine.updateFramePhysics()
                val scratchMultiplier = djScratchEngine.scratchSpeed.value

                // Clear chunk buffers
                leftPcm.fill(0f)
                rightPcm.fill(0f)

                // Render Sequencer & Pads synth samples
                val currentTrackList = tracks.value
                val currentSynthP = synthParams.value
                val currentFxP = mixerFxParams.value

                var isKickStep = false

                for (t in currentTrackList) {
                    if (t.muted) continue

                    val stepObj = t.steps[activeStep % t.steps.size]
                    if (stepObj.active) {
                        if (t.type == TrackType.KICK) isKickStep = true

                        val midiNote = 60 + stepObj.pitchOffset
                        val freq = SynthEngine.midiToFreq(midiNote)

                        val rendered = synthEngine.renderSynthBuffer(
                            freqHz = freq,
                            numSamples = chunkSize,
                            params = currentSynthP,
                            type = t.type
                        )

                        val vol = t.volume * stepObj.velocity
                        val panL = (1.0f - t.pan.coerceIn(0f, 1f))
                        val panR = (1.0f + t.pan.coerceIn(-1f, 0f))

                        for (i in 0 until chunkSize) {
                            val sampleVal = rendered[i] * vol
                            leftPcm[i] += sampleVal * panL
                            rightPcm[i] += sampleVal * panR
                        }
                    }
                }

                // Render live pad triggers
                synchronized(padTriggerQueue) {
                    val padIter = padTriggerQueue.iterator()
                    while (padIter.hasNext()) {
                        val pad = padIter.next()
                        val freq = SynthEngine.midiToFreq(pad.note)
                        val padBuffer = synthEngine.renderSynthBuffer(
                            freqHz = freq,
                            numSamples = chunkSize,
                            params = currentSynthP,
                            type = pad.trackType
                        )
                        for (i in 0 until chunkSize) {
                            leftPcm[i] += padBuffer[i] * 0.8f
                            rightPcm[i] += padBuffer[i] * 0.8f
                        }
                        padIter.remove()
                    }
                }

                // Process FX Chain & DJ Scratch Pitch Shift
                effectsProcessor.processFxChain(leftPcm, rightPcm, currentFxP, isKickStep)

                // Update 60FPS Visualizer Engine
                visualizerEngine.updateVisualsFromBuffer(leftPcm, rightPcm)

                // Convert float to 16-bit stereo PCM with Scratch pitch warping
                for (i in 0 until chunkSize) {
                    val sampleL = (leftPcm[i] * scratchMultiplier).coerceIn(-1.0f, 1.0f)
                    val sampleR = (rightPcm[i] * scratchMultiplier).coerceIn(-1.0f, 1.0f)
                    outputShorts[i * 2] = (sampleL * 32767f).toInt().toShort()
                    outputShorts[i * 2 + 1] = (sampleR * 32767f).toInt().toShort()
                }

                audioTrack?.write(outputShorts, 0, outputShorts.size)

                // Advance sequencer step counter
                stepSampleCounter += chunkSize
                if (stepSampleCounter >= stepSamples) {
                    stepSampleCounter = 0
                    activeStep = (activeStep + 1) % _stepsCount.value
                    _currentStep.value = activeStep
                }
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        audioThreadJob?.cancel()
        audioThreadJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
        _currentStep.value = 0
    }

    /**
     * Export / Bounce 32-bit Float or 16-bit PCM WAV Stems
     */
    fun bounceStemsToWav(
        outputDir: File,
        projectName: String
    ): List<File> {
        val exportedFiles = mutableListOf<File>()
        val currentTrackList = tracks.value
        val totalSteps = _stepsCount.value
        val samplesPerStep = ((60.0 / _bpm.value / 4.0) * 44100).toInt()
        val totalSamples = totalSteps * samplesPerStep

        // 1. Export Full Master Mix
        val masterFile = File(outputDir, "${projectName}_Master_Mix.wav")
        val masterLeft = FloatArray(totalSamples)
        val masterRight = FloatArray(totalSamples)

        for (t in currentTrackList) {
            val stemFile = File(outputDir, "${projectName}_Stem_${t.name.replace(" ", "_")}.wav")
            val stemLeft = FloatArray(totalSamples)
            val stemRight = FloatArray(totalSamples)

            for (step in 0 until totalSteps) {
                val stepObj = t.steps[step]
                if (stepObj.active) {
                    val startSample = step * samplesPerStep
                    val numS = samplesPerStep.coerceAtMost(totalSamples - startSample)
                    val freq = SynthEngine.midiToFreq(60 + stepObj.pitchOffset)
                    val rendered = synthEngine.renderSynthBuffer(freq, numS, synthParams.value, t.type)

                    for (i in 0 until numS) {
                        val idx = startSample + i
                        val valL = rendered[i] * t.volume * stepObj.velocity
                        stemLeft[idx] += valL
                        stemRight[idx] += valL
                        masterLeft[idx] += valL
                        masterRight[idx] += valL
                    }
                }
            }

            writeWavFile(stemFile, stemLeft, stemRight)
            exportedFiles.add(stemFile)
        }

        writeWavFile(masterFile, masterLeft, masterRight)
        exportedFiles.add(masterFile)

        return exportedFiles
    }

    private fun writeWavFile(file: File, left: FloatArray, right: FloatArray) {
        val fos = FileOutputStream(file)
        val numChannels = 2
        val sampleRate = 44100
        val bitsPerSample = 16
        val dataSize = left.size * numChannels * (bitsPerSample / 8)

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size
        header.putShort(1) // PCM format
        header.putShort(numChannels.toShort())
        header.putInt(sampleRate)
        header.putInt(sampleRate * numChannels * bitsPerSample / 8) // ByteRate
        header.putShort((numChannels * bitsPerSample / 8).toShort()) // BlockAlign
        header.putShort(bitsPerSample.toShort())
        header.put("data".toByteArray())
        header.putInt(dataSize)

        fos.write(header.array())

        val pcmBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (i in left.indices) {
            val sL = (left[i].coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
            val sR = (right[i].coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
            pcmBuffer.putShort(sL)
            pcmBuffer.putShort(sR)
        }
        fos.write(pcmBuffer.array())
        fos.close()
    }
}
