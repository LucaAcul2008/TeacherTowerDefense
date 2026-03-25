package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class CustomGameMenu extends FXGLMenu {

    public static boolean autoStart = false;

    private Rectangle autoBtn;
    private Text      autoText;

    public CustomGameMenu() {
        super(MenuType.GAME_MENU);

        int W = FXGL.getAppWidth();
        int H = FXGL.getAppHeight();

        // Titel
        Text titel = new Text("PAUSIERT");
        titel.setFill(Color.WHITE);
        titel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // XP Anzeige (Lehrer1)
        int xp = SaveData.lehrerXP.length > 0 ? SaveData.lehrerXP[0] : 0;
        Text xpText = new Text("⭐  Lehrer XP: " + xp);
        xpText.setFill(Color.web("#f1c40f"));
        xpText.setStyle("-fx-font-size: 13px;");

        // Münzen Anzeige
        Text muenzenText = new Text("💰  Münzen: " + SaveData.muenzen);
        muenzenText.setFill(Color.web("#f1c40f"));
        muenzenText.setStyle("-fx-font-size: 13px;");

        HBox statsRow = new HBox(24, xpText, muenzenText);
        statsRow.setAlignment(Pos.CENTER);

        Line trenn1 = new Line(0, 0, 300, 0);
        trenn1.setStroke(Color.web("#333355"));

        // Buttons
        var btnResume    = erstelleBtn("▶   Weiterspielen",  Color.web("#27ae60"), () -> fireResume());
        var btnHauptmenu = erstelleBtn("🏠   Hauptmenü",     Color.web("#2c3e7a"), () -> fireExitToMainMenu());
        var btnExit      = erstelleBtn("✕   Beenden",        Color.web("#7f0000"), () -> fireExit());

        // Auto-Start Toggle
        Text autoLabel = new Text("Auto-Start nächste Runde:");
        autoLabel.setFill(Color.LIGHTGRAY);
        autoLabel.setStyle("-fx-font-size: 13px;");

        autoBtn = new Rectangle(52, 26, autoStart ? Color.web("#27ae60") : Color.web("#555555"));
        autoBtn.setArcWidth(26); autoBtn.setArcHeight(26);
        autoText = new Text(autoStart ? "AN" : "AUS");
        autoText.setFill(autoStart ? Color.WHITE : Color.LIGHTGRAY);
        autoText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        StackPane toggle = new StackPane(autoBtn, autoText);
        HBox autoRow = new HBox(16, autoLabel, toggle);
        autoRow.setAlignment(Pos.CENTER);
        autoBtn.setOnMouseClicked(e -> toggleAutoStart());
        autoText.setOnMouseClicked(e -> toggleAutoStart());

        Line trenn2 = new Line(0, 0, 300, 0);
        trenn2.setStroke(Color.web("#333355"));

        // Layout
        VBox layout = new VBox(14,
                titel, statsRow, trenn1,
                btnResume, btnHauptmenu, autoRow,
                trenn2, btnExit);
        layout.setAlignment(Pos.CENTER);

        double bgH = 380;
        double bgW = 380;
        double startX = W / 2.0 - bgW / 2.0;
        double startY = H / 2.0 - bgH / 2.0;

        Rectangle bg = new Rectangle(bgW, bgH, Color.color(0.07, 0.07, 0.12, 0.97));
        bg.setArcWidth(16); bg.setArcHeight(16);
        bg.setStroke(Color.web("#333355")); bg.setStrokeWidth(2);
        bg.setTranslateX(startX);
        bg.setTranslateY(startY);

        layout.setPrefWidth(bgW);
        layout.setMaxWidth(bgW);
        layout.setTranslateX(startX);
        layout.setTranslateY(startY);

        getContentRoot().getChildren().addAll(bg, layout);
    }

    private void toggleAutoStart() {
        autoStart = !autoStart;
        autoBtn.setFill(autoStart ? Color.web("#27ae60") : Color.web("#555555"));
        autoText.setText(autoStart ? "AN" : "AUS");
        autoText.setFill(autoStart ? Color.WHITE : Color.LIGHTGRAY);
    }

    private HBox erstelleBtn(String text, Color farbe, Runnable aktion) {
        Rectangle bg = new Rectangle(300, 42, farbe);
        bg.setArcWidth(8); bg.setArcHeight(8);
        Text label = new Text(text);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        StackPane stack = new StackPane(bg, label);
        bg.setOnMouseClicked(e -> aktion.run());
        label.setOnMouseClicked(e -> aktion.run());
        bg.setOnMouseEntered(e -> bg.setFill(farbe.brighter()));
        bg.setOnMouseExited(e  -> bg.setFill(farbe));
        HBox wrapper = new HBox(stack);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }
}