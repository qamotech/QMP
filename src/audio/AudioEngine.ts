import { getSoundPreset } from './SoundLibrary';

type FeatureSettings = {
  limiter: { threshold: number; release: number };
  saturation: { drive: number };
  compressor: { low: number; mid: number; high: number };
  lfo: { rate: number; depth: number };
  arpeggiator: { mode: 'up' | 'bounce' | 'gate' };
  enhancements: Record<string, boolean>;
};

export class AudioEngine {
  private ctx: AudioContext | null = null;

  private inputGain: GainNode | null = null;
  private masterGain: GainNode | null = null;
  private driveNode: WaveShaperNode | null = null;
  private compressorNode: DynamicsCompressorNode | null = null;
  private limiterNode: DynamicsCompressorNode | null = null;
  private widthSplitter: ChannelSplitterNode | null = null;
  private widthMerger: ChannelMergerNode | null = null;
  private widthLeftGain: GainNode | null = null;
  private widthRightGain: GainNode | null = null;
  private monoMixGain: GainNode | null = null;

  private eqBands: BiquadFilterNode[] = [];
  private pannerNode: StereoPannerNode | null = null;
  private analyser: AnalyserNode | null = null;
  private frequencyAnalyser: AnalyserNode | null = null;

  private activePack = 'classic-hiphop';
  private masterPitch = 0;
  private masterVolume = 0.8;
  private pannerVolume = 0.5;

  private globalFilterNode: BiquadFilterNode | null = null;
  private delayNode: DelayNode | null = null;
  private delayGain: GainNode | null = null;
  private delayFeedbackGain: GainNode | null = null;
  private reverbNode: ConvolverNode | null = null;
  private reverbGain: GainNode | null = null;

  private featureSettings: FeatureSettings = {
    limiter: { threshold: -0.1, release: 50 },
    saturation: { drive: 60 },
    compressor: { low: 58, mid: 64, high: 52 },
    lfo: { rate: 35, depth: 42 },
    arpeggiator: { mode: 'bounce' },
    enhancements: {},
  };

  init() {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || (window as any).webkitAudioContext)();

      this.inputGain = this.ctx.createGain();
      this.masterGain = this.ctx.createGain();
      this.driveNode = this.ctx.createWaveShaper();
      this.compressorNode = this.ctx.createDynamicsCompressor();
      this.limiterNode = this.ctx.createDynamicsCompressor();

      this.globalFilterNode = this.ctx.createBiquadFilter();
      this.globalFilterNode.type = 'lowpass';
      this.globalFilterNode.frequency.value = 20000;

      this.delayNode = this.ctx.createDelay(1.0);
      this.delayNode.delayTime.value = 0.3;
      this.delayGain = this.ctx.createGain();
      this.delayFeedbackGain = this.ctx.createGain();
      this.delayGain.gain.value = 0;
      this.delayFeedbackGain.gain.value = 0.35;

      this.reverbNode = this.ctx.createConvolver();
      this.reverbGain = this.ctx.createGain();
      this.reverbGain.gain.value = 0;

      const irLen = this.ctx.sampleRate * 2;
      const irBuffer = this.ctx.createBuffer(2, irLen, this.ctx.sampleRate);
      for (let ch = 0; ch < 2; ch++) {
        const data = irBuffer.getChannelData(ch);
        for (let i = 0; i < irLen; i++) {
          data[i] = (Math.random() * 2 - 1) * Math.pow(1 - i / irLen, 2);
        }
      }
      this.reverbNode.buffer = irBuffer;

      this.widthSplitter = this.ctx.createChannelSplitter(2);
      this.widthMerger = this.ctx.createChannelMerger(2);
      this.widthLeftGain = this.ctx.createGain();
      this.widthRightGain = this.ctx.createGain();
      this.monoMixGain = this.ctx.createGain();

      this.analyser = this.ctx.createAnalyser();
      this.frequencyAnalyser = this.ctx.createAnalyser();
      this.analyser.fftSize = 1024;
      this.frequencyAnalyser.fftSize = 256;
      this.frequencyAnalyser.smoothingTimeConstant = 0.8;

      this.inputGain.connect(this.delayNode);
      this.inputGain.connect(this.reverbNode);
      this.inputGain.connect(this.globalFilterNode);

      this.delayNode.connect(this.delayFeedbackGain);
      this.delayFeedbackGain.connect(this.delayNode);
      this.delayNode.connect(this.delayGain);
      this.delayGain.connect(this.globalFilterNode);

      this.reverbNode.connect(this.reverbGain);
      this.reverbGain.connect(this.globalFilterNode);

      let prevNode: AudioNode = this.globalFilterNode;

      if (this.ctx.createStereoPanner) {
        this.pannerNode = this.ctx.createStereoPanner();
        this.pannerNode.pan.value = 0;
        prevNode.connect(this.pannerNode);
        prevNode = this.pannerNode;
      }

      const freqs = [60, 250, 1000, 4000, 12000];
      const types: BiquadFilterType[] = ['lowshelf', 'peaking', 'peaking', 'peaking', 'highshelf'];
      freqs.forEach((freq, idx) => {
        const filter = this.ctx!.createBiquadFilter();
        filter.type = types[idx];
        filter.frequency.value = freq;
        filter.gain.value = 0;
        prevNode.connect(filter);
        prevNode = filter;
        this.eqBands.push(filter);
      });

      prevNode.connect(this.driveNode);
      this.driveNode.connect(this.compressorNode);
      this.compressorNode.connect(this.limiterNode);
      this.limiterNode.connect(this.widthSplitter);

      this.widthSplitter.connect(this.widthLeftGain, 0);
      this.widthSplitter.connect(this.widthRightGain, 1);
      this.widthSplitter.connect(this.monoMixGain, 0);
      this.widthSplitter.connect(this.monoMixGain, 1);

      this.widthLeftGain.connect(this.widthMerger, 0, 0);
      this.widthRightGain.connect(this.widthMerger, 0, 1);
      this.monoMixGain.connect(this.widthMerger, 0, 0);
      this.monoMixGain.connect(this.widthMerger, 0, 1);

      this.widthMerger.connect(this.masterGain);
      this.masterGain.connect(this.analyser);
      this.masterGain.connect(this.frequencyAnalyser);
      this.analyser.connect(this.ctx.destination);

      this.updateTotalVolume();
      this.applyFeatureNodes();
    }

    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  getAudioData(dataArray: Uint8Array) {
    if (this.analyser) {
      this.analyser.getByteTimeDomainData(dataArray);
    }
  }

  getFrequencyData(dataArray: Uint8Array) {
    if (this.frequencyAnalyser) {
      this.frequencyAnalyser.getByteFrequencyData(dataArray);
    }
  }

  private updateTotalVolume() {
    if (this.masterGain) {
      this.masterGain.gain.value = this.masterVolume * this.pannerVolume;
    }
  }

  private makeDistortionCurve(amount: number) {
    const samples = 44100;
    const curve = new Float32Array(samples);
    const k = Math.max(0, amount);
    for (let i = 0; i < samples; i++) {
      const x = (i * 2) / samples - 1;
      curve[i] = ((3 + k) * x * 20 * (Math.PI / 180)) / (Math.PI + k * Math.abs(x));
    }
    return curve;
  }

  private applyFeatureNodes() {
    if (!this.driveNode || !this.compressorNode || !this.limiterNode || !this.widthLeftGain || !this.widthRightGain || !this.monoMixGain) {
      return;
    }

    const { saturation, compressor, limiter, enhancements } = this.featureSettings;
    const drive = saturation.drive / 5;
    this.driveNode.curve = this.makeDistortionCurve(drive);
    this.driveNode.oversample = '4x';

    const avgCompression = (compressor.low + compressor.mid + compressor.high) / 3;
    this.compressorNode.threshold.value = -12 - avgCompression / 6;
    this.compressorNode.knee.value = 12;
    this.compressorNode.ratio.value = 1.5 + avgCompression / 35;
    this.compressorNode.attack.value = 0.003;
    this.compressorNode.release.value = 0.08 + avgCompression / 1000;

    this.limiterNode.threshold.value = limiter.threshold;
    this.limiterNode.knee.value = 0;
    this.limiterNode.ratio.value = 20;
    this.limiterNode.attack.value = 0.001;
    this.limiterNode.release.value = Math.max(0.02, limiter.release / 1000);

    const widthBoost = enhancements.widener || enhancements.stereo ? 1.2 : 1;
    const monoForce = enhancements.mono ? 0.5 : 0;
    this.widthLeftGain.gain.value = widthBoost;
    this.widthRightGain.gain.value = widthBoost;
    this.monoMixGain.gain.value = monoForce;
  }

  private shapeAmount(amount: number) {
    return this.makeDistortionCurve(Math.max(5, amount));
  }

  private createBitcrusher() {
    if (!this.ctx) return null;
    const processor = this.ctx.createScriptProcessor(1024, 1, 1);
    let hold = 0;
    let phase = 0;
    const step = 0.18;
    const normFreq = 0.22;

    processor.onaudioprocess = (event) => {
      const input = event.inputBuffer.getChannelData(0);
      const output = event.outputBuffer.getChannelData(0);
      for (let i = 0; i < input.length; i++) {
        phase += normFreq;
        if (phase >= 1.0) {
          phase -= 1.0;
          hold = step * Math.floor(input[i] / step + 0.5);
        }
        output[i] = hold;
      }
    };

    return processor;
  }

  private connectVoiceEnhancements(source: AudioNode, output: AudioNode, category: string, decay: number) {
    if (!this.ctx) {
      source.connect(output);
      return;
    }

    const { enhancements } = this.featureSettings;
    let current: AudioNode = source;

    const addFilter = (type: BiquadFilterType, frequency: number, q = 0.7, gain = 0) => {
      const filter = this.ctx!.createBiquadFilter();
      filter.type = type;
      filter.frequency.value = frequency;
      filter.Q.value = q;
      if ('gain' in filter) filter.gain.value = gain;
      current.connect(filter);
      current = filter;
    };

    const addDrive = (amount: number, mix = 1) => {
      const shaper = this.ctx!.createWaveShaper();
      shaper.curve = this.shapeAmount(amount);
      shaper.oversample = '4x';
      if (mix >= 1) {
        current.connect(shaper);
        current = shaper;
        return;
      }
      const dry = this.ctx!.createGain();
      const wet = this.ctx!.createGain();
      const sum = this.ctx!.createGain();
      dry.gain.value = 1 - mix;
      wet.gain.value = mix;
      current.connect(dry);
      current.connect(shaper);
      shaper.connect(wet);
      dry.connect(sum);
      wet.connect(sum);
      current = sum;
    };

    const addModulatedDelay = (baseDelay: number, depth: number, rate: number, wetAmount: number) => {
      const delay = this.ctx!.createDelay(0.08);
      const wet = this.ctx!.createGain();
      const dry = this.ctx!.createGain();
      const sum = this.ctx!.createGain();
      const lfo = this.ctx!.createOscillator();
      const lfoDepth = this.ctx!.createGain();

      delay.delayTime.value = baseDelay;
      wet.gain.value = wetAmount;
      dry.gain.value = 1 - wetAmount * 0.6;
      lfo.frequency.value = rate;
      lfoDepth.gain.value = depth;

      lfo.connect(lfoDepth);
      lfoDepth.connect(delay.delayTime);

      current.connect(dry);
      current.connect(delay);
      delay.connect(wet);
      dry.connect(sum);
      wet.connect(sum);
      lfo.start();
      lfo.stop(this.ctx!.currentTime + Math.max(0.25, decay + 0.2));

      current = sum;
    };

    const addPhaser = () => {
      const stages = Array.from({ length: 3 }, () => this.ctx!.createBiquadFilter());
      const lfo = this.ctx!.createOscillator();
      const lfoGain = this.ctx!.createGain();
      const wet = this.ctx!.createGain();
      const dry = this.ctx!.createGain();
      const sum = this.ctx!.createGain();

      lfo.type = 'sine';
      lfo.frequency.value = 0.45;
      lfoGain.gain.value = 900;
      wet.gain.value = 0.55;
      dry.gain.value = 0.65;

      let phaserInput = current;
      stages.forEach((stage, index) => {
        stage.type = 'allpass';
        stage.frequency.value = 500 + index * 220;
        stage.Q.value = 0.8;
        lfo.connect(lfoGain);
        lfoGain.connect(stage.frequency);
        phaserInput.connect(stage);
        phaserInput = stage;
      });

      current.connect(dry);
      phaserInput.connect(wet);
      dry.connect(sum);
      wet.connect(sum);
      lfo.start();
      lfo.stop(this.ctx!.currentTime + Math.max(0.3, decay + 0.3));
      current = sum;
    };

    if (enhancements.tube) addDrive(20, 0.35);
    if (enhancements.crunch) addDrive(60, 0.55);
    if (enhancements.grit) addDrive(40, 0.4);
    if (enhancements.fuzz) addDrive(120, 0.75);
    if (enhancements.tape) addDrive(28, 0.3);
    if (enhancements.bitcrush) {
      const crusher = this.createBitcrusher();
      if (crusher) {
        current.connect(crusher);
        current = crusher;
      } else {
        addDrive(85, 0.5);
      }
    }

    if (enhancements.warmth) addFilter('lowshelf', 220, 0.7, 5);
    if (enhancements.air) addFilter('highshelf', 6500, 0.7, 6);
    if (enhancements.sub && category === 'Kick') addFilter('lowshelf', 90, 0.8, 8);
    if (enhancements.snap) addFilter('highshelf', 3500, 0.7, 5);
    if (enhancements.excite) addFilter('peaking', 4200, 1.4, 4);
    if (enhancements.deess) addFilter('peaking', 6500, 3.5, -5);
    if (enhancements.lofi) addFilter('lowpass', 2400, 0.8);

    if (enhancements.phase) addPhaser();

    if (enhancements.flange) addModulatedDelay(0.0035, 0.0018, 0.28, 0.45);

    if (enhancements.chorus) addModulatedDelay(0.018, 0.006, 0.18, 0.35);

    if (enhancements.transient || enhancements.punch) {
      const transientGain = this.ctx!.createGain();
      current.connect(transientGain);
      transientGain.gain.setValueAtTime(1.15, this.ctx!.currentTime);
      transientGain.gain.exponentialRampToValueAtTime(1, this.ctx!.currentTime + Math.min(0.05, decay * 0.25));
      current = transientGain;
    }

    if (enhancements.gate) {
      const gateGain = this.ctx!.createGain();
      current.connect(gateGain);
      gateGain.gain.setValueAtTime(1, this.ctx!.currentTime);
      gateGain.gain.exponentialRampToValueAtTime(0.0001, this.ctx!.currentTime + Math.max(0.03, decay * 0.8));
      current = gateGain;
    }

    if (enhancements.vinyl && category !== 'Kick') {
      const vinylBuffer = this.ctx!.createBuffer(1, Math.floor(this.ctx!.sampleRate * 0.08), this.ctx!.sampleRate);
      const vinylData = vinylBuffer.getChannelData(0);
      for (let i = 0; i < vinylData.length; i++) {
        const crackle = Math.random() > 0.992 ? (Math.random() * 2 - 1) * 0.16 : 0;
        const rumble = Math.sin((i / vinylData.length) * Math.PI * 2 * 3) * 0.02;
        vinylData[i] = crackle + rumble;
      }
      const vinylSrc = this.ctx!.createBufferSource();
      const vinylFilter = this.ctx!.createBiquadFilter();
      const vinylGain = this.ctx!.createGain();
      vinylSrc.buffer = vinylBuffer;
      vinylFilter.type = 'bandpass';
      vinylFilter.frequency.value = 1800;
      vinylFilter.Q.value = 0.6;
      vinylGain.gain.value = 0.16;
      vinylSrc.connect(vinylFilter);
      vinylFilter.connect(vinylGain);
      vinylGain.connect(output);
      vinylSrc.start(this.ctx!.currentTime);
    }

    if (enhancements.limit) {
      const localLimiter = this.ctx!.createDynamicsCompressor();
      localLimiter.threshold.value = -3;
      localLimiter.knee.value = 0;
      localLimiter.ratio.value = 16;
      localLimiter.attack.value = 0.001;
      localLimiter.release.value = 0.03;
      current.connect(localLimiter);
      current = localLimiter;
    }

    current.connect(output);
  }

  setPannerPosition(pos: { x: number; y: number }) {
    if (this.pannerNode) {
      this.pannerNode.pan.value = (pos.x - 50) / 50;
    }
    const distance = 1 - pos.y / 100;
    this.pannerVolume = 0.2 + 0.8 * distance;
    this.updateTotalVolume();
  }

  setEqLevels(levels: number[]) {
    if (this.eqBands.length !== 5) return;
    levels.forEach((val, idx) => {
      const db = ((val - 50) / 50) * 12;
      this.eqBands[idx].gain.value = db;
    });
  }

  setEffects(reverb: number, delay: number, filterType: 'lowpass' | 'highpass' | 'bandpass') {
    if (this.reverbGain) {
      this.reverbGain.gain.value = reverb / 100;
    }
    if (this.delayGain) {
      this.delayGain.gain.value = delay / 100;
    }
    if (this.delayFeedbackGain) {
      this.delayFeedbackGain.gain.value = 0.2 + delay / 250;
    }
    if (this.globalFilterNode) {
      this.globalFilterNode.type = filterType;
      if (filterType === 'lowpass') {
        this.globalFilterNode.frequency.value = 20000;
        this.globalFilterNode.Q.value = 0.1;
      } else if (filterType === 'highpass') {
        this.globalFilterNode.frequency.value = 120;
        this.globalFilterNode.Q.value = 0.7;
      } else {
        this.globalFilterNode.frequency.value = 1800;
        this.globalFilterNode.Q.value = 1.2;
      }
    }
  }

  private synthParams = { cutoff: 70, resonance: 30, envMod: 50, decay: 40 };

  setSynthParams(params: { cutoff: number; resonance: number; envMod: number; decay: number }) {
    this.synthParams = params;
  }

  setVolume(vol: number) {
    this.masterVolume = vol;
    this.updateTotalVolume();
  }

  setMasterPitch(pitch: number) {
    this.masterPitch = pitch;
  }

  setFeatureSettings(settings: FeatureSettings) {
    this.featureSettings = settings;
    this.applyFeatureNodes();
  }

  private applyPitch(freq: number) {
    return freq * Math.pow(2, this.masterPitch / 12);
  }

  setSamplePack(pack: string) {
    this.activePack = pack;
  }

  playSound(trackIdx: number, soundId?: string) {
    if (this.ctx?.state !== 'running' || !this.inputGain) return;

    if (!soundId) {
      switch (trackIdx) {
        case 0: soundId = 'classic-kick'; break;
        case 1: soundId = 'classic-snare'; break;
        case 2: soundId = 'classic-hihat'; break;
        case 3: soundId = 'classic-perc'; break;
        default: soundId = 'classic-kick';
      }
    }

    const preset = getSoundPreset(soundId);
    const decayMod = 0.2 + (this.synthParams.decay / 100) * 2;
    const pitchMod = this.synthParams.envMod / 50;
    const globalCutoff = 200 + (this.synthParams.cutoff / 100) * 15000;
    const globalQ = (this.synthParams.resonance / 100) * 20;
    const lfoDepth = this.featureSettings.lfo.depth / 100;
    const lfoRate = 0.25 + (this.featureSettings.lfo.rate / 100) * 12;

    let decay = preset.decay * decayMod;
    let baseFreq = this.applyPitch(preset.baseFreq * pitchMod);
    const attack = preset.attack || 0.005;

    if (this.featureSettings.enhancements.sub && preset.category === 'Kick') baseFreq *= 0.9;
    if (this.featureSettings.enhancements.air && preset.category === 'HiHat') baseFreq *= 1.08;
    if (this.featureSettings.enhancements.punch) decay *= 0.9;
    if (this.featureSettings.enhancements.warmth) decay *= 1.1;
    if (this.featureSettings.enhancements.gate) decay *= 0.8;

    const gain = this.ctx.createGain();
    const voiceFilter = this.ctx.createBiquadFilter();
    voiceFilter.type = 'lowpass';
    voiceFilter.frequency.value = globalCutoff;
    voiceFilter.Q.value = globalQ;

    gain.connect(voiceFilter);
    this.connectVoiceEnhancements(voiceFilter, this.inputGain, preset.category, decay);

    if (preset.type === 'osc') {
      const osc = this.ctx.createOscillator();
      osc.type = preset.oscType || 'sine';

      const filter = this.ctx.createBiquadFilter();
      filter.type = preset.filterType || 'lowpass';
      filter.frequency.value = preset.filterFreq || 20000;
      filter.Q.value = preset.filterQ || 1;

      osc.connect(filter);
      filter.connect(gain);

      const lfoValue = 1 + Math.sin(this.ctx.currentTime * lfoRate) * 0.04 * lfoDepth;
      osc.frequency.setValueAtTime(baseFreq * lfoValue, this.ctx.currentTime);
      if (preset.sweep && preset.sweep !== 1) {
        osc.frequency.exponentialRampToValueAtTime(Math.max(10, baseFreq * preset.sweep), this.ctx.currentTime + decay);
      }

      gain.gain.setValueAtTime(0.01, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.8, this.ctx.currentTime + attack);
      gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + attack + decay);

      osc.start(this.ctx.currentTime);
      osc.stop(this.ctx.currentTime + attack + decay);
    } else if (preset.type === 'noise') {
      const bufferSize = this.ctx.sampleRate * Math.max(decay + attack, 0.5);
      const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = Math.random() * 2 - 1;
      }

      const noise = this.ctx.createBufferSource();
      noise.buffer = buffer;

      const filter = this.ctx.createBiquadFilter();
      filter.type = preset.filterType || 'highpass';
      filter.frequency.value = this.applyPitch((preset.filterFreq || 1000) * pitchMod);
      filter.Q.value = preset.filterQ || 1;

      noise.connect(filter);
      filter.connect(gain);

      gain.gain.setValueAtTime(0.01, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.5, this.ctx.currentTime + attack);
      gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + attack + decay);

      noise.start(this.ctx.currentTime);

      if (preset.category === 'Snare') {
        const snapOsc = this.ctx.createOscillator();
        const snapGain = this.ctx.createGain();
        snapOsc.type = 'triangle';
        snapOsc.connect(snapGain);
        snapGain.connect(voiceFilter);
        snapOsc.frequency.setValueAtTime(this.applyPitch(baseFreq || 180), this.ctx.currentTime);
        snapGain.gain.setValueAtTime(0.5, this.ctx.currentTime);
        snapGain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + attack + decay * 0.5);
        snapOsc.start(this.ctx.currentTime);
        snapOsc.stop(this.ctx.currentTime + attack + decay);
      }
    }
  }
}

export const audioEngine = new AudioEngine();