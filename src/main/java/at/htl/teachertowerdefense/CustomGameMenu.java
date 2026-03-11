package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Ersetzt das Standard-FXGL ESC-Menü.
 * Enthält RESUME, AUTO-START Toggle und EXIT.
 */
public class CustomGameMenu extends FXGLMenu {

    // Statische Variable damit TeacherTowerDefenseApp drauf zugreifen kann
    public static boolean autoStart = false;

    private Rectangle autoBtn;
    private Text      autoText;

    public CustomGameMenu() {
        super(MenuType.GAME_MENU);

        // Titel
        Text titel = new Text("PAUSIERT");
        titel.setFill(Color.WHITE);
        titel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        // RESUME Button
        var btnResume = erstelleMenuBtn("▶  WEITERSPIELEN", Color.web("#27ae60"), () -> fireResume());

        // AUTO-START Toggle Zeile
        Text autoLabel = new Text("Auto-Start nächste Runde:");
        autoLabel.setFill(Color.LIGHTGRAY);
        autoLabel.setStyle("-fx-font-size: 14px;");

        autoBtn = new Rectangle(58, 28, autoStart ? Color.web("#27ae60") : Color.web("#555555"));
        autoBtn.setArcWidth(28); autoBtn.setArcHeight(28);

        autoText = new Text(autoStart ? "AN" : "AUS");
        autoText.setFill(autoStart ? Color.WHITE : Color.LIGHTGRAY);
        autoText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        HBox toggleBox = new HBox(12, autoBtn, autoText);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        autoBtn.setOnMouseClicked(e -> toggleAutoStart());
        autoText.setOnMouseClicked(e -> toggleAutoStart());

        HBox autoRow = new HBox(20, autoLabel, toggleBox);
        autoRow.setAlignment(Pos.CENTER);

        // EXIT Button
        var btnExit = erstelleMenuBtn("✕  BEENDEN", Color.web("#7f0000"), () -> fireExit());

        // Layout
        VBox layout = new VBox(20, titel, btnResume, autoRow, btnExit);
        layout.setAlignment(Pos.CENTER);
        layout.setTranslateX(FXGL.getAppWidth()  / 2.0 - 175);
        layout.setTranslateY(FXGL.getAppHeight() / 2.0 - 130);

        // Hintergrund
        Rectangle bg = new Rectangle(350, 260, Color.color(0.07, 0.07, 0.12, 0.97));
        bg.setArcWidth(16); bg.setArcHeight(16);
        bg.setStroke(Color.web("#333355")); bg.setStrokeWidth(2);
        bg.setTranslateX(FXGL.getAppWidth()  / 2.0 - 175);
        bg.setTranslateY(FXGL.getAppHeight() / 2.0 - 145);

        getContentRoot().getChildren().addAll(bg, layout);
    }

    private void toggleAutoStart() {
        autoStart = !autoStart;
        autoBtn.setFill(autoStart ? Color.web("#27ae60") : Color.web("#555555"));
        autoText.setText(autoStart ? "AN" : "AUS");
        autoText.setFill(autoStart ? Color.WHITE : Color.LIGHTGRAY);
    }

    private HBox erstelleMenuBtn(String text, Color farbe, Runnable aktion) {
        Rectangle bg = new Rectangle(300, 44, farbe);
        bg.setArcWidth(8); bg.setArcHeight(8);

        Text label = new Text(text);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox btn = new HBox(bg);
        btn.setAlignment(Pos.CENTER);

        // Label über den Button legen
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(bg, label);

        bg.setOnMouseClicked(e -> aktion.run());
        label.setOnMouseClicked(e -> aktion.run());
        bg.setOnMouseEntered(e -> bg.setFill(farbe.brighter()));
        bg.setOnMouseExited(e  -> bg.setFill(farbe));

        // Wir geben StackPane zurück als HBox-Wrapper
        HBox wrapper = new HBox(stack);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }
}
