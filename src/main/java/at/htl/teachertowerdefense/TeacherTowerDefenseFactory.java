package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.WaypointMoveComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TeacherTowerDefenseFactory implements EntityFactory {

    @Spawns("Schueler")
    public Entity newSchueler(SpawnData data) {
        SchuelerTyp typ  = data.hasKey("typ")           ? data.get("typ")                : SchuelerTyp.TYP1;
        int startIndex   = data.hasKey("startWaypoint") ? (int) data.get("startWaypoint"): 0;

        List<Point2D> route = WaypointData.routeAbIndex(startIndex);
        int g = typ.groesse;

        Entity schueler = FXGL.entityBuilder(data)
                .type(EntityType.SCHUELER)
                .viewWithBBox(new Rectangle(g, g, typ.farbe))
                .collidable()
                .with(new SchuelerComponent(typ))
                .zIndex(100)
                .build();

        WaypointMoveComponent navi = new WaypointMoveComponent(typ.speed, route);
        navi.atDestinationProperty().addListener((obs, old, arrived) -> {
            if (arrived) { FXGL.inc("leben", -1); schueler.removeFromWorld(); }
        });
        schueler.addComponent(navi);
        return schueler;
    }

    @Spawns("Lehrer1")
    public Entity newLehrer1(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.LEHRER)
                .viewWithBBox(new Rectangle(30, 30, Color.BLUE))
                .with(new LehrerComponent())
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
                .view(rangeCircle)
                .viewWithBBox(body)
                .zIndex(100)
                .build();
    }

    @Spawns("Projektil")
    public Entity newProjektil(SpawnData data) {
        Entity target   = data.get("target");
        boolean spezial = data.hasKey("spezial") && (boolean) data.get("spezial");
        Color farbe     = spezial ? Color.CYAN : Color.YELLOW;
        return FXGL.entityBuilder(data)
                .type(EntityType.PROJEKTIL)
                .viewWithBBox(new Circle(spezial ? 8 : 5, farbe))
                .collidable()
                .with(new HomingComponent(target, 400))
                .zIndex(150)
                .build();
    }

    @Spawns("RangeIndicator")
    public Entity newRangeIndicator(SpawnData data) {
        double range = data.get("range");
        Circle c = new Circle(range, Color.color(1, 1, 0, 0.15));
        c.setStroke(Color.color(1, 1, 0, 0.6));
        c.setStrokeWidth(2);
        // FIX: Circle center auf (0,0) → Entity-Position ist der Mittelpunkt
        c.setCenterX(0);
        c.setCenterY(0);
        return FXGL.entityBuilder(data)
                .view(c)
                .zIndex(5)
                .build();
    }

    @Spawns("Pfad")
    public Entity newPfad(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.PFAD).build();
    }

    @Spawns("PfadAussen")
    public Entity newPfadAussen(SpawnData data) {
        Entity e = baueHindernis(data);
        e.setProperty("usePip", true);
        e.setProperty("blockiertSchuss", false); // ← FIX: Projektile fliegen über PfadAussen
        return e;
    }

    @Spawns("Teich")          public Entity newTeich(SpawnData d)          { return baueHindernisOhneSchuss(d); }
    @Spawns("Haus")           public Entity newHaus(SpawnData d)           { return baueHindernis(d); }
    @Spawns("KleinHaus")      public Entity newKleinHaus(SpawnData d)      { return baueHindernis(d); }
    @Spawns("KleinHausEimer") public Entity newKleinHausEimer(SpawnData d) { return baueHindernis(d); }
    @Spawns("Tent")           public Entity newTent(SpawnData d)           { return baueHindernis(d); }
    @Spawns("Baum")           public Entity newBaum(SpawnData d)           { return baueHindernis(d); }
    @Spawns("Busch")          public Entity newBusch(SpawnData d)          { return baueHindernis(d); }

    @Spawns("Spawn,Ziel,")
    public Entity newEmpty(SpawnData data) { return new Entity(); }

    // ============================================================
    // HILFSMETHODEN
    // ============================================================

    /** Hindernis das Lehrer-Platzierung UND Schüsse blockiert */
    private Entity baueHindernis(SpawnData data) {
        Entity e = baueHindernisBase(data);
        e.setProperty("blockiertSchuss", true);
        return e;
    }

    /** Hindernis das Lehrer-Platzierung blockiert aber Schüsse NICHT (z.B. Teich) */
    private Entity baueHindernisOhneSchuss(SpawnData data) {
        Entity e = baueHindernisBase(data);
        e.setProperty("blockiertSchuss", false);
        return e;
    }

    private Entity baueHindernisBase(SpawnData data) {
        double offsetX = 0, offsetY = 0, w, h;
        List<Double> polygonPunkte = null;

        if (data.hasKey("polygon")) {
            Polygon p = data.get("polygon");
            polygonPunkte = new ArrayList<>(p.getPoints());
            double[] bbox = berechneBBox(polygonPunkte);
            offsetX = bbox[0]; offsetY = bbox[1]; w = bbox[2]; h = bbox[3];
        } else if (data.hasKey("polyline")) {
            Polyline pl = data.get("polyline");
            polygonPunkte = new ArrayList<>(pl.getPoints());
            double[] bbox = berechneBBox(polygonPunkte);
            offsetX = bbox[0]; offsetY = bbox[1]; w = bbox[2]; h = bbox[3];
        } else {
            w = data.hasKey("width")  ? Double.parseDouble(data.get("width").toString())  : 50;
            h = data.hasKey("height") ? Double.parseDouble(data.get("height").toString()) : 50;
        }

        Entity e = FXGL.entityBuilder(data)
                .type(EntityType.HINDERNIS)
                .bbox(new HitBox(new Point2D(offsetX, offsetY), BoundingShape.box(w, h)))
                .collidable()
                .build();

        if (polygonPunkte != null) e.setProperty("polygonPunkte", polygonPunkte);
        return e;
    }

    private double[] berechneBBox(List<Double> coords) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i+1 < coords.size(); i += 2) {
            double px = coords.get(i), py = coords.get(i+1);
            if (px < minX) minX = px; if (py < minY) minY = py;
            if (px > maxX) maxX = px; if (py > maxY) maxY = py;
        }
        return new double[]{ minX, minY, maxX-minX, maxY-minY };
    }
}