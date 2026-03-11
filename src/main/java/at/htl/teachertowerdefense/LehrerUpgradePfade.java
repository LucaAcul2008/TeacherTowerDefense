package at.htl.teachertowerdefense;

import java.util.List;

/**
 * Upgrade-Pfade für jeden Lehrer-Typ.
 * 3 Pfade à 5 Upgrades – BTD6-Regel: 5-2-0 (Hauptpfad max 5, einer der anderen max 2, dritter 0)
 */
public class LehrerUpgradePfade {

    public static final double BASE_RANGE        = 150;
    public static final double BASE_SHOOT_DELAY  = 1.0;
    public static final int    BASE_DAMAGE       = 1;
    public static final int    BASE_MULTI_TARGET = 1;

    // --- LEHRER 1 ---

    /** Pfad A – Schussgeschwindigkeit */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_A = List.of(
            LehrerUpgrade.speed ("Schnellere Reflexe",       100, -0.15),
            LehrerUpgrade.speed ("Turbo-Wurf",               200, -0.15),
            LehrerUpgrade.speed ("Blitzreflexe",             350, -0.15),
            LehrerUpgrade.speed ("Maschinengewehr-Schwamm",  600, -0.15),
            LehrerUpgrade.spezial("MEGA-Schwamm",           1000)
    );

    /** Pfad B – Schaden */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_B = List.of(
            LehrerUpgrade.damage("Härterer Schwamm",     80,   1),
            LehrerUpgrade.damage("Stahlschwamm",        180,   1),
            LehrerUpgrade.damage("Beton-Schwamm",       320,   2),
            LehrerUpgrade.damage("Titan-Schwamm",       550,   2),
            LehrerUpgrade.damage("Schwarzes Loch",     1000,   5)
    );

    /** Pfad C – Reichweite & Multi-Target */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_C = List.of(
            LehrerUpgrade.range      ("Weitsichtigkeit",      80,  25),
            LehrerUpgrade.multiTarget("Multitasking",        150,   1),
            LehrerUpgrade.range      ("Adleraugen",          250,  35),
            LehrerUpgrade.multiTarget("Klassenmanagement",   400,   2),
            new LehrerUpgrade("360° Rundumwerfer",           800,
                    50, -0.1, 0, 2, false)
    );
}