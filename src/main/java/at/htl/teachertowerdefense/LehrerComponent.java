package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;
import java.util.List;

/**
 * Verwaltet Upgrade-Zustand eines Lehrers.
 * Unterstützt Lehrer 0 (Groebl), 1 (Feichtner), 2 (Winkler).
 *
 * 2-stufiges System:
 *   1. FREISCHALTEN mit XP (dauerhaft, SaveData)
 *   2. KAUFEN mit Münzen (pro Turm, pro Spiel)
 */
public class LehrerComponent extends Component {

    private final int lehrerTyp; // 0=Groebl, 1=Feichtner, 2=Winkler

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

    /** Lehrer 0 = Groebl (Standard) */
    public LehrerComponent() { this(0); }

    public LehrerComponent(int lehrerTyp) {
        this.lehrerTyp = lehrerTyp;

        switch (lehrerTyp) {
            case 1 -> { // Feichtner – Sniper
                pfadA = LehrerUpgradePfade.LEHRER2_PFAD_A;
                pfadB = LehrerUpgradePfade.LEHRER2_PFAD_B;
                pfadC = LehrerUpgradePfade.LEHRER2_PFAD_C;
                range = LehrerUpgradePfade.BASE_RANGE_L2;
                shootDelay = LehrerUpgradePfade.BASE_SHOOT_DELAY_L2;
                damage = LehrerUpgradePfade.BASE_DAMAGE_L2;
                multiTarget = LehrerUpgradePfade.BASE_MULTI_TARGET_L2;
            }
            case 2 -> { // Winkler – Rapidfire
                pfadA = LehrerUpgradePfade.LEHRER3_PFAD_A;
                pfadB = LehrerUpgradePfade.LEHRER3_PFAD_B;
                pfadC = LehrerUpgradePfade.LEHRER3_PFAD_C;
                range = LehrerUpgradePfade.BASE_RANGE_L3;
                shootDelay = LehrerUpgradePfade.BASE_SHOOT_DELAY_L3;
                damage = LehrerUpgradePfade.BASE_DAMAGE_L3;
                multiTarget = LehrerUpgradePfade.BASE_MULTI_TARGET_L3;
            }
            default -> { // 0 = Groebl – Allrounder
                pfadA = LehrerUpgradePfade.LEHRER1_PFAD_A;
                pfadB = LehrerUpgradePfade.LEHRER1_PFAD_B;
                pfadC = LehrerUpgradePfade.LEHRER1_PFAD_C;
                range = LehrerUpgradePfade.BASE_RANGE_L1;
                shootDelay = LehrerUpgradePfade.BASE_SHOOT_DELAY_L1;
                damage = LehrerUpgradePfade.BASE_DAMAGE_L1;
                multiTarget = LehrerUpgradePfade.BASE_MULTI_TARGET_L1;
            }
        }
        spezialProjektil = false;
    }

    // ============================================================
    // BTD6 5-2-0 REGEL
    // ============================================================

    private int pfadeMitUpgrades() {
        int c = 0;
        if (stufePfadA > 0) c++;
        if (stufePfadB > 0) c++;
        if (stufePfadC > 0) c++;
        return c;
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

    public boolean freischaltenA() {
        if (stufePfadA >= pfadA.size()) return false;
        return SaveData.upgradeFreischalten(lehrerTyp, 0, stufePfadA, pfadA.get(stufePfadA).xpKosten);
    }

    public boolean freischaltenB() {
        if (stufePfadB >= pfadB.size()) return false;
        return SaveData.upgradeFreischalten(lehrerTyp, 1, stufePfadB, pfadB.get(stufePfadB).xpKosten);
    }

    public boolean freischaltenC() {
        if (stufePfadC >= pfadC.size()) return false;
        return SaveData.upgradeFreischalten(lehrerTyp, 2, stufePfadC, pfadC.get(stufePfadC).xpKosten);
    }

    public boolean istFreigeschaltetA() { return stufePfadA < pfadA.size() && SaveData.istUpgradeFrei(lehrerTyp, 0, stufePfadA); }
    public boolean istFreigeschaltetB() { return stufePfadB < pfadB.size() && SaveData.istUpgradeFrei(lehrerTyp, 1, stufePfadB); }
    public boolean istFreigeschaltetC() { return stufePfadC < pfadC.size() && SaveData.istUpgradeFrei(lehrerTyp, 2, stufePfadC); }

    public int xpKostenA() { return stufePfadA < pfadA.size() ? pfadA.get(stufePfadA).xpKosten : -1; }
    public int xpKostenB() { return stufePfadB < pfadB.size() ? pfadB.get(stufePfadB).xpKosten : -1; }
    public int xpKostenC() { return stufePfadC < pfadC.size() ? pfadC.get(stufePfadC).xpKosten : -1; }

    // ============================================================
    // KAUFEN (Münzen) – pro Turm
    // ============================================================

    public boolean kannUpgradeA() {
        if (stufePfadA >= 5) return false;
        if (!SaveData.istUpgradeFrei(lehrerTyp, 0, stufePfadA)) return false;
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

    public int     getLehrerTyp()        { return lehrerTyp; }
    public double  getRange()            { return range; }
    public double  getShootDelay()       { return shootDelay; }
    public int     getDamage()           { return damage; }
    public int     getMultiTarget()      { return multiTarget; }
    public boolean isSpezialProjektil()  { return spezialProjektil; }
    public int     getStufePfadA()       { return stufePfadA; }
    public int     getStufePfadB()       { return stufePfadB; }
    public int     getStufePfadC()       { return stufePfadC; }
    public String  getUpgradeStatus()    { return stufePfadA + "-" + stufePfadB + "-" + stufePfadC; }
}