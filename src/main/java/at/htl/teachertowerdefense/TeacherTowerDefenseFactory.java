package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.dsl.components.WaypointMoveComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;

import java.util.ArrayList;
import java.util.List;

public class TeacherTowerDefenseFactory implements EntityFactory {

    @Spawns("Schueler")
    public Entity newSchueler(SpawnData data) {
        List<Point2D> wegpunkte = List.of(
                new Point2D(2.67, 519.33), new Point2D(60.00, 518.67), new Point2D(88.00, 501.33),
                new Point2D(200.67, 501.33), new Point2D(228.00, 488.00), new Point2D(350.00, 485.33),
                new Point2D(376.00, 474.67), new Point2D(412.00, 472.00), new Point2D(437.33, 472.00),
                new Point2D(450.00, 455.33), new Point2D(454.67, 438.67), new Point2D(455.33, 418.00),
                new Point2D(456.00, 334.67), new Point2D(461.33, 327.33), new Point2D(474.00, 320.67),
                new Point2D(488.00, 312.67), new Point2D(550.00, 310.67), new Point2D(622.67, 310.00),
                new Point2D(631.33, 300.67), new Point2D(641.33, 293.33), new Point2D(661.33, 291.33),
                new Point2D(687.33, 289.33), new Point2D(715.33, 289.33), new Point2D(733.33, 291.33),
                new Point2D(748.00, 291.33), new Point2D(768.00, 294.67), new Point2D(796.67, 310.67),
                new Point2D(880.67, 310.00), new Point2D(902.00, 326.00), new Point2D(951.33, 325.33),
                new Point2D(960.00, 325.33)
        );

        List<Point2D> finaleRoute = new ArrayList<>();
        for (Point2D p : wegpunkte) { finaleRoute.add(p.subtract(10, 10)); }

        Entity schueler = FXGL.entityBuilder(data)
                .type(EntityType.SCHUELER)
                .at(finaleRoute.get(0))
                .viewWithBBox(new Rectangle(20, 20, Color.RED))
                .collidable() // WICHTIG: Damit Projektile ihn treffen können!
                .with(new HealthIntComponent(3)) // NEU: Der Schüler hält 3 Treffer aus
                .zIndex(100)
                .build();

        WaypointMoveComponent navi = new WaypointMoveComponent(100, finaleRoute);
        navi.atDestinationProperty().addListener((obs, old, arrived) -> {
            if (arrived) {
                FXGL.inc("leben", -1);
                schueler.removeFromWorld();
            }
        });

        schueler.addComponent(navi);
        return schueler;
    }

    @Spawns("Lehrer1")
    public Entity newLehrer1(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.LEHRER)
                .viewWithBBox(new Rectangle(30, 30, Color.BLUE))
                // NEU: Reichweite 150 Pixel, wirft alle 1.0 Sekunden
                .with(new TowerComponent(150, 1.0))
                .zIndex(10)
                .build();
    }

    @Spawns("LehrerSchatten")
    public Entity newLehrerSchatten(SpawnData data) {

        Circle rangeCircle = new Circle(150, Color.color(1, 1, 1, 0.2));
        rangeCircle.setStroke(Color.WHITE);
        rangeCircle.setCenterX(15);
        rangeCircle.setCenterY(15);


        Rectangle body = new Rectangle(30, 30, Color.color(0, 0, 1, 0.5));

        return FXGL.entityBuilder(data)
                .view(rangeCircle)     // Fügt den Kreis hinzu
                .viewWithBBox(body)    // Fügt den Körper inkl. Hitbox hinzu
                .zIndex(100)
                .build();
    }

    @Spawns("Projektil")
    public Entity newProjektil(SpawnData data) {
        Entity target = data.get("target");

        return FXGL.entityBuilder(data)
                .type(EntityType.PROJEKTIL)
                .viewWithBBox(new Circle(5, Color.YELLOW))
                .collidable()
                .with(new HomingComponent(target, 400))
                .zIndex(150)
                .build();
    }




    // --- DIE ZUWEISUNGEN ---
    @Spawns("Pfad")
    public Entity newPfad(SpawnData data) { return baueHindernis(data); }

    @Spawns("Teich")
    public Entity newTeich(SpawnData data) { return baueHindernis(data); }

    @Spawns("Haus")
    public Entity newHaus(SpawnData data) { return baueHindernis(data); }

    @Spawns("KleinHaus")
    public Entity newKleinHaus(SpawnData data) { return baueHindernis(data); }

    @Spawns("KleinHausEimer")
    public Entity newKleinHausEimer(SpawnData data) { return baueHindernis(data); }

    @Spawns("Tent")
    public Entity newTent(SpawnData data) { return baueHindernis(data); }

    @Spawns("Baum")
    public Entity newBaum(SpawnData data) { return baueHindernis(data); }

    @Spawns("Busch")
    public Entity newBusch(SpawnData data) { return baueHindernis(data); }

    private Entity baueHindernis(SpawnData data) {
        double w = data.hasKey("width") ? Double.parseDouble(data.get("width").toString()) : 50;
        double h = data.hasKey("height") ? Double.parseDouble(data.get("height").toString()) : 50;

        return FXGL.entityBuilder(data)
                .type(EntityType.HINDERNIS)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .collidable()
                .build();
    }


    // --- LOGIK ---
    @Spawns("Spawn,Ziel,")
    public Entity newEmpty(SpawnData data) { return new Entity(); }
}