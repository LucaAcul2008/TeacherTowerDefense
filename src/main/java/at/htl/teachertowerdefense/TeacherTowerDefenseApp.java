package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;

public class TeacherTowerDefenseApp extends GameApplication {

    private Entity lehrerSchatten;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(640);
        settings.setTitle("Teacher Tower Defense");
        settings.setVersion("0.1");
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setPreserveResizeRatio(true);

        settings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("leben", 20);
        vars.put("geld", 100);
        vars.put("runde", 1);
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new TeacherTowerDefenseFactory());
        FXGL.setLevelFromMap("Map1.tmx");

        // Spawnt alle 1,5 Sekunden einen Schüler
        FXGL.getGameTimer().runAtInterval(() -> {
            FXGL.spawn("Schueler", 0, 0);
        }, Duration.seconds(1.5));
    }

    // NEU: Die Physik! Was passiert bei Kollisionen?
    @Override
    protected void initPhysics() {
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.SCHUELER, (projektil, schueler) -> {
            projektil.removeFromWorld(); // Das Projektil verschwindet

            // Ziehe dem Schüler 1 Leben ab
            HealthIntComponent hp = schueler.getComponent(HealthIntComponent.class);
            hp.damage(1);

            // Ist der Schüler besiegt?
            if (hp.isZero()) {
                FXGL.inc("geld", 5); // Belohnung: +5€
                schueler.removeFromWorld();
            }

        });
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.HINDERNIS, (projektil, hindernis) -> {
            // Der Schwamm zerschellt am Baum oder fällt ins Wasser!
            projektil.removeFromWorld();
        });
    }

    @Override
    protected void initUI() {
        // --- LINKE KOMMANDOZENTRALE ---
        Rectangle bgPanel = new Rectangle(200, 120, Color.color(0, 0, 0, 0.6));
        bgPanel.setTranslateX(10); bgPanel.setTranslateY(10);
        bgPanel.setArcWidth(15); bgPanel.setArcHeight(15);

        Text textLeben = new Text();
        textLeben.setTranslateX(25); textLeben.setTranslateY(40);
        textLeben.setFill(Color.RED); textLeben.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textLeben.textProperty().bind(FXGL.getip("leben").asString("Leben: %d"));

        Text textGeld = new Text();
        textGeld.setTranslateX(25); textGeld.setTranslateY(75);
        textGeld.setFill(Color.GOLD); textGeld.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textGeld.textProperty().bind(FXGL.getip("geld").asString("Geld: %d €"));

        Text textRunde = new Text();
        textRunde.setTranslateX(25); textRunde.setTranslateY(110);
        textRunde.setFill(Color.WHITE); textRunde.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textRunde.textProperty().bind(FXGL.getip("runde").asString("Runde: %d / 40"));

        // --- RECHTER SHOP ---
        Rectangle shopPanel = new Rectangle(240, 640, Color.web("#2b2b2b"));
        shopPanel.setTranslateX(960); shopPanel.setTranslateY(0);

        Text shopTitel = new Text("LEHRER SHOP");
        shopTitel.setTranslateX(990); shopTitel.setTranslateY(40);
        shopTitel.setFill(Color.WHITE); shopTitel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // --- DRAG & DROP LEHRER ---
        Rectangle shopIcon1 = new Rectangle(40, 40, Color.BLUE);
        shopIcon1.setTranslateX(1060); shopIcon1.setTranslateY(100);

        double startX = shopIcon1.getTranslateX();
        double startY = shopIcon1.getTranslateY();

        // 1. ANKLICKEN
        shopIcon1.setOnMousePressed(e -> {
            if (FXGL.geti("geld") >= 20) {
                // Wir spawnen jetzt den DUMMEN Schatten, nicht mehr den echten Lehrer!
                lehrerSchatten = FXGL.spawn("LehrerSchatten", -100, -100);
            }
        });

        // 2. ZIEHEN
        shopIcon1.setOnMouseDragged(e -> {
            shopIcon1.setTranslateX(FXGL.getInput().getMouseXUI() - 20);
            shopIcon1.setTranslateY(FXGL.getInput().getMouseYUI() - 20);

            if (lehrerSchatten != null) {
                double mouseX = FXGL.getInput().getMouseXWorld();
                double mouseY = FXGL.getInput().getMouseYWorld();
                lehrerSchatten.setPosition(mouseX - 15, mouseY - 15);

                boolean kollision = false;


                for (Entity h : FXGL.getGameWorld().getEntitiesByType(EntityType.HINDERNIS)) {
                    if (lehrerSchatten.isColliding(h)) { kollision = true; break; }
                }


                for (Entity anderer : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
                    if (anderer.distance(lehrerSchatten) < 30) { kollision = true; break; }
                }

                Circle range = (Circle) lehrerSchatten.getViewComponent().getChildren().get(0);
                Rectangle body = (Rectangle) lehrerSchatten.getViewComponent().getChildren().get(1);

                if (kollision || mouseX >= 960) {
                    body.setFill(Color.color(1, 0, 0, 0.5));
                    range.setFill(Color.color(1, 0, 0, 0.2));
                    range.setStroke(Color.RED);
                } else {
                    body.setFill(Color.color(0, 0, 1, 0.5));
                    range.setFill(Color.color(1, 1, 1, 0.2));
                    range.setStroke(Color.WHITE);
                }
            }
        });

        // 3. LOSLASSEN
        shopIcon1.setOnMouseReleased(e -> {
            if (lehrerSchatten != null) {
                double mouseX = FXGL.getInput().getMouseXWorld();
                double mouseY = FXGL.getInput().getMouseYWorld();

                boolean darfPlatzieren = true;
                for (Entity h : FXGL.getGameWorld().getEntitiesByType(EntityType.HINDERNIS)) {
                    if (lehrerSchatten.isColliding(h)) { darfPlatzieren = false; break; }
                }
                for (Entity anderer : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
                    if (anderer.distance(lehrerSchatten) < 30) { darfPlatzieren = false; break; }
                }

                if (darfPlatzieren && mouseX < 960) {
                    // Erst JETZT spawnen wir den echten, schießenden Lehrer!
                    FXGL.spawn("Lehrer1", mouseX - 15, mouseY - 15);
                    FXGL.inc("geld", -20);
                }

                lehrerSchatten.removeFromWorld();
                lehrerSchatten = null;
            }
            shopIcon1.setTranslateX(startX);
            shopIcon1.setTranslateY(startY);
        });

        FXGL.getGameScene().addUINodes(shopPanel, shopTitel, shopIcon1, bgPanel, textLeben, textGeld, textRunde);
    }

    public static void main(String[] args) {
        launch(args);
    }
}