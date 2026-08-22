import { useState } from 'react';
import { Activity, Radio, BarChart, HardDrive, Settings2, Sliders, Disc3, Mic2, Wand2 } from 'lucide-react';
import { ENHANCEMENTS, EnhancementId } from '../audio/SoundLibrary';

const CHORDS = [
  { name: 'C Maj', notes: ['C', 'E', 'G'] },
  { name: 'A Min', notes: ['A', 'C', 'E'] },
  { name: 'F Maj', notes: ['F', 'A', 'C'] },
  { name: 'G Maj', notes: ['G', 'B', 'D'] },
];

export function SpectralAnalyzer({
  bars,
  isPlaying,
}: {
  bars: number[];
  isPlaying: boolean;
}) {
  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden group shrink-0">
      <div className="absolute inset-0 bg-linear-to-t from-cyan-900/20 to-transparent pointer-events-none" />
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-cyan-400" />
          <h3 className="font-semibold text-zinc-200 leading-none">Spectral Analyzer</h3>
        </div>
        <span className="text-[10px] uppercase tracking-[0.25em] text-cyan-400/80">
          {isPlaying ? 'Live' : 'Idle'}
        </span>
      </div>
      <div className="h-24 w-full bg-zinc-900 rounded-lg flex items-end justify-between p-2 border border-zinc-800/50">
        {bars.map((height, i) => (
          <div
            key={i}
            className="w-full mx-0.5 bg-cyan-500/60 rounded-t-sm transition-all duration-150"
            style={{ height: `${height}%` }}
          />
        ))}
      </div>
    </div>
  );
}

export function MasterLimiter({
  threshold,
  release,
  onThresholdChange,
  onReleaseChange,
}: {
  threshold: number;
  release: number;
  onThresholdChange: (value: number) => void;
  onReleaseChange: (value: number) => void;
}) {

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden group shrink-0">
      <div className="flex items-center gap-2 mb-4">
        <Settings2 className="w-5 h-5 text-purple-400" />
        <h3 className="font-semibold text-zinc-200 leading-none">Master Limiter</h3>
      </div>
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <div className="flex justify-between items-center text-xs">
            <span className="text-zinc-400 font-medium">Threshold</span>
            <span className="text-purple-400 font-bold">{threshold.toFixed(1)} dB</span>
          </div>
          <input type="range" className="w-full accent-purple-500" min="-24" max="0" step="0.1" value={threshold} onChange={(e) => onThresholdChange(Number(e.target.value))} />
        </div>
        <div className="flex flex-col gap-1">
          <div className="flex justify-between items-center text-xs">
            <span className="text-zinc-400 font-medium">Release</span>
            <span className="text-purple-400 font-bold">{release} ms</span>
          </div>
          <input type="range" className="w-full accent-purple-500" min="10" max="500" step="1" value={release} onChange={(e) => onReleaseChange(Number(e.target.value))} />
        </div>
        <div className="text-[11px] text-zinc-500">
          Ceiling protection preview for final output shaping.
        </div>
      </div>
    </div>
  );
}

export function TapeSaturation({ drive, onDriveChange }: { drive: number; onDriveChange: (value: number) => void }) {

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden group shrink-0">
      <div className="flex items-center gap-2 mb-4">
        <Disc3 className="w-5 h-5 text-amber-400" />
        <h3 className="font-semibold text-zinc-200 leading-none">Tape Saturation</h3>
      </div>
      <div className="flex gap-6 items-center">
        <div
          className="w-16 h-16 rounded-full border-[6px] border-zinc-800 border-t-amber-500 border-l-amber-500 shadow-[0_0_15px_rgba(245,158,11,0.2)]"
          style={{ animation: `spin ${Math.max(1.2, 5 - drive / 25)}s linear infinite` }}
        />
        <div className="flex-1 flex flex-col justify-center gap-2">
          <div className="flex justify-between items-center text-xs">
            <span className="text-zinc-400 font-medium">Drive</span>
            <span className="text-amber-400 font-bold">{drive}%</span>
          </div>
          <input type="range" className="w-full accent-amber-500" min="0" max="100" value={drive} onChange={(e) => onDriveChange(Number(e.target.value))} />
        </div>
      </div>
    </div>
  );
}

export function MultiBandCompressor({
  low,
  mid,
  high,
  onChange,
}: {
  low: number;
  mid: number;
  high: number;
  onChange: (band: 'low' | 'mid' | 'high', value: number) => void;
}) {

  const bands = [
    { label: 'Low', key: 'low' as const, value: low, color: 'text-cyan-400' },
    { label: 'Mid', key: 'mid' as const, value: mid, color: 'text-fuchsia-400' },
    { label: 'High', key: 'high' as const, value: high, color: 'text-emerald-400' },
  ];

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden shrink-0">
      <div className="flex items-center gap-2 mb-4">
        <Sliders className="w-5 h-5 text-cyan-400" />
        <h3 className="font-semibold text-zinc-200 leading-none">Multi-Band Compressor</h3>
      </div>
      <div className="space-y-3">
        {bands.map((band) => (
          <div key={band.label} className="space-y-1">
            <div className="flex items-center justify-between text-xs">
              <span className="text-zinc-400">{band.label}</span>
              <span className={`${band.color} font-bold`}>{band.value}%</span>
            </div>
            <input
              type="range"
              min="0"
              max="100"
              value={band.value}
              onChange={(e) => onChange(band.key, Number(e.target.value))}
              className="w-full accent-cyan-500"
            />
          </div>
        ))}
      </div>
    </div>
  );
}

export function LfoModulator({
  rate,
  depth,
  onRateChange,
  onDepthChange,
}: {
  rate: number;
  depth: number;
  onRateChange: (value: number) => void;
  onDepthChange: (value: number) => void;
}) {

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden shrink-0">
      <div className="flex items-center gap-2 mb-4">
        <Radio className="w-5 h-5 text-cyan-400" />
        <h3 className="font-semibold text-zinc-200 leading-none">LFO Modulator</h3>
      </div>
      <div className="space-y-3">
        <div className="text-[11px] text-zinc-500">Animate movement and pulse depth for synth gestures.</div>
        <div>
          <div className="flex justify-between text-xs mb-1"><span className="text-zinc-400">Rate</span><span className="text-cyan-400 font-bold">{rate}%</span></div>
          <input type="range" min="0" max="100" value={rate} onChange={(e) => onRateChange(Number(e.target.value))} className="w-full accent-cyan-500" />
        </div>
        <div>
          <div className="flex justify-between text-xs mb-1"><span className="text-zinc-400">Depth</span><span className="text-cyan-400 font-bold">{depth}%</span></div>
          <input type="range" min="0" max="100" value={depth} onChange={(e) => onDepthChange(Number(e.target.value))} className="w-full accent-cyan-500" />
        </div>
      </div>
    </div>
  );
}

export function ReverbChamber({
  reverb,
  delay,
  onReverbChange,
  onDelayChange,
}: {
  reverb: number;
  delay: number;
  onReverbChange: (value: number) => void;
  onDelayChange: (value: number) => void;
}) {
  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden shrink-0">
      <div className="flex items-center gap-2 mb-4">
        <Mic2 className="w-5 h-5 text-blue-400" />
        <h3 className="font-semibold text-zinc-200 leading-none">Reverb Chamber</h3>
      </div>
      <div className="space-y-4">
        <div>
          <div className="flex justify-between text-xs mb-1">
            <span className="text-zinc-400">Room Size</span>
            <span className="text-blue-400 font-bold">{reverb}%</span>
          </div>
          <input type="range" min="0" max="100" value={reverb} onChange={(e) => onReverbChange(Number(e.target.value))} className="w-full accent-blue-500" />
        </div>
        <div>
          <div className="flex justify-between text-xs mb-1">
            <span className="text-zinc-400">Echo Tail</span>
            <span className="text-blue-400 font-bold">{delay}%</span>
          </div>
          <input type="range" min="0" max="100" value={delay} onChange={(e) => onDelayChange(Number(e.target.value))} className="w-full accent-blue-500" />
        </div>
      </div>
    </div>
  );
}

export function ChordGenerator({
  targetTrack,
  onTargetTrackChange,
  onApplyChord,
  trackCount,
}: {
  targetTrack: number;
  onTargetTrackChange: (track: number) => void;
  onApplyChord: (track: number, notes: string[]) => void;
  trackCount: number;
}) {
  const handleDragStart = (e: React.DragEvent, chord: { name: string; notes: string[] }) => {
    e.dataTransfer.setData('application/json', JSON.stringify({ type: 'chord', ...chord }));
  };

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden group shrink-0">
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2">
          <HardDrive className="w-5 h-5 text-purple-400" />
          <h3 className="font-semibold text-zinc-200 leading-none">Chord Gen</h3>
        </div>
        <select
          value={targetTrack}
          onChange={(e) => onTargetTrackChange(Number(e.target.value))}
          className="bg-zinc-900 border border-zinc-700 rounded px-2 py-1 text-xs text-zinc-300"
        >
          {Array.from({ length: trackCount }, (_, i) => (
            <option key={i} value={i}>Track {i + 1}</option>
          ))}
        </select>
      </div>
      <div className="flex flex-col gap-2">
        <span className="text-[10px] text-zinc-500 uppercase tracking-widest text-center">Click to apply or drag to sequencer</span>
        <div className="grid grid-cols-2 gap-2">
          {CHORDS.map(chord => (
            <button
              key={chord.name}
              type="button"
              draggable
              onDragStart={(e) => handleDragStart(e, chord)}
              onClick={() => onApplyChord(targetTrack, chord.notes)}
              className="bg-purple-500/10 border border-purple-500/30 rounded p-2 text-center cursor-grab active:cursor-grabbing hover:bg-purple-500/20 transition-colors"
            >
              <div className="text-purple-200 font-bold text-xs">{chord.name}</div>
              <div className="text-[10px] text-purple-400 mt-1">{chord.notes.join(' - ')}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

export function Arpeggiator({
  targetTrack,
  onTargetTrackChange,
  onGenerate,
  mode,
  onModeChange,
  trackCount,
}: {
  targetTrack: number;
  onTargetTrackChange: (track: number) => void;
  onGenerate: (track: number) => void;
  mode: 'up' | 'bounce' | 'gate';
  onModeChange: (mode: 'up' | 'bounce' | 'gate') => void;
  trackCount: number;
}) {
  const bars = mode === 'up'
    ? [1, 2, 3, 4, 5, 6]
    : mode === 'gate'
      ? [4, 1, 4, 1, 4, 1]
      : [1, 2, 3, 4, 3, 2];

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden group shrink-0">
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2">
          <BarChart className="w-5 h-5 text-orange-400" />
          <h3 className="font-semibold text-zinc-200 leading-none">Arpeggiator</h3>
        </div>
        <select
          value={targetTrack}
          onChange={(e) => onTargetTrackChange(Number(e.target.value))}
          className="bg-zinc-900 border border-zinc-700 rounded px-2 py-1 text-xs text-zinc-300"
        >
          {Array.from({ length: trackCount }, (_, i) => (
            <option key={i} value={i}>Track {i + 1}</option>
          ))}
        </select>
      </div>
      <div className="space-y-3">
        <div className="flex gap-2">
          {(['up', 'bounce', 'gate'] as const).map((patternMode) => (
            <button
              key={patternMode}
              type="button"
              onClick={() => onModeChange(patternMode)}
              className={`px-2 py-1 rounded text-[10px] uppercase tracking-widest border transition-colors ${mode === patternMode ? 'bg-orange-500/20 border-orange-400 text-orange-300' : 'bg-zinc-900 border-zinc-700 text-zinc-500 hover:text-zinc-300'}`}
            >
              {patternMode}
            </button>
          ))}
        </div>
        <div className="flex items-end gap-1 h-12 justify-center">
          {bars.map((step, i) => (
            <div key={i} className="w-3 bg-orange-500/50 rounded-t transition-all" style={{ height: `${step * 16}%` }} />
          ))}
        </div>
        <button
          type="button"
          onClick={() => onGenerate(targetTrack)}
          className="w-full flex items-center justify-center gap-2 rounded-lg border border-orange-400/40 bg-orange-500/10 px-3 py-2 text-sm text-orange-300 hover:bg-orange-500/20 transition-colors"
        >
          <Wand2 className="w-4 h-4" />
          Generate Arp Pattern
        </button>
      </div>
    </div>
  );
}

export function EnhancementsRack({
  enabled,
  onToggle,
}: {
  enabled: Record<EnhancementId, boolean>;
  onToggle: (id: EnhancementId) => void;
}) {
  const enabledCount = Object.values(enabled).filter(Boolean).length;

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl relative overflow-hidden shrink-0">
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2">
          <Settings2 className="w-5 h-5 text-cyan-400" />
          <h3 className="font-semibold text-zinc-200 leading-none">24 Enhancements Rack</h3>
        </div>
        <span className="text-[10px] uppercase tracking-[0.25em] text-cyan-400/80">{enabledCount} on</span>
      </div>
      <div className="grid grid-cols-4 sm:grid-cols-6 gap-2">
        {ENHANCEMENTS.map((enhancement) => {
          const active = enabled[enhancement.id];
          return (
            <button key={enhancement.id} type="button" onClick={() => onToggle(enhancement.id)} className="flex flex-col items-center gap-1 group cursor-pointer" title={enhancement.description}>
              <div className={`w-3 h-3 rounded-full border transition-all ${active ? 'bg-cyan-500 border-cyan-400 shadow-[0_0_10px_rgba(34,211,238,0.5)]' : 'bg-zinc-900 border-zinc-700 group-hover:border-zinc-500'}`} />
              <span className={`text-[8px] font-bold uppercase tracking-tighter ${active ? 'text-zinc-200' : 'text-zinc-500 group-hover:text-zinc-300'}`}>{enhancement.name}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}