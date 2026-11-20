package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherTowerDefenseApp extends GameApplication {

    private static final Color FARBE_A     = Color.web("#e74c3c");
    private static final Color FARBE_A_DIM = Color.web("#5a1a1a");
    private static final Color FARBE_B     = Color.web("#f1c40f");
    private static final Color FARBE_B_DIM = Color.web("#5a4a00");
    private static final Color FARBE_C     = Color.web("#2ecc71");
    private static final Color FARBE_C_DIM = Color.web("#0a3d1f");
    private static final Color PANEL_BG    = Color.web("#12121e");
    private static final Color PANEL_SEC   = Color.web("#1e1e30");

    private static final String[] LEHRER_NAMEN = {"Groebl", "Feichtner", "Winkler", "Aigner", "Gassner"};

    /** true sobald Gewonnen- oder Game-Over-Screen angezeigt wird. */
    private boolean spielVorbei = false;

    private Rectangle speedBtn;
    private Text      speedText;

    /** Shop-Einträge, damit Icons ausgegraut werden können wenn das Geld nicht reicht. */
    private record ShopEintrag(javafx.scene.Node icon, Text preis, int kosten, boolean zaehltZumLimit) {}
    private final List<ShopEintrag> shopEintraege = new ArrayList<>();

    private Entity lehrerSchatten;
    private Entity ausgewaehlterLehrer = null;
    private Entity rangeIndicator      = null;
    private RoundManager roundManager;

    private Rectangle startButton;
    private Text      startButtonText;

    private boolean speedErhoehen = false;

    // Upgrade Panel
    private Rectangle  upgradePanel;
    private Text       upgradeTitel, upgradeXp;
    private Rectangle  statsBg;
    private Text       statRange, statDamage, statSpeed, statTargets;
    private Rectangle  btnVerkaufen;
    private Text       textVerkaufen;
    private PfadButton pfadBtnA, pfadBtnB, pfadBtnC;
    private Entity     rangeVorschau = null;
    private final List<javafx.scene.Node> panelNodes = new ArrayList<>();

    // Lehrer-Limit-Anzeige im Shop + Hinweis-Text unten auf der Karte
    private Text lehrerZaehler;
    private javafx.scene.Node hinweis;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(640);
        settings.setTitle("Teacher Tower Defense");
        settings.setVersion("0.1");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setPreserveResizeRatio(true);
        settings.setDeveloperMenuEnabled(false);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setSceneFactory(new CustomSceneFactory());
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("leben", 20);
        vars.put("geld",  (int)(500 * GameConfig.getStartgeldMulti()));
        vars.put("runde", 1);
    }

    @Override
    protected void initGame() {
        // Zustand vom vorherigen Spiel zurücksetzen (die App-Instanz bleibt zwischen Spielen dieselbe)
        spielVorbei         = false;
        speedErhoehen       = false;
        lehrerSchatten      = null;
        ausgewaehlterLehrer = null;
        rangeIndicator      = null;
        SaveData.laden();
        SaveData.mapFreigeschaltet[1] = true;
        FXGL.getGameWorld().addEntityFactory(new TeacherTowerDefenseFactory());
        FXGL.setLevelFromMap(GameConfig.getMapDatei());



        GameConfig.speedMulti = 1.0;
        Sounds.musik("musik_spiel");

        roundManager = new RoundManager();
        roundManager.setOnRundeEnde(() -> {
            FXGL.set("runde", roundManager.getAktuelleRundeAnzeige());
            if (spielVorbei) return;
            if (!roundManager.isSpielEnde()) Sounds.play("runde_geschafft");
            if (roundManager.isSpielEnde()) {
                beendeSpiel();
                SaveData.mapBeendet(GameConfig.selectedMap, GameConfig.selectedDiff,
                        GameConfig.getMuenzenBelohnung(), 50);
                int muenzen = GameConfig.getMuenzenBelohnung();
                zeigeGewonnen(muenzen);
            } else if (CustomGameMenu.autoStart) {
                aktualisiereStartButton(true);
                FXGL.getGameTimer().runOnceAfter(() -> { if (CustomGameMenu.autoStart) starteRunde(); },
                        javafx.util.Duration.seconds(3));
            } else {
                aktualisiereStartButton(true);
            }
            SaveData.muenzen += 10;
            SaveData.speichern();
        });

        // Game Over wenn Leben = 0
        FXGL.getip("leben").addListener((obs, old, newVal) -> {
            if (newVal.intValue() < old.intValue() && !spielVorbei) zeigeTrefferBlitz();
            if (newVal.intValue() <= 0 && !spielVorbei) {
                beendeSpiel();
                SaveData.speichern();
                zeigeGameOver();
            }
        });

        // Live-Update des Upgrade-Panels wenn Geld sich ändert
        FXGL.getip("geld").addListener((obs, old, newVal) -> {
            if (ausgewaehlterLehrer != null && ausgewaehlterLehrer.isActive()) {
                aktualisiereUpgradePanel();
            }
            aktualisiereShopLeistbarkeit();
        });
    }

    @Override
    protected void initPhysics() {
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.SCHUELER, (projektil, schueler) -> {
            int dmg = projektil.getProperties().exists("damage") ? projektil.getInt("damage") : 1;

            // Pops-Counter: Lehrer-Referenz aus Projektil lesen und addPop() aufrufen
            if (projektil.getProperties().exists("lehrer")) {
                Entity lehrerEnt = projektil.getObject("lehrer");
                if (lehrerEnt != null && lehrerEnt.isActive()
                        && lehrerEnt.hasComponent(LehrerComponent.class)) {
                    lehrerEnt.getComponent(LehrerComponent.class).addPops(dmg);
                    if (lehrerEnt == ausgewaehlterLehrer) {
                        aktualisiereUpgradePanel();
                    }
                }
            }

            projektil.removeFromWorld();
            schueler.getComponent(SchuelerComponent.class).damage(dmg);
        });
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.HINDERNIS, (projektil, hindernis) -> {
            if (hindernis.getProperties().exists("blockiertSchuss") && hindernis.getBoolean("blockiertSchuss"))
                projektil.removeFromWorld();
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        if (roundManager != null) roundManager.update(tpf);
        if (ausgewaehlterLehrer != null && ausgewaehlterLehrer.isActive()) {
            aktualisiereUpgradePanel();
        }
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) { deselect(); return; }
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (lehrerSchatten != null || spielVorbei) return;

            // 1. Koordinaten aus der FXGL-Welt holen (diese sind korrekt skaliert)
            double wx = FXGL.getInput().getMouseXWorld();
            double wy = FXGL.getInput().getMouseYWorld();

            // 2. Prüfen, ob der Klick im Shop-Bereich (rechts von X=960) war
            if (wx >= 960) return;

            Entity gefunden = null;
            for (Entity lehrer : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
                if (Math.abs(lehrer.getX()+24 - wx) < 28 && Math.abs(lehrer.getY()+24 - wy) < 28) {
                    gefunden = lehrer; break;
                }
            }
            if (gefunden != null) waehleLehrer(gefunden);
            else deselect();
        });

        // Tastenkürzel
        FXGL.onKeyDown(javafx.scene.input.KeyCode.DIGIT1, "Upgrade Pfad A", () -> upgradePerTaste('A'));
        FXGL.onKeyDown(javafx.scene.input.KeyCode.DIGIT2, "Upgrade Pfad B", () -> upgradePerTaste('B'));
        FXGL.onKeyDown(javafx.scene.input.KeyCode.DIGIT3, "Upgrade Pfad C", () -> upgradePerTaste('C'));
        FXGL.onKeyDown(javafx.scene.input.KeyCode.DELETE,     "Lehrer verkaufen",       this::verkaufeLehrer);
        FXGL.onKeyDown(javafx.scene.input.KeyCode.BACK_SPACE, "Lehrer verkaufen (Back)", this::verkaufeLehrer);
        FXGL.onKeyDown(javafx.scene.input.KeyCode.SPACE, "Runde starten", this::starteRunde);
        FXGL.onKeyDown(javafx.scene.input.KeyCode.M, "Musik umschalten",
                () -> Sounds.setMusikAn(!SaveData.musikAn));
        FXGL.onKeyDown(javafx.scene.input.KeyCode.F, "Tempo umschalten", () -> {
            if (speedBtn != null && !spielVorbei) toggleSpeed(speedBtn, speedText);
        });
    }

    @Override
    protected void initUI() {

        // === STATS PANEL oben links ===
        Rectangle bgPanel = new Rectangle(210, 120, Color.color(0,0,0,0.6));
        bgPanel.setTranslateX(10); bgPanel.setTranslateY(10);
        bgPanel.setArcWidth(15); bgPanel.setArcHeight(15);

        Text textLeben = new Text();
        textLeben.setTranslateX(25); textLeben.setTranslateY(40);
        textLeben.setFill(Color.web("#e74c3c"));
        textLeben.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textLeben.textProperty().bind(FXGL.getip("leben").asString("❤  %d"));

        Text textGeld = new Text();
        textGeld.setTranslateX(25); textGeld.setTranslateY(75);
        textGeld.setFill(Color.web("#f1c40f"));
        textGeld.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textGeld.textProperty().bind(FXGL.getip("geld").asString("💰  %d €"));

        Text textRunde = new Text();
        textRunde.setTranslateX(25); textRunde.setTranslateY(110);
        textRunde.setFill(Color.WHITE);
        textRunde.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textRunde.textProperty().bind(FXGL.getip("runde").asString("Runde %d / " + roundManager.getMaxRunden()));

        // === SHOP PANEL ===
        Rectangle shopPanel = new Rectangle(240, 640, Color.web("#1a1a2e"));
        shopPanel.setTranslateX(960);

        Text shopTitel = new Text("LEHRER SHOP");
        shopTitel.setTranslateX(978); shopTitel.setTranslateY(35);
        shopTitel.setFill(Color.WHITE);
        shopTitel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        lehrerZaehler = mkText("", 1122, 34, Color.LIGHTGRAY, 12, true);

        Line shopTrenn = new Line(965, 42, 1195, 42);
        shopTrenn.setStroke(Color.web("#333355")); shopTrenn.setStrokeWidth(1);

        // ── Shop Icons: 3 Lehrer nebeneinander ──────────────────
        javafx.scene.Node shopIcon1 = ladeLehrerShopIcon("Groebl.png",    968, 50, 52, 60, Color.web("#2980b9"));
        Text shopName1  = mkText("Groebl",    968, 118, Color.LIGHTGRAY, 10, false);
        Text shopPreis1 = mkText("100 €",    975, 130, Color.web("#f1c40f"), 11, true);

        javafx.scene.Node shopIcon2 = ladeLehrerShopIcon("Feichtner.png", 1043, 50, 52, 60, Color.web("#8e24aa"));
        Text shopName2  = mkText("Feichtner", 1033, 118, Color.LIGHTGRAY, 10, false);
        Text shopPreis2 = mkText("150 €",     1048, 130, Color.web("#f1c40f"), 11, true);

        javafx.scene.Node shopIcon3 = ladeLehrerShopIcon("Winkler.png",   1118, 50, 52, 60, Color.web("#27ae60"));
        Text shopName3  = mkText("Winkler",   1113, 118, Color.LIGHTGRAY, 10, false);
        Text shopPreis3 = mkText("125 €",     1122, 130, Color.web("#f1c40f"), 11, true);

        Line shopTrenn2 = new Line(965, 138, 1195, 138);
        shopTrenn2.setStroke(Color.web("#333355")); shopTrenn2.setStrokeWidth(1);

        // ── Shop Icons Zeile 2: Aigner (4) und Gassner (5) ──────
        javafx.scene.Node shopIcon4 = ladeLehrerShopIcon("Aigner.png",  993, 148, 52, 52, Color.web("#8e24aa"));
        Text shopName4  = mkText("Aigner",   990, 208, Color.LIGHTGRAY, 10, false);
        Text shopPreis4 = mkText("200 €",    997, 220, Color.web("#f1c40f"), 11, true);

        javafx.scene.Node shopIcon5 = ladeLehrerShopIcon("Gassner.png", 1113, 148, 52, 52, Color.web("#27ae60"));
        Text shopName5  = mkText("Gassner", 1108, 208, Color.LIGHTGRAY, 10, false);
        Text shopPreis5 = mkText("175 €",   1118, 220, Color.web("#f1c40f"), 11, true);

        Line shopTrenn3 = new Line(965, 228, 1195, 228);
        shopTrenn3.setStroke(Color.web("#333355")); shopTrenn3.setStrokeWidth(1);

        shopEintraege.clear();
        registriereShopDrag(shopIcon1, shopPreis1, "Lehrer1", 100, LehrerUpgradePfade.BASE_RANGE_L1);
        registriereShopDrag(shopIcon2, shopPreis2, "Lehrer2", 150, LehrerUpgradePfade.BASE_RANGE_L2);
        registriereShopDrag(shopIcon3, shopPreis3, "Lehrer3", 125, LehrerUpgradePfade.BASE_RANGE_L3);
        registriereShopDrag(shopIcon4, shopPreis4, "Lehrer4", 200, LehrerUpgradePfade.BASE_RANGE_L4);
        registriereShopDrag(shopIcon5, shopPreis5, "Lehrer5", 175, LehrerUpgradePfade.BASE_RANGE_L5);
        aktualisiereShopLeistbarkeit();

        // === UPGRADE PANEL ===
        double PX = 965, PY = 232;
        panelNodes.clear(); // sonst hängen Nodes vom vorherigen Spiel noch drin

        upgradePanel = new Rectangle(230, 296, PANEL_BG);
        upgradePanel.setTranslateX(PX); upgradePanel.setTranslateY(PY);
        upgradePanel.setArcWidth(10); upgradePanel.setArcHeight(10);
        upgradePanel.setStroke(Color.web("#333355")); upgradePanel.setStrokeWidth(1);

        upgradeTitel = mkText("Lehrer", PX+10, PY+20, Color.WHITE, 13, true);
        upgradeXp    = mkText("", PX+150, PY+20, Color.web("#f1c40f"), 11, true);

        statsBg = new Rectangle(210, 48, PANEL_SEC);
        statsBg.setTranslateX(PX+10); statsBg.setTranslateY(PY+26);
        statsBg.setArcWidth(6); statsBg.setArcHeight(6);

        statRange   = mkText("", PX+18,  PY+43, Color.LIGHTBLUE,      11, false);
        statDamage  = mkText("", PX+18,  PY+58, Color.web("#e74c3c"), 11, false);
        statSpeed   = mkText("", PX+125, PY+43, Color.web("#f1c40f"), 11, false);
        statTargets = mkText("", PX+125, PY+58, Color.web("#2ecc71"), 11, false);

        pfadBtnA = new PfadButton('A', 1, PX+10, PY+80,  FARBE_A, FARBE_A_DIM);
        pfadBtnB = new PfadButton('B', 2, PX+10, PY+136, FARBE_B, FARBE_B_DIM);
        pfadBtnC = new PfadButton('C', 3, PX+10, PY+192, FARBE_C, FARBE_C_DIM);

        btnVerkaufen = new Rectangle(210, 38, Color.web("#5a0000"));
        btnVerkaufen.setTranslateX(PX+10); btnVerkaufen.setTranslateY(PY+250);
        btnVerkaufen.setArcWidth(8); btnVerkaufen.setArcHeight(8);
        btnVerkaufen.setStroke(Color.web("#e74c3c")); btnVerkaufen.setStrokeWidth(1);
        btnVerkaufen.setCursor(javafx.scene.Cursor.HAND);
        textVerkaufen = mkText("", PX+20, PY+274, Color.web("#ff8a80"), 12, true);
        textVerkaufen.setCursor(javafx.scene.Cursor.HAND);
        btnVerkaufen.setOnMouseClicked(e -> verkaufeLehrer());
        textVerkaufen.setOnMouseClicked(e -> verkaufeLehrer());
        btnVerkaufen.setOnMouseEntered(e -> btnVerkaufen.setFill(Color.web("#8b0000")));
        btnVerkaufen.setOnMouseExited(e  -> btnVerkaufen.setFill(Color.web("#5a0000")));

        panelNodes.addAll(List.of(upgradePanel, upgradeTitel, upgradeXp,
                statsBg, statRange, statDamage, statSpeed, statTargets,
                pfadBtnA.gruppe, pfadBtnB.gruppe, pfadBtnC.gruppe,
                btnVerkaufen, textVerkaufen));

        setzeUpgradePanelSichtbar(false);

        // === START BUTTON ===
        startButton = new Rectangle(110, 40, Color.web("#27ae60"));
        startButton.setArcWidth(8); startButton.setArcHeight(8);
        startButton.setTranslateX(PX); startButton.setTranslateY(540);
        startButtonText = mkText("▶  START", PX + 12, 567, Color.WHITE, 13, true);
        startButton.setCursor(javafx.scene.Cursor.HAND);
        startButtonText.setCursor(javafx.scene.Cursor.HAND);
        startButton.setOnMouseClicked(e -> starteRunde());
        startButtonText.setOnMouseClicked(e -> starteRunde());
        startButton.setOnMouseEntered(e -> { if (startButton.getFill().equals(Color.web("#27ae60"))) startButton.setFill(Color.web("#2ecc71")); });
        startButton.setOnMouseExited(e  -> { if (startButton.getFill().equals(Color.web("#2ecc71"))) startButton.setFill(Color.web("#27ae60")); });

        // === SPEED BUTTON ===
        speedBtn = new Rectangle(100, 40, Color.web("#1a1a2e"));
        speedBtn.setTranslateX(PX + 120); speedBtn.setTranslateY(540);
        speedBtn.setArcWidth(8); speedBtn.setArcHeight(8);
        speedBtn.setStroke(Color.web("#333355")); speedBtn.setStrokeWidth(1);
        speedText = mkText("▶▶  1x", PX + 134, 567, Color.WHITE, 13, true);
        speedBtn.setOnMouseClicked(e -> toggleSpeed(speedBtn, speedText));
        speedText.setOnMouseClicked(e -> toggleSpeed(speedBtn, speedText));

        // === INLINE SKIN BUTTONS ===
        Line skinTrenn = new Line(965, 588, 1195, 588);
        skinTrenn.setStroke(Color.web("#333355")); skinTrenn.setStrokeWidth(1);

        Text skinLabel = mkText("🎨 SKINS:", PX + 4, 610, Color.web("#9b59b6"), 10, true);

        Rectangle[] skinBtns  = new Rectangle[3];
        Text[]      skinTexts = new Text[3];

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            boolean gekauft = SaveData.skinFreigeschaltet[i][1];
            boolean aktiv   = SaveData.aktiverSkin[i] == 1;

            skinBtns[i] = new Rectangle(56, 34,
                    aktiv    ? Color.web("#6c3483")
                            : gekauft ? Color.web("#2c3e7a")
                            : (SaveData.muenzen >= SaveData.SKIN_PREISE[i]
                            ? Color.web("#1a3a1a")
                            : Color.color(0.1, 0.1, 0.1, 0.9)));
            skinBtns[i].setArcWidth(6); skinBtns[i].setArcHeight(6);
            skinBtns[i].setTranslateX(PX + 60 + i * 66); skinBtns[i].setTranslateY(595);
            skinBtns[i].setStroke(Color.web("#9b59b6")); skinBtns[i].setStrokeWidth(1);

            String btnLabel = gekauft ? (aktiv ? "✓ AN" : "○ AUS") : SaveData.SKIN_PREISE[i] + "💰";
            skinTexts[i] = mkText(btnLabel, PX + 64 + i * 66, 617, Color.WHITE, 10, true);

            skinBtns[i].setOnMouseClicked(e -> {
                if (SaveData.kaufeSkin(idx)) {
                    for (int j = 0; j < 3; j++) {
                        boolean jGekauft = SaveData.skinFreigeschaltet[j][1];
                        boolean jAktiv   = SaveData.aktiverSkin[j] == 1;
                        skinBtns[j].setFill(jAktiv ? Color.web("#6c3483")
                                : jGekauft ? Color.web("#2c3e7a")
                                : Color.color(0.1, 0.1, 0.1, 0.9));
                        skinTexts[j].setText(jGekauft ? (jAktiv ? "✓ AN" : "○ AUS") : SaveData.SKIN_PREISE[j] + "💰");
                    }
                }
            });
            skinTexts[i].setOnMouseClicked(skinBtns[i].getOnMouseClicked());
        }

        Text settingsHint = mkText("ESC Menü · LEER Start · F Tempo · M Musik", PX + 4, 637, Color.web("#7777aa"), 9, false);




        FXGL.getGameScene().addUINodes(
                shopPanel, shopTitel, lehrerZaehler, shopTrenn,
                shopIcon1, shopName1, shopPreis1,
                shopIcon2, shopName2, shopPreis2,
                shopIcon3, shopName3, shopPreis3,
                shopTrenn2,
                shopIcon4, shopName4, shopPreis4,
                shopIcon5, shopName5, shopPreis5,
                shopTrenn3,
                startButton, startButtonText,
                speedBtn, speedText,
                skinTrenn, skinLabel,
                skinBtns[0], skinTexts[0],
                skinBtns[1], skinTexts[1],
                skinBtns[2], skinTexts[2],
                settingsHint,
                bgPanel, textLeben, textGeld, textRunde
        );


        for (javafx.scene.Node node : panelNodes) FXGL.getGameScene().addUINode(node);



    }



    // ============================================================
    // UPGRADE PANEL
    // ============================================================

    private void waehleLehrer(Entity lehrer) {
        ausgewaehlterLehrer = lehrer;
        aktualisiereRangeIndicator();
        aktualisiereUpgradePanel();
        setzeUpgradePanelSichtbar(true);
    }

    private void deselect() {
        ausgewaehlterLehrer = null;
        entferneRangeIndicator();
        entferneRangeVorschau();
        setzeUpgradePanelSichtbar(false);
    }

    private void zeigeGewonnen(int muenzen) {
        Sounds.musikBeenden();
        Sounds.play("gewonnen");
        int maxRunden = roundManager.getMaxRunden();

        Rectangle overlay = new Rectangle(1200, 640, Color.color(0.0, 0.05, 0.0, 0.88));

        Text titelText = new Text("GEWONNEN!");
        titelText.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        titelText.setFill(Color.web("#2ecc71"));
        titelText.setStroke(Color.web("#1a6b3a")); titelText.setStrokeWidth(1.5);
        titelText.setTextAlignment(TextAlignment.CENTER);

        Line trenn1 = new Line(-200, 0, 200, 0);
        trenn1.setStroke(Color.web("#27ae60")); trenn1.setStrokeWidth(1.5);

        Text rundenText = new Text("Alle " + maxRunden + " Runden überstanden!");
        rundenText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rundenText.setFill(Color.web("#ecf0f1")); rundenText.setTextAlignment(TextAlignment.CENTER);

        String diffName = switch (GameConfig.selectedDiff) {
            case 1 -> "Medium"; case 2 -> "Hard"; default -> "Easy";
        };
        Text diffText = new Text("Schwierigkeit: " + diffName + "  |  +" + muenzen + " Münzen");
        diffText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        diffText.setFill(Color.web("#f1c40f")); diffText.setTextAlignment(TextAlignment.CENTER);

        Text statsLabel = new Text("Lehrer-Statistiken");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        statsLabel.setFill(Color.web("#f1c40f")); statsLabel.setTextAlignment(TextAlignment.CENTER);

        VBox statsBox = baueStatsBox();

        Line trenn2 = new Line(-200, 0, 200, 0);
        trenn2.setStroke(Color.web("#333355")); trenn2.setStrokeWidth(1);

        Rectangle btnBg = new Rectangle(220, 48, Color.web("#27ae60"));
        btnBg.setArcWidth(10); btnBg.setArcHeight(10);
        btnBg.setStroke(Color.web("#2ecc71")); btnBg.setStrokeWidth(1.5);
        Text btnText = new Text("Zum Hauptmenü");
        btnText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btnText.setFill(Color.WHITE);

        StackPane btnStack = new StackPane(btnBg, btnText);
        btnStack.setAlignment(Pos.CENTER);
        btnStack.setCursor(javafx.scene.Cursor.HAND);
        btnStack.setOnMouseEntered(e -> btnBg.setFill(Color.web("#2ecc71")));
        btnStack.setOnMouseExited(e  -> btnBg.setFill(Color.web("#27ae60")));
        btnStack.setOnMouseClicked(e -> FXGL.getGameController().gotoMainMenu());

        VBox content = new VBox(14, titelText, trenn1, rundenText, diffText,
                statsLabel, statsBox, trenn2, btnStack);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30, 30, 30, 30));
        content.setMaxWidth(460);

        Rectangle panel = new Rectangle(480, 420);
        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#001a00")), new Stop(1.0, Color.web("#001020")));
        panel.setFill(grad); panel.setArcWidth(18); panel.setArcHeight(18);
        panel.setStroke(Color.web("#27ae60")); panel.setStrokeWidth(2.5);

        StackPane panelStack = new StackPane(panel, content);
        panelStack.setAlignment(Pos.CENTER);
        panelStack.setTranslateX(360); panelStack.setTranslateY(110);

        FXGL.getGameScene().addUINodes(overlay, panelStack);
    }

    private void aktualisiereRangeIndicator() {
        entferneRangeIndicator();
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        double range = lc.getRange();
        if (range <= 0) return;
        rangeIndicator = FXGL.spawn("RangeIndicator",
                new SpawnData(ausgewaehlterLehrer.getX()+24, ausgewaehlterLehrer.getY()+24)
                        .put("range", range));
    }

    private void entferneRangeIndicator() {
        if (rangeIndicator != null && rangeIndicator.isActive()) rangeIndicator.removeFromWorld();
        rangeIndicator = null;
    }

    private void aktualisiereUpgradePanel() {
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) { deselect(); return; }
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        int lehrerIdx = lc.getLehrerTyp();

        String titelName = LEHRER_NAMEN[Math.min(lehrerIdx, LEHRER_NAMEN.length - 1)];
        if (lehrerIdx == 0 && lc.getProjektilTyp().equals("ProjektilGolfball")) titelName += " [Golf]";
        if (lehrerIdx == 2 && lc.getProjektilTyp().equals("ProjektilMusikdisk")) titelName += " [Musik]";
        upgradeTitel.setText(titelName + "   " + lc.getUpgradeStatus());
        upgradeXp.setText("⭐ " + SaveData.getXP(lehrerIdx) + " XP");
        upgradeXp.setTranslateX(965 + 220 - upgradeXp.getLayoutBounds().getWidth());

        if (lehrerIdx == 3 && ausgewaehlterLehrer.hasComponent(AignerBuffComponent.class)) {
            AignerBuffComponent ab = ausgewaehlterLehrer.getComponent(AignerBuffComponent.class);
            statRange.setText(String.format("🔮  %.0fpx Aura",   ab.getBuffRadius()));
            statDamage.setText(String.format("⚔+  %d Buff",      ab.getDamageBonus()));
            statSpeed.setText(String.format("⚡+  %.2fs Tempo",   ab.getShootDelayBonus()));
            statTargets.setText("(Bufferin)");
        } else if (lehrerIdx == 4 && ausgewaehlterLehrer.hasComponent(GassnerComponent.class)) {
            GassnerComponent gc = ausgewaehlterLehrer.getComponent(GassnerComponent.class);
            int stufeC = lc.getStufePfadC();
            String lebenInfo = stufeC >= 5 ? " +2❤" : stufeC >= 4 ? " +1❤" : "";
            statRange.setText(String.format("💰  %d€ je Auszahlung%s", gc.getGenBetrag(), lebenInfo));
            statDamage.setText(String.format("📅  %dx pro Runde", gc.getAuszahlungenProRunde()));
            statSpeed.setText(String.format("🔗  x%.2f Synergie", gc.getSynergie()));
            statTargets.setText(String.format("💵  %d€ generiert", gc.getGeneratedGeld()));
        } else {
            statRange.setText(String.format("🎯  %.0fpx",         lc.getRange()));
            statDamage.setText(String.format("⚔  %d Schaden",     lc.getDamage()));
            statSpeed.setText(String.format("⚡  %.2fs",          lc.getShootDelay()));
            statTargets.setText(String.format("💥  %d Pop%s",      lc.getPops(), lc.getPops() != 1 ? "s" : ""));
        }

        String[] pfadNamen = switch (lehrerIdx) {
            case 1 -> new String[]{"Radius", "Schaden", "Speed"};
            case 2 -> lc.getProjektilTyp().equals("ProjektilMusikdisk")
                    ? new String[]{"Rhythmus", "Tempo", "Klang"}
                    : new String[]{"Ziele", "Speed", "Reichweite"};
            case 3 -> new String[]{"Aura-Radius", "Schadensbuff", "Tempobuff"};
            case 4 -> new String[]{"Frequenz", "Betrag", "Synergie"};
            default -> lc.getProjektilTyp().equals("ProjektilGolfball")
                    ? new String[]{"Schwung", "Schlagkraft", "Präzision"}
                    : new String[]{"Speed", "Schaden", "Reichweite"};
        };
        pfadBtnA.aktualisiere(lc, pfadNamen[0]);
        pfadBtnB.aktualisiere(lc, pfadNamen[1]);
        pfadBtnC.aktualisiere(lc, pfadNamen[2]);

        int refund = (int) (lc.getInvestiertesGeld() * 0.75);
        textVerkaufen.setText("🗑  Verkaufen  +" + refund + " €      [Entf]");
    }

    /** Ein Upgrade-Pfad im Panel: Taste + Pfadname + Stufenpunkte, nächstes Upgrade + Preis, Effekt. */
    private final class PfadButton {
        final char  pfad;
        final int   taste;
        final Color farbe, dim;
        final javafx.scene.Group gruppe = new javafx.scene.Group();
        final Rectangle bg = new Rectangle(210, 52);
        final Text kopf, name, preis, effekt;
        final Circle[] dots = new Circle[5];
        boolean hover = false;

        PfadButton(char pfad, int taste, double x, double y, Color farbe, Color dim) {
            this.pfad = pfad; this.taste = taste; this.farbe = farbe; this.dim = dim;
            bg.setArcWidth(8); bg.setArcHeight(8); bg.setStrokeWidth(1.5);
            kopf   = mkText("", 8, 14, farbe, 10, true);
            name   = mkText("", 8, 30, Color.WHITE, 11, true);
            preis  = mkText("", 150, 30, Color.web("#f1c40f"), 11, true);
            effekt = mkText("", 8, 45, Color.web("#c0c0d8"), 10, false);
            gruppe.getChildren().addAll(bg, kopf, name, preis, effekt);
            for (int i = 0; i < 5; i++) {
                dots[i] = new Circle(4.5);
                dots[i].setStrokeWidth(1.5);
                dots[i].setTranslateX(140 + i * 15);
                dots[i].setTranslateY(10);
                gruppe.getChildren().add(dots[i]);
            }
            gruppe.setTranslateX(x); gruppe.setTranslateY(y);
            gruppe.setCursor(javafx.scene.Cursor.HAND);
            gruppe.setOnMouseClicked(e -> kaufeUpgrade(pfad));
            gruppe.setOnMouseEntered(e -> { hover = true;  zeigeRangeVorschau(pfad); aktualisiereUpgradePanel(); });
            gruppe.setOnMouseExited(e  -> { hover = false; entferneRangeVorschau();  aktualisiereUpgradePanel(); });
        }

        void aktualisiere(LehrerComponent lc, String pfadName) {
            int stufe = lc.getStufe(pfad);
            LehrerUpgrade u = lc.naechstesUpgrade(pfad);
            boolean regelOk = lc.regelErlaubt(pfad);

            kopf.setText("[" + taste + "]  " + pfadName);
            for (int i = 0; i < 5; i++) {
                if (i < stufe)                  { dots[i].setFill(farbe);             dots[i].setStroke(farbe.brighter()); }
                else if (i == stufe && regelOk) { dots[i].setFill(Color.TRANSPARENT); dots[i].setStroke(farbe); }
                else                            { dots[i].setFill(dim);               dots[i].setStroke(dim.darker()); }
            }

            String preisText, effektText;
            Color  preisFarbe, bgFarbe, rand;
            boolean aktiv; // kann man jetzt klicken und es passiert etwas?
            if (u == null) {
                name.setText("Maximal ausgebaut");
                preisText = "✓"; preisFarbe = farbe; effektText = "";
                bgFarbe = dim.darker(); rand = farbe; aktiv = false;
            } else if (!regelOk) {
                name.setText(u.name);
                preisText = "🔒"; preisFarbe = Color.GRAY; effektText = lc.sperrGrund(pfad);
                bgFarbe = dim.darker().darker(); rand = Color.web("#2a2a44"); aktiv = false;
            } else if (!SaveData.istUpgradeFrei(lc.getLehrerTyp(), pfad - 'A', stufe) && u.xpKosten > 0) {
                // Muss erst mit XP dauerhaft freigeschaltet werden
                boolean genugXp = SaveData.getXP(lc.getLehrerTyp()) >= u.xpKosten;
                name.setText(u.name);
                preisText  = "🔓 " + u.xpKosten + " XP";
                preisFarbe = genugXp ? Color.web("#5dade2") : Color.web("#e74c3c");
                effektText = genugXp ? "Freischalten: " + lc.effektText(pfad)
                                     : "Zu wenig XP – " + lc.effektText(pfad);
                bgFarbe = genugXp ? Color.web("#163450") : dim.darker();
                rand    = genugXp ? Color.web("#5dade2") : Color.web("#2a2a44");
                aktiv   = genugXp;
            } else {
                boolean leistbar = FXGL.geti("geld") >= u.kosten;
                name.setText(u.name);
                preisText  = u.kosten + " €";
                preisFarbe = leistbar ? Color.web("#f1c40f") : Color.web("#e74c3c");
                effektText = lc.effektText(pfad);
                bgFarbe = leistbar ? dim : dim.darker();
                rand    = leistbar ? farbe : Color.web("#2a2a44");
                aktiv   = leistbar;
            }

            name.setFill(aktiv || u == null ? Color.WHITE : Color.web("#9a9ab0"));
            preis.setText(preisText);
            preis.setFill(preisFarbe);
            preis.setTranslateX(202 - preis.getLayoutBounds().getWidth());
            effekt.setText(effektText);
            bg.setFill(hover && aktiv ? bgFarbe.brighter() : bgFarbe);
            bg.setStroke(rand);
            bg.setStrokeWidth(hover && aktiv ? 2.5 : 1.5);
        }
    }

    /** Zeigt beim Drüberfahren über ein Reichweiten-Upgrade den neuen Radius gestrichelt an. */
    private void zeigeRangeVorschau(char pfad) {
        entferneRangeVorschau();
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        LehrerUpgrade u = lc.naechstesUpgrade(pfad);
        if (u == null || u.rangeDelta <= 0 || lc.getRange() <= 0) return;
        rangeVorschau = FXGL.spawn("RangeIndicator",
                new SpawnData(ausgewaehlterLehrer.getX() + 24, ausgewaehlterLehrer.getY() + 24)
                        .put("range", lc.getRange() + u.rangeDelta)
                        .put("vorschau", true));
    }

    private void entferneRangeVorschau() {
        if (rangeVorschau != null && rangeVorschau.isActive()) rangeVorschau.removeFromWorld();
        rangeVorschau = null;
    }

    private void upgradePerTaste(char pfad) {
        if (!spielVorbei && ausgewaehlterLehrer != null) kaufeUpgrade(pfad);
    }

    private void kaufeUpgrade(char pfad) {
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        switch (pfad) {
            case 'A' -> {
                if (!lc.istFreigeschaltetA()) {
                    if (lc.xpKostenA() == 0) {
                        lc.freischaltenA(); // Gratis-Stufe: ohne Extra-Klick freischalten und gleich kaufen
                    } else {
                        Sounds.play(lc.freischaltenA() ? "freischalten" : "fehler");
                        aktualisiereUpgradePanel(); return;
                    }
                }
                int k = lc.kostenA(); if (k < 0 || FXGL.geti("geld") < k) { Sounds.play("fehler"); return; }
                FXGL.inc("geld", -k); lc.upgradeA();
                Sounds.play("upgrade");
            }
            case 'B' -> {
                if (!lc.istFreigeschaltetB()) {
                    if (lc.xpKostenB() == 0) {
                        lc.freischaltenB(); // Gratis-Stufe: ohne Extra-Klick freischalten und gleich kaufen
                    } else {
                        Sounds.play(lc.freischaltenB() ? "freischalten" : "fehler");
                        aktualisiereUpgradePanel(); return;
                    }
                }
                int k = lc.kostenB(); if (k < 0 || FXGL.geti("geld") < k) { Sounds.play("fehler"); return; }
                FXGL.inc("geld", -k); lc.upgradeB();
                Sounds.play("upgrade");
            }
            case 'C' -> {
                if (!lc.istFreigeschaltetC()) {
                    if (lc.xpKostenC() == 0) {
                        lc.freischaltenC(); // Gratis-Stufe: ohne Extra-Klick freischalten und gleich kaufen
                    } else {
                        Sounds.play(lc.freischaltenC() ? "freischalten" : "fehler");
                        aktualisiereUpgradePanel(); return;
                    }
                }
                int k = lc.kostenC(); if (k < 0 || FXGL.geti("geld") < k) { Sounds.play("fehler"); return; }
                FXGL.inc("geld", -k); lc.upgradeC();
                Sounds.play("upgrade");
            }
        }
        aktualisiereRangeIndicator();
        zeigeRangeVorschau(pfad); // nächste Stufe gleich als Vorschau zeigen
        aktualisiereUpgradePanel();
    }

    private void verkaufeLehrer() {
        if (spielVorbei || ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        int refund = (int) (lc.getInvestiertesGeld() * 0.75);
        FXGL.inc("geld", refund);
        Sounds.play("muenze");
        ausgewaehlterLehrer.removeFromWorld();
        deselect();
        aktualisiereShopLeistbarkeit();
    }

    private void setzeUpgradePanelSichtbar(boolean v) {
        if (!v) for (PfadButton pb : new PfadButton[]{pfadBtnA, pfadBtnB, pfadBtnC}) if (pb != null) pb.hover = false;
        for (javafx.scene.Node node : panelNodes){
            node.setVisible(v);
        }
    }

    // ============================================================
    // RUNDEN
    // ============================================================

    private void starteRunde() {
        if (roundManager == null || spielVorbei) return;
        if (roundManager.isRundeAktiv() || roundManager.isSpielEnde()) return;
        String beschreibung = roundManager.getNaechsteRundeBeschreibung();
        int nummer = roundManager.getAktuelleRundeAnzeige();
        aktualisiereStartButton(false);
        roundManager.starteNaechsteRunde();
        Sounds.play("rundenstart");
        zeigeRundenBanner("Runde " + nummer + " / " + roundManager.getMaxRunden(), beschreibung);
    }

    /** Großer Text oben in der Kartenmitte, der nach kurzer Zeit ausblendet. */
    private void zeigeRundenBanner(String titel, String untertitel) {
        Text t = new Text(titel);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        t.setFill(Color.WHITE);
        t.setStroke(Color.BLACK); t.setStrokeWidth(1.5);

        Text u = new Text(untertitel);
        u.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        u.setFill(Color.web("#f1c40f"));
        u.setStroke(Color.BLACK); u.setStrokeWidth(0.6);

        VBox box = new VBox(4, t, u);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(960);
        box.setTranslateY(150);
        box.setMouseTransparent(true);
        FXGL.getGameScene().addUINode(box);

        FXGL.animationBuilder()
                .delay(javafx.util.Duration.seconds(1.4))
                .duration(javafx.util.Duration.seconds(0.6))
                .onFinished(() -> FXGL.getGameScene().removeUINode(box))
                .fadeOut(box)
                .buildAndPlay();
    }

    /** Kurzer roter Blitz über der Karte, wenn ein Schüler entkommt. */
    private void zeigeTrefferBlitz() {
        Sounds.play("leben_verloren");
        Rectangle blitz = new Rectangle(960, 640, Color.color(0.9, 0, 0, 0.28));
        blitz.setMouseTransparent(true);
        FXGL.getGameScene().addUINode(blitz);
        FXGL.animationBuilder()
                .duration(javafx.util.Duration.seconds(0.35))
                .onFinished(() -> FXGL.getGameScene().removeUINode(blitz))
                .fadeOut(blitz)
                .buildAndPlay();
    }

    /** Stoppt Wellen, entfernt alle Schüler/Projektile und sperrt die Eingabe. */
    private void beendeSpiel() {
        spielVorbei = true;
        roundManager.stoppen();
        deselect();
        if (lehrerSchatten != null) { lehrerSchatten.removeFromWorld(); lehrerSchatten = null; }
        startButton.setFill(Color.web("#555555"));
        startButtonText.setText("■  ENDE");
        // Verzögert, weil wir evtl. gerade mitten im World-Update sind
        FXGL.getGameTimer().runOnceAfter(() ->
                FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER, EntityType.PROJEKTIL)
                        .forEach(Entity::removeFromWorld), javafx.util.Duration.ZERO);
    }

    private void aktualisiereStartButton(boolean aktiv) {
        startButton.setFill(aktiv ? Color.web("#27ae60") : Color.web("#555555"));
        // Kurze Texte – "RUNDE STARTEN" war für den 110px-Button zu breit und wurde abgeschnitten
        startButtonText.setText(aktiv ? "▶  START" : "⏳  LÄUFT...");
    }

    // ============================================================
    // KOLLISIONS-CHECKS
    // ============================================================

    private static final double PFAD_PUFFER = 20;

    private boolean kollidiert(double mouseX, double mouseY) {
        double[][] ecken = {{mouseX-15,mouseY-15},{mouseX+15,mouseY-15},{mouseX-15,mouseY+15},{mouseX+15,mouseY+15}};

        for (Entity h : FXGL.getGameWorld().getEntitiesByType(EntityType.HINDERNIS)) {
            if (h.getProperties().exists("usePip")) {
                List<Double> pts = h.getObject("polygonPunkte");
                double ex = h.getX(), ey = h.getY();
                for (double[] ecke : ecken) if (punktInPolygon(ecke[0]-ex, ecke[1]-ey, pts)) return true;
            } else {
                double hx = h.getBoundingBoxComponent().getMinXWorld();
                double hy = h.getBoundingBoxComponent().getMinYWorld();
                double hw = h.getWidth()  > 0 ? h.getWidth()  : 50;
                double hh = h.getHeight() > 0 ? h.getHeight() : 50;
                if (new Rectangle2D(mouseX-15,mouseY-15,30,30).intersects(new Rectangle2D(hx-4,hy-4,hw+8,hh+8))) return true;
            }
        }

        for (Entity p : FXGL.getGameWorld().getEntitiesByType(EntityType.PFAD)) {
            double px = p.getBoundingBoxComponent().getMinXWorld();
            double py = p.getBoundingBoxComponent().getMinYWorld();
            double pw = p.getWidth()  > 0 ? p.getWidth()  : 32;
            double ph = p.getHeight() > 0 ? p.getHeight() : 32;
            if (new Rectangle2D(mouseX-15, mouseY-15, 30, 30)
                    .intersects(new Rectangle2D(px - PFAD_PUFFER, py - PFAD_PUFFER,
                            pw + PFAD_PUFFER*2, ph + PFAD_PUFFER*2))) return true;
        }

        for (Entity a : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER))
            if (Math.abs(a.getX()+24-mouseX) < 42 && Math.abs(a.getY()+24-mouseY) < 42) return true;

        return false;
    }

    private boolean punktInPolygon(double px, double py, List<Double> pts) {
        int n=pts.size()/2; boolean inside=false; int j=n-1;
        for (int i=0;i<n;i++) {
            double xi=pts.get(i*2),yi=pts.get(i*2+1),xj=pts.get(j*2),yj=pts.get(j*2+1);
            if(((yi>py)!=(yj>py))&&(px<(xj-xi)*(py-yi)/(yj-yi)+xi)) inside=!inside;
            j=i;
        }
        return inside;
    }

    // ============================================================
    // UI HELPER
    // ============================================================

    private Text mkText(String txt, double x, double y, Color fill, int size, boolean bold) {
        Text t = new Text(txt); t.setTranslateX(x); t.setTranslateY(y); t.setFill(fill);
        t.setStyle("-fx-font-size:"+size+"px;"+(bold?" -fx-font-weight:bold;":""));
        return t;
    }

    private Line mkLine(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1,y1,x2,y2); l.setStroke(Color.web("#333355")); l.setStrokeWidth(1);
        return l;
    }

    private void aktualisiereLehrerSchatten() {
        if (lehrerSchatten == null) return;
        double mx = FXGL.getInput().getMouseXWorld(), my = FXGL.getInput().getMouseYWorld();
        lehrerSchatten.setPosition(mx - 24, my - 24);
        boolean koll = kollidiert(mx, my) || mx >= 960;
        Circle rc    = (Circle)    lehrerSchatten.getViewComponent().getChildren().get(0);
        Rectangle bd = (Rectangle) lehrerSchatten.getViewComponent().getChildren().get(1);
        if (koll) { bd.setFill(Color.color(1,0,0,0.5)); rc.setFill(Color.color(1,0,0,0.2)); rc.setStroke(Color.RED); }
        else       { bd.setFill(Color.color(0,0.4,1,0.5)); rc.setFill(Color.color(1,1,1,0.2)); rc.setStroke(Color.WHITE); }
    }

    private javafx.scene.Node ladeLehrerShopIcon(String dateiname, double x, double y, double w, double h, Color fallback) {
        try {
            if (!TeacherTowerDefenseFactory.texturExistiert(dateiname)) throw new IllegalStateException(dateiname);
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(FXGL.image(dateiname));
            iv.setFitWidth(w); iv.setFitHeight(h);
            iv.setPreserveRatio(true); iv.setSmooth(false);
            iv.setTranslateX(x); iv.setTranslateY(y);
            return iv;
        } catch (Exception e) {
            javafx.scene.layout.StackPane sp = new javafx.scene.layout.StackPane();
            Rectangle r = new Rectangle(w, h, fallback);
            r.setArcWidth(8); r.setArcHeight(8);
            String initial = dateiname.substring(0, 1).toUpperCase();
            Text t = new Text(initial);
            t.setFill(Color.WHITE);
            t.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
            sp.getChildren().addAll(r, t);
            sp.setTranslateX(x); sp.setTranslateY(y);
            return sp;
        }
    }

    /** Drag & Drop aus dem Shop auf die Karte (für alle Lehrer gleich). */
    private void registriereShopDrag(javafx.scene.Node icon, Text preis, String spawnName,
                                     int kosten, double vorschauRange) {
        double sx = icon.getTranslateX(), sy = icon.getTranslateY();
        icon.setCursor(javafx.scene.Cursor.HAND);
        // Gassner (Geldgenerator) zählt nicht zum Lehrer-Limit
        boolean zaehltZumLimit = !spawnName.equals("Lehrer5");

        icon.setOnMousePressed(e -> {
            if (spielVorbei) return;
            if (zaehltZumLimit && anzahlLehrer() >= GameConfig.MAX_LEHRER) {
                Sounds.play("fehler");
                zeigeHinweis("Maximal " + GameConfig.MAX_LEHRER + " Lehrer – verbessere lieber deine Lehrer!");
                return;
            }
            if (FXGL.geti("geld") < kosten) { Sounds.play("fehler"); return; }
            deselect();
            lehrerSchatten = FXGL.spawn("LehrerSchatten",
                    new SpawnData(-100, -100).put("range", vorschauRange));
        });
        icon.setOnMouseDragged(e -> {
            if (lehrerSchatten == null) return;
            icon.setTranslateX(FXGL.getInput().getMouseXUI() - 28);
            icon.setTranslateY(FXGL.getInput().getMouseYUI() - 28);
            aktualisiereLehrerSchatten();
        });
        icon.setOnMouseReleased(e -> {
            if (lehrerSchatten != null) {
                double mx = FXGL.getInput().getMouseXWorld(), my = FXGL.getInput().getMouseYWorld();
                if (!kollidiert(mx, my) && mx < 960 && FXGL.geti("geld") >= kosten
                        && (!zaehltZumLimit || anzahlLehrer() < GameConfig.MAX_LEHRER)) {
                    Entity l = FXGL.spawn(spawnName, mx - 24, my - 24);
                    l.getComponent(LehrerComponent.class).setBaseKosten(kosten);
                    FXGL.inc("geld", -kosten);
                    Sounds.play("platzieren");
                    aktualisiereShopLeistbarkeit();
                } else if (mx < 960) {
                    Sounds.play("fehler");
                }
                lehrerSchatten.removeFromWorld(); lehrerSchatten = null;
            }
            icon.setTranslateX(sx); icon.setTranslateY(sy);
        });

        shopEintraege.add(new ShopEintrag(icon, preis, kosten, zaehltZumLimit));
    }

    /** Lehrer, die zum Limit zählen (alle außer Gassner). */
    private int anzahlLehrer() {
        return (int) FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER).stream()
                .filter(e -> !e.hasComponent(GassnerComponent.class))
                .count();
    }

    /** Kurzer Hinweis unten auf der Karte, der von selbst wieder verschwindet. */
    private void zeigeHinweis(String text) {
        if (hinweis != null) FXGL.getGameScene().removeUINode(hinweis);
        Text t = new Text(text);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        t.setFill(Color.WHITE);
        Rectangle bg = new Rectangle(t.getLayoutBounds().getWidth() + 36, 36, Color.color(0.55, 0.05, 0.05, 0.9));
        bg.setArcWidth(12); bg.setArcHeight(12);
        StackPane box = new StackPane(bg, t);
        box.setMouseTransparent(true);
        box.setTranslateX(480 - bg.getWidth() / 2);
        box.setTranslateY(570);
        hinweis = box;
        FXGL.getGameScene().addUINode(box);
        FXGL.animationBuilder()
                .delay(javafx.util.Duration.seconds(1.8))
                .duration(javafx.util.Duration.seconds(0.5))
                .onFinished(() -> FXGL.getGameScene().removeUINode(box))
                .fadeOut(box)
                .buildAndPlay();
    }

    /** Graut Lehrer im Shop aus, die man sich nicht leisten kann oder wenn das Lehrer-Limit erreicht ist. */
    private void aktualisiereShopLeistbarkeit() {
        int geld   = FXGL.geti("geld");
        int anzahl = anzahlLehrer();
        boolean voll = anzahl >= GameConfig.MAX_LEHRER;
        if (lehrerZaehler != null) {
            lehrerZaehler.setText("Lehrer " + anzahl + "/" + GameConfig.MAX_LEHRER);
            lehrerZaehler.setFill(voll ? Color.web("#e74c3c") : Color.LIGHTGRAY);
        }
        for (ShopEintrag se : shopEintraege) {
            boolean leistbar = (!voll || !se.zaehltZumLimit()) && geld >= se.kosten();
            se.icon().setOpacity(leistbar ? 1.0 : 0.35);
            se.preis().setFill(leistbar ? Color.web("#f1c40f") : Color.web("#e74c3c"));
        }
    }

    private void toggleSpeed(Rectangle btn, Text text) {
        speedErhoehen = !speedErhoehen;
        GameConfig.speedMulti = speedErhoehen ? 2.0 : 1.0;

        FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER).forEach(e -> {
            var wmc = e.getComponent(
                    com.almasb.fxgl.dsl.components.WaypointMoveComponent.class);
            if (wmc != null) wmc.setSpeed(e.getComponent(SchuelerComponent.class)
                    .getTyp().speed * GameConfig.speedMulti);
        });

        if (speedErhoehen) {
            btn.setFill(Color.web("#e67e22"));
            btn.setStroke(Color.web("#f39c12"));
            text.setText("▶▶  2x");
            text.setFill(Color.WHITE); // vorher orange auf orange – kaum lesbar
        } else {
            btn.setFill(Color.web("#1a1a2e"));
            btn.setStroke(Color.web("#333355"));
            text.setText("▶▶  1x");
            text.setFill(Color.WHITE);
        }
    }


    private void zeigeGameOver() {
        Sounds.musikBeenden();
        Sounds.play("game_over");
        int erreichtRunde = roundManager.getAktuelleRundeAnzeige();
        int maxRunden     = roundManager.getMaxRunden();

        Rectangle overlay = new Rectangle(1200, 640, Color.color(0.0, 0.0, 0.05, 0.88));

        Text titelText = new Text("GAME OVER");
        titelText.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        titelText.setFill(Color.web("#e74c3c"));
        titelText.setStroke(Color.web("#8b0000"));
        titelText.setStrokeWidth(1.5);
        titelText.setTextAlignment(TextAlignment.CENTER);

        Line trenn1 = new Line(-200, 0, 200, 0);
        trenn1.setStroke(Color.web("#c0392b")); trenn1.setStrokeWidth(1.5);

        Text rundenText = new Text("Runde " + erreichtRunde + " / " + maxRunden + " erreicht");
        rundenText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        rundenText.setFill(Color.web("#ecf0f1"));
        rundenText.setTextAlignment(TextAlignment.CENTER);

        String diffName = switch (GameConfig.selectedDiff) {
            case 1 -> "Medium"; case 2 -> "Hard"; default -> "Easy";
        };
        Text diffText = new Text("Schwierigkeit: " + diffName);
        diffText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        diffText.setFill(Color.web("#95a5a6"));
        diffText.setTextAlignment(TextAlignment.CENTER);

        Text statsLabel = new Text("Lehrer-Statistiken");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        statsLabel.setFill(Color.web("#f1c40f"));
        statsLabel.setTextAlignment(TextAlignment.CENTER);

        VBox statsBox = baueStatsBox();

        Line trenn2 = new Line(-200, 0, 200, 0);
        trenn2.setStroke(Color.web("#333355")); trenn2.setStrokeWidth(1);

        Rectangle btnBg = new Rectangle(220, 48, Color.web("#27ae60"));
        btnBg.setArcWidth(10); btnBg.setArcHeight(10);
        btnBg.setStroke(Color.web("#2ecc71")); btnBg.setStrokeWidth(1.5);
        Text btnText = new Text("Zum Hauptmenü");
        btnText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btnText.setFill(Color.WHITE);

        StackPane btnStack = new StackPane(btnBg, btnText);
        btnStack.setAlignment(Pos.CENTER);
        btnStack.setCursor(javafx.scene.Cursor.HAND);
        btnStack.setOnMouseEntered(e -> btnBg.setFill(Color.web("#2ecc71")));
        btnStack.setOnMouseExited(e  -> btnBg.setFill(Color.web("#27ae60")));
        btnStack.setOnMouseClicked(e -> FXGL.getGameController().gotoMainMenu());

        VBox content = new VBox(14, titelText, trenn1, rundenText, diffText,
                statsLabel, statsBox, trenn2, btnStack);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30, 30, 30, 30));
        content.setMaxWidth(460);

        Rectangle panel = new Rectangle(480, 420);
        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#1a0010")), new Stop(1.0, Color.web("#0a0020")));
        panel.setFill(grad);
        panel.setArcWidth(18); panel.setArcHeight(18);
        panel.setStroke(Color.web("#c0392b")); panel.setStrokeWidth(2.5);

        StackPane panelStack = new StackPane(panel, content);
        panelStack.setAlignment(Pos.CENTER);
        panelStack.setTranslateX(360);
        panelStack.setTranslateY(110);

        FXGL.getGameScene().addUINodes(overlay, panelStack);
    }

    private VBox baueStatsBox() {

        java.util.List<Entity> lehrerListe = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.LEHRER).stream()
                .filter(e -> e.hasComponent(LehrerComponent.class))
                .toList();

        // Sortieren: bester Lehrer zuerst
        java.util.List<Entity> sortiert = new java.util.ArrayList<>(lehrerListe);
        sortiert.sort((a, b) -> {
            LehrerComponent lcA = a.getComponent(LehrerComponent.class);
            LehrerComponent lcB = b.getComponent(LehrerComponent.class);
            int wertA = (lcA.getLehrerTyp() == 4 && a.hasComponent(GassnerComponent.class))
                    ? a.getComponent(GassnerComponent.class).getGeneratedGeld() : lcA.getPops();
            int wertB = (lcB.getLehrerTyp() == 4 && b.hasComponent(GassnerComponent.class))
                    ? b.getComponent(GassnerComponent.class).getGeneratedGeld() : lcB.getPops();
            return Integer.compare(wertB, wertA);
        });

        int totalPops = lehrerListe.stream()
                .filter(e -> e.getComponent(LehrerComponent.class).getLehrerTyp() != 4)
                .mapToInt(e -> e.getComponent(LehrerComponent.class).getPops())
                .sum();
        int totalGold = lehrerListe.stream()
                .filter(e -> e.getComponent(LehrerComponent.class).getLehrerTyp() == 4
                        && e.hasComponent(GassnerComponent.class))
                .mapToInt(e -> e.getComponent(GassnerComponent.class).getGeneratedGeld())
                .sum();

        VBox statsBox = new VBox(4);
        statsBox.setAlignment(Pos.CENTER);

        if (!lehrerListe.isEmpty()) {
            String summaryStr = "💥 " + totalPops + " Pops gesamt";
            if (totalGold > 0) summaryStr += "   💰 " + totalGold + "€ generiert";
            Text summary = new Text(summaryStr);
            summary.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            summary.setFill(Color.web("#f1c40f"));
            summary.setTextAlignment(TextAlignment.CENTER);
            statsBox.getChildren().add(summary);

            Line sep = new Line(-185, 0, 185, 0);
            sep.setStroke(Color.web("#444466"));
            sep.setStrokeWidth(1);
            statsBox.getChildren().add(sep);
        }

        int MAX = 5;
        for (int i = 0; i < Math.min(sortiert.size(), MAX); i++) {
            Entity lehrerEnt = sortiert.get(i);
            LehrerComponent lc = lehrerEnt.getComponent(LehrerComponent.class);
            int idx = lc.getLehrerTyp();
            String name = LEHRER_NAMEN[Math.min(idx, LEHRER_NAMEN.length - 1)];
            String stat;
            if (idx == 4 && lehrerEnt.hasComponent(GassnerComponent.class)) {
                stat = lehrerEnt.getComponent(GassnerComponent.class).getGeneratedGeld() + "€ generiert";
            } else {
                stat = lc.getPops() + " Pops";
            }
            String medaille = i == 0 ? "🥇 " : i == 1 ? "🥈 " : i == 2 ? "🥉 " : "     ";
            Text zeile = new Text(medaille + name + "  [" + lc.getUpgradeStatus() + "]  –  " + stat);
            zeile.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            zeile.setFill(i < 3 ? Color.web("#ecf0f1") : Color.web("#bdc3c7"));
            zeile.setTextAlignment(TextAlignment.CENTER);
            statsBox.getChildren().add(zeile);
        }
        if (sortiert.size() > MAX) {
            Text mehr = new Text("... und " + (sortiert.size() - MAX) + " weitere");
            mehr.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            mehr.setFill(Color.web("#7f8c8d"));
            mehr.setTextAlignment(TextAlignment.CENTER);
            statsBox.getChildren().add(mehr);
        }
        if (lehrerListe.isEmpty()) {
            Text keineLehrer = new Text("Keine Lehrer platziert");
            keineLehrer.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            keineLehrer.setFill(Color.web("#7f8c8d"));
            keineLehrer.setTextAlignment(TextAlignment.CENTER);
            statsBox.getChildren().add(keineLehrer);
        }

        return statsBox;
    }

    public static void main(String[] args) { launch(args); }
}