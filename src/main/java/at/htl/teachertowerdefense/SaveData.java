package at.htl.teachertowerdefense;

import java.util.prefs.Preferences;

public class SaveData {

    private static final Preferences PREFS = Preferences.userNodeForPackage(SaveData.class);

    public static int         muenzen             = 0;
    public static int[]       lehrerXP            = new int[5];
    public static boolean[][] abgeschlossen       = new boolean[3][3];
    public static boolean[]   mapFreigeschaltet   = { true, false, false };

    private static boolean[][][] upgradesFrei = new boolean[5][3][5];

    // ── SKINS ────────────────────────────────────────────────────
    public static int[]      aktiverSkin          = new int[3];
    public static boolean[][] skinFreigeschaltet  = new boolean[3][2];
    public static final int[] SKIN_PREISE         = { 200, 300, 250 };

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

        // Skins laden
        for (int i = 0; i < 3; i++) {
            aktiverSkin[i]           = PREFS.getInt("skin_aktiv_" + i, 0);
            skinFreigeschaltet[i][0] = true; // Standard immer gratis
            skinFreigeschaltet[i][1] = PREFS.getBoolean("skin_frei_" + i + "_1", false);
        }
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

        // Skins speichern
        for (int i = 0; i < 3; i++) {
            PREFS.putInt("skin_aktiv_" + i, aktiverSkin[i]);
            PREFS.putBoolean("skin_frei_" + i + "_1", skinFreigeschaltet[i][1]);
        }
    }

    // -------------------------------------------------------
    // Upgrade Freischalten mit XP
    // -------------------------------------------------------

    public static boolean upgradeFreischalten(int lehrerTyp, int pfad, int stufe, int xpKosten) {
        if (upgradesFrei[lehrerTyp][pfad][stufe]) return true;
        if (stufe > 0 && !upgradesFrei[lehrerTyp][pfad][stufe - 1]) return false;
        if (lehrerXP[lehrerTyp] < xpKosten) return false;
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
    // Skins
    // -------------------------------------------------------

    public static boolean kaufeSkin(int lehrerTyp) {
        if (skinFreigeschaltet[lehrerTyp][1]) {
            // Schon gekauft → nur an/ausschalten
            aktiverSkin[lehrerTyp] = aktiverSkin[lehrerTyp] == 1 ? 0 : 1;
            speichern();
            return true;
        }
        int preis = SKIN_PREISE[lehrerTyp];
        if (muenzen < preis) return false;
        muenzen -= preis;
        skinFreigeschaltet[lehrerTyp][1] = true;
        aktiverSkin[lehrerTyp] = 1;
        speichern();
        return true;
    }

    // -------------------------------------------------------
    // Map / Münzen
    // -------------------------------------------------------

    public static void mapBeendet(int mapIndex, int schwierigkeit, int belohnungMuenzen, int belohnungXP) {
        abgeschlossen[mapIndex][schwierigkeit] = true;
        muenzen += belohnungMuenzen;
        for (int i = 0; i < lehrerXP.length; i++) lehrerXP[i] += belohnungXP;
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

    public static void reset() {
        try { PREFS.clear(); } catch (Exception ignored) {}
        muenzen = 0;
        lehrerXP = new int[5];
        abgeschlossen = new boolean[3][3];
        mapFreigeschaltet = new boolean[]{ true, false, false };
        upgradesFrei = new boolean[5][3][5];
        aktiverSkin = new int[3];
        skinFreigeschaltet = new boolean[3][2];
    }
}