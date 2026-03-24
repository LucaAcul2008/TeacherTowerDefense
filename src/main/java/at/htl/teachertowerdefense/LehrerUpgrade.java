package at.htl.teachertowerdefense;

/**
 * Ein einzelnes Upgrade.
 * xpKosten = XP um es dauerhaft freizuschalten (einmalig, gespeichert in SaveData)
 * kosten   = Münzen um es im Spiel zu kaufen (pro Turm)
 */
public class LehrerUpgrade {

    public final String  name;
    public final int     kosten;           // Münzen im Spiel
    public final int     xpKosten;         // XP zum Freischalten (einmalig)
    public final double  rangeDelta;
    public final double  shootDelayDelta;
    public final int     damageDelta;
    public final int     multiTargetDelta;
    public final boolean spezialProjektil;

    public LehrerUpgrade(String name, int kosten, int xpKosten,
                         double rangeDelta, double shootDelayDelta,
                         int damageDelta, int multiTargetDelta,
                         boolean spezialProjektil) {
        this.name             = name;
        this.kosten           = kosten;
        this.xpKosten         = xpKosten;
        this.rangeDelta       = rangeDelta;
        this.shootDelayDelta  = shootDelayDelta;
        this.damageDelta      = damageDelta;
        this.multiTargetDelta = multiTargetDelta;
        this.spezialProjektil = spezialProjektil;
    }

    public static LehrerUpgrade speed(String name, int kosten, int xpKosten, double delta) {
        return new LehrerUpgrade(name, kosten, xpKosten, 0, delta, 0, 0, false);
    }
    public static LehrerUpgrade damage(String name, int kosten, int xpKosten, int delta) {
        return new LehrerUpgrade(name, kosten, xpKosten, 0, 0, delta, 0, false);
    }
    public static LehrerUpgrade range(String name, int kosten, int xpKosten, double delta) {
        return new LehrerUpgrade(name, kosten, xpKosten, delta, 0, 0, 0, false);
    }
    public static LehrerUpgrade multiTarget(String name, int kosten, int xpKosten, int delta) {
        return new LehrerUpgrade(name, kosten, xpKosten, 0, 0, 0, delta, false);
    }
    public static LehrerUpgrade spezial(String name, int kosten, int xpKosten) {
        return new LehrerUpgrade(name, kosten, xpKosten, 0, 0, 0, 0, true);
    }
}