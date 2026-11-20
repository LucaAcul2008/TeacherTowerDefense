"""Erzeugt Campus.tmx ("HTL Saalfelden") fuer Teacher Tower Defense.

Tileset: Kenney "RPG Urban Pack" (CC0) -> src/main/resources/assets/levels/rpg_urban.png
         16px Kacheln, 1px Abstand, 27 Spalten. Kachel-ID = Zeile*27 + Spalte + 1.
Alle mehrteiligen Objekte (Baeume, Autos, Laternen, Fenster) sind einzeln in starker
Vergroesserung geprueft – bitte nur die Kombinationen unten verwenden.

Idee: Die Schueler kommen aus dem Haupteingang der HTL und wollen ueber den Campus,
durch das Tor im Schulzaun, zur Bushaltestelle abhauen.

Aufruf (im Projektordner):  python tools/gen_campus.py
"""
import os
import random

W, H, T = 60, 40, 16
rng = random.Random(11)
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
AUSGABE = os.path.join(ROOT, "src", "main", "resources", "assets", "levels", "Campus.tmx")

# ------------------------------------------------------------------ Kacheln (geprueft!)
# Asphalt
ASPHALT = 442                    # saubere Flaeche ohne Linien
LINIE_OBEN, LINIE_UNTEN = 407, 461
LINIE_LINKS, LINIE_RECHTS = 462, 464
ECKE_OL, ECKE_OR, ECKE_UL, ECKE_UR = 438, 439, 465, 466   # abgerundete Spielfeld-Ecken
MITTELPUNKT = 408
MITTELSTREIFEN = 434
ZEBRA = (406, 433, 460)          # waagrechte Balken: oben, mitte, unten (fuer waagrechte Strasse)
P_MARKIERUNG = 443

# Flaechen mit Rand (3x3 + Innenecken)
GEHWEG = dict(TL=9, T=10, TR=11, L=36, C=37, R=38, BL=63, B=64, BR=65, iBR=14, iBL=15, iTR=41, iTL=42)
RASEN = dict(TL=1, T=2, TR=3, L=28, C=29, R=30, BL=55, B=56, BR=57, iBR=6, iBL=7, iTR=33, iTL=34)
DACH_GRAU = dict(TL=90, T=91, TR=92, L=117, C=118, R=119, BL=144, B=145, BR=146)
DACH_BEIGE = dict(TL=82, T=83, TR=84, L=109, C=110, R=111, BL=136, B=137, BR=138)

# Fassaden: oben Sims, Mitte, unten Sockel – jeweils (links, mitte, rechts)
FASSADE_ZIEGEL = dict(oben=(18, 19, 20), mitte=(72, 73, 74), unten=(99, 100, 101))
FASSADE_GLATT = dict(oben=(21, 22, 23), mitte=(75, 76, 77), unten=(102, 103, 104))
FENSTER = (364, 365)
FENSTER_GROSS = (391, 391)
GLASTUER = 286
HOLZTUER = 285
GLASFRONT_OBEN = (360, 361, 362)
KLIMA = (334, 335)

# Pflanzen
BAUM_GROSS = [(233, 260), (314, 341)]          # (Krone oben, Krone+Stamm unten)
BAUM_MITTEL = [(234, 261), (315, 342)]
BAUM_KLEIN = [266, 293, 347, 374]              # 1 Kachel
KUEBELBAUM = 292                                # kleiner Baum im Pflanzkuebel (1 Kachel)
BAUMGRUPPE = [((236, 237), (263, 264), (290, 291)), ((317, 318), (344, 345), (371, 372))]  # 2x3

# Moebel / Strasse
HOLZBANK = 382
MISTKUEBEL = 280
ZAUN = (356, 357, 358)                          # Maschendraht: Anfang, Mitte, Ende
PARKLATERNE = (170, 197)
STRASSENLATERNE = (165, 192)
HALTESTELLE = (169, 196)
AUTO_SENKRECHT = [(450, 477), (453, 480)]       # 1 breit, 2 hoch
TAXI_QUER = [(421, 422), (424, 425)]            # 2 breit, 1 hoch

# ------------------------------------------------------------------ Layout
# Zeilen:  0-8 Gebaeude | 9-11 Schulhof | 12-13 Wiese | 14-16 Weg | 17-21 Wiese |
#          22-24 Weg | 25-29 Wiese (Basketball) | 30 Zaun | 31-34 Gehweg | 35-39 Strasse
ROUTE = [(32, 8), (32, 15), (6, 15), (6, 23), (44, 23), (44, 32), (61, 32)]

strasse = [[False] * W for _ in range(H)]
gehweg = [[False] * W for _ in range(H)]
gebaeude = [[False] * W for _ in range(H)]
weg = [[False] * W for _ in range(H)]
belegt = [[False] * W for _ in range(H)]


def rechteck(mask, c0, r0, c1, r1, wert=True):
    for r in range(max(0, r0), min(H, r1 + 1)):
        for c in range(max(0, c0), min(W, c1 + 1)):
            mask[r][c] = wert


for (c0, r0), (c1, r1) in zip(ROUTE, ROUTE[1:]):
    rechteck(weg, min(c0, c1) - 1, max(9, min(r0, r1) - 1), max(c0, c1) + 1, max(r0, r1) + 1)

rechteck(strasse, 0, 35, W - 1, H - 1)          # Strasse
PARK = (47, 12, 58, 20)                         # Lehrerparkplatz (c0, r0, c1, r1)
rechteck(strasse, *PARK)
rechteck(strasse, 58, 15, W - 1, 17)            # Einfahrt vom rechten Rand

for r in range(H):
    for c in range(W):
        gehweg[r][c] = weg[r][c]
rechteck(gehweg, 0, 9, W - 1, 11)               # Schulhof
rechteck(gehweg, 0, 31, W - 1, 34)              # Gehweg an der Strasse

GEBAEUDE = [
    # name,          c0, r0, c1, r1, dach,       fassade,        tuer,      fenster
    ("Werkstaetten",  1, 1, 15, 8, DACH_BEIGE, FASSADE_GLATT, HOLZTUER, FENSTER_GROSS),
    ("HTL",          19, 1, 45, 8, DACH_GRAU, FASSADE_ZIEGEL, GLASTUER, FENSTER),
    ("Turnhalle",    48, 1, 58, 8, DACH_BEIGE, FASSADE_ZIEGEL, HOLZTUER, FENSTER_GROSS),
]
for _, c0, r0, c1, r1, *_ in GEBAEUDE:
    rechteck(gebaeude, c0, r0, c1, r1)
    rechteck(gehweg, c0, r0, c1, r1, False)

rasen = [[not (strasse[r][c] or gehweg[r][c] or gebaeude[r][c]) for c in range(W)] for r in range(H)]


def autotile(mask, tiles, rand_offen):
    def m(c, r):
        if not (0 <= c < W and 0 <= r < H):
            return rand_offen
        return mask[r][c]

    out = [[0] * W for _ in range(H)]
    for r in range(H):
        for c in range(W):
            if not mask[r][c]:
                continue
            n, s, w, e = m(c, r - 1), m(c, r + 1), m(c - 1, r), m(c + 1, r)
            if not n and not w: k = 'TL'
            elif not n and not e: k = 'TR'
            elif not s and not w: k = 'BL'
            elif not s and not e: k = 'BR'
            elif not n: k = 'T'
            elif not s: k = 'B'
            elif not w: k = 'L'
            elif not e: k = 'R'
            elif 'iBR' in tiles and not m(c + 1, r + 1): k = 'iBR'
            elif 'iBL' in tiles and not m(c - 1, r + 1): k = 'iBL'
            elif 'iTR' in tiles and not m(c + 1, r - 1): k = 'iTR'
            elif 'iTL' in tiles and not m(c - 1, r - 1): k = 'iTL'
            else: k = 'C'
            out[r][c] = tiles[k]
    return out


boden = [[ASPHALT] * W for _ in range(H)]
pflaster = autotile([[not strasse[r][c] for c in range(W)] for r in range(H)], GEHWEG, True)
haus = [[0] * W for _ in range(H)]
deko = [[0] * W for _ in range(H)]
oben = [[0] * W for _ in range(H)]       # Ebene ueber der Deko (Dachaufbauten)
objekte = []


def obj(typ, c, r, w=1, h=1):
    objekte.append((typ, c * T, r * T, w * T, h * T))


def setze(c, r, kachel, ebene=None):
    (ebene or deko)[r][c] = kachel
    belegt[r][c] = True


# ------------------------------------------------------------------ Strasse
for c in range(W):
    boden[35][c] = LINIE_OBEN
    boden[39][c] = LINIE_UNTEN
    if c % 3 != 2:
        boden[37][c] = MITTELSTREIFEN
for c in (40, 41):                       # Zebrastreifen links vom Tor
    boden[35][c], boden[39][c] = ZEBRA[0], ZEBRA[2]
    for r in (36, 37, 38):
        boden[r][c] = ZEBRA[1]
for c, r, (l, rr) in [(8, 36, TAXI_QUER[0]), (25, 38, TAXI_QUER[1]), (54, 36, TAXI_QUER[0])]:
    deko[r][c], deko[r][c + 1] = l, rr
    obj("Auto", c, r, 2, 1)

# ------------------------------------------------------------------ Parkplatz
c0, r0, c1, r1 = PARK
for c in range(c0 + 1, c1):
    for r in (r0 + 1, r0 + 2, r1 - 2, r1 - 1):   # zwei Reihen Stellplaetze, 2 Kacheln tief
        boden[r][c] = LINIE_LINKS
boden[r0 + 4][c1 - 1] = P_MARKIERUNG
for c in range(c0 + 1, c1, 2):
    for r in (r0 + 1, r1 - 2):
        if rng.random() < 0.7:
            krone, unten = rng.choice(AUTO_SENKRECHT)
            deko[r][c], deko[r + 1][c] = krone, unten
            obj("Auto", c, r, 1, 2)

# ------------------------------------------------------------------ Basketballplatz (untere Wiese)
FELD = (14, 26, 29, 28)
fc0, fr0, fc1, fr1 = FELD
rechteck(rasen, fc0, fr0, fc1, fr1, False)
for r in range(fr0, fr1 + 1):
    for c in range(fc0, fc1 + 1):
        k = ASPHALT
        if r == fr0: k = LINIE_OBEN
        if r == fr1: k = LINIE_UNTEN
        if c == fc0: k = {fr0: ECKE_OL, fr1: ECKE_UL}.get(r, LINIE_LINKS)
        if c == fc1: k = {fr0: ECKE_OR, fr1: ECKE_UR}.get(r, LINIE_RECHTS)
        haus[r][c] = k
        belegt[r][c] = True
haus[(fr0 + fr1) // 2][(fc0 + fc1) // 2] = MITTELPUNKT
gruen = autotile(rasen, RASEN, True)    # Rasen rund um das Feld (Kanten passen sich an)

# ------------------------------------------------------------------ Gebaeude
for name, c0, r0, c1, r1, dach, fassade, tuer, fenster in GEBAEUDE:
    dach_r1 = r1 - 3
    dmask = [[False] * W for _ in range(H)]
    rechteck(dmask, c0, r0, c1, dach_r1)
    dt = autotile(dmask, dach, False)
    for r in range(r0, dach_r1 + 1):
        for c in range(c0, c1 + 1):
            haus[r][c] = dt[r][c]
    for i, zeile in enumerate(("oben", "mitte", "unten")):
        r = dach_r1 + 1 + i
        links, mitte_k, rechts = fassade[zeile]
        for c in range(c0, c1 + 1):
            haus[r][c] = links if c == c0 else rechts if c == c1 else mitte_k
    tuer_c = ROUTE[0][0] if name == "HTL" else (c0 + c1) // 2
    for c in range(c0 + 2, c1 - 1, 2):
        if abs(c - tuer_c) <= 2:
            continue
        deko[dach_r1 + 2][c] = fenster[0]
        deko[dach_r1 + 3][c] = fenster[1]
    if name == "HTL":
        # moderne Glasfront ueber dem Haupteingang, darunter drei Glastueren
        for i, c in enumerate(range(tuer_c - 1, tuer_c + 2)):
            deko[dach_r1 + 2][c] = GLASFRONT_OBEN[i]
            deko[dach_r1 + 3][c] = GLASTUER
        for c, r in [(21, 2), (22, 2), (43, 4), (21, 4)]:   # Klimageraete am Dachrand (Schriftzug bleibt frei)
            oben[r][c] = KLIMA[(c + r) % 2]
    else:
        deko[dach_r1 + 3][tuer_c] = tuer
        oben[r0 + 1][c0 + 2] = KLIMA[0]
        oben[r0 + 2][c1 - 2] = KLIMA[1]
    obj("Haus", c0, r0, c1 - c0 + 1, r1 - r0 + 1)


def frei(c, r, abstand=1):
    if not (0 <= c < W and 0 <= r < H) or belegt[r][c] or gebaeude[r][c]:
        return False
    for rr in range(r - abstand, r + abstand + 1):
        for cc in range(c - abstand, c + abstand + 1):
            if 0 <= cc < W and 0 <= rr < H and weg[rr][cc]:
                return False
    return True


def platz(c0, r0, c1, r1, nur_rasen=True):
    """Bereich frei, mit 1 Kachel Abstand zu anderer Deko und zum Weg?"""
    for r in range(r0 - 1, r1 + 2):
        for c in range(c0 - 1, c1 + 2):
            if 0 <= c < W and 0 <= r < H and belegt[r][c]:
                return False
    for r in range(r0, r1 + 1):
        for c in range(c0, c1 + 1):
            if not (0 <= c < W and 0 <= r < H) or (nur_rasen and not rasen[r][c]) or not frei(c, r, 1):
                return False
    return True


def baum(c, r, art):
    """art: 'gross'/'mittel' (2 hoch, Stamm auf r), 'klein' (1 Kachel), 'gruppe' (2x3, unten-links auf c,r)."""
    if art in ("gross", "mittel"):
        if not platz(c, r - 1, c, r):
            return False
        k_oben, k_unten = rng.choice(BAUM_GROSS if art == "gross" else BAUM_MITTEL)
        setze(c, r - 1, k_oben); setze(c, r, k_unten)
        obj("Baum", c, r - 1, 1, 2)
    elif art == "klein":
        if not platz(c, r, c, r):
            return False
        setze(c, r, rng.choice(BAUM_KLEIN))
        obj("Busch", c, r)
    else:
        if not platz(c, r - 2, c + 1, r):
            return False
        gruppe = rng.choice(BAUMGRUPPE)
        for i, (l, rr) in enumerate(gruppe):
            setze(c, r - 2 + i, l); setze(c + 1, r - 2 + i, rr)
        obj("Baum", c, r - 2, 2, 3)
    return True


# ------------------------------------------------------------------ Schulhof
for c in range(3, 58, 4):                       # Kuebelbaeume vorne am Schulhof
    if frei(c, 11, 1):
        setze(c, 11, KUEBELBAUM); obj("Busch", c, 11)
for c in range(5, 58, 8):                       # Holzbaenke und Mistkuebel dazwischen
    if frei(c, 11, 1):
        setze(c, 11, HOLZBANK); obj("Bank", c, 11)
    if frei(c + 2, 11, 1):
        setze(c + 2, 11, MISTKUEBEL); obj("Mistkübel", c + 2, 11)

# ------------------------------------------------------------------ Schulzaun mit Tor beim Weg
tor = range(ROUTE[4][0] - 1, ROUTE[4][0] + 2)
c = 0
while c < W:
    if c in tor:
        c += 1
        continue
    ende = c
    while ende + 1 < W and ende + 1 not in tor:
        ende += 1
    for x in range(c, ende + 1):
        setze(x, 30, ZAUN[0] if x == c else ZAUN[2] if x == ende else ZAUN[1])
    objekte.append(("Zaun", c * T, 30 * T + 6, (ende - c + 1) * T, 6))
    c = ende + 1

# Haltestelle rechts vom Tor
setze(56, 29, HALTESTELLE[0]); setze(56, 30, HALTESTELLE[1])
obj("Bushaltestelle", 56, 29, 1, 2)

# Strassenlaternen am Bordstein (Zeile 33-34, nur wo kein Weg ist)
for c in (3, 15, 27, 37):
    if not weg[33][c] and not weg[34][c]:
        setze(c, 33, STRASSENLATERNE[0]); setze(c, 34, STRASSENLATERNE[1])
        obj("Laterne", c, 33, 1, 2)

# ------------------------------------------------------------------ Gruen
# Baumgruppen an den Raendern, einzelne Baeume locker verteilt (Wiesen bleiben Bauplatz!)
for c, r in [(0, 29), (0, 21), (35, 21), (46, 29), (57, 28)]:
    baum(c, r, "gruppe")
for c, r in [(12, 19), (22, 21), (30, 18), (39, 19), (3, 26), (9, 29), (34, 28), (40, 26),
             (36, 13), (43, 13), (50, 25), (53, 28), (59, 22)]:
    baum(c, r, rng.choice(["gross", "mittel", "mittel"]))
for c, r in [(17, 18), (27, 20), (8, 27), (37, 27), (48, 23), (55, 23), (2, 13), (16, 12)]:
    baum(c, r, "klein")
# Baumreihe entlang des Schulzauns (nicht vor dem Basketballplatz) und Gruppe rechts unten
for c in (2, 8, 32, 38, 48, 53):
    baum(c, 28, "gross" if c % 3 == 2 else "mittel")
baum(52, 26, "gruppe")
# Parklaternen entlang des Wegs
for c, r in [(10, 18), (26, 18), (38, 18), (20, 26), (2, 18)]:
    if platz(c, r - 1, c, r):
        setze(c, r - 1, PARKLATERNE[0]); setze(c, r, PARKLATERNE[1])
        obj("Laterne", c, r - 1, 1, 2)
# Baenke am Weg
for c, r in [(18, 17), (34, 17), (12, 25)]:
    if platz(c, r, c, r):
        setze(c, r, HOLZBANK); obj("Bank", c, r)

# ------------------------------------------------------------------ Weg-Kollision


def rechtecke(mask):
    offen, fertig = {}, []
    for r in range(H + 1):
        laeufe = []
        if r < H:
            c = 0
            while c < W:
                if mask[r][c]:
                    s = c
                    while c < W and mask[r][c]:
                        c += 1
                    laeufe.append((s, c - 1))
                else:
                    c += 1
        neu = {}
        for lauf in laeufe:
            neu[lauf] = offen.pop(lauf) if lauf in offen else r
        for (s, e), r0_ in offen.items():
            fertig.append((s, r0_, e, r - 1))
        offen = neu
    return fertig


for c0, r0, c1, r1 in rechtecke(weg):
    w, h = (c1 - c0 + 1) * T, (r1 - r0 + 1) * T
    objekte.append(("PfadAussen", c0 * T, r0 * T, [(0, 0), (w, 0), (w, h), (0, h)]))

# ------------------------------------------------------------------ TMX schreiben


def mitte(c, r):
    return c * T + T / 2, r * T + T / 2


def csv(layer):
    return ",\n".join(",".join(str(v) for v in row) for row in layer)


lines = [
    '<?xml version="1.0" encoding="UTF-8"?>',
    f'<map version="1.10" tiledversion="1.11.2" orientation="orthogonal" renderorder="right-down" '
    f'width="{W}" height="{H}" tilewidth="{T}" tileheight="{T}" infinite="0" nextlayerid="8" '
    f'nextobjectid="{len(objekte) + 10}">',
    ' <tileset firstgid="1" name="RPG Urban" tilewidth="16" tileheight="16" spacing="1" tilecount="486" columns="27">',
    '  <image source="rpg_urban.png" width="458" height="305"/>',
    ' </tileset>',
]
for lid, name, layer in [(1, "Asphalt", boden), (2, "Pflaster", pflaster), (3, "Rasen", gruen),
                         (4, "Gebaeude", haus), (5, "Deko", deko), (6, "Oben", oben)]:
    lines += [f' <layer id="{lid}" name="{name}" width="{W}" height="{H}">',
              '  <data encoding="csv">', csv(layer), '</data>', ' </layer>']

lines.append(' <objectgroup id="7" name="Logik">')
oid = 1
sx, sy = mitte(*ROUTE[0])
ex, ey = mitte(*ROUTE[-1])
lines.append(f'  <object id="{oid}" name="Spawn" type="Spawn" x="{sx - 24:g}" y="{sy - 8:g}" width="48" height="16"/>'); oid += 1
lines.append(f'  <object id="{oid}" name="Ziel" type="Ziel" x="952" y="{ey - 24:g}" width="8" height="48"/>'); oid += 1
pts = " ".join(f"{mitte(c, r)[0] - sx:g},{mitte(c, r)[1] - sy:g}" for c, r in ROUTE)
lines += [f'  <object id="{oid}" name="Pfad" type="Pfad" x="{sx:g}" y="{sy:g}">',
          f'   <polyline points="{pts}"/>', '  </object>']
oid += 1
for o in objekte:
    if isinstance(o[3], list):
        typ, x, y, punkte = o
        p = " ".join(f"{px:g},{py:g}" for px, py in punkte)
        lines += [f'  <object id="{oid}" name="{typ}" type="{typ}" x="{x:g}" y="{y:g}">',
                  f'   <polygon points="{p}"/>', '  </object>']
    else:
        typ, x, y, w, h = o
        lines.append(f'  <object id="{oid}" name="{typ}" type="{typ}" x="{x:g}" y="{y:g}" width="{w}" height="{h}"/>')
    oid += 1
lines += [' </objectgroup>', '</map>', '']

with open(AUSGABE, "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(lines))

print("geschrieben:", os.path.relpath(AUSGABE, ROOT), "-", len(objekte) + 3, "Objekte")
print("Route (Pixel-Mittellinie):", [mitte(c, r) for c, r in ROUTE])
