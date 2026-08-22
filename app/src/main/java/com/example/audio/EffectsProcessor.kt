package com.example.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

class EffectsProcessor {
    private val delayBufferLeft = FloatArray(44100 * 2)
    private val delayBufferRight = FloatArray(44100 * 2)
    private var delayWriteIndex = 0

    private val reverbBuffer1 = FloatArray(2205)
    private val reverbBuffer2 = FloatArray(3528)
    private val reverbBuffer3 = FloatArray(4851)
    private var reverbIdx1 = 0
    private var reverbIdx2 = 0
    private var reverbIdx3 = 0

    /**
     * Process stereo PCM audio frame buffer with full Master FX chain
     */
    fun processFxChain(
        leftBuffer: FloatArray,
        rightBuffer: FloatArray,
        params: MixerFxParams,
        isKickBeatActive: Boolean = false
    ) {
        val numSamples = leftBuffer.size

        // 1. 5-Band EQ Gain Factors
        val eqLowGain = 10.0.pow(params.eqLow / 20.0).toFloat()
        val eqMidGain = 10.0.pow(params.eqMid / 20.0).toFloat()
        val eqHighGain = 10.0.pow(params.eqHigh / 20.0).toFloat()

        // 2. Tube Saturator Drive
        val driveFactor = 1.0f + params.saturatorDrive * 4.0f

        // 3. Bitcrusher Step size
        val bitSteps = 2.0.pow(params.bitcrusherDepth.toDouble()).toFloat()

        // 4. Sidechain Ducking
        val duckingGain = if (isKickBeatActive) (1.0f - params.sidechainDucking * 0.7f) else 1.0f

        // 5. Delay Time in samples
        val delaySamples = (params.delayTime * 44100f).toInt().coerceIn(100, 44000)

        for (i in 0 until numSamples) {
            var l = leftBuffer[i]
            var r = rightBuffer[i]

            // Apply 5-Band EQ Gain approximation
            l = (l * eqLowGain * 0.4f) + (l * eqMidGain * 0.4f) + (l * eqHighGain * 0.2f)
            r = (r * eqLowGain * 0.4f) + (r * eqMidGain * 0.4f) + (r * eqHighGain * 0.2f)

            // Sidechain Ducking
            l *= duckingGain
            r *= duckingGain

            // Tube Saturator (Harmonic Soft-Clipping)
            if (params.saturatorDrive > 0.05f) {
                l = tanh(l * driveFactor)
                r = tanh(r * driveFactor)
            }

            // Bitcrusher
            if (params.bitcrusherDepth < 16) {
                l = (kotlin.math.round(l * bitSteps) / bitSteps)
                r = (kotlin.math.round(r * bitSteps) / bitSteps)
            }

            // Delay Processor
            val readIdx = (delayWriteIndex - delaySamples + delayBufferLeft.size) % delayBufferLeft.size
            val dL = delayBufferLeft[readIdx]
            val dR = delayBufferRight[readIdx]

            delayBufferLeft[delayWriteIndex] = l + dR * params.delayFeedback
            delayBufferRight[delayWriteIndex] = r + dL * params.delayFeedback
            delayWriteIndex = (delayWriteIndex + 1) % delayBufferLeft.size

            l += dL * params.delayWet
            r += dR * params.delayWet

            // Convolution Reverb simulation (Comb Filter network)
            if (params.reverbWet > 0.05f) {
                val rev1 = reverbBuffer1[reverbIdx1]
                val rev2 = reverbBuffer2[reverbIdx2]
                val rev3 = reverbBuffer3[reverbIdx3]

                reverbBuffer1[reverbIdx1] = l + rev1 * 0.7f * params.reverbSize
                reverbBuffer2[reverbIdx2] = r + rev2 * 0.7f * params.reverbSize
                reverbBuffer3[reverbIdx3] = (l + r) * 0.5f + rev3 * 0.6f * params.reverbSize

                reverbIdx1 = (reverbIdx1 + 1) % reverbBuffer1.size
                reverbIdx2 = (reverbIdx2 + 1) % reverbBuffer2.size
                reverbIdx3 = (reverbIdx3 + 1) % reverbBuffer3.size

                val wetSigL = (rev1 + rev3) * 0.3f
                val wetSigR = (rev2 + rev3) * 0.3f
                l = l * (1f - params.reverbWet * 0.5f) + wetSigL * params.reverbWet
                r = r * (1f - params.reverbWet * 0.5f) + wetSigR * params.reverbWet
            }

            // 3D Spatial Panning & Stereo Imager
            val panX = params.panner3dX.coerceIn(-1.0f, 1.0f)
            val leftPanGain = cos((panX + 1f) * PI.toFloat() / 4f)
            val rightPanGain = sin((panX + 1f) * PI.toFloat() / 4f)

            val mid = (l + r) * 0.5f
            val side = (l - r) * 0.5f * params.stereoWidth

            l = (mid + side) * leftPanGain
            r = (mid - side) * rightPanGain

            // True Peak Brickwall Limiter
            val maxPeak = params.masterLimiterThreshold
            l = l.coerceIn(-maxPeak, maxPeak)
            r = r.coerceIn(-maxPeak, maxPeak)

            leftBuffer[i] = l
            rightBuffer[i] = r
        }
    }
}
