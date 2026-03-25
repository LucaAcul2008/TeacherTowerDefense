package at.htl.teachertowerdefense;

import javafx.scene.paint.Color;

public enum SchuelerTyp {

    // --- KLEIN (20x20, speed 100) ---
    TYP1("Erstklässler",    1, 48, 100, Color.RED,                  null,  0, 5,   1),
    TYP2("Zweitklässler",   2, 48, 100, Color.BLUE,                 TYP1,  1, 8,   1),
    TYP3("Drittklässler",   3, 48, 100, Color.GREEN,                TYP2,  1, 10,  1),
    TYP4("Viertklässler",   4, 48, 100, Color.YELLOW,               TYP3,  1, 12,  1),
    TYP5("Fünftklässler",   5, 60,  70, Color.ORANGE,               TYP4,  2, 20,  2),
    TYP6("Sechstklässler",  6, 60,  70, Color.PURPLE,               TYP5,  2, 30,  2),
    TYP7("Maturant",        8, 72,  50, Color.DARKGRAY,             TYP5,  4, 50,  3),
    TYP8("Schulleiter",    12, 72,  50, Color.color(0.1,0.1,0.1),   TYP7,  2, 100, 5);

    public final String anzeigeName;
    public final int    maxHp;
    public final int    groesse;    // Pixel (Breite & Höhe)
    public final double speed;
    public final Color  farbe;
    public final SchuelerTyp kindTyp;
    public final int         kindAnzahl;
    public final int         belohnung;
    public final int         lebenSchaden; // Leben die abgezogen werden wenn Schüler Ziel erreicht

    SchuelerTyp(String anzeigeName, int maxHp, int groesse, double speed,
                Color farbe, SchuelerTyp kindTyp, int kindAnzahl, int belohnung, int lebenSchaden) {
        this.anzeigeName  = anzeigeName;
        this.maxHp        = maxHp;
        this.groesse      = groesse;
        this.speed        = speed;
        this.farbe        = farbe;
        this.kindTyp      = kindTyp;
        this.kindAnzahl   = kindAnzahl;
        this.belohnung    = belohnung;
        this.lebenSchaden = lebenSchaden;
    }
}