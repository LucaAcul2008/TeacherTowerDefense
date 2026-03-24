package at.htl.teachertowerdefense;

import java.util.prefs.Preferences;

/**
 * Speichert Spielfortschritt via Java Preferences.
 * Tracks: Münzen, XP pro Lehrer, Maps, freigeschaltete Upgrades.
 */
public class SaveData {

    private static final Preferences PREFS = Preferences.userNodeForPackage(SaveData.class);

    public static int         muenzen             = 0;
    public static int[]       lehrerXP            = new int[5];
    public static boolean[][] abgeschlossen       = new boolean[3][3];
    public static boolean[]   mapFreigeschaltet   = { true, false, false };

    // Freigeschaltete Upgrades: [lehrerTyp][pfad 0-2][stufe 0-4]
    private static boolean[][][] upgradesFrei = new boolean[5][3][5];

    // -------------------------------------------------------
    // Laden / Speichern
    // -------------------------------------------------------

    public static void laden() {
        muenzen = PREFS.getInt("muenzen", 0);
        for (int i = 0; i < lehrerXP.length; i++)
            lehrerXP[i] = PREFS.getInt("xp" + i, 0);
        for (int m = 0; m < 3; m++) {
            mapFreigeschaltet[m] = PREFS.getBoolean("map_frei_" + m, m == 0);
            for (int s = 0; s < 3; s++)
                abgeschlossen[m][s] = PREFS.getBoolean("abg_" + m + "_" + s, false);
        }
        for (int l = 0; l < 5; l++)
            for (int p = 0; p < 3; p++)
                for (int s = 0; s < 5; s++)
                    upgradesFrei[l][p][s] = PREFS.getBoolean("upg_" + l + "_" + p + "_" + s, false);
    }

    public static void speichern() {
        PREFS.putInt("muenzen", muenzen);
        for (int i = 0; i < lehrerXP.length; i++)
            PREFS.putInt("xp" + i, lehrerXP[i]);
        for (int m = 0; m < 3; m++) {
            PREFS.putBoolean("map_frei_" + m, mapFreigeschaltet[m]);
            for (int s = 0; s < 3; s++)
                PREFS.putBoolean("abg_" + m + "_" + s, abgeschlossen[m][s]);
        }
        for (int l = 0; l < 5; l++)
            for (int p = 0; p < 3; p++)
                for (int s = 0; s < 5; s++)
                    PREFS.putBoolean("upg_" + l + "_" + p + "_" + s, upgradesFrei[l][p][s]);
    }

    // -------------------------------------------------------
    // Upgrade Freischalten mit XP
    // -------------------------------------------------------

    /**
     * Schaltet ein Upgrade dauerhaft frei.
     * Vorgänger-Stufe muss bereits freigeschaltet sein (außer Stufe 0).
     * @return true wenn erfolgreich
     */
    public static boolean upgradeFreischalten(int lehrerTyp, int pfad, int stufe, int xpKosten) {
        if (upgradesFrei[lehrerTyp][pfad][stufe]) return true;        // schon frei
        if (stufe > 0 && !upgradesFrei[lehrerTyp][pfad][stufe - 1]) return false; // Vorgänger fehlt
        if (lehrerXP[lehrerTyp] < xpKosten) return false;             // zu wenig XP
        lehrerXP[lehrerTyp] -= xpKosten;
        upgradesFrei[lehrerTyp][pfad][stufe] = true;
        speichern();
        return true;
    }

    public static boolean istUpgradeFrei(int lehrerTyp, int pfad, int stufe) {
        return upgradesFrei[lehrerTyp][pfad][stufe];
    }

    public static int getXP(int lehrerTyp) { return lehrerXP[lehrerTyp]; }

    // -------------------------------------------------------
    // Map / Münzen
    // -------------------------------------------------------

    public static void mapBeendet(int mapIndex, int schwierigkeit, int belohnungMuenzen, int belohnungXP) {
        abgeschlossen[mapIndex][schwierigkeit] = true;
        muenzen += belohnungMuenzen;
        lehrerXP[0] += belohnungXP;
        if (schwierigkeit == 0 && mapIndex + 1 < mapFreigeschaltet.length)
            mapFreigeschaltet[mapIndex + 1] = true;
        speichern();
    }

    public static boolean istFreigeschaltet(int mapIndex, int schwierigkeit) {
        if (!mapFreigeschaltet[mapIndex]) return false;
        if (schwierigkeit == 0) return true;
        return abgeschlossen[mapIndex][schwierigkeit - 1];
    }

    public static int getSterne(int mapIndex) {
        int sterne = 0;
        for (boolean b : abgeschlossen[mapIndex]) if (b) sterne++;
        return sterne;
    }

    /** Für Entwicklung: kompletten Fortschritt zurücksetzen */
    public static void reset() {
        try { PREFS.clear(); } catch (Exception ignored) {}
        muenzen = 0; lehrerXP = new int[5];
        abgeschlossen = new boolean[3][3];
        mapFreigeschaltet = new boolean[]{ true, false, false };
        upgradesFrei = new boolean[5][3][5];
    }
}