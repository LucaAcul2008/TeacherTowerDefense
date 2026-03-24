package at.htl.teachertowerdefense;

/**
 * Globale Spielkonfiguration die zwischen Menü und Spielstart geteilt wird.
 */
public class GameConfig {
    public static int selectedMap  = 0; // 0 = Map1 (HTL Saalfelden)
    public static int selectedDiff = 0; // 0=Easy, 1=Medium, 2=Hard

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
            case 1 -> "Map2.tmx";
            case 2 -> "Map3.tmx";
            default -> "Map1.tmx";
        };
    }
}
