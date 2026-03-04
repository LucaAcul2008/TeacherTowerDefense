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

        // Erlaubt das Vergrößern des Fensters
        settings.setManualResizeEnabled(true);


    }

    @Override
    protected void initGame() {
        // Der Tarn-Trick: Färbt den Hintergrund exakt in der Rasen-Farbe deines Tilesets


        FXGL.getGameWorld().addEntityFactory(new TeacherTowerDefenseFactory());
        FXGL.setLevelFromMap("Map1.tmx");
    }

    public static void main(String[] args) {
        launch(args);
    }
}