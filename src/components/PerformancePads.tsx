import { useState, useEffect } from 'react';
import { Activity } from 'lucide-react';
import { audioEngine } from '../audio/AudioEngine';
import { SOUND_LIBRARY } from '../audio/SoundLibrary';

const PAD_SOUNDS = [
  { id: 'kick-10', name: 'Punch' },
  { id: 'snare-20', name: 'Crack' },
  { id: 'hihat-30', name: 'Crisp' },
  { id: 'perc-40', name: 'Clave' },
  { id: 'kick-50', name: 'Boom' },
  { id: 'snare-55', name: 'Clap' },
  { id: 'hihat-50', name: 'Open' },
  { id: 'synth-45', name: 'Bass' },
  { id: 'synth-10', name: 'Lead' },
  { id: 'synth-20', name: 'Pluck' },
  { id: 'synth-30', name: 'Pad' },
  { id: 'synth-50', name: 'Bell' },
  { id: 'perc-10', name: 'Wood' },
  { id: 'perc-20', name: 'Cow' },
  { id: 'perc-30', name: 'Zap' },
  { id: 'perc-50', name: 'Laser' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
  { id: 'empty', name: '' },
];

export default function PerformancePads() {
  const [activePad, setActivePad] = useState<number | null>(null);
  const [explosions, setExplosions] = useState<{ id: number, x: number, y: number }[]>([]);

  const triggerPad = (i: number, e: React.PointerEvent) => {
    audioEngine.init();
    setActivePad(i);
    
    // Create explosion effect
    const explosion = { id: Date.now(), x: e.clientX, y: e.clientY };
    setExplosions(prev => [...prev, explosion]);
    setTimeout(() => setExplosions(prev => prev.filter(exp => exp.id !== explosion.id)), 500);

    const soundId = PAD_SOUNDS[i].id;
    if (soundId !== 'empty') {
      audioEngine.playSound(20 + i, soundId);
    }
  };

  return (
    <div className="bg-zinc-950/60 backdrop-blur-xl border border-zinc-800/50 rounded-2xl p-5 shadow-2xl transition-all duration-300 hover:shadow-purple-500/10">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-purple-400 drop-shadow-[0_0_8px_rgba(168,85,247,0.5)]" />
          <h3 className="font-semibold text-zinc-200">Performance Pads</h3>
        </div>
      </div>
      <div className="grid grid-cols-6 gap-2">
        {PAD_SOUNDS.map((pad, i) => (
          <div 
            key={i} 
            onPointerDown={(e) => triggerPad(i, e)}
            onPointerUp={() => setActivePad(null)}
            onPointerLeave={() => setActivePad(null)}
            className={`
              aspect-square rounded-lg transition-all cursor-pointer relative overflow-hidden flex items-center justify-center
              ${activePad === i 
                ? 'bg-purple-600 shadow-[inset_0_2px_4px_rgba(0,0,0,0.3),0_0_15px_rgba(168,85,247,0.8)] border border-purple-400' 
                : 'bg-linear-to-br from-zinc-700 via-zinc-800 to-zinc-900 shadow-[0_4px_6px_rgba(0,0,0,0.4)] border border-zinc-600/30 hover:border-purple-500/50 hover:shadow-[0_0_10px_rgba(168,85,247,0.2)]'}
            `} 
          >
            <div className="absolute inset-0 bg-linear-to-br from-white/10 to-transparent pointer-events-none" />
            <span className="text-[9px] font-bold text-zinc-300 text-center uppercase tracking-wider">{pad.name}</span>
          </div>
        ))}
        {explosions.map(exp => (
          <div 
            key={exp.id} 
            className="fixed w-4 h-4 bg-purple-500 rounded-full animate-ping pointer-events-none"
            style={{ left: exp.x - 8, top: exp.y - 8 }}
          />
        ))}
      </div>
    </div>
  );
}
