package at.htl.teachertowerdefense;

import java.util.List;

/**
 * Upgrade-Definitionen für alle Lehrer-Typen.
 * Stufe 1 jedes Pfades = kostenlos freigeschalten (xpKosten=0)
 */
public class LehrerUpgradePfade {

    // ── LEHRER 1: GROEBL – Fisch-Boomerang ──────────────────────
    public static final double BASE_RANGE_L1        = 150;
    public static final double BASE_SHOOT_DELAY_L1  = 1.0;
    public static final int    BASE_DAMAGE_L1       = 1;
    public static final int    BASE_MULTI_TARGET_L1 = 1;

    // Rückwärtskompatibel (LehrerComponent default)
    public static final double BASE_RANGE        = BASE_RANGE_L1;
    public static final double BASE_SHOOT_DELAY  = BASE_SHOOT_DELAY_L1;
    public static final int    BASE_DAMAGE       = BASE_DAMAGE_L1;
    public static final int    BASE_MULTI_TARGET = BASE_MULTI_TARGET_L1;

    // ── LEHRER 2: FEICHTNER – Alchemistin ───────────────────────
    public static final double BASE_RANGE_L2        = 180;
    public static final double BASE_SHOOT_DELAY_L2  = 2.0;
    public static final int    BASE_DAMAGE_L2       = 2;
    public static final int    BASE_MULTI_TARGET_L2 = 1;

    // ── LEHRER 3: WINKLER – Floppy Shooter ──────────────────────
    public static final double BASE_RANGE_L3        = 110;
    public static final double BASE_SHOOT_DELAY_L3  = 0.4;
    public static final int    BASE_DAMAGE_L3       = 1;
    public static final int    BASE_MULTI_TARGET_L3 = 1;

    // ── LEHRER 1: GROEBL – Fisch-Boomerang ──────────────────────
    /** Pfad A – Wurfgeschwindigkeit (ROT) */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_A = List.of(
            LehrerUpgrade.speed  ("Schnellerer Schwung",      100,    0, -0.15),
            LehrerUpgrade.speed  ("Turbo-Fisch",              200,   50, -0.15),
            LehrerUpgrade.speed  ("Blitz-Forelle",            350,  150, -0.15),
            LehrerUpgrade.speed  ("Schallmauer-Lachs",        600,  350, -0.15),
            LehrerUpgrade.spezial("MEGA-Hecht",              1000,  700        )
    );
    /** Pfad B – Schaden (GELB) */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_B = List.of(
            LehrerUpgrade.damage("Frischer Fisch",         80,    0,  1),
            LehrerUpgrade.damage("Geräucherter Lachs",    180,   50,  1),
            LehrerUpgrade.damage("Tiefkühl-Thunfisch",    320,  150,  2),
            LehrerUpgrade.damage("Titan-Schwertfisch",    550,  350,  2),
            LehrerUpgrade.damage("Nuklearer Karpfen",    1000,  700,  5)
    );
    /** Pfad C – Reichweite & Mehrfach-Treffer (GRÜN) */
    public static final List<LehrerUpgrade> LEHRER1_PFAD_C = List.of(
            LehrerUpgrade.range      ("Längere Angel",          80,    0,  25),
            LehrerUpgrade.multiTarget("Doppelwurf",            150,   50,   1),
            LehrerUpgrade.range      ("Weitwurf-Training",     250,  150,  35),
            LehrerUpgrade.multiTarget("Fisch-Schwarm",         400,  350,   2),
            new LehrerUpgrade        ("360° Fischsturm",       800,  700,  50, -0.1, 0, 2, false)
    );

    // ── LEHRER 2: FEICHTNER – Alchemistin / Potions ──────────────
    /** Pfad A – Explosionsradius (ROT) */
    public static final List<LehrerUpgrade> LEHRER2_PFAD_A = List.of(
            LehrerUpgrade.range("Größere Flasche",          120,    0,  20),
            LehrerUpgrade.range("Verstärkte Mischung",      250,   60,  25),
            LehrerUpgrade.range("Mega-Explosion",           400,  180,  35),
            LehrerUpgrade.range("Atomare Säure",            700,  400,  45),
            LehrerUpgrade.spezial("Apokalypse-Elixier",    1200,  800      )
    );
    /** Pfad B – Schaden (GELB) */
    public static final List<LehrerUpgrade> LEHRER2_PFAD_B = List.of(
            LehrerUpgrade.damage("Säure-Potion",            100,    0,  1),
            LehrerUpgrade.damage("Gift-Extrakt",            220,   60,  2),
            LehrerUpgrade.damage("Nebelgranate",            380,  180,  3),
            LehrerUpgrade.damage("Plasma-Potion",           600,  400,  4),
            LehrerUpgrade.damage("Höllentrank",            1100,  800,  8)
    );
    /** Pfad C – Wurfgeschwindigkeit (GRÜN) */
    public static final List<LehrerUpgrade> LEHRER2_PFAD_C = List.of(
            LehrerUpgrade.speed("Schnelles Brauen",          90,    0, -0.2),
            LehrerUpgrade.speed("Fließband-Alchemie",       200,   60, -0.2),
            LehrerUpgrade.speed("Turbo-Labor",              350,  180, -0.25),
            LehrerUpgrade.speed("Quantenbrauen",            600,  400, -0.25),
            LehrerUpgrade.spezial("Perpetuum-Elixier",     1100,  800      )
    );

    // ── LEHRER 3: WINKLER – Floppy Disk Shooter ─────────────────
    /** Pfad A – Mehrfach-Ziele (ROT) */
    public static final List<LehrerUpgrade> LEHRER3_PFAD_A = List.of(
            LehrerUpgrade.multiTarget("Doppel-Disk",            80,    0,  1),
            LehrerUpgrade.multiTarget("Triple-Stack",          160,   40,  1),
            LehrerUpgrade.multiTarget("RAID-Angriff",          280,  120,  1),
            LehrerUpgrade.multiTarget("Cluster-Floppy",        500,  280,  2),
            LehrerUpgrade.spezial    ("Disketten-Sturm",       900,  600     )
    );
    /** Pfad B – Schnelligkeit (GELB) */
    public static final List<LehrerUpgrade> LEHRER3_PFAD_B = List.of(
            LehrerUpgrade.speed("Schneller Einschub",        60,    0, -0.05),
            LehrerUpgrade.speed("Overclock",                130,   40, -0.05),
            LehrerUpgrade.speed("SSD-Modus",                220,  120, -0.06),
            LehrerUpgrade.speed("NVMe-Reflexe",             380,  280, -0.07),
            LehrerUpgrade.spezial("Quantenbit-Transfer",    750,  600       )
    );
    /** Pfad C – Reichweite & Schaden (GRÜN) */
    public static final List<LehrerUpgrade> LEHRER3_PFAD_C = List.of(
            LehrerUpgrade.range ("Weitwurf-Algorithmus",     70,    0,  20),
            LehrerUpgrade.damage("Formatierter Schaden",    140,   40,  1),
            LehrerUpgrade.range ("Extended Reach",          240,  120,  25),
            LehrerUpgrade.damage("Bootsektor-Zerstörer",   420,  280,  2),
            new LehrerUpgrade   ("System-Crash",            800,  600,  30, -0.05, 2, 1, false)
    );
}