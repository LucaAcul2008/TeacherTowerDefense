package at.htl.teachertowerdefense;

import java.util.List;

/**
 * Definiert alle Upgrade-Pfade für jeden Lehrer-Typ.
 * Jeder Pfad hat genau 5 Upgrades (Index 0-4).
 *
 * Pfad A = eher offensiv (Schaden/Speed)
 * Pfad B = eher unterstützend (Reichweite/Multi-Target)
 */
public class LehrerUpgradePfade {

    // Basis-Stats für Lehrer1
    public static final double BASE_RANGE       = 150;
    public static final double BASE_SHOOT_DELAY = 1.0;
    public static final int    BASE_DAMAGE      = 1;
    public static final int    BASE_MULTI_TARGET = 1;

    /**
     * Pfad A – Fokus auf Schussgeschwindigkeit und Schaden
     */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_A = List.of(
        LehrerUpgrade.speed ("Schnellere Reflexe",    100, -0.15),  // 1: schneller schießen
        LehrerUpgrade.damage("Härterer Schwamm",      200,  1),     // 2: +1 Schaden
        LehrerUpgrade.speed ("Doppelwurf-Vorbereitung",300, -0.15), // 3: noch schneller
        LehrerUpgrade.damage("Schwammkanone",         500,  2),     // 4: +2 Schaden
        LehrerUpgrade.spezial("MEGA-Schwamm",        1000)          // 5: Spezial-Projektil
    );

    /**
     * Pfad B – Fokus auf Reichweite und mehrere Ziele
     */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_B = List.of(
        LehrerUpgrade.range      ("Weitsichtigkeit",       80,   25),   // 1: +25 Reichweite
        LehrerUpgrade.multiTarget("Multitasking",         150,    1),   // 2: +1 Ziel
        LehrerUpgrade.range      ("Adleraugen",           250,   35),   // 3: +35 Reichweite
        LehrerUpgrade.multiTarget("Klassenmanagement",    400,    2),   // 4: +2 Ziele
        new LehrerUpgrade("360° Rundumwerfer", 800,
                          50, -0.1, 0, 2, false)                        // 5: Reichweite+Speed+Multi
    );
}
