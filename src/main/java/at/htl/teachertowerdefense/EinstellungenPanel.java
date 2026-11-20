package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Einstellungsfenster (Sound, Musik, Vollbild, optional "Alle Karten freischalten").
 * Wird im Hauptmenü und im Spiel verwendet. Liegt als Overlay über dem ganzen Bildschirm.
 */
public class EinstellungenPanel extends Pane {

    private static final double PW = 460;

    private final Rectangle sSound = new Rectangle(), sMusik = new Rectangle(), sVollbild = new Rectangle(), sAlleFrei;
    private final Text tSound = new Text(), tMusik = new Text(), tVollbild = new Text(), tAlleFrei;
    private final Runnable beimSchliessen;

    /**
     * @param mitFreischalten  Zeile "Alle Karten freischalten" anzeigen (nur im Hauptmenü sinnvoll)
     * @param nachFreischalten wird nach dem Umschalten des Präsentationsmodus aufgerufen (darf null sein)
     * @param beimSchliessen   wird beim Schließen aufgerufen (z.B. Spiel fortsetzen, darf null sein)
     */
    public EinstellungenPanel(double w, double h, boolean mitFreischalten,
                              Runnable nachFreischalten, Runnable beimSchliessen) {
        this.beimSchliessen = beimSchliessen;
        sAlleFrei = mitFreischalten ? new Rectangle() : null;
        tAlleFrei = mitFreischalten ? new Text() : null;

        Rectangle dimmer = new Rectangle(w, h, Color.color(0, 0, 0, 0.65));
        dimmer.setOnMouseClicked(e -> { e.consume(); schliessen(); });   // Klick daneben schließt
        dimmer.setOnMousePressed(e -> e.consume());

        double ph = mitFreischalten ? 360 : 310;
        double px = (w - PW) / 2, py = (h - ph) / 2;
        Rectangle panel = new Rectangle(PW, ph, Color.color(0.07, 0.07, 0.12, 0.98));
        panel.setArcWidth(16); panel.setArcHeight(16);
        panel.setStroke(Color.web("#5d6d9e")); panel.setStrokeWidth(2);
        panel.setTranslateX(px); panel.setTranslateY(py);
        panel.setOnMouseClicked(e -> e.consume());
        panel.setOnMousePressed(e -> e.consume());

        getChildren().addAll(dimmer, panel, text("⚙  EINSTELLUNGEN", px + 28, py + 42, Color.WHITE, 22, true));

        schalter(px, py + 80, "🔊  Soundeffekte", null, sSound, tSound,
                () -> Sounds.setSoundAn(!SaveData.soundAn));
        schalter(px, py + 130, "🎵  Musik", null, sMusik, tMusik,
                () -> Sounds.setMusikAn(!SaveData.musikAn));
        schalter(px, py + 180, "🖥  Vollbild", null, sVollbild, tVollbild,
                () -> FXGL.getPrimaryStage().setFullScreen(!FXGL.getPrimaryStage().isFullScreen()));
        if (mitFreischalten) {
            schalter(px, py + 230, "🔓  Alle Karten freischalten",
                    "Präsentationsmodus: alle Karten & Schwierigkeiten spielbar", sAlleFrei, tAlleFrei,
                    () -> {
                        SaveData.alleMapsFrei = !SaveData.alleMapsFrei;
                        SaveData.speichern();
                        if (nachFreischalten != null) nachFreischalten.run();
                    });
        }

        Rectangle zu = new Rectangle(160, 40, Color.web("#27ae60"));
        zu.setArcWidth(10); zu.setArcHeight(10);
        zu.setTranslateX(px + (PW - 160) / 2); zu.setTranslateY(py + ph - 62);
        Text zuText = text("Fertig", 0, 0, Color.WHITE, 15, true);
        zuText.setTextOrigin(VPos.CENTER);
        zuText.setTranslateX(px + PW / 2 - zuText.getLayoutBounds().getWidth() / 2);
        zuText.setTranslateY(py + ph - 42);
        zu.setCursor(Cursor.HAND); zuText.setCursor(Cursor.HAND);
        zu.setOnMouseClicked(e -> { e.consume(); schliessen(); });
        zuText.setOnMouseClicked(e -> { e.consume(); schliessen(); });
        zu.setOnMouseEntered(e -> zu.setFill(Color.web("#2ecc71")));
        zu.setOnMouseExited(e  -> zu.setFill(Color.web("#27ae60")));
        getChildren().addAll(zu, zuText);

        setVisible(false);
    }

    public void oeffnen() {
        aktualisiere();
        setVisible(true);
        toFront();
        Sounds.play("klick");
    }

    public void schliessen() {
        if (!isVisible()) return;
        setVisible(false);
        Sounds.play("klick");
        if (beimSchliessen != null) beimSchliessen.run();
    }

    public boolean istOffen() {
        return isVisible();
    }

    /** Eine Zeile mit Beschriftung (optional Untertitel) und AN/AUS-Schalter rechts. */
    private void schalter(double px, double y, String label, String hinweis,
                          Rectangle btn, Text t, Runnable aktion) {
        getChildren().add(text(label, px + 32, y + (hinweis == null ? 18 : 12), Color.web("#e0e4f5"), 15, true));
        if (hinweis != null) getChildren().add(text(hinweis, px + 32, y + 30, Color.web("#8a90b0"), 11, false));
        btn.setWidth(64); btn.setHeight(28);
        btn.setArcWidth(28); btn.setArcHeight(28);
        btn.setTranslateX(px + PW - 32 - 64); btn.setTranslateY(y);
        t.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        t.setTextOrigin(VPos.CENTER);
        t.setTranslateY(y + 14);
        btn.setCursor(Cursor.HAND); t.setCursor(Cursor.HAND);
        Runnable klick = () -> { aktion.run(); Sounds.play("klick"); aktualisiere(); };
        btn.setOnMouseClicked(e -> { e.consume(); klick.run(); });
        t.setOnMouseClicked(e -> { e.consume(); klick.run(); });
        getChildren().addAll(btn, t);
    }

    private void aktualisiere() {
        setze(sSound, tSound, SaveData.soundAn);
        setze(sMusik, tMusik, SaveData.musikAn);
        setze(sVollbild, tVollbild, FXGL.getPrimaryStage().isFullScreen());
        if (sAlleFrei != null) setze(sAlleFrei, tAlleFrei, SaveData.alleMapsFrei);
    }

    private static void setze(Rectangle btn, Text t, boolean an) {
        btn.setFill(an ? Color.web("#27ae60") : Color.web("#555566"));
        t.setText(an ? "AN" : "AUS");
        t.setFill(an ? Color.WHITE : Color.LIGHTGRAY);
        t.setTranslateX(btn.getTranslateX() + 32 - t.getLayoutBounds().getWidth() / 2);
    }

    private static Text text(String s, double x, double y, Color fill, int size, boolean bold) {
        Text t = new Text(s);
        t.setTranslateX(x); t.setTranslateY(y);
        t.setFill(fill);
        t.setStyle("-fx-font-size: " + size + "px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return t;
    }
}
