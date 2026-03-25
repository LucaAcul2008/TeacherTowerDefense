package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Vollbild-Hauptmenü mit Hintergrundbild, Map-Auswahl und Schwierigkeitsgrad.
 */
public class CustomMainMenu extends FXGLMenu {

    // Welche Map/Schwierigkeit ausgewählt ist
    private int selectedMap  = 0;
    private int selectedDiff = 0; // 0=Easy, 1=Medium, 2=Hard

    private static final String[] MAP_NAMEN = { "HTL Saalfelden", "City", "Map 3" };
    private static final String[] DIFF_NAMEN = { "Easy", "Medium", "Hard" };
    private static final Color[]  DIFF_FARBEN = {
            Color.web("#27ae60"), Color.web("#e67e22"), Color.web("#c0392b")
    };
    private static final int[] DIFF_MUENZEN  = { 100, 175, 250 };

    // Münzen-Anzeige
    private Text muenzenText;

    // Map-Karten
    private VBox[] mapKarten = new VBox[3];
    private Rectangle[] mapBorders = new Rectangle[3];

    // Diff-Buttons
    private Rectangle[] diffBtns = new Rectangle[3];
    private Text[]       diffTexts = new Text[3];

    // Start-Button
    private Rectangle startBtn;
    private Text      startText;

    public CustomMainMenu() {
        super(MenuType.MAIN_MENU);

        int W = FXGL.getAppWidth();
        int H = FXGL.getAppHeight();

        // ── Hintergrundbild ──────────────────────────────────────
        try {
            ImageView bg = new ImageView(FXGL.image("menu_bg.png"));
            bg.setFitWidth(W); bg.setFitHeight(H);
            bg.setPreserveRatio(false);
            getContentRoot().getChildren().add(bg);
        } catch (Exception e) {
            Rectangle bg = new Rectangle(W, H, Color.web("#1a2a1a"));
            getContentRoot().getChildren().add(bg);
        }

        // ── Dunkler Overlay unten ────────────────────────────────
        Rectangle overlay = new Rectangle(W, 320, Color.color(0, 0, 0, 0.72));
        overlay.setTranslateY(H - 320);
        getContentRoot().getChildren().add(overlay);

        // ── Münzen oben links ────────────────────────────────────
        Rectangle muenzenBg = new Rectangle(180, 44, Color.color(0,0,0,0.65));
        muenzenBg.setArcWidth(10); muenzenBg.setArcHeight(10);
        muenzenBg.setTranslateX(20); muenzenBg.setTranslateY(20);

        muenzenText = mkText("💰  " + SaveData.muenzen + " Münzen", 32, 47,
                Color.web("#f1c40f"), 15, true);

        getContentRoot().getChildren().addAll(muenzenBg, muenzenText);

        // ── MAP AUSWAHL ──────────────────────────────────────────
        Text mapTitel = mkText("KARTE WÄHLEN", W/2.0 - 100, H - 265,
                Color.WHITE, 13, true);
        mapTitel.setOpacity(0.7);
        getContentRoot().getChildren().add(mapTitel);

        double karteW = 200, karteH = 130;
        double startX = W / 2.0 - (karteW * 3 + 20 * 2) / 2.0;

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            boolean frei = SaveData.mapFreigeschaltet[i];
            int sterne = SaveData.getSterne(i);

            // Rahmen
            Rectangle border = new Rectangle(karteW, karteH);
            border.setArcWidth(10); border.setArcHeight(10);
            border.setFill(frei ? Color.color(0.15, 0.15, 0.25, 0.9) : Color.color(0.1,0.1,0.1,0.85));
            border.setStroke(i == selectedMap && frei ? Color.web("#f1c40f") : Color.web("#444466"));
            border.setStrokeWidth(i == selectedMap && frei ? 2.5 : 1.5);
            mapBorders[i] = border;

            // Map-Name
            Text name = mkText(MAP_NAMEN[i], 0, 0, frei ? Color.WHITE : Color.GRAY, 13, true);

            // Sterne
            StringBuilder sterneStr = new StringBuilder();
            for (int s = 0; s < 3; s++) sterneStr.append(s < sterne ? "★" : "☆");
            Text sterneText = mkText(sterneStr.toString(), 0, 0,
                    Color.web("#f1c40f"), 16, false);

            // Gesperrt-Overlay
            Text lockText = frei ? new Text("") : mkText("🔒 Gesperrt", 0, 0, Color.web("#888888"), 11, false);

            VBox karte = new VBox(8, name, sterneText, lockText);
            karte.setAlignment(Pos.CENTER);
            karte.setPrefSize(karteW, karteH);
            karte.setTranslateX(startX + i * (karteW + 20));
            karte.setTranslateY(H - 260);

            // Klick
            if (frei) {
                border.setOnMouseClicked(e -> waehleMap(idx));
                karte.setOnMouseClicked(e -> waehleMap(idx));
                border.setOnMouseEntered(e -> { if (idx != selectedMap) border.setFill(Color.color(0.2,0.2,0.35,0.9)); });
                border.setOnMouseExited(e  -> { if (idx != selectedMap) border.setFill(Color.color(0.15,0.15,0.25,0.9)); });
            }

            // Border hinter VBox legen
            border.setTranslateX(startX + i * (karteW + 20));
            border.setTranslateY(H - 260);

            mapKarten[i] = karte;
            getContentRoot().getChildren().addAll(border, karte);
        }

        // ── SCHWIERIGKEIT ────────────────────────────────────────
        Text diffTitel = mkText("SCHWIERIGKEIT", W/2.0 - 100, H - 110,
                Color.WHITE, 13, true);
        diffTitel.setOpacity(0.7);
        getContentRoot().getChildren().add(diffTitel);

        double diffW = 130, diffH = 40;
        double diffStartX = W / 2.0 - (diffW * 3 + 12 * 2) / 2.0;

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Rectangle btn = new Rectangle(diffW, diffH);
            btn.setArcWidth(8); btn.setArcHeight(8);
            btn.setFill(i == 0 ? DIFF_FARBEN[i] : Color.color(0.15,0.15,0.25,0.9));
            btn.setStroke(i == 0 ? DIFF_FARBEN[i].brighter() : Color.web("#444466"));
            btn.setStrokeWidth(1.5);
            btn.setTranslateX(diffStartX + i * (diffW + 12));
            btn.setTranslateY(H - 95);
            diffBtns[i] = btn;

            String label = DIFF_NAMEN[i] + "  +" + DIFF_MUENZEN[i] + "💰";
            Text txt = mkText(label,
                    diffStartX + i * (diffW + 12) + 10, H - 70,
                    i == 0 ? Color.WHITE : Color.GRAY, 11, true);
            diffTexts[i] = txt;

            btn.setOnMouseClicked(e -> waehleDiff(idx));
            txt.setOnMouseClicked(e -> waehleDiff(idx));

            getContentRoot().getChildren().addAll(btn, txt);
        }

        // ── START BUTTON ─────────────────────────────────────────
        startBtn = new Rectangle(220, 52, Color.web("#27ae60"));
        startBtn.setArcWidth(10); startBtn.setArcHeight(10);
        startBtn.setTranslateX(W / 2.0 - 110);
        startBtn.setTranslateY(H - 42);
        DropShadow glow = new DropShadow(15, Color.web("#2ecc71"));
        startBtn.setEffect(glow);

        startText = mkText("▶   SPIELEN", W / 2.0 - 60, H - 12, Color.WHITE, 16, true);

        startBtn.setOnMouseClicked(e -> starteSpiel());
        startText.setOnMouseClicked(e -> starteSpiel());
        startBtn.setOnMouseEntered(e -> startBtn.setFill(Color.web("#2ecc71")));
        startBtn.setOnMouseExited(e  -> startBtn.setFill(Color.web("#27ae60")));

        // ── EXIT BUTTON ──────────────────────────────────────────
        Rectangle exitBtn = new Rectangle(100, 36, Color.color(0.4, 0.05, 0.05, 0.85));
        exitBtn.setArcWidth(8); exitBtn.setArcHeight(8);
        exitBtn.setTranslateX(W - 120); exitBtn.setTranslateY(20);
        exitBtn.setStroke(Color.web("#c0392b")); exitBtn.setStrokeWidth(1);
        Text exitText = mkText("✕  Beenden", W - 112, 43, Color.web("#e74c3c"), 12, true);
        exitBtn.setOnMouseClicked(e -> fireExit());
        exitText.setOnMouseClicked(e -> fireExit());
        exitBtn.setOnMouseEntered(e -> exitBtn.setFill(Color.color(0.6,0.05,0.05,0.9)));
        exitBtn.setOnMouseExited(e  -> exitBtn.setFill(Color.color(0.4,0.05,0.05,0.85)));

        getContentRoot().getChildren().addAll(startBtn, startText, exitBtn, exitText);
    }

    // ──────────────────────────────────────────────────────────────
    // AKTIONEN
    // ──────────────────────────────────────────────────────────────

    private void waehleMap(int idx) {
        selectedMap = idx;
        for (int i = 0; i < 3; i++) {
            boolean sel = i == idx;
            mapBorders[i].setStroke(sel ? Color.web("#f1c40f") : Color.web("#444466"));
            mapBorders[i].setStrokeWidth(sel ? 2.5 : 1.5);
            mapBorders[i].setFill(sel
                    ? Color.color(0.2, 0.18, 0.05, 0.95)
                    : Color.color(0.15, 0.15, 0.25, 0.9));
        }
        // Schwierigkeit validieren (Hard/Medium evtl. gesperrt)
        waehleDiff(Math.min(selectedDiff, hoechsteFreieDiff()));
    }

    private void waehleDiff(int idx) {
        if (!SaveData.istFreigeschaltet(selectedMap, idx)) return;
        selectedDiff = idx;
        for (int i = 0; i < 3; i++) {
            boolean sel = i == idx;
            boolean frei = SaveData.istFreigeschaltet(selectedMap, i);
            diffBtns[i].setFill(sel ? DIFF_FARBEN[i] : Color.color(0.15,0.15,0.25, frei ? 0.9 : 0.5));
            diffBtns[i].setStroke(sel ? DIFF_FARBEN[i].brighter() : Color.web("#444466"));
            diffTexts[i].setFill(sel ? Color.WHITE : frei ? Color.LIGHTGRAY : Color.GRAY);
        }
    }

    private int hoechsteFreieDiff() {
        for (int i = 2; i >= 0; i--) {
            if (SaveData.istFreigeschaltet(selectedMap, i)) return i;
        }
        return 0;
    }

    private void starteSpiel() {
        // Schwierigkeit & Map global speichern damit App darauf zugreifen kann
        GameConfig.selectedMap  = selectedMap;
        GameConfig.selectedDiff = selectedDiff;
        fireNewGame();
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER
    // ──────────────────────────────────────────────────────────────

    private Text mkText(String txt, double x, double y, Color fill, int size, boolean bold) {
        Text t = new Text(txt);
        t.setTranslateX(x); t.setTranslateY(y); t.setFill(fill);
        t.setStyle("-fx-font-size:" + size + "px;" + (bold ? "-fx-font-weight:bold;" : ""));
        return t;
    }
}