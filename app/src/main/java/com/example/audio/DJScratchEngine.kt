package com.example.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class DJScratchEngine {
    private val _platterAngle = MutableStateFlow(0f)
    val platterAngle: StateFlow<Float> = _platterAngle

    private val _scratchSpeed = MutableStateFlow(1.0f) // 1.0 = Normal forward playback, negative = reverse
    val scratchSpeed: StateFlow<Float> = _scratchSpeed

    private var isDragging = false
    private var lastTouchAngle = 0f
    private var inertiaVelocity = 0f

    private val _stutterRollRate = MutableStateFlow(0) // 0 = off, 8 = 1/8th, 16 = 1/16th, 32 = 1/32th
    val stutterRollRate: StateFlow<Int> = _stutterRollRate

    fun onTouchDown(angle: Float) {
        isDragging = true
        lastTouchAngle = angle
        inertiaVelocity = 0f
    }

    fun onTouchMove(angle: Float) {
        if (!isDragging) return
        var deltaAngle = angle - lastTouchAngle
        if (deltaAngle > 180f) deltaAngle -= 360f
        if (deltaAngle < -180f) deltaAngle += 360f

        lastTouchAngle = angle
        _platterAngle.value = (_platterAngle.value + deltaAngle) % 360f

        // Convert angular velocity to scratch speed multiplier
        val speed = (deltaAngle / 15f)
        _scratchSpeed.value = speed
        inertiaVelocity = speed * 0.8f
    }

    fun onTouchUp() {
        isDragging = false
    }

    fun triggerBackspin() {
        isDragging = false
        inertiaVelocity = -4.5f // Rapid spin backwards
    }

    fun setStutterRoll(rate: Int) {
        _stutterRollRate.value = rate
    }

    /**
     * Called every audio frame step (~60 FPS) to update platter inertia
     */
    fun updateFramePhysics() {
        if (!isDragging) {
            if (abs(inertiaVelocity) > 0.1f) {
                _scratchSpeed.value = inertiaVelocity
                _platterAngle.value = (_platterAngle.value + inertiaVelocity * 10f) % 360f
                inertiaVelocity *= 0.88f // Friction slowdown
            } else {
                // Normal vinyl playback speed
                _scratchSpeed.value = 1.0f
                _platterAngle.value = (_platterAngle.value + 3.0f) % 360f
            }
        }
    }
}
