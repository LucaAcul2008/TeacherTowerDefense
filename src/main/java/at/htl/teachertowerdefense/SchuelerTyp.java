package at.htl.teachertowerdefense;

import javafx.scene.paint.Color;

public enum SchuelerTyp {

    // --- KLEIN (20x20, speed 100) ---
    TYP1("Erstklässler",    1, 20, 100, Color.RED,                  null,  0, 5),
    TYP2("Zweitklässler",   1, 20, 100, Color.BLUE,                 TYP1,  1, 8),
    TYP3("Drittklässler",   1, 20, 100, Color.GREEN,                TYP2,  1, 10),
    TYP4("Viertklässler",   1, 20, 100, Color.YELLOW,               TYP3,  1, 12),

    // --- MITTEL (30x30, speed 70) ---
    TYP5("Fünftklässler",   1, 30,  70, Color.ORANGE,               TYP4,  2, 20),
    TYP6("Sechstklässler",  1, 30,  70, Color.PURPLE,               TYP5,  2, 30),

    // --- GROSS (45x45, speed 50) ---
    TYP7("Maturant",        1, 45,  50, Color.DARKGRAY,             TYP5,  4, 50),
    TYP8("Schulleiter",     1, 45,  50, Color.color(0.1,0.1,0.1),   TYP7,  2, 100);

    public final String anzeigeName;
    public final int    maxHp;
    public final int    groesse;    // Pixel (Breite & Höhe)
    public final double speed;      // Pixel pro Sekunde
    public final Color  farbe;
    public final SchuelerTyp kindTyp;     // null = keine Kinder beim Tod
    public final int         kindAnzahl;
    public final int         belohnung;   // Geld bei Kill

    SchuelerTyp(String anzeigeName, int maxHp, int groesse, double speed,
                Color farbe, SchuelerTyp kindTyp, int kindAnzahl, int belohnung) {
        this.anzeigeName = anzeigeName;
        this.maxHp       = maxHp;
        this.groesse     = groesse;
        this.speed       = speed;
        this.farbe       = farbe;
        this.kindTyp     = kindTyp;
        this.kindAnzahl  = kindAnzahl;
        this.belohnung   = belohnung;
    }
}
