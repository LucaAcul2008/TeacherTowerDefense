package at.htl.teachertowerdefense;

/**
 * Globale Spielkonfiguration die zwischen Menü und Spielstart geteilt wird.
 */
public class GameConfig {
    public static int selectedMap  = 0; // 0 = HTL Saalfelden (Campus), 1 = City, 2 = Schulausflug, 3 = Teichwiese
    public static final int ANZAHL_MAPS = 4;
    public static int selectedDiff = 0; // 0=Easy, 1=Medium, 2=Hard
    public static double speedMulti = 1.0;

    // ── Fenstergröße ─────────────────────────────────────────────
    // Karte (960x640) + Shop (240) = 1200x640. Damit auf 16:9/16:10-Monitoren keine schwarzen
    // Ränder entstehen, wird die Spielfläche ans Seitenverhältnis angepasst:
    // höhere Bildschirme bekommen unten eine Infoleiste, breitere einen breiteren Shop.
    public static final int KARTE_BREITE = 960;
    public static final int KARTE_HOEHE  = 640;
    public static final int SHOP_BREITE  = 240;
    public static int breite = KARTE_BREITE + SHOP_BREITE;
    public static int hoehe  = KARTE_HOEHE;

    public static void passeAnBildschirmAn() {
        double basis = (double) (KARTE_BREITE + SHOP_BREITE) / KARTE_HOEHE;
        double ratio = basis;
        try {
            java.awt.DisplayMode dm = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDisplayMode();
            ratio = (double) dm.getWidth() / dm.getHeight();
        } catch (Exception | Error e) {
            // z.B. ohne Bildschirm (headless) → Standardgröße
        }
        ratio = Math.max(1.25, Math.min(3.0, ratio));
        if (ratio < basis) {
            breite = KARTE_BREITE + SHOP_BREITE;
            hoehe  = (int) Math.round(breite / ratio);
        } else {
            hoehe  = KARTE_HOEHE;
            breite = (int) Math.round(hoehe * ratio);
        }
    }

    /** Liegt der Punkt auf der Spielkarte (nicht im Shop / in der Infoleiste)? */
    public static boolean aufKarte(double x, double y) {
        return x >= 0 && y >= 0 && x < KARTE_BREITE && y < KARTE_HOEHE;
    }

    // ── Skin-Währung ─────────────────────────────────────────────
    // Bleibt zwischen Spielen erhalten und sieht bewusst anders aus als das Spielgeld (💰 €, gold).
    public static final String DIAMANT       = "💎";
    public static final String DIAMANT_NAME  = "Diamanten";
    public static final javafx.scene.paint.Color DIAMANT_FARBE = javafx.scene.paint.Color.web("#5ce1ff");

    /** z.B. "💎  120 Diamanten" */
    public static String diamanten(int anzahl) {
        return DIAMANT + "  " + anzahl + " " + DIAMANT_NAME;
    }

    /** Höchstens so viele Lehrer gleichzeitig auf der Karte – man soll upgraden statt spammen. */
    public static final int MAX_LEHRER = 8;

    // Multiplikatoren pro Schwierigkeit
    public static double getStartgeldMulti() {
        return switch (selectedDiff) {
            case 1 -> 0.75; // Medium: weniger Startgeld
            case 2 -> 0.5;  // Hard: noch weniger
            default -> 1.0;
        };
    }

    public static double getSchuelerHpMulti() {
        return switch (selectedDiff) {
            case 1 -> 1.5;
            case 2 -> 2.0;
            default -> 1.0;
        };
    }

    public static int getMuenzenBelohnung() {
        return switch (selectedDiff) {
            case 1 -> 175;
            case 2 -> 250;
            default -> 100;
        };
    }

    public static String getMapDatei() {
        return switch (selectedMap) {
            case 1  -> "City.tmx";
            case 2  -> "Map3.tmx";
            case 3  -> "Map1.tmx";      // die ursprüngliche erste Map (Wiese mit Teichen)
            default -> "Campus.tmx";    // HTL Saalfelden
        };
    }
}