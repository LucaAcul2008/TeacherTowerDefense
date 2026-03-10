package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Map;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;


public class TeacherTowerDefenseApp extends GameApplication {

    // 1. Wir definieren Typen für unsere Objekte, damit sie sich später erkennen
    public enum EntityType {
        TEACHER, STUDENT, PROJECTILE
    }

    @Override
    protected void initSettings(GameSettings settings) {
        // 960 Pixel für deine Map + 240 Pixel für deinen neuen Shop
        settings.setWidth(1200);
        settings.setHeight(640);
        settings.setTitle("Teacher Tower Defense");
        settings.setVersion("0.1");

        // Erlaubt und erzwingt den Vollbildmodus direkt beim Start!
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);

        // Verhindert, dass die Pixel eklig gestreckt werden
        settings.setPreserveResizeRatio(true);
    }

    @Override
    protected void initGame() {
        // Der Tarn-Trick: Färbt den Hintergrund exakt in der Rasen-Farbe deines Tilesets


        FXGL.getGameWorld().addEntityFactory(new TeacherTowerDefenseFactory());
        FXGL.setLevelFromMap("Map1.tmx");
        FXGL.getGameTimer().runAtInterval(() -> {
            FXGL.spawn("Schueler", 0, 0);
        }, Duration.seconds(1.5));
    }
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        // Hier legen wir unsere Startwerte fest
        vars.put("leben", 20);
        vars.put("geld", 100);
        vars.put("runde", 1);
    }

    @Override
    protected void initUI() {
        // Deine bisherige linke Kommandozentrale...
        Rectangle bgPanel = new Rectangle(200, 120, Color.color(0, 0, 0, 0.6));
        bgPanel.setTranslateX(10);
        bgPanel.setTranslateY(10);
        bgPanel.setArcWidth(15);
        bgPanel.setArcHeight(15);

        Text textLeben = new Text();
        textLeben.setTranslateX(25);
        textLeben.setTranslateY(40);
        textLeben.setFill(Color.RED);
        textLeben.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textLeben.textProperty().bind(FXGL.getip("leben").asString("Leben: %d"));

        Text textGeld = new Text();
        textGeld.setTranslateX(25);
        textGeld.setTranslateY(75);
        textGeld.setFill(Color.GOLD);
        textGeld.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textGeld.textProperty().bind(FXGL.getip("geld").asString("Geld: %d €"));

        Text textRunde = new Text();
        textRunde.setTranslateX(25);
        textRunde.setTranslateY(110);
        textRunde.setFill(Color.WHITE);
        textRunde.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textRunde.textProperty().bind(FXGL.getip("runde").asString("Runde: %d / 40"));

        // --- NEU: DER LEHRER SHOP ---
        // Ein dunkles Panel, das genau da anfängt, wo die Map (960 Pixel) aufhört!
        Rectangle shopPanel = new Rectangle(240, 640, Color.web("#2b2b2b"));
        shopPanel.setTranslateX(960);
        shopPanel.setTranslateY(0);

        Text shopTitel = new Text("LEHRER SHOP");
        shopTitel.setTranslateX(990);
        shopTitel.setTranslateY(40);
        shopTitel.setFill(Color.WHITE);
        shopTitel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // WICHTIG: Füge bgPanel, shopPanel und shopTitel zu den UINodes hinzu!
        FXGL.getGameScene().addUINodes(shopPanel, shopTitel, bgPanel, textLeben, textGeld, textRunde);
    }

    public static void main(String[] args) {
        launch(args);
    }
}