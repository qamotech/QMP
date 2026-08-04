package com.example.data

import com.example.audio.MixerFxParams
import com.example.audio.SynthParams
import com.example.data.local.StudioPreset
import org.json.JSONArray
import org.json.JSONObject

object PresetSerializer {

    fun synthParamsToJson(p: SynthParams): String {
        val obj = JSONObject()
        obj.put("attack", p.attack.toDouble())
        obj.put("decay", p.decay.toDouble())
        obj.put("sustain", p.sustain.toDouble())
        obj.put("release", p.release.toDouble())
        obj.put("lfoRate", p.lfoRate.toDouble())
        obj.put("lfoShape", p.lfoShape)
        obj.put("lfoTarget", p.lfoTarget)
        obj.put("pitchScale", p.pitchScale)
        obj.put("formantVowel", p.formantVowel)
        obj.put("cutoff", p.cutoff.toDouble())
        obj.put("resonance", p.resonance.toDouble())

        val centsArr = JSONArray()
        p.microTuningCents.forEach { centsArr.put(it.toDouble()) }
        obj.put("microTuningCents", centsArr)

        return obj.toString()
    }

    fun jsonToSynthParams(jsonStr: String): SynthParams {
        if (jsonStr.isBlank()) return SynthParams()
        return try {
            val obj = JSONObject(jsonStr)
            val centsList = MutableList(12) { 0f }
            if (obj.has("microTuningCents")) {
                val arr = obj.getJSONArray("microTuningCents")
                for (i in 0 until arr.length().coerceAtMost(12)) {
                    centsList[i] = arr.optDouble(i, 0.0).toFloat()
                }
            }

            SynthParams(
                attack = obj.optDouble("attack", 0.02).toFloat(),
                decay = obj.optDouble("decay", 0.25).toFloat(),
                sustain = obj.optDouble("sustain", 0.6).toFloat(),
                release = obj.optDouble("release", 0.4).toFloat(),
                lfoRate = obj.optDouble("lfoRate", 2.0).toFloat(),
                lfoShape = obj.optString("lfoShape", "Sine"),
                lfoTarget = obj.optString("lfoTarget", "Filter Cutoff"),
                pitchScale = obj.optString("pitchScale", "Minor"),
                microTuningCents = centsList,
                formantVowel = obj.optString("formantVowel", "Off"),
                cutoff = obj.optDouble("cutoff", 4000.0).toFloat(),
                resonance = obj.optDouble("resonance", 1.5).toFloat()
            )
        } catch (e: Exception) {
            SynthParams()
        }
    }

    fun mixerFxParamsToJson(p: MixerFxParams): String {
        val obj = JSONObject()
        obj.put("eqLow", p.eqLow.toDouble())
        obj.put("eqLowMid", p.eqLowMid.toDouble())
        obj.put("eqMid", p.eqMid.toDouble())
        obj.put("eqHighMid", p.eqHighMid.toDouble())
        obj.put("eqHigh", p.eqHigh.toDouble())
        obj.put("panner3dX", p.panner3dX.toDouble())
        obj.put("panner3dY", p.panner3dY.toDouble())
        obj.put("reverbWet", p.reverbWet.toDouble())
        obj.put("reverbSize", p.reverbSize.toDouble())
        obj.put("delayWet", p.delayWet.toDouble())
        obj.put("delayTime", p.delayTime.toDouble())
        obj.put("delayFeedback", p.delayFeedback.toDouble())
        obj.put("saturatorDrive", p.saturatorDrive.toDouble())
        obj.put("bitcrusherDepth", p.bitcrusherDepth)
        obj.put("sidechainDucking", p.sidechainDucking.toDouble())
        obj.put("masterLimiterThreshold", p.masterLimiterThreshold.toDouble())
        obj.put("stereoWidth", p.stereoWidth.toDouble())
        obj.put("harmonicExciter", p.harmonicExciter.toDouble())
        obj.put("transientPunch", p.transientPunch.toDouble())
        return obj.toString()
    }

    fun jsonToMixerFxParams(jsonStr: String): MixerFxParams {
        if (jsonStr.isBlank()) return MixerFxParams()
        return try {
            val obj = JSONObject(jsonStr)
            MixerFxParams(
                eqLow = obj.optDouble("eqLow", 0.0).toFloat(),
                eqLowMid = obj.optDouble("eqLowMid", 0.0).toFloat(),
                eqMid = obj.optDouble("eqMid", 0.0).toFloat(),
                eqHighMid = obj.optDouble("eqHighMid", 0.0).toFloat(),
                eqHigh = obj.optDouble("eqHigh", 0.0).toFloat(),
                panner3dX = obj.optDouble("panner3dX", 0.0).toFloat(),
                panner3dY = obj.optDouble("panner3dY", 0.0).toFloat(),
                reverbWet = obj.optDouble("reverbWet", 0.2).toFloat(),
                reverbSize = obj.optDouble("reverbSize", 0.5).toFloat(),
                delayWet = obj.optDouble("delayWet", 0.15).toFloat(),
                delayTime = obj.optDouble("delayTime", 0.25).toFloat(),
                delayFeedback = obj.optDouble("delayFeedback", 0.35).toFloat(),
                saturatorDrive = obj.optDouble("saturatorDrive", 0.1).toFloat(),
                bitcrusherDepth = obj.optInt("bitcrusherDepth", 16),
                sidechainDucking = obj.optDouble("sidechainDucking", 0.25).toFloat(),
                masterLimiterThreshold = obj.optDouble("masterLimiterThreshold", -0.5).toFloat(),
                stereoWidth = obj.optDouble("stereoWidth", 1.0).toFloat(),
                harmonicExciter = obj.optDouble("harmonicExciter", 0.2).toFloat(),
                transientPunch = obj.optDouble("transientPunch", 0.3).toFloat()
            )
        } catch (e: Exception) {
            MixerFxParams()
        }
    }

    fun exportPresetToJson(preset: StudioPreset): String {
        val root = JSONObject()
        root.put("name", preset.name)
        root.put("category", preset.category)
        root.put("description", preset.description)
        root.put("bpm", preset.bpm)
        root.put("swing", preset.swing.toDouble())
        root.put("synthConfig", JSONObject(preset.synthConfigJson.ifBlank { "{}" }))
        root.put("mixerConfig", JSONObject(preset.mixerConfigJson.ifBlank { "{}" }))
        return root.toString(2)
    }

    fun importPresetFromJson(jsonStr: String): StudioPreset? {
        return try {
            val root = JSONObject(jsonStr)
            val name = root.optString("name", "Imported Preset")
            val category = root.optString("category", "Custom Preset")
            val description = root.optString("description", "User imported configuration")
            val bpm = root.optInt("bpm", 120)
            val swing = root.optDouble("swing", 0.0).toFloat()

            val synthJson = root.optJSONObject("synthConfig")?.toString() ?: ""
            val mixerJson = root.optJSONObject("mixerConfig")?.toString() ?: ""

            StudioPreset(
                name = name,
                category = category,
                description = description,
                isBuiltIn = false,
                synthConfigJson = synthJson,
                mixerConfigJson = mixerJson,
                bpm = bpm,
                swing = swing
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getFactoryPresets(): List<StudioPreset> {
        return listOf(
            StudioPreset(
                name = "HipHop Platinum 808",
                category = "Full Studio Configuration",
                description = "Deep booming 808 sub, premastered punchy kick, 16th trap hi-hats, tight snare, and heavy sidechain ducking.",
                isBuiltIn = true,
                bpm = 92,
                swing = 0.08f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.005f,
                        decay = 0.28f,
                        sustain = 0.45f,
                        release = 0.3f,
                        lfoRate = 4.0f,
                        lfoShape = "Square",
                        lfoTarget = "Filter Cutoff",
                        pitchScale = "Minor",
                        cutoff = 3200f,
                        resonance = 2.4f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqLow = 6.0f,
                        eqMid = -1.0f,
                        eqHigh = 2.5f,
                        saturatorDrive = 0.3f,
                        sidechainDucking = 0.65f,
                        transientPunch = 0.85f,
                        masterLimiterThreshold = -0.2f,
                        harmonicExciter = 0.35f,
                        stereoWidth = 1.25f
                    )
                )
            ),
            StudioPreset(
                name = "Pop Radio Hitmaker",
                category = "Full Studio Configuration",
                description = "Bright punchy premastered kick, snappy clap & snare, polished synth leads, stereo spatial width & limiter.",
                isBuiltIn = true,
                bpm = 120,
                swing = 0.0f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.01f,
                        decay = 0.3f,
                        sustain = 0.65f,
                        release = 0.4f,
                        lfoRate = 2.5f,
                        lfoShape = "Saw",
                        lfoTarget = "Filter Cutoff",
                        pitchScale = "Major",
                        cutoff = 6200f,
                        resonance = 2.0f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqLow = 3.5f,
                        eqMid = 1.0f,
                        eqHigh = 3.0f,
                        reverbWet = 0.25f,
                        reverbSize = 0.5f,
                        delayWet = 0.2f,
                        saturatorDrive = 0.15f,
                        transientPunch = 0.7f,
                        masterLimiterThreshold = -0.3f,
                        harmonicExciter = 0.4f,
                        stereoWidth = 1.4f
                    )
                )
            ),
            StudioPreset(
                name = "RnB Smooth Velvet Keys",
                category = "Full Studio Configuration",
                description = "Warm electric piano Rhodes tone, silky sub bass, lush chorus reverb, micro-timing swing, and gentle compression.",
                isBuiltIn = true,
                bpm = 88,
                swing = 0.18f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.04f,
                        decay = 0.5f,
                        sustain = 0.75f,
                        release = 0.65f,
                        lfoRate = 1.2f,
                        lfoShape = "Triangle",
                        lfoTarget = "Pitch",
                        pitchScale = "Pentatonic",
                        formantVowel = "E (Eh)",
                        cutoff = 2800f,
                        resonance = 1.1f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqLow = 4.0f,
                        eqMid = 1.5f,
                        eqHigh = -1.0f,
                        reverbWet = 0.45f,
                        reverbSize = 0.75f,
                        delayWet = 0.25f,
                        delayTime = 0.33f,
                        delayFeedback = 0.4f,
                        saturatorDrive = 0.2f,
                        stereoWidth = 1.35f,
                        harmonicExciter = 0.25f
                    )
                )
            ),
            StudioPreset(
                name = "80s Cyber Synthwave",
                category = "Full Studio Configuration",
                description = "Neon retro synth with high resonance cutoff, tape delay, chorus space & +3dB low end punch.",
                isBuiltIn = true,
                bpm = 124,
                swing = 0.05f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.01f,
                        decay = 0.35f,
                        sustain = 0.7f,
                        release = 0.5f,
                        lfoRate = 3.5f,
                        lfoShape = "Saw",
                        lfoTarget = "Filter Cutoff",
                        pitchScale = "Minor",
                        cutoff = 5500f,
                        resonance = 2.8f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqLow = 3.0f,
                        eqHigh = 2.0f,
                        reverbWet = 0.35f,
                        reverbSize = 0.65f,
                        delayWet = 0.25f,
                        delayTime = 0.25f,
                        delayFeedback = 0.4f,
                        saturatorDrive = 0.2f,
                        stereoWidth = 1.4f,
                        transientPunch = 0.4f
                    )
                )
            ),
            StudioPreset(
                name = "Lo-Fi Cassette Chill",
                category = "Synth Patch",
                description = "Warm muffled filter with 10-bit crushing, subtle tape saturation & vintage room reverb.",
                isBuiltIn = true,
                bpm = 85,
                swing = 0.15f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.08f,
                        decay = 0.4f,
                        sustain = 0.5f,
                        release = 0.6f,
                        lfoRate = 0.8f,
                        lfoShape = "Triangle",
                        lfoTarget = "Pitch",
                        formantVowel = "O",
                        cutoff = 1800f,
                        resonance = 1.2f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqLow = 2.0f,
                        eqHigh = -4.0f,
                        bitcrusherDepth = 10,
                        saturatorDrive = 0.35f,
                        reverbWet = 0.45f,
                        reverbSize = 0.7f,
                        delayWet = 0.3f,
                        delayTime = 0.38f,
                        delayFeedback = 0.45f,
                        stereoWidth = 0.9f
                    )
                )
            ),
            StudioPreset(
                name = "Heavy Trap & Sub Punch",
                category = "Full Studio Configuration",
                description = "Snappy synth envelope, boosted sub frequencies, heavy sidechain ducking & master limiter.",
                isBuiltIn = true,
                bpm = 140,
                swing = 0.02f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.005f,
                        decay = 0.2f,
                        sustain = 0.3f,
                        release = 0.2f,
                        lfoRate = 6.0f,
                        lfoShape = "Square",
                        lfoTarget = "Filter Cutoff",
                        cutoff = 2200f,
                        resonance = 3.2f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqLow = 6.0f,
                        eqHighMid = 3.0f,
                        saturatorDrive = 0.25f,
                        sidechainDucking = 0.6f,
                        transientPunch = 0.8f,
                        masterLimiterThreshold = -0.2f,
                        stereoWidth = 1.1f
                    )
                )
            ),
            StudioPreset(
                name = "Ambient Cathedral Pad",
                category = "Mixer FX",
                description = "Dreamy long attack synth pad routed into deep space cathedral reverb & wide stereo width.",
                isBuiltIn = true,
                bpm = 95,
                swing = 0.0f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.25f,
                        decay = 0.8f,
                        sustain = 0.85f,
                        release = 1.2f,
                        lfoRate = 1.2f,
                        lfoShape = "Sine",
                        lfoTarget = "Amplitude",
                        cutoff = 3200f,
                        resonance = 1.0f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        reverbWet = 0.75f,
                        reverbSize = 0.9f,
                        delayWet = 0.4f,
                        delayTime = 0.5f,
                        delayFeedback = 0.6f,
                        stereoWidth = 1.8f,
                        harmonicExciter = 0.3f
                    )
                )
            ),
            StudioPreset(
                name = "Acid Distortion Lab",
                category = "Synth Patch",
                description = "Aggressive screaming acid filter resonance driven through 8-bit crushing & harmonic drive.",
                isBuiltIn = true,
                bpm = 130,
                swing = 0.08f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.001f,
                        decay = 0.15f,
                        sustain = 0.2f,
                        release = 0.15f,
                        lfoRate = 8.0f,
                        lfoShape = "Saw",
                        lfoTarget = "Filter Cutoff",
                        cutoff = 7500f,
                        resonance = 4.5f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        eqMid = 4.0f,
                        saturatorDrive = 0.7f,
                        bitcrusherDepth = 8,
                        harmonicExciter = 0.6f,
                        transientPunch = 0.7f,
                        masterLimiterThreshold = -0.1f
                    )
                )
            ),
            StudioPreset(
                name = "Clean Acoustic Studio",
                category = "Full Studio Configuration",
                description = "Transparent, uncolored neutral mix with gentle room ambiance & high dynamic range.",
                isBuiltIn = true,
                bpm = 110,
                swing = 0.0f,
                synthConfigJson = synthParamsToJson(
                    SynthParams(
                        attack = 0.02f,
                        decay = 0.3f,
                        sustain = 0.6f,
                        release = 0.35f,
                        lfoRate = 2.0f,
                        lfoShape = "Sine",
                        lfoTarget = "Filter Cutoff",
                        cutoff = 4200f,
                        resonance = 1.2f
                    )
                ),
                mixerConfigJson = mixerFxParamsToJson(
                    MixerFxParams(
                        reverbWet = 0.15f,
                        reverbSize = 0.4f,
                        delayWet = 0.05f,
                        stereoWidth = 1.1f,
                        masterLimiterThreshold = -0.5f
                    )
                )
            )
        )
    }
}
