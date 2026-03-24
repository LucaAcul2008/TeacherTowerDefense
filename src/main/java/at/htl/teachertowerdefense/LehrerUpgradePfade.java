package at.htl.teachertowerdefense;

import java.util.List;

/**
 * Upgrade-Definitionen für Lehrer 1.
 * Stufe 1 jedes Pfades = kostenlos freigeschalten (xpKosten=0)
 * Höhere Stufen brauchen immer mehr XP zum dauerhaften Freischalten.
 */
public class LehrerUpgradePfade {

    public static final double BASE_RANGE        = 150;
    public static final double BASE_SHOOT_DELAY  = 1.0;
    public static final int    BASE_DAMAGE       = 1;
    public static final int    BASE_MULTI_TARGET = 1;

    // --- LEHRER 1 ---
    // Format: speed/damage/range/multiTarget(name, münzKosten, xpKosten, wert)

    /** Pfad A – Schussgeschwindigkeit (ROT) */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_A = List.of(
            LehrerUpgrade.speed  ("Schnellere Reflexe",       100,    0, -0.15),  // Stufe 1: gratis freigeschalten
            LehrerUpgrade.speed  ("Turbo-Wurf",               200,   50, -0.15),  // Stufe 2: 50 XP
            LehrerUpgrade.speed  ("Blitzreflexe",             350,  150, -0.15),  // Stufe 3: 150 XP
            LehrerUpgrade.speed  ("Maschinengewehr-Schwamm",  600,  350, -0.15),  // Stufe 4: 350 XP
            LehrerUpgrade.spezial("MEGA-Schwamm",            1000,  700         )  // Stufe 5: 700 XP
    );

    /** Pfad B – Schaden (GELB) */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_B = List.of(
            LehrerUpgrade.damage("Härterer Schwamm",      80,    0,  1),
            LehrerUpgrade.damage("Stahlschwamm",         180,   50,  1),
            LehrerUpgrade.damage("Beton-Schwamm",        320,  150,  2),
            LehrerUpgrade.damage("Titan-Schwamm",        550,  350,  2),
            LehrerUpgrade.damage("Schwarzes Loch",      1000,  700,  5)
    );

    /** Pfad C – Reichweite & Multi-Target (GRÜN) */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_C = List.of(
            LehrerUpgrade.range      ("Weitsichtigkeit",       80,    0,  25),
            LehrerUpgrade.multiTarget("Multitasking",         150,   50,   1),
            LehrerUpgrade.range      ("Adleraugen",           250,  150,  35),
            LehrerUpgrade.multiTarget("Klassenmanagement",    400,  350,   2),
            new LehrerUpgrade        ("360° Rundumwerfer",    800,  700,  50, -0.1, 0, 2, false)
    );
}