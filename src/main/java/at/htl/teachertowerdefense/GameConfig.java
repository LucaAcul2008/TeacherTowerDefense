package at.htl.teachertowerdefense;

/**
 * Globale Spielkonfiguration die zwischen Menü und Spielstart geteilt wird.
 */
public class GameConfig {
    public static int selectedMap  = 0; // 0 = Map1 (HTL Saalfelden)
    public static int selectedDiff = 0; // 0=Easy, 1=Medium, 2=Hard
    public static double speedMulti = 1.0;

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
            default -> "Map1.tmx";
        };
    }
}