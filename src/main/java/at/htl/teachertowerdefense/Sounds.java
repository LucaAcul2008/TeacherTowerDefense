package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;

import java.util.HashMap;
import java.util.Map;

/**
 * Zentrale Stelle für Soundeffekte und Musik.
 * Dateien liegen in assets/sounds/ bzw. assets/music/ und werden von tools/gen_sounds.py erzeugt.
 * Ob Sound/Musik an sind, wird in SaveData gespeichert (Pause-Menü bzw. Taste M).
 */
public final class Sounds {

    private Sounds() {}

    /** Mindestabstand pro Effekt, damit z.B. Winklers Dauerfeuer nicht zu einem Brei wird. */
    private static final long MIN_ABSTAND_MS = 60;
    private static final Map<String, Long> zuletztGespielt = new HashMap<>();

    private static String aktuelleMusik = null;
    private static boolean musikLaeuft  = false;

    public static void play(String name) {
        if (!SaveData.soundAn) return;
        long jetzt = System.currentTimeMillis();
        Long zuletzt = zuletztGespielt.get(name);
        if (zuletzt != null && jetzt - zuletzt < MIN_ABSTAND_MS) return;
        zuletztGespielt.put(name, jetzt);
        try {
            FXGL.play(name + ".wav");
        } catch (Exception ignored) {
            // Ein fehlender Sound soll nie das Spiel abstürzen lassen
        }
    }

    /** Startet eine Hintergrundmusik in Dauerschleife (läuft sie schon, passiert nichts). */
    public static void musik(String name) {
        if (name.equals(aktuelleMusik) && (musikLaeuft || !SaveData.musikAn)) return;
        stoppeMusik();
        aktuelleMusik = name;
        starteAktuelleMusik();
    }

    public static void stoppeMusik() {
        try { FXGL.getAudioPlayer().stopAllMusic(); } catch (Exception ignored) {}
        musikLaeuft = false;
    }

    /** Für Game-Over/Gewonnen: Musik aus, damit der Jingle hörbar ist. Nächster musik()-Aufruf startet neu. */
    public static void musikBeenden() {
        stoppeMusik();
        aktuelleMusik = null;
    }

    public static void setSoundAn(boolean an) {
        SaveData.soundAn = an;
        SaveData.speichern();
    }

    public static void setMusikAn(boolean an) {
        SaveData.musikAn = an;
        SaveData.speichern();
        if (an) starteAktuelleMusik();
        else    stoppeMusik();
    }

    private static void starteAktuelleMusik() {
        if (aktuelleMusik == null || !SaveData.musikAn || musikLaeuft) return;
        try {
            FXGL.loopBGM(aktuelleMusik + ".wav");
            musikLaeuft = true;
        } catch (Exception ignored) {}
    }
}
