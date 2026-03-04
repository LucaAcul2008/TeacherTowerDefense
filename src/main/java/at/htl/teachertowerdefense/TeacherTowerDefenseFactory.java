package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;


import java.awt.*;

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
        return FXGL.entityBuilder(data)
                // Zeichnet ein 20x20 Pixel großes, rotes Quadrat als Platzhalter
                .viewWithBBox(new Rectangle(20, 20, Color.RED))
                .build();
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