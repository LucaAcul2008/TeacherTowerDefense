package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;

import java.util.List;

/**
 * Verwaltet den Upgrade-Zustand eines Lehrers.
 *
 * BTD6-Regel:
 *   - Max 5 Upgrades auf einem Pfad, max 2 auf dem anderen
 *   - Sobald ein Pfad Stufe 3+ erreicht, ist der andere auf max 2 begrenzt
 *   - Niemals beide Pfade auf 3+ gleichzeitig
 */
public class LehrerComponent extends Component {

    // Aktuelle Upgrade-Stufen (0 = kein Upgrade)
    private int stufePfadA = 0;
    private int stufePfadB = 0;

    // Aktuelle Stats (werden beim Upgrade angepasst)
    private double range;
    private double shootDelay;
    private int    damage;
    private int    multiTarget;
    private boolean spezialProjektil;

    // Upgrade-Definitionen
    private final List<LehrerUpgrade> pfadA;
    private final List<LehrerUpgrade> pfadB;

    public LehrerComponent() {
        this.pfadA = LehrerUpgradePfade.LEHRER1_PFAD_A;
        this.pfadB = LehrerUpgradePfade.LEHRER1_PFAD_B;

        // Basis-Stats
        this.range            = LehrerUpgradePfade.BASE_RANGE;
        this.shootDelay       = LehrerUpgradePfade.BASE_SHOOT_DELAY;
        this.damage           = LehrerUpgradePfade.BASE_DAMAGE;
        this.multiTarget      = LehrerUpgradePfade.BASE_MULTI_TARGET;
        this.spezialProjektil = false;
    }

    // ============================================================
    // UPGRADE-LOGIK
    // ============================================================

    /** Gibt zurück ob Pfad A auf die nächste Stufe upgegraded werden kann. */
    public boolean kannUpgradeA() {
        if (stufePfadA >= 5) return false;
        // BTD6-Regel: wenn B schon auf 3+, darf A max auf 2
        if (stufePfadB >= 3 && stufePfadA >= 2) return false;
        return true;
    }

    /** Gibt zurück ob Pfad B auf die nächste Stufe upgegraded werden kann. */
    public boolean kannUpgradeB() {
        if (stufePfadB >= 5) return false;
        // BTD6-Regel: wenn A schon auf 3+, darf B max auf 2
        if (stufePfadA >= 3 && stufePfadB >= 2) return false;
        return true;
    }

    /** Führt das nächste Upgrade auf Pfad A durch (Kosten bereits geprüft). */
    public void upgradeA() {
        if (!kannUpgradeA()) return;
        LehrerUpgrade u = pfadA.get(stufePfadA);
        wendeAnUpgrade(u);
        stufePfadA++;
    }

    /** Führt das nächste Upgrade auf Pfad B durch (Kosten bereits geprüft). */
    public void upgradeB() {
        if (!kannUpgradeB()) return;
        LehrerUpgrade u = pfadB.get(stufePfadB);
        wendeAnUpgrade(u);
        stufePfadB++;
    }

    private void wendeAnUpgrade(LehrerUpgrade u) {
        range            += u.rangeDelta;
        shootDelay       += u.shootDelayDelta;
        damage           += u.damageDelta;
        multiTarget      += u.multiTargetDelta;
        if (u.spezialProjektil) spezialProjektil = true;

        // Minimum-Grenze für shootDelay
        if (shootDelay < 0.1) shootDelay = 0.1;
    }

    // ============================================================
    // KOSTEN-ABFRAGEN
    // ============================================================

    /** Kosten des nächsten Upgrades auf Pfad A, -1 wenn nicht verfügbar. */
    public int kostenNaechstesUpgradeA() {
        if (!kannUpgradeA()) return -1;
        return pfadA.get(stufePfadA).kosten;
    }

    /** Kosten des nächsten Upgrades auf Pfad B, -1 wenn nicht verfügbar. */
    public int kostenNaechstesUpgradeB() {
        if (!kannUpgradeB()) return -1;
        return pfadB.get(stufePfadB).kosten;
    }

    /** Name des nächsten Upgrades auf Pfad A. */
    public String nameNaechstesUpgradeA() {
        if (stufePfadA >= pfadA.size()) return "MAX";
        return pfadA.get(stufePfadA).name;
    }

    /** Name des nächsten Upgrades auf Pfad B. */
    public String nameNaechstesUpgradeB() {
        if (stufePfadB >= pfadB.size()) return "MAX";
        return pfadB.get(stufePfadB).name;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public double  getRange()            { return range; }
    public double  getShootDelay()       { return shootDelay; }
    public int     getDamage()           { return damage; }
    public int     getMultiTarget()      { return multiTarget; }
    public boolean isSpezialProjektil()  { return spezialProjektil; }
    public int     getStufePfadA()       { return stufePfadA; }
    public int     getStufePfadB()       { return stufePfadB; }

    /** Gibt den Upgrade-Zustand als String zurück z.B. "2-1" */
    public String getUpgradeStatus() { return stufePfadA + "-" + stufePfadB; }
}
