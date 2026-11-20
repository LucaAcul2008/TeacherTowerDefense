"""Erzeugt alle Soundeffekte und die Musik fuer Teacher Tower Defense (Retro/Chiptune-Stil).

Alles wird synthetisch erzeugt - keine fremden Dateien, keine Lizenzfragen.
Nur Python-Standardbibliothek.

Aufruf (im Projektordner):  python tools/gen_sounds.py
Schreibt nach src/main/resources/assets/sounds/ und .../assets/music/
"""
import math
import os
import random
import struct
import wave

SR = 22050
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets")
rng = random.Random(42)

NOTEN = {"C": 0, "C#": 1, "D": 2, "D#": 3, "E": 4, "F": 5, "F#": 6, "G": 7, "G#": 8, "A": 9, "A#": 10, "B": 11}


def hz(note):
    """'A4' -> 440.0"""
    name, okt = note[:-1], int(note[-1])
    midi = 12 * (okt + 1) + NOTEN[name]
    return 440.0 * 2 ** ((midi - 69) / 12)


# ------------------------------------------------------------------ Bausteine

def welle(art, phase, duty=0.5):
    p = phase % 1.0
    if art == "sin":
        return math.sin(2 * math.pi * p)
    if art == "sq":
        return 1.0 if p < duty else -1.0
    if art == "tri":
        return 4 * p - 1 if p < 0.5 else 3 - 4 * p
    if art == "saw":
        return 2 * p - 1
    raise ValueError(art)


def ton(f0, dauer, art="sq", vol=0.5, f1=None, attack=0.005, release=0.05, duty=0.5, vibrato=0.0, kurve=1.0):
    """Ein Ton mit optionalem Frequenz-Slide (f0 -> f1) und Huellkurve."""
    n = int(dauer * SR)
    out = [0.0] * n
    phase = 0.0
    f1 = f0 if f1 is None else f1
    for i in range(n):
        t = i / SR
        x = i / max(1, n - 1)
        f = f0 + (f1 - f0) * (x ** kurve)
        if vibrato:
            f *= 1 + 0.012 * math.sin(2 * math.pi * vibrato * t)
        phase += f / SR
        env = min(1.0, t / attack) if attack > 0 else 1.0
        rest = dauer - t
        if rest < release:
            env *= max(0.0, rest / release)
        out[i] = welle(art, phase, duty) * vol * env
    return out


def rauschen(dauer, vol=0.5, abfall=8.0, tiefpass=0.0):
    n = int(dauer * SR)
    out, letzter = [0.0] * n, 0.0
    for i in range(n):
        r = rng.uniform(-1, 1)
        letzter = letzter * tiefpass + r * (1 - tiefpass)
        out[i] = letzter * vol * math.exp(-abfall * i / SR)
    return out


def glocke(f, dauer, vol=0.5):
    """Glockenklang: Sinus mit unharmonischen Obertoenen, langes Ausklingen."""
    n = int(dauer * SR)
    teile = [(1.0, 1.0, 3.0), (2.76, 0.45, 5.0), (5.40, 0.25, 8.0), (8.93, 0.12, 12.0)]
    out = [0.0] * n
    for i in range(n):
        t = i / SR
        s = 0.0
        for mult, amp, abfall in teile:
            s += amp * math.sin(2 * math.pi * f * mult * t) * math.exp(-abfall * t)
        out[i] = s * vol * min(1.0, t / 0.003)
    return out


def mische(ziel, quelle, start_sek=0.0, gain=1.0):
    s = int(start_sek * SR)
    if len(ziel) < s + len(quelle):
        ziel.extend([0.0] * (s + len(quelle) - len(ziel)))
    for i, v in enumerate(quelle):
        ziel[s + i] += v * gain
    return ziel


def folge(noten, dauer, art="sq", vol=0.4, luecke=0.0, **kw):
    out = []
    for i, n in enumerate(noten):
        if n:
            mische(out, ton(hz(n), dauer, art, vol, **kw), i * (dauer + luecke))
        else:
            mische(out, [0.0] * int(dauer * SR), i * (dauer + luecke))
    return out


def speichern(pfad, samples, peak=0.8):
    m = max(1e-9, max(abs(v) for v in samples))
    k = peak / m if m > peak else 1.0
    os.makedirs(os.path.dirname(pfad), exist_ok=True)
    with wave.open(pfad, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, v * k)) * 32767)) for v in samples))
    print(f"  {os.path.relpath(pfad, ROOT):32s} {len(samples) / SR:5.2f}s")


def sfx(name, samples, peak=0.8):
    speichern(os.path.join(ROOT, "sounds", name + ".wav"), samples, peak)


# ------------------------------------------------------------------ Effekte

def effekte():
    # Wuerfe (kurz und leise - kommen oft vor)
    w = rauschen(0.16, 0.5, abfall=6, tiefpass=0.85)
    mische(w, ton(500, 0.16, "sin", 0.25, f1=250))
    sfx("wurf_fisch", w, 0.45)
    sfx("wurf_trank", ton(320, 0.12, "sin", 0.6, f1=780, kurve=0.6, release=0.04), 0.45)
    sfx("wurf_floppy", ton(1100, 0.045, "sq", 0.4, f1=850, duty=0.25, release=0.02), 0.25)
    sfx("wurf_golf", mische(rauschen(0.05, 0.6, abfall=60), ton(1400, 0.05, "sin", 0.4, f1=900)), 0.4)
    sfx("wurf_musik", folge(["E6", "G6"], 0.04, "sq", 0.4, duty=0.25), 0.25)

    # Treffer / Schueler besiegt
    p = ton(700, 0.08, "sin", 0.7, f1=160, kurve=0.5, release=0.03)
    mische(p, rauschen(0.03, 0.25, abfall=80))
    sfx("pop", p, 0.5)

    # Trank-Explosion
    e = rauschen(0.4, 0.9, abfall=9, tiefpass=0.6)
    mische(e, ton(120, 0.35, "sin", 0.7, f1=45, release=0.2))
    mische(e, ton(900, 0.25, "sin", 0.15, f1=1500, release=0.2), 0.03)  # "Glas"
    sfx("explosion", e, 0.6)

    # Leben verloren
    sfx("leben_verloren", mische(ton(160, 0.32, "sq", 0.5, f1=95, release=0.1),
                                 ton(163, 0.32, "sq", 0.35, f1=97, release=0.1)), 0.55)

    # Lehrer platzieren / Upgrade / Freischalten / Muenzen
    sfx("platzieren", folge(["C5", "G5"], 0.06, "tri", 0.6, release=0.03), 0.6)
    sfx("upgrade", folge(["C5", "E5", "G5", "C6"], 0.06, "sq", 0.4, duty=0.25, release=0.02), 0.5)
    fr = folge(["G5", "B5", "D6", "G6"], 0.05, "tri", 0.5)
    mische(fr, glocke(hz("G6"), 0.5, 0.3), 0.2)
    sfx("freischalten", fr, 0.55)
    sfx("muenze", mische(ton(hz("B5"), 0.07, "sq", 0.4, duty=0.25),
                         ton(hz("E6"), 0.25, "sq", 0.4, duty=0.25, release=0.2), 0.07), 0.45)
    sfx("fehler", folge(["E3", "C3"], 0.08, "sq", 0.4, duty=0.5), 0.35)

    # Schulglocke zum Rundenstart (Ding-Dong-Ding)
    g = []
    for i, n in enumerate(["E5", "C5", "D5", "G4"]):
        mische(g, glocke(hz(n), 1.4, 0.5), i * 0.32)
    sfx("rundenstart", g, 0.6)

    # Runde geschafft
    rg = folge(["C5", "E5", "G5"], 0.09, "tri", 0.5)
    mische(rg, ton(hz("C6"), 0.35, "tri", 0.5, release=0.25), 0.27)
    sfx("runde_geschafft", rg, 0.6)

    # Game Over: absteigend, mit Vibrato
    go = []
    for i, n in enumerate(["G4", "F#4", "F4"]):
        mische(go, ton(hz(n), 0.36, "sq", 0.4, duty=0.5, release=0.08), i * 0.38)
    mische(go, ton(hz("E4"), 1.1, "sq", 0.4, vibrato=6, release=0.5), 3 * 0.38)
    sfx("game_over", go, 0.6)

    # Gewonnen: Fanfare
    fan = []
    for t, n, d in [(0.0, "C5", 0.12), (0.14, "C5", 0.12), (0.28, "C5", 0.12), (0.42, "C5", 0.35),
                    (0.80, "G#4", 0.35), (1.18, "A#4", 0.35), (1.56, "C5", 0.2), (1.80, "A#4", 0.12),
                    (1.94, "C5", 0.9)]:
        mische(fan, ton(hz(n), d, "sq", 0.35, duty=0.25, release=min(0.3, d / 2)), t)
        mische(fan, ton(hz(n) / 2, d, "tri", 0.3, release=min(0.3, d / 2)), t)
    sfx("gewonnen", fan, 0.65)

    sfx("klick", ton(1500, 0.025, "sq", 0.4, f1=1100, duty=0.25, release=0.01), 0.3)


# ------------------------------------------------------------------ Musik

def schlagzeug(spur, t, art, vol=1.0):
    if art == "kick":
        mische(spur, ton(150, 0.14, "sin", 0.9 * vol, f1=45, kurve=0.4, release=0.06), t)
    elif art == "snare":
        mische(spur, rauschen(0.12, 0.45 * vol, abfall=25, tiefpass=0.3), t)
        mische(spur, ton(220, 0.06, "tri", 0.25 * vol, f1=160), t)
    elif art == "hat":
        mische(spur, rauschen(0.03, 0.15 * vol, abfall=120), t)


def musik_spiel():
    bpm = 132
    achtel = 60 / bpm / 2
    takt = 8 * achtel
    akkorde = [("A2", "A3"), ("F2", "F3"), ("C3", "C4"), ("G2", "G3")]
    teil_a = [[("A4", 2), ("C5", 1), ("E5", 1), ("A5", 2), ("G5", 1), ("E5", 1)],
              [("F5", 2), ("E5", 1), ("C5", 1), ("A4", 2), ("C5", 2)],
              [("G4", 2), ("C5", 1), ("E5", 1), ("G5", 2), ("E5", 1), ("C5", 1)],
              [("D5", 2), ("B4", 1), ("G4", 1), ("D5", 3), (None, 1)]]
    teil_b = [[("E5", 1), ("E5", 1), ("D5", 1), ("C5", 1), ("D5", 2), ("E5", 2)],
              [("C5", 2), ("A4", 2), ("C5", 1), ("D5", 1), ("E5", 2)],
              [("G5", 1), ("G5", 1), ("E5", 1), ("C5", 1), ("D5", 2), ("C5", 2)],
              [("B4", 2), ("D5", 2), ("G5", 3), (None, 1)]]
    teil_b2 = teil_b[:3] + [[("B4", 2), ("C5", 1), ("D5", 1), ("E5", 3), (None, 1)]]
    melodie = teil_a + teil_b + teil_a + teil_b2
    takte = len(melodie)
    gesamt = int(takte * takt * SR)
    spur = [0.0] * gesamt

    for b in range(takte):
        t0 = b * takt
        tief, hoch = akkorde[b % 4]
        # Bass: Grundton / Oktave im Achtelrhythmus
        for k in range(8):
            n = tief if k % 2 == 0 else hoch
            mische(spur, ton(hz(n), achtel * 0.9, "tri", 0.55, release=0.03), t0 + k * achtel)
        # Schlagzeug
        for k in range(8):
            schlagzeug(spur, t0 + k * achtel, "hat", 0.8 if k % 2 else 0.5)
        for k in (0, 4):
            schlagzeug(spur, t0 + k * achtel, "kick")
        for k in (2, 6):
            schlagzeug(spur, t0 + k * achtel, "snare")
        if b % 4 == 3:
            schlagzeug(spur, t0 + 7 * achtel, "kick", 0.7)
        # Melodie (ab dem 2. Durchgang mit Oktav-Echo)
        pos = 0
        for note, laenge in melodie[b]:
            if note:
                d = laenge * achtel
                mische(spur, ton(hz(note), d * 0.92, "sq", 0.22, duty=0.25, release=min(0.08, d / 3)),
                       t0 + pos * achtel)
                if b >= 8:
                    mische(spur, ton(hz(note) * 2, d * 0.85, "sq", 0.06, duty=0.125, release=0.05),
                           t0 + pos * achtel + achtel / 2)
            pos += laenge
    speichern(os.path.join(ROOT, "music", "musik_spiel.wav"), spur[:gesamt], 0.55)


def musik_menu():
    bpm = 96
    achtel = 60 / bpm / 2
    takt = 8 * achtel
    akkorde = [["C4", "E4", "G4", "C5"], ["F3", "A3", "C4", "F4"], ["A3", "C4", "E4", "A4"], ["G3", "B3", "D4", "G4"]]
    lead = [["E5", "G5"], ["F5", "C5"], ["E5", "A4"], ["D5", "B4"],
            ["C5", "E5"], ["A4", "C5"], ["C5", "E5"], ["D5", None]]
    takte = 8
    gesamt = int(takte * takt * SR)
    spur = [0.0] * gesamt
    for b in range(takte):
        t0 = b * takt
        ak = akkorde[b % 4]
        muster = [0, 1, 2, 3, 2, 1, 2, 1]
        for k in range(8):
            mische(spur, ton(hz(ak[muster[k]]), achtel * 1.6, "tri", 0.3, release=achtel), t0 + k * achtel)
        mische(spur, ton(hz(ak[0]) / 2, takt * 0.95, "sin", 0.35, release=0.3), t0)
        for h, n in enumerate(lead[b]):
            if n:
                mische(spur, ton(hz(n), takt / 2 * 0.95, "sq", 0.1, duty=0.25, vibrato=5,
                                 attack=0.03, release=0.25), t0 + h * takt / 2)
        for k in (0, 4):
            schlagzeug(spur, t0 + k * achtel, "kick", 0.5)
        for k in range(1, 8, 2):
            schlagzeug(spur, t0 + k * achtel, "hat", 0.5)
    speichern(os.path.join(ROOT, "music", "musik_menu.wav"), spur[:gesamt], 0.5)


if __name__ == "__main__":
    print("Effekte:")
    effekte()
    print("Musik:")
    musik_spiel()
    musik_menu()
