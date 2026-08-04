package com.example.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.max

enum class VisualizerMode(val displayName: String) {
    OSCILLOSCOPE("OLED Waveform"),
    SPECTRUM_BARS("FL Spectrum Analyzer"),
    VECTOR_SCOPE("Stereo Phase Scope")
}

class VisualizerEngine {
    private val _mode = MutableStateFlow(VisualizerMode.OSCILLOSCOPE)
    val mode: StateFlow<VisualizerMode> = _mode

    private val _waveformState = MutableStateFlow(FloatArray(128))
    val waveformState: StateFlow<FloatArray> = _waveformState

    private val _spectrumState = MutableStateFlow(FloatArray(16))
    val spectrumState: StateFlow<FloatArray> = _spectrumState

    private val _vectorScopeState = MutableStateFlow(List(64) { Pair(0f, 0f) })
    val vectorScopeState: StateFlow<List<Pair<Float, Float>>> = _vectorScopeState

    private val _vuLeftPeak = MutableStateFlow(0f)
    val vuLeftPeak: StateFlow<Float> = _vuLeftPeak

    private val _vuRightPeak = MutableStateFlow(0f)
    val vuRightPeak: StateFlow<Float> = _vuRightPeak

    private val _isClipping = MutableStateFlow(false)
    val isClipping: StateFlow<Boolean> = _isClipping

    fun setVisualizerMode(newMode: VisualizerMode) {
        _mode.value = newMode
    }

    fun updateVisualsFromBuffer(leftSamples: FloatArray, rightSamples: FloatArray) {
        if (leftSamples.isEmpty()) return

        // Peak VU level calculation
        var maxL = 0f
        var maxR = 0f
        for (i in leftSamples.indices) {
            maxL = max(maxL, abs(leftSamples[i]))
            maxR = max(maxR, abs(rightSamples[i]))
        }
        _vuLeftPeak.value = maxL.coerceIn(0f, 1f)
        _vuRightPeak.value = maxR.coerceIn(0f, 1f)
        _isClipping.value = (maxL >= 0.98f || maxR >= 0.98f)

        // 1. Waveform downsampling
        val wave = FloatArray(128)
        val step = (leftSamples.size / 128).coerceAtLeast(1)
        for (i in 0 until 128) {
            val idx = (i * step).coerceAtMost(leftSamples.size - 1)
            wave[i] = (leftSamples[idx] + rightSamples[idx]) * 0.5f
        }
        _waveformState.value = wave

        // 2. 16-Band FFT Spectrum Band Energy Approximation
        val spectrum = FloatArray(16)
        val bandSize = (leftSamples.size / 16).coerceAtLeast(1)
        for (b in 0 until 16) {
            var energy = 0f
            val start = b * bandSize
            val end = ((b + 1) * bandSize).coerceAtMost(leftSamples.size)
            for (i in start until end) {
                energy += abs(leftSamples[i])
            }
            val avg = (energy / (end - start).coerceAtLeast(1)) * (1f + b * 0.15f) // High boost for visual balance
            spectrum[b] = avg.coerceIn(0.05f, 1.0f)
        }
        _spectrumState.value = spectrum

        // 3. Stereo Vector Scope (Goniometer L/R 45-degree projection)
        val scope = ArrayList<Pair<Float, Float>>(64)
        val scopeStep = (leftSamples.size / 64).coerceAtLeast(1)
        for (i in 0 until 64) {
            val idx = (i * scopeStep).coerceAtMost(leftSamples.size - 1)
            val l = leftSamples[idx]
            val r = rightSamples[idx]
            // X = L - R, Y = L + R
            val x = (l - r) * 0.707f
            val y = (l + r) * 0.707f
            scope.add(Pair(x, y))
        }
        _vectorScopeState.value = scope
    }
}

