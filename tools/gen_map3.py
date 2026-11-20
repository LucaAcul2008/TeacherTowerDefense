"""Erzeugt Map3.tmx ("Schulausflug") fuer Teacher Tower Defense.

Kacheln: Kenney roguelikeSheet_transparent.png (16px, spacing 1, 57 Spalten).
Grafik und Kollisions-Objekte werden aus denselben Masken erzeugt, damit sie zusammenpassen.
Aufruf: python gen_map3.py <ausgabe.tmx>
"""
import random
import sys

W, H, T = 60, 40, 16
rng = random.Random(3)

# ---------------------------------------------------------------- Kacheln
GRAS = 6
DIRT = dict(TL=521, T=522, TR=523, L=578, C=579, R=580, BL=635, B=636, BR=637,
            iBR=519, iBL=520, iTR=576, iTL=577)
WASSER = dict(TL=3, T=4, TR=5, L=60, C=61, R=62, BL=117, B=118, BR=119,
              iBR=58, iBL=59, iTR=115, iTL=116)
BAEUME = [(584, 641), (586, 643), (587, 644), (589, 646), (584, 641), (586, 643)]  # (oben, unten)
APFELBAUM = (594, 651)
BUESCHE = [537, 538, 652]
BLUMEN = [653, 653, 593, 650]  # Einzelblumen/Sprossen (die Blumenfeld-Kacheln haben graue Ecken)
ZELT = ((619, 620), (676, 677))
LAGERFEUER = 471

# ---------------------------------------------------------------- Weg
# Mittellinie in Kachel-Koordinaten (Spalte, Zeile); der Weg ist 3 Kacheln breit.
ROUTE = [(-1, 13), (16, 13), (16, 31), (32, 31), (32, 9), (46, 9), (46, 24), (60, 24)]

weg = [[False] * W for _ in range(H)]
for (c0, r0), (c1, r1) in zip(ROUTE, ROUTE[1:]):
    for c in range(min(c0, c1) - 1, max(c0, c1) + 2):
        for r in range(min(r0, r1) - 1, max(r0, r1) + 2):
            if 0 <= c < W and 0 <= r < H:
                weg[r][c] = True


def nahe_weg(c, r, abstand):
    for rr in range(r - abstand, r + abstand + 1):
        for cc in range(c - abstand, c + abstand + 1):
            if 0 <= cc < W and 0 <= rr < H and weg[rr][cc]:
                return True
    return False


# ---------------------------------------------------------------- Teiche
wasser = [[False] * W for _ in range(H)]
TEICHE = [
    # Teich in der ersten Weg-Schleife (macht den besten Bauplatz knapper)
    [(20, 17, 27, 22), (22, 15, 26, 16), (21, 23, 25, 25)],
    # Teich oben rechts beim Zeltlager
    [(50, 3, 56, 8), (52, 9, 55, 10), (51, 2, 54, 2)],
    # kleiner Teich unten rechts
    [(51, 30, 56, 34), (53, 35, 55, 36)],
]
for teich in TEICHE:
    for c0, r0, c1, r1 in teich:
        for r in range(r0, r1 + 1):
            for c in range(c0, c1 + 1):
                wasser[r][c] = True


def autotile(mask, tiles, rand_offen):
    """Waehlt Rand/Ecke/Innenecke je nach Nachbarn. rand_offen: Kartenrand zaehlt als 'gleich'."""
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
            elif not m(c + 1, r + 1): k = 'iBR'
            elif not m(c - 1, r + 1): k = 'iBL'
            elif not m(c + 1, r - 1): k = 'iTR'
            elif not m(c - 1, r - 1): k = 'iTL'
            else: k = 'C'
            out[r][c] = tiles[k]
    return out


boden = [[GRAS] * W for _ in range(H)]
wt = autotile(wasser, WASSER, False)
for r in range(H):
    for c in range(W):
        if wt[r][c]:
            boden[r][c] = wt[r][c]
weg_layer = autotile(weg, DIRT, True)
deko = [[0] * W for _ in range(H)]
belegt = [[False] * W for _ in range(H)]  # Deko-Kacheln, damit sich nichts ueberlappt

objekte = []  # (name, x, y, w, h) Rechteck  oder  (name, x, y, [punkte])


def frei(c, r, wegabstand=2):
    return (0 <= c < W and 0 <= r < H and not belegt[r][c] and not wasser[r][c]
            and not nahe_weg(c, r, wegabstand))


def setze_baum(c, r, art=None):
    """Baum mit Stamm auf (c, r) und Krone auf (c, r-1)."""
    if not (frei(c, r) and frei(c, r - 1)):
        return False
    oben, unten = art or rng.choice(BAEUME)
    deko[r - 1][c], deko[r][c] = oben, unten
    belegt[r - 1][c] = belegt[r][c] = True
    objekte.append(("Baum", c * T, (r - 1) * T, 16, 32))
    return True


def wald(c0, r0, c1, r1, anzahl, versuche=400):
    gesetzt = 0
    for _ in range(versuche):
        if gesetzt >= anzahl:
            break
        if setze_baum(rng.randint(c0, c1), rng.randint(r0, r1)):
            gesetzt += 1


# Zeltlager oben (zwischen den Weg-Schleifen) mit Lagerfeuer
for zc, zr in [(20, 3), (24, 2), (28, 4)]:
    (a, b), (cc, d) = ZELT
    deko[zr][zc], deko[zr][zc + 1], deko[zr + 1][zc], deko[zr + 1][zc + 1] = a, b, cc, d
    for rr in (zr, zr + 1):
        for ccc in (zc, zc + 1):
            belegt[rr][ccc] = True
    objekte.append(("Tent", zc * T, zr * T, 32, 32))
deko[6][24] = LAGERFEUER
belegt[6][24] = True
objekte.append(("Busch", 24 * T, 6 * T, 16, 16))

# Waelder (feste Seed -> immer gleiche Karte)
wald(0, 1, 13, 9, 26)        # oben links (unter dem HUD)
wald(0, 17, 12, 39, 14)      # links unten
wald(17, 35, 47, 39, 16)     # Baumreihe unten
wald(34, 1, 45, 6, 6)        # oben Mitte-rechts
wald(48, 12, 59, 21, 8)      # rechts zwischen Teich und Weg
wald(48, 27, 59, 39, 7)      # rechts unten
for c, r in [(36, 15), (42, 18), (38, 20)]:
    setze_baum(c, r, APFELBAUM)  # Obstgarten in der zweiten Schleife

# Bueschen (blockieren Platzierung/Schuesse) und Blumen (nur Deko)
for _ in range(14):
    c, r = rng.randint(0, W - 1), rng.randint(0, H - 1)
    if frei(c, r, 2):
        deko[r][c] = rng.choice(BUESCHE)
        belegt[r][c] = True
        objekte.append(("Busch", c * T, r * T, 16, 16))
for _ in range(45):
    c, r = rng.randint(0, W - 1), rng.randint(0, H - 1)
    if frei(c, r, 1):
        deko[r][c] = rng.choice(BLUMEN)
        belegt[r][c] = True

# ---------------------------------------------------------------- Kollisions-Objekte


def rechtecke(mask):
    """Zerlegt eine Maske in Rechtecke (Zeilen-Laeufe, vertikal zusammengefasst)."""
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
            neu[lauf] = (offen.pop(lauf) if lauf in offen else r)
        for (s, e), r0 in offen.items():
            fertig.append((s, r0, e, r - 1))
        offen = neu
    return fertig


def poly(name, c0, r0, c1, r1, inset=0.0):
    x, y = c0 * T + inset, r0 * T + inset
    w, h = (c1 - c0 + 1) * T - 2 * inset, (r1 - r0 + 1) * T - 2 * inset
    objekte.append((name, x, y, [(0, 0), (w, 0), (w, h), (0, h)]))


for c0, r0, c1, r1 in rechtecke(wasser):
    poly("Teich", c0, r0, c1, r1, inset=3)
for c0, r0, c1, r1 in rechtecke(weg):
    poly("PfadAussen", c0, r0, c1, r1)

# ---------------------------------------------------------------- TMX schreiben


def mitte(c, r):
    return c * T + T / 2, r * T + T / 2


def csv(layer):
    return ",\n".join(",".join(str(v) for v in row) for row in layer)


lines = [
    '<?xml version="1.0" encoding="UTF-8"?>',
    f'<map version="1.10" tiledversion="1.11.2" orientation="orthogonal" renderorder="right-down" '
    f'width="{W}" height="{H}" tilewidth="{T}" tileheight="{T}" infinite="0" nextlayerid="5" '
    f'nextobjectid="{len(objekte) + 10}">',
    ' <tileset firstgid="1" name="Roguelike" tilewidth="16" tileheight="16" spacing="1" tilecount="1767" columns="57">',
    '  <image source="roguelikeSheet_transparent.png" width="968" height="526"/>',
    ' </tileset>',
]
for lid, name, layer in [(1, "Boden", boden), (2, "Weg", weg_layer), (3, "Deko", deko)]:
    lines += [f' <layer id="{lid}" name="{name}" width="{W}" height="{H}">',
              '  <data encoding="csv">', csv(layer), '</data>', ' </layer>']

lines.append(' <objectgroup id="4" name="Logik">')
oid = 1
sx, sy = mitte(*ROUTE[0])
ex, ey = mitte(*ROUTE[-1])
lines.append(f'  <object id="{oid}" name="Spawn" type="Spawn" x="0" y="{sy - 24}" width="8" height="48"/>'); oid += 1
lines.append(f'  <object id="{oid}" name="Ziel" type="Ziel" x="952" y="{ey - 24}" width="8" height="48"/>'); oid += 1
pts = " ".join(f"{mitte(c, r)[0] - sx:g},{mitte(c, r)[1] - sy:g}" for c, r in ROUTE)
lines += [f'  <object id="{oid}" name="Pfad" type="Pfad" x="{sx:g}" y="{sy:g}">',
          f'   <polyline points="{pts}"/>', '  </object>']
oid += 1
for o in objekte:
    if isinstance(o[3], list):
        name, x, y, punkte = o
        p = " ".join(f"{px:g},{py:g}" for px, py in punkte)
        lines += [f'  <object id="{oid}" name="{name}" type="{name}" x="{x:g}" y="{y:g}">',
                  f'   <polygon points="{p}"/>', '  </object>']
    else:
        name, x, y, w, h = o
        lines.append(f'  <object id="{oid}" name="{name}" type="{name}" x="{x:g}" y="{y:g}" width="{w}" height="{h}"/>')
    oid += 1
lines += [' </objectgroup>', '</map>', '']

with open(sys.argv[1], "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(lines))

print("Objekte:", len(objekte) + 3)
print("Route (Pixel-Mittellinie):", [mitte(c, r) for c, r in ROUTE])
