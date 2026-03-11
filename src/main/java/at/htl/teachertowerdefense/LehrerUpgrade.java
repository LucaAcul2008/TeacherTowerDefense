package at.htl.teachertowerdefense;

/**
 * Beschreibt ein einzelnes Upgrade auf einem Pfad.
 * Alle Werte sind Deltas (werden zum aktuellen Wert addiert).
 */
public class LehrerUpgrade {

    public final String  name;
    public final int     kosten;
    public final double  rangeDelta;       // Reichweite +
    public final double  shootDelayDelta;  // Schussfrequenz (negativ = schneller)
    public final int     damageDelta;      // Schaden +
    public final int     multiTargetDelta; // Zusätzliche Ziele gleichzeitig +
    public final boolean spezialProjektil; // Schaltet Spezial-Projektil frei

    public LehrerUpgrade(String name, int kosten,
                         double rangeDelta, double shootDelayDelta,
                         int damageDelta, int multiTargetDelta,
                         boolean spezialProjektil) {
        this.name             = name;
        this.kosten           = kosten;
        this.rangeDelta       = rangeDelta;
        this.shootDelayDelta  = shootDelayDelta;
        this.damageDelta      = damageDelta;
        this.multiTargetDelta = multiTargetDelta;
        this.spezialProjektil = spezialProjektil;
    }

    // Convenience-Konstruktoren
    public static LehrerUpgrade range(String name, int kosten, double delta) {
        return new LehrerUpgrade(name, kosten, delta, 0, 0, 0, false);
    }
    public static LehrerUpgrade speed(String name, int kosten, double delta) {
        return new LehrerUpgrade(name, kosten, 0, delta, 0, 0, false);
    }
    public static LehrerUpgrade damage(String name, int kosten, int delta) {
        return new LehrerUpgrade(name, kosten, 0, 0, delta, 0, false);
    }
    public static LehrerUpgrade multiTarget(String name, int kosten, int delta) {
        return new LehrerUpgrade(name, kosten, 0, 0, 0, delta, false);
    }
    public static LehrerUpgrade spezial(String name, int kosten) {
        return new LehrerUpgrade(name, kosten, 0, 0, 0, 0, true);
    }
}
