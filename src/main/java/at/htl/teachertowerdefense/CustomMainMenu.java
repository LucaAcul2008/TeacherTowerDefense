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

    private static final String[] MAP_NAMEN = { "HTL Saalfelden", "City", "Schulausflug", "Teichwiese" };
    private static final String[] DIFF_NAMEN = { "Easy", "Medium", "Hard" };
    private static final Color[]  DIFF_FARBEN = {
            Color.web("#27ae60"), Color.web("#e67e22"), Color.web("#c0392b")
    };
    private static final int[] DIFF_MUENZEN  = { 100, 175, 250 };

    // Münzen-Anzeige
    private Text muenzenText;

    // Map-Karten
    private VBox[] mapKarten = new VBox[GameConfig.ANZAHL_MAPS];
    private Rectangle[] mapBorders = new Rectangle[GameConfig.ANZAHL_MAPS];
    private Text[] sterneTexte = new Text[GameConfig.ANZAHL_MAPS];
    private Text[] mapNamenTexte = new Text[GameConfig.ANZAHL_MAPS];
    private Text[] lockTexte = new Text[GameConfig.ANZAHL_MAPS];

    // Einstellungen
    private Pane einstellungen;
    private Rectangle sSound, sMusik, sVollbild, sAlleFrei;
    private Text tSound, tMusik, tVollbild, tAlleFrei;

    // Diff-Buttons
    private Rectangle[] diffBtns = new Rectangle[3];
    private Text[]       diffTexts = new Text[3];

    // Start-Button
    private Rectangle startBtn;
    private Text      startText;

    public CustomMainMenu() {
        super(MenuType.MAIN_MENU);

        // Immer neu laden wenn Menü geöffnet wird (z.B. nach Gewinn)
        SaveData.laden();

        int W = FXGL.getAppWidth();
        int H = FXGL.getAppHeight();

        // ── Hintergrundbild ──────────────────────────────────────
        try {
            // Bild füllt den ganzen Bildschirm, ohne verzerrt zu werden (Überstand wird abgeschnitten)
            javafx.scene.image.Image img = FXGL.image("menu_bg.png");
            double skala = Math.max(W / img.getWidth(), H / img.getHeight());
            ImageView bg = new ImageView(img);
            bg.setFitWidth(img.getWidth() * skala); bg.setFitHeight(img.getHeight() * skala);
            bg.setTranslateX((W - img.getWidth() * skala) / 2);
            bg.setTranslateY((H - img.getHeight() * skala) / 2);
            javafx.scene.Group bgGruppe = new javafx.scene.Group(bg);
            bgGruppe.setClip(new Rectangle(W, H));
            getContentRoot().getChildren().add(bgGruppe);
        } catch (Exception e) {
            Rectangle bg = new Rectangle(W, H, Color.web("#1a2a1a"));
            getContentRoot().getChildren().add(bg);
        }

        // ── Dunkler Overlay unten ────────────────────────────────
        Rectangle overlay = new Rectangle(W, 344, Color.color(0, 0, 0, 0.72));
        overlay.setTranslateY(H - 344);
        getContentRoot().getChildren().add(overlay);

        // ── Diamanten (Skin-Währung) oben links ──────────────────
        Rectangle muenzenBg = new Rectangle(200, 44, Color.color(0.02, 0.10, 0.16, 0.8));
        muenzenBg.setArcWidth(10); muenzenBg.setArcHeight(10);
        muenzenBg.setTranslateX(20); muenzenBg.setTranslateY(20);
        muenzenBg.setStroke(GameConfig.DIAMANT_FARBE.darker()); muenzenBg.setStrokeWidth(1.5);

        muenzenText = mkText(GameConfig.diamanten(SaveData.muenzen), 32, 47,
                GameConfig.DIAMANT_FARBE, 15, true);

        getContentRoot().getChildren().addAll(muenzenBg, muenzenText);

        // ── SKIN SHOP (links oben, unter Münzen) ─────────────────
        final double SKIN_X = 20, SKIN_Y = 75;
        final double SKIN_W = 340, SKIN_H = 160;
        final double BTN_W = 96, BTN_H = 44, BTN_GAP = 8;

        Rectangle skinBg = new Rectangle(SKIN_W, SKIN_H, Color.color(0, 0, 0, 0.70));
        skinBg.setArcWidth(10); skinBg.setArcHeight(10);
        skinBg.setTranslateX(SKIN_X); skinBg.setTranslateY(SKIN_Y);
        skinBg.setStroke(Color.web("#5a2d82")); skinBg.setStrokeWidth(1.5);

        Text skinTitel = mkText("🎨  LEHRER SKINS", SKIN_X + 10, SKIN_Y + 22,
                Color.web("#c39bd3"), 12, true);

        // Hintergrund zuerst – Buttons rendern darüber
        getContentRoot().getChildren().addAll(skinBg, skinTitel);

        String[] skinNamen = { "Groebl Alt", "Feichtner Alt", "Winkler Alt" };
        int[] skinPreise = SaveData.SKIN_PREISE;

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            boolean gekauft = SaveData.skinFreigeschaltet[i][1];
            boolean aktiv   = SaveData.aktiverSkin[i] == 1;

            double bx = SKIN_X + 8 + i * (BTN_W + BTN_GAP);

            // Skin-Name über Button
            Text skinName = mkText(skinNamen[i], bx + 4, SKIN_Y + 40,
                    Color.LIGHTGRAY, 10, false);

            // Button-Farbe je nach Status
            Color btnFarbe = aktiv                            ? Color.web("#6c3483")
                    : gekauft                                 ? Color.web("#1f3a8a")
                    : SaveData.muenzen >= skinPreise[i]       ? Color.web("#1a3a1a")
                    :                                           Color.color(0.12, 0.12, 0.12, 0.9);

            Rectangle skinBtn = new Rectangle(BTN_W, BTN_H, btnFarbe);
            skinBtn.setArcWidth(8); skinBtn.setArcHeight(8);
            skinBtn.setTranslateX(bx); skinBtn.setTranslateY(SKIN_Y + 46);
            skinBtn.setStroke(aktiv ? Color.web("#9b59b6") : Color.web("#555577"));
            skinBtn.setStrokeWidth(aktiv ? 2.0 : 1.0);

            // Label im Button
            String btnLabel = gekauft ? (aktiv ? "✓  AN" : "○  AUS") : skinPreise[i] + " " + GameConfig.DIAMANT;
            Text skinBtnText = mkText(btnLabel, bx + 12, SKIN_Y + 74, Color.WHITE, 11, true);

            // Status-Text unter Button
            String statusLabel = aktiv ? "● aktiv" : gekauft ? "○ inaktiv" : "nicht gekauft";
            Color  statusFarbe = aktiv ? Color.web("#9b59b6") : gekauft ? Color.web("#3498db") : Color.web("#666688");
            Text statusText = mkText(statusLabel, bx + 4, SKIN_Y + 108, statusFarbe, 9, false);

            skinBtn.setOnMouseEntered(e -> {
                if (!SaveData.skinFreigeschaltet[idx][1] && SaveData.muenzen < SaveData.SKIN_PREISE[idx]) return;
                skinBtn.setOpacity(0.75);
            });
            skinBtn.setOnMouseExited(e -> skinBtn.setOpacity(1.0));

            skinBtn.setOnMouseClicked(e -> {
                if (SaveData.kaufeSkin(idx)) {
                    muenzenText.setText(GameConfig.diamanten(SaveData.muenzen));
                    boolean jetzt = SaveData.aktiverSkin[idx] == 1;
                    skinBtn.setFill(jetzt ? Color.web("#6c3483") : Color.web("#1f3a8a"));
                    skinBtn.setStroke(jetzt ? Color.web("#9b59b6") : Color.web("#555577"));
                    skinBtn.setStrokeWidth(jetzt ? 2.0 : 1.0);
                    skinBtnText.setText(jetzt ? "✓  AN" : "○  AUS");
                    statusText.setText(jetzt ? "● aktiv" : "○ inaktiv");
                    statusText.setFill(jetzt ? Color.web("#9b59b6") : Color.web("#3498db"));
                }
            });
            skinBtnText.setOnMouseClicked(skinBtn.getOnMouseClicked());

            getContentRoot().getChildren().addAll(skinName, skinBtn, skinBtnText, statusText);
        }



        // ── MAP AUSWAHL ──────────────────────────────────────────
        Text mapTitel = mkText("KARTE WÄHLEN", W/2.0 - 100, H - 289,
                Color.WHITE, 13, true);
        mapTitel.setOpacity(0.7);
        getContentRoot().getChildren().add(mapTitel);

        int anzahl = GameConfig.ANZAHL_MAPS;
        double karteW = 200, karteH = 130;
        double startX = W / 2.0 - (karteW * anzahl + 20 * (anzahl - 1)) / 2.0;

        for (int i = 0; i < anzahl; i++) {
            final int idx = i;
            boolean frei = SaveData.mapFrei(i);
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
            sterneTexte[i] = sterneText; // Referenz speichern für späteren Refresh

            // Gesperrt-Overlay
            Text lockText = mkText(i == 0 ? "" : "🔒 " + MAP_NAMEN[i - 1] + " auf Easy schaffen",
                    0, 0, Color.web("#888888"), 11, false);
            lockText.setVisible(!frei);
            mapNamenTexte[i] = name;
            lockTexte[i] = lockText;

            VBox karte = new VBox(8, name, sterneText, lockText);
            karte.setAlignment(Pos.CENTER);
            karte.setPrefSize(karteW, karteH);
            karte.setTranslateX(startX + i * (karteW + 20));
            karte.setTranslateY(H - 284);

            // Klick – die Sperre wird jedes Mal neu geprüft (kann sich über die Einstellungen ändern)
            border.setOnMouseClicked(e -> { if (SaveData.mapFrei(idx)) waehleMap(idx); });
            karte.setOnMouseClicked(e -> { if (SaveData.mapFrei(idx)) waehleMap(idx); });
            border.setOnMouseEntered(e -> { if (idx != selectedMap && SaveData.mapFrei(idx)) border.setFill(Color.color(0.2,0.2,0.35,0.9)); });
            border.setOnMouseExited(e  -> { if (idx != selectedMap && SaveData.mapFrei(idx)) border.setFill(Color.color(0.15,0.15,0.25,0.9)); });

            // Border hinter VBox legen
            border.setTranslateX(startX + i * (karteW + 20));
            border.setTranslateY(H - 284);

            mapKarten[i] = karte;
            getContentRoot().getChildren().addAll(border, karte);
        }

        // ── SCHWIERIGKEIT ────────────────────────────────────────
        Text diffTitel = mkText("SCHWIERIGKEIT", W/2.0 - 100, H - 134,
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
            btn.setTranslateY(H - 119);
            diffBtns[i] = btn;

            String label = DIFF_NAMEN[i] + "  +" + DIFF_MUENZEN[i] + GameConfig.DIAMANT;
            Text txt = mkText(label,
                    diffStartX + i * (diffW + 12) + 10, H - 94,
                    i == 0 ? Color.WHITE : Color.GRAY, 11, true);
            diffTexts[i] = txt;

            btn.setOnMouseClicked(e -> waehleDiff(idx));
            txt.setOnMouseClicked(e -> waehleDiff(idx));

            getContentRoot().getChildren().addAll(btn, txt);
        }

        // ── START BUTTON ─────────────────────────────────────────
        // 15px Abstand zu den Schwierigkeits-Buttons, 18px zum unteren Rand
        double startY = H - 64;
        startBtn = new Rectangle(220, 46, Color.web("#27ae60"));
        startBtn.setArcWidth(10); startBtn.setArcHeight(10);
        startBtn.setTranslateX(W / 2.0 - 110);
        startBtn.setTranslateY(startY);
        DropShadow glow = new DropShadow(15, Color.web("#2ecc71"));
        startBtn.setEffect(glow);

        startText = mkText("▶   SPIELEN", 0, 0, Color.WHITE, 16, true);
        startText.setTextOrigin(javafx.geometry.VPos.CENTER);   // genau mittig im Button
        startText.setTranslateX(W / 2.0 - startText.getLayoutBounds().getWidth() / 2);
        startText.setTranslateY(startY + 23);

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

        // ── EINSTELLUNGEN ────────────────────────────────────────
        Rectangle einstBtn = new Rectangle(140, 36, Color.color(0.12, 0.14, 0.25, 0.9));
        einstBtn.setArcWidth(8); einstBtn.setArcHeight(8);
        einstBtn.setTranslateX(W - 270); einstBtn.setTranslateY(20);
        einstBtn.setStroke(Color.web("#5d6d9e")); einstBtn.setStrokeWidth(1);
        Text einstText = mkText("⚙  Einstellungen", W - 258, 43, Color.web("#d6dcf5"), 12, true);
        einstBtn.setCursor(javafx.scene.Cursor.HAND);
        einstText.setCursor(javafx.scene.Cursor.HAND);
        einstBtn.setOnMouseClicked(e -> zeigeEinstellungen(true));
        einstText.setOnMouseClicked(e -> zeigeEinstellungen(true));
        einstBtn.setOnMouseEntered(e -> einstBtn.setFill(Color.color(0.18, 0.21, 0.36, 0.95)));
        einstBtn.setOnMouseExited(e  -> einstBtn.setFill(Color.color(0.12, 0.14, 0.25, 0.9)));
        getContentRoot().getChildren().addAll(einstBtn, einstText);

        einstellungen = baueEinstellungen(W, H);
        einstellungen.setVisible(false);
        getContentRoot().getChildren().add(einstellungen);
    }

    // ──────────────────────────────────────────────────────────────
    // EINSTELLUNGEN
    // ──────────────────────────────────────────────────────────────

    private Pane baueEinstellungen(int W, int H) {
        Pane root = new Pane();
        Rectangle dimmer = new Rectangle(W, H, Color.color(0, 0, 0, 0.65));
        dimmer.setOnMouseClicked(e -> zeigeEinstellungen(false));   // Klick daneben schließt

        double pw = 460, ph = 360;
        double px = (W - pw) / 2, py = (H - ph) / 2;
        Rectangle panel = new Rectangle(pw, ph, Color.color(0.07, 0.07, 0.12, 0.98));
        panel.setArcWidth(16); panel.setArcHeight(16);
        panel.setStroke(Color.web("#5d6d9e")); panel.setStrokeWidth(2);
        panel.setTranslateX(px); panel.setTranslateY(py);
        panel.setOnMouseClicked(e -> e.consume());

        Text titel = mkText("⚙  EINSTELLUNGEN", px + 28, py + 42, Color.WHITE, 22, true);

        sSound = new Rectangle(); tSound = new Text();
        sMusik = new Rectangle(); tMusik = new Text();
        sVollbild = new Rectangle(); tVollbild = new Text();
        sAlleFrei = new Rectangle(); tAlleFrei = new Text();

        root.getChildren().addAll(dimmer, panel, titel);
        schalter(root, px, py + 80,  "🔊  Soundeffekte", null, sSound, tSound,
                () -> Sounds.setSoundAn(!SaveData.soundAn));
        schalter(root, px, py + 130, "🎵  Musik", null, sMusik, tMusik,
                () -> Sounds.setMusikAn(!SaveData.musikAn));
        schalter(root, px, py + 180, "🖥  Vollbild", null, sVollbild, tVollbild,
                () -> FXGL.getPrimaryStage().setFullScreen(!FXGL.getPrimaryStage().isFullScreen()));
        schalter(root, px, py + 230, "🔓  Alle Karten freischalten",
                "Präsentationsmodus: alle Karten & Schwierigkeiten spielbar", sAlleFrei, tAlleFrei,
                () -> {
                    SaveData.alleMapsFrei = !SaveData.alleMapsFrei;
                    SaveData.speichern();
                    aktualisiereKarten();
                });

        Rectangle zu = new Rectangle(160, 40, Color.web("#27ae60"));
        zu.setArcWidth(10); zu.setArcHeight(10);
        zu.setTranslateX(px + (pw - 160) / 2); zu.setTranslateY(py + ph - 62);
        Text zuText = mkText("Fertig", 0, 0, Color.WHITE, 15, true);
        zuText.setTextOrigin(javafx.geometry.VPos.CENTER);
        zuText.setTranslateX(px + pw / 2 - zuText.getLayoutBounds().getWidth() / 2);
        zuText.setTranslateY(py + ph - 42);
        zu.setCursor(javafx.scene.Cursor.HAND); zuText.setCursor(javafx.scene.Cursor.HAND);
        zu.setOnMouseClicked(e -> zeigeEinstellungen(false));
        zuText.setOnMouseClicked(e -> zeigeEinstellungen(false));
        zu.setOnMouseEntered(e -> zu.setFill(Color.web("#2ecc71")));
        zu.setOnMouseExited(e  -> zu.setFill(Color.web("#27ae60")));
        root.getChildren().addAll(zu, zuText);
        return root;
    }

    /** Eine Zeile mit Beschriftung (optional Untertitel) und AN/AUS-Schalter rechts. */
    private void schalter(Pane root, double px, double y, String label, String hinweis,
                          Rectangle btn, Text text, Runnable aktion) {
        Text l = mkText(label, px + 32, y + (hinweis == null ? 18 : 12), Color.web("#e0e4f5"), 15, true);
        root.getChildren().add(l);
        if (hinweis != null) {
            root.getChildren().add(mkText(hinweis, px + 32, y + 30, Color.web("#8a90b0"), 11, false));
        }
        btn.setWidth(64); btn.setHeight(28);
        btn.setArcWidth(28); btn.setArcHeight(28);
        btn.setTranslateX(px + 460 - 32 - 64); btn.setTranslateY(y);
        text.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        text.setTextOrigin(javafx.geometry.VPos.CENTER);
        text.setTranslateY(y + 14);
        btn.setCursor(javafx.scene.Cursor.HAND); text.setCursor(javafx.scene.Cursor.HAND);
        Runnable klick = () -> { aktion.run(); Sounds.play("klick"); aktualisiereSchalter(); };
        btn.setOnMouseClicked(e -> klick.run());
        text.setOnMouseClicked(e -> klick.run());
        root.getChildren().addAll(btn, text);
    }

    private void zeigeEinstellungen(boolean an) {
        if (an) aktualisiereSchalter();
        einstellungen.setVisible(an);
        Sounds.play("klick");
    }

    private void aktualisiereSchalter() {
        setzeSchalter(sSound, tSound, SaveData.soundAn);
        setzeSchalter(sMusik, tMusik, SaveData.musikAn);
        setzeSchalter(sVollbild, tVollbild, FXGL.getPrimaryStage().isFullScreen());
        setzeSchalter(sAlleFrei, tAlleFrei, SaveData.alleMapsFrei);
    }

    private void setzeSchalter(Rectangle btn, Text text, boolean an) {
        btn.setFill(an ? Color.web("#27ae60") : Color.web("#555566"));
        text.setText(an ? "AN" : "AUS");
        text.setFill(an ? Color.WHITE : Color.LIGHTGRAY);
        text.setTranslateX(btn.getTranslateX() + 32 - text.getLayoutBounds().getWidth() / 2);
    }

    /** Sperr-Anzeige der Karten neu setzen (z.B. nach Präsentationsmodus an/aus). */
    private void aktualisiereKarten() {
        for (int i = 0; i < GameConfig.ANZAHL_MAPS; i++) {
            if (mapNamenTexte[i] == null) continue;
            boolean frei = SaveData.mapFrei(i);
            mapNamenTexte[i].setFill(frei ? Color.WHITE : Color.GRAY);
            lockTexte[i].setVisible(!frei);
        }
        if (!SaveData.mapFrei(selectedMap)) selectedMap = 0;
        waehleMap(selectedMap);
    }

    // ──────────────────────────────────────────────────────────────
    // LIFECYCLE – Sterne & Münzen aktualisieren wenn Menü geöffnet wird
    // ──────────────────────────────────────────────────────────────

    @Override
    public void onEnteredFrom(com.almasb.fxgl.scene.Scene prev) {
        super.onEnteredFrom(prev);
        SaveData.laden();
        Sounds.musik("musik_menu");
        muenzenText.setText(GameConfig.diamanten(SaveData.muenzen));
        for (int i = 0; i < sterneTexte.length; i++) {
            if (sterneTexte[i] == null) continue;
            int s = SaveData.getSterne(i);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 3; j++) sb.append(j < s ? "★" : "☆");
            sterneTexte[i].setText(sb.toString());
        }
        aktualisiereKarten();
    }

    // ──────────────────────────────────────────────────────────────
    // AKTIONEN
    // ──────────────────────────────────────────────────────────────

    private void waehleMap(int idx) {
        selectedMap = idx;
        for (int i = 0; i < GameConfig.ANZAHL_MAPS; i++) {
            boolean sel = i == idx;
            mapBorders[i].setStroke(sel ? Color.web("#f1c40f") : Color.web("#444466"));
            mapBorders[i].setStrokeWidth(sel ? 2.5 : 1.5);
            mapBorders[i].setFill(sel
                    ? Color.color(0.2, 0.18, 0.05, 0.95)
                    : SaveData.mapFrei(i) ? Color.color(0.15, 0.15, 0.25, 0.9) : Color.color(0.1, 0.1, 0.1, 0.85));
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