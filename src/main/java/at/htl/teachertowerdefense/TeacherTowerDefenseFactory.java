package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.shape.Rectangle;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import com.almasb.fxgl.dsl.components.WaypointMoveComponent;
import java.util.List;


public class TeacherTowerDefenseFactory implements EntityFactory {

    @Spawns("Ziel")
    public Entity newZiel(SpawnData data) { return new Entity(); }

    @Spawns("Pfad")
    public Entity newPfad(SpawnData data) { return new Entity(); }

    // --- Ab hier: Leere Pläne für deine Dekorationen, damit FXGL nicht abstürzt ---

    @Spawns("Teich")
    public Entity newTeich(SpawnData data) { return new Entity(); }

    @Spawns("Haus")
    public Entity newHaus(SpawnData data) { return new Entity(); }

    @Spawns("KleinHaus")
    public Entity newKleinHaus(SpawnData data) { return new Entity(); }

    @Spawns("KleinHausEimer")
    public Entity newKleinHausEimer(SpawnData data) { return new Entity(); }

    @Spawns("Tent")
    public Entity newTent(SpawnData data) { return new Entity(); }

    @Spawns("Baum")
    public Entity newBaum(SpawnData data) { return new Entity(); }

    @Spawns("Busch")
    public Entity newBusch(SpawnData data) { return new Entity(); }

    @Spawns("Schueler")
    public Entity newSchueler(SpawnData data) {

        // Deine exakte Zickzack-Linie aus Tiled
        List<Point2D> wegpunkte = List.of(
                new Point2D(2.67, 519.33),
                new Point2D(60.00, 518.67),
                new Point2D(88.00, 501.33),
                new Point2D(200.67, 501.33),
                new Point2D(228.00, 488.00),
                new Point2D(350.00, 485.33),
                new Point2D(376.00, 474.67),
                new Point2D(412.00, 472.00),
                new Point2D(437.33, 472.00),
                new Point2D(450.00, 455.33),
                new Point2D(454.67, 438.67),
                new Point2D(455.33, 418.00),
                new Point2D(456.00, 334.67),
                new Point2D(461.33, 327.33),
                new Point2D(474.00, 320.67),
                new Point2D(488.00, 312.67),
                new Point2D(550.00, 310.67),
                new Point2D(622.67, 310.00),
                new Point2D(631.33, 300.67),
                new Point2D(641.33, 293.33),
                new Point2D(661.33, 291.33),
                new Point2D(687.33, 289.33),
                new Point2D(715.33, 289.33),
                new Point2D(733.33, 291.33),
                new Point2D(748.00, 291.33),
                new Point2D(768.00, 294.67),
                new Point2D(796.67, 310.67),
                new Point2D(880.67, 310.00),
                new Point2D(902.00, 326.00),
                new Point2D(951.33, 325.33),
                new Point2D(960.00, 325.33)
        );

        // Wir bauen uns eine perfekte, korrigierte Route!
        List<Point2D> finaleRoute = new java.util.ArrayList<>();

        // Wir ignorieren die ungenaue Ecke der Tiled-Box komplett!
        // Wir nehmen direkt deine Wegpunkte und zentrieren sie (-10 Pixel).
        // Das Navi setzt den Schüler dann beim Start automatisch exakt auf den ersten Punkt!
        for (Point2D p : wegpunkte) {
            finaleRoute.add(p.subtract(10, 10));
        }

        // Wir bauen den Schüler zuerst (ohne das Navi)
        Entity schueler = FXGL.entityBuilder(data)
                .at(finaleRoute.get(0))
                .viewWithBBox(new Rectangle(20, 20, Color.RED))
                .zIndex(100)
                .build();

        // Wir erstellen das Navi als eigenständiges Teil
        WaypointMoveComponent navi = new WaypointMoveComponent(100, finaleRoute);

        // Wir sagen dem Navi: Sag Bescheid, wenn du am Ziel bist!
        navi.atDestinationProperty().addListener((obs, alterWert, angekommen) -> {
            if (angekommen) {
                FXGL.inc("leben", -1); // Zieht 1 Leben ab
                schueler.removeFromWorld(); // Löscht den Schüler vom Bildschirm
            }
        });

        // Wir stecken dem Schüler das Navi an
        schueler.addComponent(navi);

        return schueler;
    }




    @Spawns("")
    public Entity newEmpty(SpawnData data) {
        // Ignoriert alle Objekte in Tiled, bei denen du vergessen hast, einen Namen einzutragen
        return new Entity();
    }

    @Spawns("Spawn")
    public Entity newSpawn(SpawnData data) {
        // Der geniale Trick: Sobald FXGL die Map lädt und das "Spawn"-Feld findet,
        // geben wir sofort den Befehl, exakt auf diesen X- und Y-Koordinaten einen Schüler zu platzieren!
        FXGL.spawn("Schueler", data.getX(), data.getY());

        return new Entity(); // Der Spawn-Punkt selbst bleibt unsichtbar
    }
}