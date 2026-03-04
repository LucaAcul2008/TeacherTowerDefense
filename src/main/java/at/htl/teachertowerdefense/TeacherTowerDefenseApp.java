package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TeacherTowerDefenseApp extends GameApplication {

    // 1. Wir definieren Typen für unsere Objekte, damit sie sich später erkennen
    public enum EntityType {
        TEACHER, STUDENT, PROJECTILE
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(960);
        settings.setHeight(640);
        settings.setTitle("Teacher Tower Defense");
        settings.setVersion("0.1");
    }

    @Override
    protected void initGame() {
        // 2. Wir machen den Hintergrund hellgrau, das sieht freundlicher aus als schwarz
        FXGL.getGameScene().setBackgroundColor(Color.LIGHTGREY);

        // 3. Wir bauen unseren ersten Lehrer (z.B. den Mathelehrer)
        Entity mathTeacher = FXGL.entityBuilder()
                .type(EntityType.TEACHER)           // Er ist vom Typ LEHRER
                .at(400, 300)                       // Position: x=400, y=300 (Mitte)
                .view(new Rectangle(40, 40, Color.BLUE)) // Aussehen: Ein 40x40 blaues Quadrat
                .buildAndAttach();                  // Bauen und aufs Spielfeld setzen!
    }

    public static void main(String[] args) {
        launch(args);
    }
}