export type SoundCategory = 'Kick' | 'Snare' | 'HiHat' | 'Perc' | 'Tom' | 'Clap' | 'Cymbal' | 'FX' | 'Synth';

export interface SoundPreset {
  id: string;
  name: string;
  category: SoundCategory;
  type: 'osc' | 'noise' | 'fm' | 'buffer';
  oscType?: OscillatorType;
  baseFreq: number;
  decay: number;
  sweep?: number;
  fmFreqRatio?: number;
  fmModIndex?: number;
  filterType?: BiquadFilterType;
  filterFreq?: number;
  filterQ?: number;
  noiseColor?: 'white' | 'pink' | 'brown';
  attack?: number;
}

export const SOUND_LIBRARY: SoundPreset[] = [];

const slug = (name: string) => name.toLowerCase().replace(/\s+/g, '-');

const addKick = (name: string, freq: number, decay: number, sweep: number, osc: OscillatorType = 'sine') => {
  SOUND_LIBRARY.push({
    id: slug(name),
    name,
    category: 'Kick',
    type: 'osc',
    oscType: osc,
    baseFreq: freq,
    decay,
    sweep,
    attack: 0.005,
  });
};

const addSnare = (name: string, freq: number, decay: number, filterFreq: number) => {
  SOUND_LIBRARY.push({
    id: slug(name),
    name,
    category: 'Snare',
    type: 'noise',
    noiseColor: 'white',
    baseFreq: freq,
    decay,
    filterType: 'highpass',
    filterFreq,
    filterQ: 1,
    attack: 0.005,
  });
};

const addHiHat = (name: string, decay: number, filterFreq: number) => {
  SOUND_LIBRARY.push({
    id: slug(name),
    name,
    category: 'HiHat',
    type: 'noise',
    noiseColor: 'white',
    baseFreq: 0,
    decay,
    filterType: 'bandpass',
    filterFreq,
    filterQ: 5,
    attack: 0.005,
  });
};

const addPerc = (name: string, freq: number, decay: number, osc: OscillatorType = 'triangle', sweep = 1) => {
  SOUND_LIBRARY.push({
    id: slug(name),
    name,
    category: 'Perc',
    type: 'osc',
    oscType: osc,
    baseFreq: freq,
    decay,
    sweep,
    attack: 0.005,
  });
};

const addSynth = (
  name: string,
  freq: number,
  decay: number,
  osc: OscillatorType = 'sawtooth',
  filterFreq = 2000
) => {
  SOUND_LIBRARY.push({
    id: slug(name),
    name,
    category: 'Synth',
    type: 'osc',
    oscType: osc,
    baseFreq: freq,
    decay,
    sweep: 1,
    filterType: 'lowpass',
    filterFreq,
    filterQ: 5,
    attack: 0.05,
  });
};

for (let i = 0; i < 60; i++) {
  addKick(`Kick ${i}`, 40 + i * 2, 0.4, 0.01);
  addSnare(`Snare ${i}`, 200 + i * 5, 0.3, 800 + i * 50);
  addHiHat(`HiHat ${i}`, 0.05 + i * 0.01, 5000 + i * 100);
  addPerc(`Perc ${i}`, 100 + i * 20, 0.2, 'triangle', 0.5);
  addSynth(`Synth ${i}`, 100 + i * 10, 0.5, 'sawtooth', 500 + i * 100);
}

// Named aliases used by sample packs / defaults
const aliases: Array<[string, string, SoundCategory, Partial<SoundPreset>]> = [
  ['classic-kick', 'Classic Kick', 'Kick', { type: 'osc', oscType: 'sine', baseFreq: 55, decay: 0.45, sweep: 0.08 }],
  ['classic-snare', 'Classic Snare', 'Snare', { type: 'noise', baseFreq: 220, decay: 0.28, filterType: 'highpass', filterFreq: 1200 }],
  ['classic-hihat', 'Classic HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.08, filterType: 'bandpass', filterFreq: 8000, filterQ: 4 }],
  ['classic-perc', 'Classic Perc', 'Perc', { type: 'osc', oscType: 'triangle', baseFreq: 380, decay: 0.18, sweep: 0.4 }],
  ['lo-fi-kick', 'Lo-Fi Kick', 'Kick', { type: 'osc', oscType: 'sine', baseFreq: 48, decay: 0.55, sweep: 0.05 }],
  ['lo-fi-snare', 'Lo-Fi Snare', 'Snare', { type: 'noise', baseFreq: 180, decay: 0.35, filterType: 'highpass', filterFreq: 900 }],
  ['lo-fi-hihat', 'Lo-Fi HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.12, filterType: 'bandpass', filterFreq: 6000, filterQ: 3 }],
  ['saw-bass', 'Saw Bass', 'Synth', { type: 'osc', oscType: 'sawtooth', baseFreq: 55, decay: 0.6, filterType: 'lowpass', filterFreq: 800, filterQ: 6 }],
  ['deep-kick', 'Deep Kick', 'Kick', { type: 'osc', oscType: 'sine', baseFreq: 42, decay: 0.5, sweep: 0.04 }],
  ['rim-snare', 'Rim Snare', 'Snare', { type: 'noise', baseFreq: 400, decay: 0.15, filterType: 'bandpass', filterFreq: 2000, filterQ: 3 }],
  ['soft-hihat', 'Soft HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.1, filterType: 'highpass', filterFreq: 7000 }],
  ['square-bass', 'Square Bass', 'Synth', { type: 'osc', oscType: 'square', baseFreq: 60, decay: 0.5, filterType: 'lowpass', filterFreq: 600, filterQ: 4 }],
  ['hard-kick', 'Hard Kick', 'Kick', { type: 'osc', oscType: 'sine', baseFreq: 65, decay: 0.35, sweep: 0.02 }],
  ['trap-snare', 'Trap Snare', 'Snare', { type: 'noise', baseFreq: 250, decay: 0.4, filterType: 'highpass', filterFreq: 1500 }],
  ['trap-hihat', 'Trap HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.04, filterType: 'bandpass', filterFreq: 10000, filterQ: 6 }],
  ['808-bass', '808 Bass', 'Synth', { type: 'osc', oscType: 'sine', baseFreq: 40, decay: 0.9, sweep: 0.5, filterType: 'lowpass', filterFreq: 400 }],
  ['massive-kick', 'Massive Kick', 'Kick', { type: 'osc', oscType: 'sine', baseFreq: 50, decay: 0.6, sweep: 0.03 }],
  ['snap-snare', 'Snap Snare', 'Snare', { type: 'noise', baseFreq: 300, decay: 0.2, filterType: 'highpass', filterFreq: 2500 }],
  ['crisp-hihat', 'Crisp HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.06, filterType: 'bandpass', filterFreq: 11000, filterQ: 5 }],
  ['sub-bass', 'Sub Bass', 'Synth', { type: 'osc', oscType: 'sine', baseFreq: 35, decay: 0.8, sweep: 0.7 }],
  ['punchy-kick', 'Punchy Kick', 'Kick', { type: 'osc', oscType: 'triangle', baseFreq: 70, decay: 0.3, sweep: 0.05 }],
  ['tight-snare', 'Tight Snare', 'Snare', { type: 'noise', baseFreq: 280, decay: 0.18, filterType: 'highpass', filterFreq: 1800 }],
  ['sharp-hihat', 'Sharp HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.05, filterType: 'highpass', filterFreq: 9000 }],
  ['acid', 'Acid', 'Synth', { type: 'osc', oscType: 'sawtooth', baseFreq: 110, decay: 0.35, filterType: 'lowpass', filterFreq: 900, filterQ: 12 }],
  ['boom-kick', 'Boom Kick', 'Kick', { type: 'osc', oscType: 'sine', baseFreq: 52, decay: 0.5, sweep: 0.06 }],
  ['clap-snare', 'Clap Snare', 'Snare', { type: 'noise', baseFreq: 200, decay: 0.32, filterType: 'bandpass', filterFreq: 1600, filterQ: 2 }],
  ['open-hihat', 'Open HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.35, filterType: 'bandpass', filterFreq: 7500, filterQ: 2 }],
  ['saw-lead', 'Saw Lead', 'Synth', { type: 'osc', oscType: 'sawtooth', baseFreq: 220, decay: 0.4, filterType: 'lowpass', filterFreq: 3000, filterQ: 3 }],
  ['zap-kick', 'Zap Kick', 'Kick', { type: 'osc', oscType: 'square', baseFreq: 80, decay: 0.25, sweep: 0.01 }],
  ['electronic-snare', 'Electronic Snare', 'Snare', { type: 'noise', baseFreq: 320, decay: 0.25, filterType: 'highpass', filterFreq: 2000 }],
  ['sizzle-hihat', 'Sizzle HiHat', 'HiHat', { type: 'noise', baseFreq: 0, decay: 0.2, filterType: 'highpass', filterFreq: 8500 }],
  ['pad', 'Pad', 'Synth', { type: 'osc', oscType: 'sawtooth', baseFreq: 130, decay: 1.2, filterType: 'lowpass', filterFreq: 1200, filterQ: 2, attack: 0.15 }],
];

for (const [id, name, category, partial] of aliases) {
  if (!SOUND_LIBRARY.find((s) => s.id === id)) {
    SOUND_LIBRARY.push({
      id,
      name,
      category,
      type: (partial.type as SoundPreset['type']) || 'osc',
      oscType: partial.oscType || 'sine',
      baseFreq: partial.baseFreq ?? 100,
      decay: partial.decay ?? 0.3,
      sweep: partial.sweep ?? 1,
      filterType: partial.filterType,
      filterFreq: partial.filterFreq,
      filterQ: partial.filterQ,
      noiseColor: partial.noiseColor || 'white',
      attack: partial.attack ?? 0.005,
    });
  }
}

export const SAMPLE_PACK_SOUNDS: Record<string, string[]> = {
  'classic-hiphop': ['classic-kick', 'classic-snare', 'classic-hihat', 'classic-perc'],
  'lofi-nights': ['lo-fi-kick', 'lo-fi-snare', 'lo-fi-hihat', 'saw-bass'],
  'rnb-grooves': ['deep-kick', 'rim-snare', 'soft-hihat', 'square-bass'],
  'trap-essentials': ['hard-kick', 'trap-snare', 'trap-hihat', '808-bass'],
  'modern-trap': ['massive-kick', 'snap-snare', 'crisp-hihat', 'sub-bass'],
  'drill-essential': ['punchy-kick', 'tight-snare', 'sharp-hihat', 'acid'],
  'pop-anthem': ['boom-kick', 'clap-snare', 'open-hihat', 'saw-lead'],
  'future-bass': ['zap-kick', 'electronic-snare', 'sizzle-hihat', 'pad'],
};

export const PACKS = {
  Techno: ['kick-10', 'snare-20', 'hihat-30', 'synth-40'],
  Ambient: ['perc-5', 'synth-10', 'synth-20', 'perc-50'],
  HipHop: ['classic-kick', 'classic-snare', 'classic-hihat', 'classic-perc'],
  House: ['kick-20', 'snare-10', 'hihat-5', 'synth-5'],
};

/** 24 master enhancements (Upgrade Rack) */
export const ENHANCEMENTS = [
  { id: 'tube', name: 'Tube', description: 'Soft tube saturation' },
  { id: 'crunch', name: 'Crunch', description: 'Aggressive distortion' },
  { id: 'warmth', name: 'Warmth', description: 'Low-mid warmth boost' },
  { id: 'excite', name: 'Excite', description: 'Harmonic exciter' },
  { id: 'air', name: 'Air', description: 'High-shelf air' },
  { id: 'sub', name: 'Sub', description: 'Sub bass boost' },
  { id: 'punch', name: 'Punch', description: 'Transient punch' },
  { id: 'snap', name: 'Snap', description: 'Snappy highs' },
  { id: 'grit', name: 'Grit', description: 'Mid grit drive' },
  { id: 'fuzz', name: 'Fuzz', description: 'Heavy fuzz' },
  { id: 'lofi', name: 'LoFi', description: 'Bit-rate style dulling' },
  { id: 'tape', name: 'Tape', description: 'Tape compression color' },
  { id: 'vinyl', name: 'Vinyl', description: 'Vinyl crackle filter' },
  { id: 'bitcrush', name: 'Bitcrush', description: 'Digital crush' },
  { id: 'chorus', name: 'Chorus', description: 'Chorus width' },
  { id: 'flange', name: 'Flange', description: 'Flanger sweep' },
  { id: 'phase', name: 'Phase', description: 'Phaser movement' },
  { id: 'widener', name: 'Widener', description: 'Stereo width' },
  { id: 'stereo', name: 'Stereo', description: 'Full stereo field' },
  { id: 'mono', name: 'Mono', description: 'Force mono sum' },
  { id: 'gate', name: 'Gate', description: 'Noise gate feel' },
  { id: 'deess', name: 'De-Ess', description: 'De-ess harsh highs' },
  { id: 'transient', name: 'Transient', description: 'Attack shaper' },
  { id: 'limit', name: 'Limit', description: 'Peak limit boost' },
] as const;

export type EnhancementId = (typeof ENHANCEMENTS)[number]['id'];

export const defaultEnhancements = (): Record<EnhancementId, boolean> =>
  Object.fromEntries(ENHANCEMENTS.map((e) => [e.id, false])) as Record<EnhancementId, boolean>;

export const getSoundPreset = (id: string): SoundPreset =>
  SOUND_LIBRARY.find((s) => s.id === id) || SOUND_LIBRARY[0];

export const NOTE_TO_SEMITONE: Record<string, number> = {
  C: 0,
  'C#': 1,
  Db: 1,
  D: 2,
  'D#': 3,
  Eb: 3,
  E: 4,
  F: 5,
  'F#': 6,
  Gb: 6,
  G: 7,
  'G#': 8,
  Ab: 8,
  A: 9,
  'A#': 10,
  Bb: 10,
  B: 11,
};
