package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

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

    private Entity lehrerSchatten;
    private Entity ausgewaehlterLehrer = null;
    private Entity rangeIndicator      = null;
    private RoundManager roundManager;

    private Rectangle startButton;
    private Text      startButtonText;

    // Upgrade Panel
    private Rectangle upgradePanel;
    private Text      upgradeTitel;
    private Rectangle statsBg;
    private Text      statRange, statDamage, statSpeed, statTargets;
    private Rectangle btnA, btnB, btnC, btnVerkaufen;
    private Text      labelA, labelB, labelC, textVerkaufen;
    private Text      kostenA, kostenB, kostenC;
    private Circle[]  dotsA = new Circle[5];
    private Circle[]  dotsB = new Circle[5];
    private Circle[]  dotsC = new Circle[5];
    private Line[]    trennlinien = new Line[4];
    private final List<javafx.scene.Node> panelNodes = new ArrayList<>();

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
        // Unser Custom ESC-Menü mit Auto-Start Toggle
        settings.setSceneFactory(new CustomSceneFactory());
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("leben", 20);
        vars.put("geld",  100);
        vars.put("runde", 1);
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new TeacherTowerDefenseFactory());
        FXGL.setLevelFromMap("Map1.tmx");

        roundManager = new RoundManager();
        roundManager.setOnRundeEnde(() -> {
            FXGL.set("runde", roundManager.getAktuelleRundeAnzeige());
            if (roundManager.isSpielEnde()) {
                FXGL.getDialogService().showMessageBox("Du hast gewonnen! 🎉", () -> {});
            } else if (CustomGameMenu.autoStart) {
                // Auto-Start: liest direkt aus CustomGameMenu.autoStart
                FXGL.getGameTimer().runOnceAfter(() -> starteRunde(), javafx.util.Duration.seconds(3));
            } else {
                aktualisiereStartButton(true);
            }
        });

        // Live-Update des Upgrade-Panels wenn Geld sich ändert
        FXGL.getip("geld").addListener((obs, old, newVal) -> {
            if (ausgewaehlterLehrer != null && ausgewaehlterLehrer.isActive()) {
                aktualisiereUpgradePanel();
            }
        });
    }

    @Override
    protected void initPhysics() {
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.SCHUELER, (projektil, schueler) -> {
            int dmg = projektil.getProperties().exists("damage") ? projektil.getInt("damage") : 1;
            projektil.removeFromWorld();
            schueler.getComponent(SchuelerComponent.class).damage(dmg);
        });
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.HINDERNIS, (projektil, hindernis) -> {
            if (hindernis.getProperties().exists("blockiertSchuss") && hindernis.getBoolean("blockiertSchuss"))
                projektil.removeFromWorld();
        });
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (lehrerSchatten != null) return;
            if (e.getSceneX() >= 960) return;

            double wx = FXGL.getInput().getMouseXWorld();
            double wy = FXGL.getInput().getMouseYWorld();

            Entity gefunden = null;
            for (Entity lehrer : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
                if (Math.abs(lehrer.getX()+15 - wx) < 20 && Math.abs(lehrer.getY()+15 - wy) < 20) {
                    gefunden = lehrer; break;
                }
            }
            if (gefunden != null) waehleLehrer(gefunden);
            else deselect();
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

        Line shopTrenn = new Line(965, 42, 1195, 42);
        shopTrenn.setStroke(Color.web("#333355")); shopTrenn.setStrokeWidth(1);

        Rectangle shopIcon1 = new Rectangle(40, 40, Color.web("#3498db"));
        shopIcon1.setTranslateX(1060); shopIcon1.setTranslateY(52);
        shopIcon1.setArcWidth(6); shopIcon1.setArcHeight(6);
        Text shopName  = new Text("Lehrer"); shopName.setTranslateX(1048); shopName.setTranslateY(104);
        shopName.setFill(Color.LIGHTGRAY); shopName.setStyle("-fx-font-size: 11px;");
        Text shopPreis = new Text("20 €");  shopPreis.setTranslateX(1058); shopPreis.setTranslateY(118);
        shopPreis.setFill(Color.web("#f1c40f")); shopPreis.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Line shopTrenn2 = new Line(965, 127, 1195, 127);
        shopTrenn2.setStroke(Color.web("#333355")); shopTrenn2.setStrokeWidth(1);

        double sx = shopIcon1.getTranslateX(), sy = shopIcon1.getTranslateY();
        shopIcon1.setOnMousePressed(e -> {
            if (FXGL.geti("geld") >= 20) { lehrerSchatten = FXGL.spawn("LehrerSchatten", -100, -100); deselect(); }
        });
        shopIcon1.setOnMouseDragged(e -> {
            shopIcon1.setTranslateX(FXGL.getInput().getMouseXUI() - 20);
            shopIcon1.setTranslateY(FXGL.getInput().getMouseYUI() - 20);
            if (lehrerSchatten != null) {
                double mx = FXGL.getInput().getMouseXWorld(), my = FXGL.getInput().getMouseYWorld();
                lehrerSchatten.setPosition(mx - 15, my - 15);
                boolean koll = kollidiert(mx, my) || mx >= 960;
                Circle rc    = (Circle)    lehrerSchatten.getViewComponent().getChildren().get(0);
                Rectangle bd = (Rectangle) lehrerSchatten.getViewComponent().getChildren().get(1);
                if (koll) { bd.setFill(Color.color(1,0,0,0.5)); rc.setFill(Color.color(1,0,0,0.2)); rc.setStroke(Color.RED); }
                else       { bd.setFill(Color.color(0,0.4,1,0.5)); rc.setFill(Color.color(1,1,1,0.2)); rc.setStroke(Color.WHITE); }
            }
        });
        shopIcon1.setOnMouseReleased(e -> {
            if (lehrerSchatten != null) {
                double mx = FXGL.getInput().getMouseXWorld(), my = FXGL.getInput().getMouseYWorld();
                if (!kollidiert(mx, my) && mx < 960) { FXGL.spawn("Lehrer1", mx-15, my-15); FXGL.inc("geld", -20); }
                lehrerSchatten.removeFromWorld(); lehrerSchatten = null;
            }
            shopIcon1.setTranslateX(sx); shopIcon1.setTranslateY(sy);
        });

        // === UPGRADE PANEL ===
        double PX = 965, PY = 134;

        upgradePanel = new Rectangle(230, 300, PANEL_BG);
        upgradePanel.setTranslateX(PX); upgradePanel.setTranslateY(PY);
        upgradePanel.setArcWidth(10); upgradePanel.setArcHeight(10);
        upgradePanel.setStroke(Color.web("#333355")); upgradePanel.setStrokeWidth(1);

        upgradeTitel = mkText("Lehrer", PX+10, PY+20, Color.WHITE, 13, true);

        statsBg = new Rectangle(210, 48, PANEL_SEC);
        statsBg.setTranslateX(PX+10); statsBg.setTranslateY(PY+26);
        statsBg.setArcWidth(6); statsBg.setArcHeight(6);

        statRange   = mkText("🎯  150px",     PX+18,  PY+43,  Color.LIGHTBLUE,       11, false);
        statDamage  = mkText("⚔  1 Schaden",  PX+18,  PY+58,  Color.web("#e74c3c"),  11, false);
        statSpeed   = mkText("⚡  1.0s",       PX+125, PY+43,  Color.web("#f1c40f"),  11, false);
        statTargets = mkText("🎯x  1 Ziel",   PX+125, PY+58,  Color.web("#2ecc71"),  11, false);

        trennlinien[0] = mkLine(PX+10, PY+80,  PX+220, PY+80);

        btnA    = mkPfadBtn(PX+10, PY+87,  FARBE_A_DIM);
        labelA  = mkText("Pfad A – Speed",      PX+18, PY+103, FARBE_A, 11, true);
        kostenA = mkText("",                    PX+18, PY+117, Color.web("#f1c40f"), 10, false);
        for (int i=0;i<5;i++) dotsA[i] = mkDot(PX+142+i*16, PY+103, FARBE_A, FARBE_A_DIM);
        btnA.setOnMouseClicked(e -> kaufeUpgrade('A')); labelA.setOnMouseClicked(e -> kaufeUpgrade('A')); kostenA.setOnMouseClicked(e -> kaufeUpgrade('A'));

        trennlinien[1] = mkLine(PX+10, PY+133, PX+220, PY+133);

        btnB    = mkPfadBtn(PX+10, PY+140, FARBE_B_DIM);
        labelB  = mkText("Pfad B – Schaden",    PX+18, PY+156, FARBE_B, 11, true);
        kostenB = mkText("",                    PX+18, PY+170, Color.web("#f1c40f"), 10, false);
        for (int i=0;i<5;i++) dotsB[i] = mkDot(PX+142+i*16, PY+156, FARBE_B, FARBE_B_DIM);
        btnB.setOnMouseClicked(e -> kaufeUpgrade('B')); labelB.setOnMouseClicked(e -> kaufeUpgrade('B')); kostenB.setOnMouseClicked(e -> kaufeUpgrade('B'));

        trennlinien[2] = mkLine(PX+10, PY+186, PX+220, PY+186);

        btnC    = mkPfadBtn(PX+10, PY+193, FARBE_C_DIM);
        labelC  = mkText("Pfad C – Reichweite", PX+18, PY+209, FARBE_C, 11, true);
        kostenC = mkText("",                    PX+18, PY+223, Color.web("#f1c40f"), 10, false);
        for (int i=0;i<5;i++) dotsC[i] = mkDot(PX+142+i*16, PY+209, FARBE_C, FARBE_C_DIM);
        btnC.setOnMouseClicked(e -> kaufeUpgrade('C')); labelC.setOnMouseClicked(e -> kaufeUpgrade('C')); kostenC.setOnMouseClicked(e -> kaufeUpgrade('C'));

        trennlinien[3] = mkLine(PX+10, PY+239, PX+220, PY+239);

        btnVerkaufen = new Rectangle(210, 36, Color.web("#5a0000"));
        btnVerkaufen.setTranslateX(PX+10); btnVerkaufen.setTranslateY(PY+246);
        btnVerkaufen.setArcWidth(6); btnVerkaufen.setArcHeight(6);
        btnVerkaufen.setStroke(Color.web("#e74c3c")); btnVerkaufen.setStrokeWidth(1);
        textVerkaufen = mkText("🗑   Verkaufen (+10 €)", PX+42, PY+269, Color.web("#e74c3c"), 12, true);
        btnVerkaufen.setOnMouseClicked(e -> verkaufeLehrer());
        textVerkaufen.setOnMouseClicked(e -> verkaufeLehrer());
        btnVerkaufen.setOnMouseEntered(e -> btnVerkaufen.setFill(Color.web("#8b0000")));
        btnVerkaufen.setOnMouseExited(e  -> btnVerkaufen.setFill(Color.web("#5a0000")));

        panelNodes.add(upgradePanel); panelNodes.add(upgradeTitel);
        panelNodes.add(statsBg); panelNodes.add(statRange); panelNodes.add(statDamage);
        panelNodes.add(statSpeed); panelNodes.add(statTargets);
        panelNodes.add(btnA); panelNodes.add(labelA); panelNodes.add(kostenA);
        panelNodes.add(btnB); panelNodes.add(labelB); panelNodes.add(kostenB);
        panelNodes.add(btnC); panelNodes.add(labelC); panelNodes.add(kostenC);
        panelNodes.add(btnVerkaufen); panelNodes.add(textVerkaufen);
        for (Line l : trennlinien) panelNodes.add(l);
        for (Circle c : dotsA) panelNodes.add(c);
        for (Circle c : dotsB) panelNodes.add(c);
        for (Circle c : dotsC) panelNodes.add(c);

        setzeUpgradePanelSichtbar(false);

        // === START BUTTON ===
        startButton = new Rectangle(210, 42, Color.web("#27ae60"));
        startButton.setTranslateX(PX); startButton.setTranslateY(560);
        startButton.setArcWidth(8); startButton.setArcHeight(8);
        startButtonText = mkText("▶   RUNDE STARTEN", PX+38, 588, Color.WHITE, 14, true);
        startButton.setOnMouseClicked(e -> starteRunde());
        startButtonText.setOnMouseClicked(e -> starteRunde());
        startButton.setOnMouseEntered(e -> { if (startButton.getFill().equals(Color.web("#27ae60"))) startButton.setFill(Color.web("#2ecc71")); });
        startButton.setOnMouseExited(e  -> { if (startButton.getFill().equals(Color.web("#2ecc71"))) startButton.setFill(Color.web("#27ae60")); });

        Text settingsHint = mkText("⚙  ESC = Einstellungen", PX+52, 626, Color.web("#555577"), 10, false);

        FXGL.getGameScene().addUINodes(
                shopPanel, shopTitel, shopTrenn, shopIcon1, shopName, shopPreis, shopTrenn2,
                startButton, startButtonText, settingsHint,
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
        setzeUpgradePanelSichtbar(false);
    }

    private void aktualisiereRangeIndicator() {
        entferneRangeIndicator();
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        rangeIndicator = FXGL.spawn("RangeIndicator",
                new SpawnData(ausgewaehlterLehrer.getX()+15, ausgewaehlterLehrer.getY()+15)
                        .put("range", lc.getRange()));
    }

    private void entferneRangeIndicator() {
        if (rangeIndicator != null && rangeIndicator.isActive()) rangeIndicator.removeFromWorld();
        rangeIndicator = null;
    }

    private void aktualisiereUpgradePanel() {
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) { deselect(); return; }
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);

        statRange.setText(String.format("🎯  %.0fpx", lc.getRange()));
        statDamage.setText(String.format("⚔  %d Schaden", lc.getDamage()));
        statSpeed.setText(String.format("⚡  %.1fs", lc.getShootDelay()));
        statTargets.setText(String.format("🎯x  %d Ziel%s", lc.getMultiTarget(), lc.getMultiTarget()>1?"e":""));

        aktualisiereDots(dotsA, lc.getStufePfadA(), lc.kannUpgradeA(), FARBE_A, FARBE_A_DIM);
        aktualisiereDots(dotsB, lc.getStufePfadB(), lc.kannUpgradeB(), FARBE_B, FARBE_B_DIM);
        aktualisiereDots(dotsC, lc.getStufePfadC(), lc.kannUpgradeC(), FARBE_C, FARBE_C_DIM);

        aktualisierePfadBtn(btnA, labelA, kostenA, "Pfad A – Speed",      lc.nameA(), lc.kostenA(), lc.kannUpgradeA(), FARBE_A_DIM, FARBE_A);
        aktualisierePfadBtn(btnB, labelB, kostenB, "Pfad B – Schaden",    lc.nameB(), lc.kostenB(), lc.kannUpgradeB(), FARBE_B_DIM, FARBE_B);
        aktualisierePfadBtn(btnC, labelC, kostenC, "Pfad C – Reichweite", lc.nameC(), lc.kostenC(), lc.kannUpgradeC(), FARBE_C_DIM, FARBE_C);
    }

    private void aktualisiereDots(Circle[] dots, int stufe, boolean kannUpgrade, Color aktiv, Color inaktiv) {
        for (int i = 0; i < 5; i++) {
            if (i < stufe)                           { dots[i].setFill(aktiv);             dots[i].setStroke(aktiv.brighter()); dots[i].setRadius(6); }
            else if (i == stufe && kannUpgrade)      { dots[i].setFill(Color.TRANSPARENT); dots[i].setStroke(aktiv);            dots[i].setRadius(5); }
            else                                     { dots[i].setFill(inaktiv);           dots[i].setStroke(inaktiv.darker()); dots[i].setRadius(5); }
        }
    }

    private void aktualisierePfadBtn(Rectangle btn, Text label, Text kosten,
                                     String pfadName, String upgradeName,
                                     int upgradeKosten, boolean kannUpgrade,
                                     Color dunkel, Color hell) {
        if (upgradeKosten < 0) {
            label.setText(pfadName + "  ✓ MAX"); kosten.setText(""); btn.setFill(dunkel.darker());
        } else if (!kannUpgrade) {
            label.setText(pfadName + "  🔒"); kosten.setText(""); btn.setFill(dunkel.darker());
        } else if (FXGL.geti("geld") >= upgradeKosten) {
            label.setText(upgradeName); kosten.setText("Kosten: " + upgradeKosten + " €  ← klicken"); btn.setFill(dunkel);
        } else {
            label.setText(upgradeName); kosten.setText("Kosten: " + upgradeKosten + " €  💸 zu wenig"); btn.setFill(dunkel.darker());
        }
    }

    private void kaufeUpgrade(char pfad) {
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        LehrerComponent lc = ausgewaehlterLehrer.getComponent(LehrerComponent.class);
        int kosten;
        switch (pfad) {
            case 'A' -> { kosten=lc.kostenA(); if(kosten<0||FXGL.geti("geld")<kosten) return; FXGL.inc("geld",-kosten); lc.upgradeA(); }
            case 'B' -> { kosten=lc.kostenB(); if(kosten<0||FXGL.geti("geld")<kosten) return; FXGL.inc("geld",-kosten); lc.upgradeB(); }
            case 'C' -> { kosten=lc.kostenC(); if(kosten<0||FXGL.geti("geld")<kosten) return; FXGL.inc("geld",-kosten); lc.upgradeC(); }
        }
        aktualisiereRangeIndicator();
        aktualisiereUpgradePanel();
    }

    private void verkaufeLehrer() {
        if (ausgewaehlterLehrer == null || !ausgewaehlterLehrer.isActive()) return;
        FXGL.inc("geld", 10);
        ausgewaehlterLehrer.removeFromWorld();
        deselect();
    }

    private void setzeUpgradePanelSichtbar(boolean v) {
        for (javafx.scene.Node node : panelNodes) node.setVisible(v);
    }

    // ============================================================
    // RUNDEN
    // ============================================================

    private void starteRunde() {
        if (roundManager.isRundeAktiv() || roundManager.isSpielEnde()) return;
        aktualisiereStartButton(false);
        roundManager.starteNaechsteRunde();
    }

    private void aktualisiereStartButton(boolean aktiv) {
        startButton.setFill(aktiv ? Color.web("#27ae60") : Color.web("#555555"));
        startButtonText.setText(aktiv ? "▶   RUNDE STARTEN" : "⏳  RUNDE LÄUFT...");
    }

    // ============================================================
    // KOLLISIONS-CHECKS
    // ============================================================

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
        for (Entity a : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER))
            if (Math.abs(a.getX()+15-mouseX) < 35 && Math.abs(a.getY()+15-mouseY) < 35) return true;
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

    private Rectangle mkPfadBtn(double x, double y, Color fill) {
        Rectangle r = new Rectangle(210, 46, fill);
        r.setTranslateX(x); r.setTranslateY(y); r.setArcWidth(6); r.setArcHeight(6);
        return r;
    }

    private Circle mkDot(double x, double y, Color aktiv, Color inaktiv) {
        Circle c = new Circle(5, inaktiv);
        c.setTranslateX(x); c.setTranslateY(y);
        c.setStroke(inaktiv.darker()); c.setStrokeWidth(1.5);
        return c;
    }

    private Line mkLine(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1,y1,x2,y2); l.setStroke(Color.web("#333355")); l.setStrokeWidth(1);
        return l;
    }

    public static void main(String[] args) { launch(args); }
}