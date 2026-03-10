package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
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

        FXGL.getGameTimer().runAtInterval(() -> {
            FXGL.spawn("Schueler", 0, 0);
        }, Duration.seconds(1.5));
    }

    @Override
    protected void initPhysics() {
        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.SCHUELER, (projektil, schueler) -> {
            projektil.removeFromWorld();
            HealthIntComponent hp = schueler.getComponent(HealthIntComponent.class);
            hp.damage(1);
            if (hp.isZero()) {
                FXGL.inc("geld", 5);
                schueler.removeFromWorld();
            }
        });

        FXGL.onCollisionBegin(EntityType.PROJEKTIL, EntityType.HINDERNIS, (projektil, hindernis) -> {
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
        textLeben.setFill(Color.RED);
        textLeben.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textLeben.textProperty().bind(FXGL.getip("leben").asString("Leben: %d"));

        Text textGeld = new Text();
        textGeld.setTranslateX(25); textGeld.setTranslateY(75);
        textGeld.setFill(Color.GOLD);
        textGeld.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textGeld.textProperty().bind(FXGL.getip("geld").asString("Geld: %d €"));

        Text textRunde = new Text();
        textRunde.setTranslateX(25); textRunde.setTranslateY(110);
        textRunde.setFill(Color.WHITE);
        textRunde.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        textRunde.textProperty().bind(FXGL.getip("runde").asString("Runde: %d / 40"));

        // --- RECHTER SHOP ---
        Rectangle shopPanel = new Rectangle(240, 640, Color.web("#2b2b2b"));
        shopPanel.setTranslateX(960); shopPanel.setTranslateY(0);

        Text shopTitel = new Text("LEHRER SHOP");
        shopTitel.setTranslateX(990); shopTitel.setTranslateY(40);
        shopTitel.setFill(Color.WHITE);
        shopTitel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // --- DRAG & DROP ---
        Rectangle shopIcon1 = new Rectangle(40, 40, Color.BLUE);
        shopIcon1.setTranslateX(1060); shopIcon1.setTranslateY(100);

        double startX = shopIcon1.getTranslateX();
        double startY = shopIcon1.getTranslateY();

        shopIcon1.setOnMousePressed(e -> {
            if (FXGL.geti("geld") >= 20) {
                lehrerSchatten = FXGL.spawn("LehrerSchatten", -100, -100);
            }
        });

        shopIcon1.setOnMouseDragged(e -> {
            shopIcon1.setTranslateX(FXGL.getInput().getMouseXUI() - 20);
            shopIcon1.setTranslateY(FXGL.getInput().getMouseYUI() - 20);

            if (lehrerSchatten != null) {
                double mouseX = FXGL.getInput().getMouseXWorld();
                double mouseY = FXGL.getInput().getMouseYWorld();
                lehrerSchatten.setPosition(mouseX - 15, mouseY - 15);

                boolean kollision = kollidiert(mouseX, mouseY) || mouseX >= 960;

                Circle range   = (Circle)    lehrerSchatten.getViewComponent().getChildren().get(0);
                Rectangle body = (Rectangle) lehrerSchatten.getViewComponent().getChildren().get(1);

                if (kollision) {
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

        shopIcon1.setOnMouseReleased(e -> {
            if (lehrerSchatten != null) {
                double mouseX = FXGL.getInput().getMouseXWorld();
                double mouseY = FXGL.getInput().getMouseYWorld();

                if (!kollidiert(mouseX, mouseY) && mouseX < 960) {
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

    /**
     * Prüft ob Platzierung an (mouseX, mouseY) blockiert ist.
     *
     * Für Polygon-Hindernisse (Teich, Haus, PfadAußen...) wird ein echter
     * Ray-Casting-Test gemacht → pixelgenau, keine riesigen Bounding Boxes.
     *
     * Für Rechteck-Hindernisse (Baum, Busch) wird weiterhin Rectangle2D benutzt.
     */
    private boolean kollidiert(double mouseX, double mouseY) {
        // Wir testen alle 4 Ecken des Lehrers (30x30), nicht nur die Mitte
        double[][] ecken = {
                { mouseX - 15, mouseY - 15 },
                { mouseX + 15, mouseY - 15 },
                { mouseX - 15, mouseY + 15 },
                { mouseX + 15, mouseY + 15 },
        };

        for (Entity h : FXGL.getGameWorld().getEntitiesByType(EntityType.HINDERNIS)) {

            if (h.getProperties().exists("usePip")) {
                // --- POLYGON: Ray-Casting-Test ---
                // Die Punkte sind relativ zur Entity-Position gespeichert
                List<Double> pts = h.getObject("polygonPunkte");
                double ex = h.getX();
                double ey = h.getY();

                for (double[] ecke : ecken) {
                    if (punktInPolygon(ecke[0] - ex, ecke[1] - ey, pts)) {
                        return true;
                    }
                }

            } else {
                // --- RECHTECK: einfacher Box-Test ---
                double hx = h.getBoundingBoxComponent().getMinXWorld();
                double hy = h.getBoundingBoxComponent().getMinYWorld();
                double hw = h.getWidth()  > 0 ? h.getWidth()  : 50;
                double hh = h.getHeight() > 0 ? h.getHeight() : 50;

                Rectangle2D box = new Rectangle2D(hx - 4, hy - 4, hw + 8, hh + 8);
                Rectangle2D lehrerBox = new Rectangle2D(mouseX - 15, mouseY - 15, 30, 30);
                if (lehrerBox.intersects(box)) return true;
            }
        }

        // Andere Lehrer prüfen
        for (Entity anderer : FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
            if (anderer.getCenter().distance(mouseX, mouseY) < 35) return true;
        }

        return false;
    }

    /**
     * Ray-Casting-Algorithmus: Prüft ob Punkt (px, py) innerhalb eines Polygons liegt.
     * Das Polygon wird als flache Liste übergeben: x0, y0, x1, y1, ...
     *
     * Funktioniert auch bei konkaven Polygonen und Polygonen mit Rotation.
     */
    private boolean punktInPolygon(double px, double py, List<Double> pts) {
        int n = pts.size() / 2; // Anzahl der Eckpunkte
        boolean inside = false;

        int j = n - 1;
        for (int i = 0; i < n; i++) {
            double xi = pts.get(i * 2),     yi = pts.get(i * 2 + 1);
            double xj = pts.get(j * 2),     yj = pts.get(j * 2 + 1);

            // Prüft ob der Strahl von (px, py) nach rechts die Kante (xi,yi)→(xj,yj) schneidet
            boolean schneidet = ((yi > py) != (yj > py))
                    && (px < (xj - xi) * (py - yi) / (yj - yi) + xi);

            if (schneidet) inside = !inside;
            j = i;
        }

        return inside;
    }

    public static void main(String[] args) {
        launch(args);
    }
}