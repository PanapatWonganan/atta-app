#!/usr/bin/env python3
"""Synthesize the Practice mood loops into app/src/main/res/raw/.

Pure stdlib. Each file is a 20 s seamless mono loop, 22050 Hz, 16-bit PCM.
LFO rates complete integer cycles per loop; noise-based moods get a 1.5 s
tail-into-head crossfade so the seam is inaudible.
"""

import array
import math
import os
import random
import wave

SR = 22050
SECONDS = 20
N = SR * SECONDS
OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


def lfo(i, cycles, lo=0.0, hi=1.0, power=1.0):
    phase = 2 * math.pi * cycles * i / N
    v = 0.5 - 0.5 * math.cos(phase)
    return lo + (hi - lo) * (v ** power)


def calm():
    """Two soft detuned drones with breathing amplitude and a faint noise bed."""
    rng = random.Random(7)
    out = [0.0] * N
    lp = 0.0
    for i in range(N):
        t = i / SR
        a = math.sin(2 * math.pi * 96.0 * t) * 0.5
        b = math.sin(2 * math.pi * 144.0 * t) * 0.3
        c = math.sin(2 * math.pi * 192.0 * t) * 0.12
        drone = (a * lfo(i, 3, 0.55, 1.0) + b * lfo(i, 2, 0.5, 1.0) + c * lfo(i, 5, 0.3, 1.0))
        lp += 0.02 * (rng.uniform(-1, 1) - lp)  # very dark noise bed
        out[i] = drone * 0.5 + lp * 0.9
    return out


def rain():
    """Lowpassed noise with a light sparkle band — steady rainfall."""
    rng = random.Random(11)
    out = [0.0] * N
    lp1 = lp2 = hp_prev_in = hp_prev = 0.0
    for i in range(N):
        w = rng.uniform(-1, 1)
        lp1 += 0.28 * (w - lp1)          # body of the rain
        lp2 += 0.55 * (w - lp2)          # brighter layer
        hp = 0.95 * (hp_prev + lp2 - hp_prev_in)  # keep only its hiss
        hp_prev_in, hp_prev = lp2, hp
        out[i] = (lp1 * 0.75 + hp * 0.5) * lfo(i, 4, 0.88, 1.0)
    return out


def waves():
    """Deep filtered noise swelling twice per loop — slow surf."""
    rng = random.Random(13)
    out = [0.0] * N
    lp1 = lp2 = 0.0
    for i in range(N):
        w = rng.uniform(-1, 1)
        lp1 += 0.09 * (w - lp1)
        lp2 += 0.05 * (lp1 - lp2)
        swell = lfo(i, 2, 0.18, 1.0, power=1.6)
        wash = lfo(i, 2, 0.5, 1.0)  # brightness follows the swell
        out[i] = (lp2 * 2.2 * wash + lp1 * 0.25) * swell
    return out


def loop_crossfade(samples, seconds=1.5):
    """Blend the tail into the head and trim it, so end==start character."""
    n = int(SR * seconds)
    total = len(samples)
    for i in range(n):
        f = i / n
        samples[i] = samples[i] * f + samples[total - n + i] * (1 - f)
    return samples[: total - n]


def write(name, samples, peak=0.42):
    top = max(abs(s) for s in samples) or 1.0
    scale = peak / top * 32767
    pcm = array.array("h", (int(max(-32767, min(32767, s * scale))) for s in samples))
    path = os.path.join(OUT_DIR, name)
    with wave.open(path, "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(SR)
        f.writeframes(pcm.tobytes())
    print(f"{name}: {os.path.getsize(path) / 1e6:.2f} MB, {len(samples) / SR:.1f}s")


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    write("mood_calm.wav", calm())  # periodic by construction, no crossfade
    write("mood_rain.wav", loop_crossfade(rain()))
    write("mood_waves.wav", loop_crossfade(waves()))
