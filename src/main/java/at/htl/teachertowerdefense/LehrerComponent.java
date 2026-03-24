package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;

import java.util.List;

/**
 * Verwaltet Upgrade-Zustand eines Lehrers.
 *
 * 2-stufiges System:
 *   1. FREISCHALTEN: Upgrade mit XP dauerhaft freischalten (SaveData, einmalig)
 *   2. KAUFEN: Freigeschaltetes Upgrade mit Münzen kaufen (pro Turm, pro Spiel)
 *
 * BTD6 5-2-0 Regel gilt weiterhin für das Kaufen.
 */
public class LehrerComponent extends Component {

    // Lehrer-Typ Index (0 = Lehrer1) für SaveData
    private final int lehrerTyp = 0;

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
    // BTD6 5-2-0 REGEL (Kauf-Logik)
    // ============================================================

    private int pfadeMitUpgrades() {
        int count = 0;
        if (stufePfadA > 0) count++;
        if (stufePfadB > 0) count++;
        if (stufePfadC > 0) count++;
        return count;
    }

    private int hauptPfad() {
        if (stufePfadA >= 3) return 0;
        if (stufePfadB >= 3) return 1;
        if (stufePfadC >= 3) return 2;
        return -1;
    }

    // ============================================================
    // FREISCHALTEN (XP) – dauerhaft in SaveData
    // ============================================================

    /** Versucht nächste Stufe von Pfad A mit XP freizuschalten */
    public boolean freischaltenA() {
        if (stufePfadA >= pfadA.size()) return false;
        LehrerUpgrade u = pfadA.get(stufePfadA);
        return SaveData.upgradeFreischalten(lehrerTyp, 0, stufePfadA, u.xpKosten);
    }

    public boolean freischaltenB() {
        if (stufePfadB >= pfadB.size()) return false;
        LehrerUpgrade u = pfadB.get(stufePfadB);
        return SaveData.upgradeFreischalten(lehrerTyp, 1, stufePfadB, u.xpKosten);
    }

    public boolean freischaltenC() {
        if (stufePfadC >= pfadC.size()) return false;
        LehrerUpgrade u = pfadC.get(stufePfadC);
        return SaveData.upgradeFreischalten(lehrerTyp, 2, stufePfadC, u.xpKosten);
    }

    /** Ist die aktuelle Stufe bereits dauerhaft freigeschaltet? */
    public boolean istFreigeschaltetA() { return stufePfadA < pfadA.size() && SaveData.istUpgradeFrei(lehrerTyp, 0, stufePfadA); }
    public boolean istFreigeschaltetB() { return stufePfadB < pfadB.size() && SaveData.istUpgradeFrei(lehrerTyp, 1, stufePfadB); }
    public boolean istFreigeschaltetC() { return stufePfadC < pfadC.size() && SaveData.istUpgradeFrei(lehrerTyp, 2, stufePfadC); }

    /** XP-Kosten der nächsten Stufe */
    public int xpKostenA() { return stufePfadA < pfadA.size() ? pfadA.get(stufePfadA).xpKosten : -1; }
    public int xpKostenB() { return stufePfadB < pfadB.size() ? pfadB.get(stufePfadB).xpKosten : -1; }
    public int xpKostenC() { return stufePfadC < pfadC.size() ? pfadC.get(stufePfadC).xpKosten : -1; }

    // ============================================================
    // KAUFEN (Münzen) – pro Turm
    // ============================================================

    public boolean kannUpgradeA() {
        if (stufePfadA >= 5) return false;
        if (!SaveData.istUpgradeFrei(lehrerTyp, 0, stufePfadA)) return false; // nicht freigeschaltet
        int hp = hauptPfad();
        if (hp != -1 && hp != 0 && stufePfadA >= 2) return false;
        if (stufePfadA == 0 && pfadeMitUpgrades() >= 2) return false;
        return true;
    }

    public boolean kannUpgradeB() {
        if (stufePfadB >= 5) return false;
        if (!SaveData.istUpgradeFrei(lehrerTyp, 1, stufePfadB)) return false;
        int hp = hauptPfad();
        if (hp != -1 && hp != 1 && stufePfadB >= 2) return false;
        if (stufePfadB == 0 && pfadeMitUpgrades() >= 2) return false;
        return true;
    }

    public boolean kannUpgradeC() {
        if (stufePfadC >= 5) return false;
        if (!SaveData.istUpgradeFrei(lehrerTyp, 2, stufePfadC)) return false;
        int hp = hauptPfad();
        if (hp != -1 && hp != 2 && stufePfadC >= 2) return false;
        if (stufePfadC == 0 && pfadeMitUpgrades() >= 2) return false;
        return true;
    }

    public void upgradeA() { if (!kannUpgradeA()) return; wendeAn(pfadA.get(stufePfadA)); stufePfadA++; }
    public void upgradeB() { if (!kannUpgradeB()) return; wendeAn(pfadB.get(stufePfadB)); stufePfadB++; }
    public void upgradeC() { if (!kannUpgradeC()) return; wendeAn(pfadC.get(stufePfadC)); stufePfadC++; }

    private void wendeAn(LehrerUpgrade u) {
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
    public String  getUpgradeStatus()   { return stufePfadA + "-" + stufePfadB + "-" + stufePfadC; }
}