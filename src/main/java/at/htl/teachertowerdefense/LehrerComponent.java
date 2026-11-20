package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;

import java.util.List;

/**
 * Verwaltet Upgrade-Zustand eines Lehrers.
 * Unterstützt Lehrer 0 (Groebl), 1 (Feichtner), 2 (Winkler).
 * <p>
 * 2-stufiges System:
 * 1. FREISCHALTEN mit XP (dauerhaft, SaveData)
 * 2. KAUFEN mit Münzen (pro Turm, pro Spiel)
 */
public class LehrerComponent extends Component {

    private final int lehrerTyp; // 0=Groebl, 1=Feichtner, 2=Winkler

    private int stufePfadA = 0;
    private int stufePfadB = 0;
    private int stufePfadC = 0;

    private double range;
    private double shootDelay;
    private int damage;
    private int multiTarget;
    private boolean spezialProjektil;
    private boolean nutztSkin;

    private int investiertesGeld = 0;
    private int pops = 0;

    public void setBaseKosten(int kosten) {
        this.investiertesGeld = kosten;
    }

    public int getInvestiertesGeld() {
        return investiertesGeld;
    }

    private final List<LehrerUpgrade> pfadA;
    private final List<LehrerUpgrade> pfadB;
    private final List<LehrerUpgrade> pfadC;

    /**
     * Lehrer 0 = Groebl (Standard)
     */
    public LehrerComponent() {
        this(0);
    }

    public LehrerComponent(int lehrerTyp) {
        this.lehrerTyp = lehrerTyp;

        this.nutztSkin = (lehrerTyp < SaveData.aktiverSkin.length && SaveData.aktiverSkin[lehrerTyp] == 1);

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
            case 3 -> { // Aigner – Bufferin
                pfadA = LehrerUpgradePfade.LEHRER4_PFAD_A;
                pfadB = LehrerUpgradePfade.LEHRER4_PFAD_B;
                pfadC = LehrerUpgradePfade.LEHRER4_PFAD_C;
                range = LehrerUpgradePfade.BASE_RANGE_L4;
                shootDelay = LehrerUpgradePfade.BASE_SHOOT_DELAY_L4;
                damage = LehrerUpgradePfade.BASE_DAMAGE_L4;
                multiTarget = LehrerUpgradePfade.BASE_MULTI_TARGET_L4;
            }
            case 4 -> { // Gassner – Geldgenerator
                pfadA = LehrerUpgradePfade.LEHRER5_PFAD_A;
                pfadB = LehrerUpgradePfade.LEHRER5_PFAD_B;
                pfadC = LehrerUpgradePfade.LEHRER5_PFAD_C;
                range = LehrerUpgradePfade.BASE_RANGE_L5;
                shootDelay = LehrerUpgradePfade.BASE_SHOOT_DELAY_L5;
                damage = LehrerUpgradePfade.BASE_DAMAGE_L5;
                multiTarget = LehrerUpgradePfade.BASE_MULTI_TARGET_L5;
            }
            default -> { // 0 = Groebl – Allrounder
                pfadA = nutztSkin ? LehrerUpgradePfade.LEHRER1_PFAD_A_GOLF : LehrerUpgradePfade.LEHRER1_PFAD_A;
                pfadB = nutztSkin ? LehrerUpgradePfade.LEHRER1_PFAD_B_GOLF : LehrerUpgradePfade.LEHRER1_PFAD_B;
                pfadC = nutztSkin ? LehrerUpgradePfade.LEHRER1_PFAD_C_GOLF : LehrerUpgradePfade.LEHRER1_PFAD_C;
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

    public boolean istFreigeschaltetA() {
        return stufePfadA < pfadA.size() && SaveData.istUpgradeFrei(lehrerTyp, 0, stufePfadA);
    }

    public boolean istFreigeschaltetB() {
        return stufePfadB < pfadB.size() && SaveData.istUpgradeFrei(lehrerTyp, 1, stufePfadB);
    }

    public boolean istFreigeschaltetC() {
        return stufePfadC < pfadC.size() && SaveData.istUpgradeFrei(lehrerTyp, 2, stufePfadC);
    }

    public int xpKostenA() {
        return stufePfadA < pfadA.size() ? pfadA.get(stufePfadA).xpKosten : -1;
    }

    public int xpKostenB() {
        return stufePfadB < pfadB.size() ? pfadB.get(stufePfadB).xpKosten : -1;
    }

    public int xpKostenC() {
        return stufePfadC < pfadC.size() ? pfadC.get(stufePfadC).xpKosten : -1;
    }

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

    public void upgradeA() {
        if (!kannUpgradeA()) return;
        wendeAn(pfadA.get(stufePfadA));
        investiertesGeld += pfadA.get(stufePfadA).kosten; // NEU
        stufePfadA++;
    }

    public void upgradeB() {
        if (!kannUpgradeB()) return;
        wendeAn(pfadB.get(stufePfadB));
        investiertesGeld += pfadB.get(stufePfadB).kosten; // NEU
        stufePfadB++;
    }

    public void upgradeC() {
        if (!kannUpgradeC()) return;
        wendeAn(pfadC.get(stufePfadC));
        investiertesGeld += pfadC.get(stufePfadC).kosten; // NEU
        stufePfadC++;
    }

    private void wendeAn(LehrerUpgrade u) {
        range += u.rangeDelta;
        shootDelay += u.shootDelayDelta;
        damage += u.damageDelta;
        multiTarget += u.multiTargetDelta;
        if (u.spezialProjektil) spezialProjektil = true;
        if (shootDelay < 0.1) shootDelay = 0.1;
    }

    // ============================================================
    // KOSTEN & NAMEN
    // ============================================================

    public int kostenA() {
        return !kannUpgradeA() || stufePfadA >= pfadA.size() ? -1 : pfadA.get(stufePfadA).kosten;
    }

    public int kostenB() {
        return !kannUpgradeB() || stufePfadB >= pfadB.size() ? -1 : pfadB.get(stufePfadB).kosten;
    }

    public int kostenC() {
        return !kannUpgradeC() || stufePfadC >= pfadC.size() ? -1 : pfadC.get(stufePfadC).kosten;
    }

    public String nameA() {
        return stufePfadA >= pfadA.size() ? "MAX" : pfadA.get(stufePfadA).name;
    }

    public String nameB() {
        return stufePfadB >= pfadB.size() ? "MAX" : pfadB.get(stufePfadB).name;
    }

    public String nameC() {
        return stufePfadC >= pfadC.size() ? "MAX" : pfadC.get(stufePfadC).name;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public int getLehrerTyp() {
        return lehrerTyp;
    }

    public double getRange() {
        return range;
    }

    public double getShootDelay() {
        return shootDelay;
    }

    /** Spezial-Upgrades (Stufe 5) verdoppeln den Schaden. */
    public int getDamage() {
        return spezialProjektil ? damage * 2 : damage;
    }

    public int getMultiTarget() {
        return multiTarget;
    }

    public boolean isSpezialProjektil() {
        return spezialProjektil;
    }

    public int getStufePfadA() {
        return stufePfadA;
    }

    public int getStufePfadB() {
        return stufePfadB;
    }

    public int getStufePfadC() {
        return stufePfadC;
    }

    public String getUpgradeStatus() {
        return stufePfadA + "-" + stufePfadB + "-" + stufePfadC;
    }

    public String getProjektilTyp() {
        switch (lehrerTyp) {
            case 1: return "ProjektilPotion";
            case 2: return nutztSkin ? "ProjektilMusikdisk" : "ProjektilFloppy";
            case 3: case 4: return ""; // Aigner und Gassner schießen nicht
            default: return nutztSkin ? "ProjektilGolfball" : "ProjektilBoomerang";
        }
    }
    // ============================================================
    // INFOS FÜRS UPGRADE-PANEL
    // ============================================================

    private List<LehrerUpgrade> pfad(char p) {
        return p == 'A' ? pfadA : p == 'B' ? pfadB : pfadC;
    }

    public int getStufe(char p) {
        return p == 'A' ? stufePfadA : p == 'B' ? stufePfadB : stufePfadC;
    }

    /** Nächstes Upgrade in diesem Pfad, oder null wenn ausgebaut. */
    public LehrerUpgrade naechstesUpgrade(char p) {
        int s = getStufe(p);
        return s < pfad(p).size() ? pfad(p).get(s) : null;
    }

    /** Erlaubt die 5-2-0-Regel ein weiteres Upgrade in diesem Pfad (unabhängig von XP/Geld)? */
    public boolean regelErlaubt(char p) {
        int s = getStufe(p);
        if (s >= 5) return false;
        int idx = p - 'A';
        int hp = hauptPfad();
        if (hp != -1 && hp != idx && s >= 2) return false;
        return !(s == 0 && pfadeMitUpgrades() >= 2);
    }

    /** Warum die 5-2-0-Regel diesen Pfad sperrt. */
    public String sperrGrund(char p) {
        return getStufe(p) == 0 ? "Nur 2 Pfade pro Lehrer möglich" : "Nur 1 Pfad darf über Stufe 2";
    }

    /** Was das nächste Upgrade bewirkt, z.B. "+25 Reichweite". */
    public String effektText(char p) {
        LehrerUpgrade u = naechstesUpgrade(p);
        if (u == null) return "";
        int s = getStufe(p);

        if (lehrerTyp == 3) { // Aigner – Buffs
            return switch (p) {
                case 'A' -> String.format("+%.0f Aura-Radius", u.rangeDelta);
                case 'B' -> "+" + u.damageDelta + " Schadens-Buff";
                default  -> {
                    double[] f = AignerBuffComponent.TEMPO_FAKTOR;
                    yield "Tempo-Buff " + Math.round((1 - f[s]) * 100) + " % → " + Math.round((1 - f[s + 1]) * 100) + " %";
                }
            };
        }
        if (lehrerTyp == 4) { // Gassner – Geld
            int[] ausz = GassnerComponent.AUSZAHLUNGEN_PRO_RUNDE;
            int[] betr = GassnerComponent.BETRAEGE;
            return switch (p) {
                case 'A' -> {
                    if (ausz[s + 1] > ausz[s]) yield ausz[s] + " → " + ausz[s + 1] + " Auszahlungen/Runde";
                    int ab = s + 2;
                    while (ab < ausz.length && ausz[ab] == ausz[s]) ab++;
                    yield ab < ausz.length ? (ausz[s] + 1) + ". Auszahlung ab Stufe " + ab : "";
                }
                case 'B' -> "+" + (betr[s + 1] - betr[s]) + " € pro Auszahlung";
                default  -> switch (s + 1) {
                    case 1  -> "Synergie +10 % je Gassner";
                    case 2  -> "Synergie +15 % je Gassner";
                    case 3  -> "Synergie +20 % je Gassner";
                    case 4  -> "+1 ❤ pro Auszahlung";
                    default -> "+2 ❤ pro Auszahlung";
                };
            };
        }

        List<String> teile = new java.util.ArrayList<>();
        boolean einzeln = (u.rangeDelta != 0 ? 1 : 0) + (u.shootDelayDelta != 0 ? 1 : 0)
                + (u.damageDelta != 0 ? 1 : 0) + (u.multiTargetDelta != 0 ? 1 : 0) <= 1;
        if (u.rangeDelta != 0)
            teile.add(String.format("+%.0f %s", u.rangeDelta, einzeln ? "Reichweite" : "Reichw."));
        if (u.shootDelayDelta != 0)
            teile.add(String.format(java.util.Locale.GERMAN, "−%.2f s%s", -u.shootDelayDelta, einzeln ? " Nachladezeit" : ""));
        if (u.damageDelta != 0)
            teile.add("+" + u.damageDelta + " Schaden");
        if (u.multiTargetDelta != 0)
            teile.add("+" + u.multiTargetDelta + (u.multiTargetDelta == 1 ? " Ziel" : " Ziele"));
        if (u.spezialProjektil)
            teile.add("SPEZIAL: doppelter Schaden");
        return String.join(" · ", teile);
    }

    public void addPops(int damage) {
        pops += damage;
    }

    public int getPops() {
        return pops;
    }
}