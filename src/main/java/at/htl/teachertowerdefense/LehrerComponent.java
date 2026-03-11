package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;

import java.util.List;

/**
 * Verwaltet Upgrade-Zustand eines Lehrers.
 *
 * BTD6 5-2-0 Regel (3 Pfade):
 *   - Nur EIN Pfad darf auf 3+ gehen (wird dann zum "Hauptpfad")
 *   - Die anderen beiden Pfade dürfen maximal auf Stufe 2
 *   - Sobald Hauptpfad bestimmt ist, kann kein anderer mehr Hauptpfad werden
 */
public class LehrerComponent extends Component {

    private int stufePfadA = 0;
    private int stufePfadB = 0;
    private int stufePfadC = 0;

    private double  range;
    private double  shootDelay;
    private int     damage;
    private int     multiTarget;
    private boolean spezialProjektil;

    private final List<LehrerUpgrade> pfadA;
    private final List<LehrerUpgrade> pfadB;
    private final List<LehrerUpgrade> pfadC;

    public LehrerComponent() {
        this.pfadA = LehrerUpgradePfade.LEHRER1_PFAD_A;
        this.pfadB = LehrerUpgradePfade.LEHRER1_PFAD_B;
        this.pfadC = LehrerUpgradePfade.LEHRER1_PFAD_C;

        this.range            = LehrerUpgradePfade.BASE_RANGE;
        this.shootDelay       = LehrerUpgradePfade.BASE_SHOOT_DELAY;
        this.damage           = LehrerUpgradePfade.BASE_DAMAGE;
        this.multiTarget      = LehrerUpgradePfade.BASE_MULTI_TARGET;
        this.spezialProjektil = false;
    }

    // ============================================================
    // BTD6 5-2-0 REGEL
    // ============================================================

    /** Wie viele Pfade haben mindestens 1 Upgrade? */
    private int pfadeMitUpgrades() {
        int count = 0;
        if (stufePfadA > 0) count++;
        if (stufePfadB > 0) count++;
        if (stufePfadC > 0) count++;
        return count;
    }

    /** Welcher Pfad ist der Hauptpfad (Stufe 3+)? -1 = noch keiner */
    private int hauptPfad() {
        if (stufePfadA >= 3) return 0;
        if (stufePfadB >= 3) return 1;
        if (stufePfadC >= 3) return 2;
        return -1;
    }

    public boolean kannUpgradeA() {
        if (stufePfadA >= 5) return false;
        int hp = hauptPfad();
        // Wenn ein anderer Pfad Hauptpfad ist, darf A max auf 2
        if (hp != -1 && hp != 0 && stufePfadA >= 2) return false;
        // 5-2-0 Regel: wenn A noch 0 hat, aber schon 2 andere Pfade Upgrades haben → gesperrt
        if (stufePfadA == 0 && pfadeMitUpgrades() >= 2) return false;
        return true;
    }

    public boolean kannUpgradeB() {
        if (stufePfadB >= 5) return false;
        int hp = hauptPfad();
        if (hp != -1 && hp != 1 && stufePfadB >= 2) return false;
        if (stufePfadB == 0 && pfadeMitUpgrades() >= 2) return false;
        return true;
    }

    public boolean kannUpgradeC() {
        if (stufePfadC >= 5) return false;
        int hp = hauptPfad();
        if (hp != -1 && hp != 2 && stufePfadC >= 2) return false;
        if (stufePfadC == 0 && pfadeMitUpgrades() >= 2) return false;
        return true;
    }

    // ============================================================
    // UPGRADE DURCHFÜHREN
    // ============================================================

    public void upgradeA() {
        if (!kannUpgradeA()) return;
        wendeAnUpgrade(pfadA.get(stufePfadA));
        stufePfadA++;
    }

    public void upgradeB() {
        if (!kannUpgradeB()) return;
        wendeAnUpgrade(pfadB.get(stufePfadB));
        stufePfadB++;
    }

    public void upgradeC() {
        if (!kannUpgradeC()) return;
        wendeAnUpgrade(pfadC.get(stufePfadC));
        stufePfadC++;
    }

    private void wendeAnUpgrade(LehrerUpgrade u) {
        range       += u.rangeDelta;
        shootDelay  += u.shootDelayDelta;
        damage      += u.damageDelta;
        multiTarget += u.multiTargetDelta;
        if (u.spezialProjektil) spezialProjektil = true;
        if (shootDelay < 0.1) shootDelay = 0.1;
    }

    // ============================================================
    // KOSTEN & NAMEN
    // ============================================================

    public int    kostenA() { return !kannUpgradeA() || stufePfadA >= pfadA.size() ? -1 : pfadA.get(stufePfadA).kosten; }
    public int    kostenB() { return !kannUpgradeB() || stufePfadB >= pfadB.size() ? -1 : pfadB.get(stufePfadB).kosten; }
    public int    kostenC() { return !kannUpgradeC() || stufePfadC >= pfadC.size() ? -1 : pfadC.get(stufePfadC).kosten; }

    public String nameA()   { return stufePfadA >= pfadA.size() ? "MAX" : pfadA.get(stufePfadA).name; }
    public String nameB()   { return stufePfadB >= pfadB.size() ? "MAX" : pfadB.get(stufePfadB).name; }
    public String nameC()   { return stufePfadC >= pfadC.size() ? "MAX" : pfadC.get(stufePfadC).name; }

    // ============================================================
    // GETTERS
    // ============================================================

    public double  getRange()           { return range; }
    public double  getShootDelay()      { return shootDelay; }
    public int     getDamage()          { return damage; }
    public int     getMultiTarget()     { return multiTarget; }
    public boolean isSpezialProjektil() { return spezialProjektil; }
    public int     getStufePfadA()      { return stufePfadA; }
    public int     getStufePfadB()      { return stufePfadB; }
    public int     getStufePfadC()      { return stufePfadC; }

    public String getUpgradeStatus()    { return stufePfadA + "-" + stufePfadB + "-" + stufePfadC; }
}