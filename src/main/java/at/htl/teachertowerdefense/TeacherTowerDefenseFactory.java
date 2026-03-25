package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.WaypointMoveComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TeacherTowerDefenseFactory implements EntityFactory {

    // Sprite > Hitbox → sieht groß aus, passt trotzdem auf den Pfad
    private static final int HITBOX_KLEIN  = 28;
    private static final int HITBOX_MITTEL = 36;
    private static final int HITBOX_GROSS  = 44;

    @Spawns("Schueler")
    public Entity newSchueler(SpawnData data) {
        SchuelerTyp typ  = data.hasKey("typ")           ? data.get("typ")                : SchuelerTyp.TYP1;
        int startIndex   = data.hasKey("startWaypoint") ? (int) data.get("startWaypoint"): 0;

        List<Point2D> route = WaypointData.routeAbIndex(startIndex);

        int hitbox = switch (typ) {
            case TYP5, TYP6 -> HITBOX_MITTEL;
            case TYP7, TYP8 -> HITBOX_GROSS;
            default         -> HITBOX_KLEIN;
        };

        Entity schueler = FXGL.entityBuilder(data)
                .type(EntityType.SCHUELER)
                .view(schuelerView(typ))
                .bbox(new HitBox("body", new Point2D(0, 0), BoundingShape.box(hitbox, hitbox)))
                .collidable()
                .with(new SchuelerComponent(typ))
                .zIndex(100)
                .build();

        WaypointMoveComponent navi = new WaypointMoveComponent(typ.speed, route);
        navi.atDestinationProperty().addListener((obs, old, arrived) -> {
            if (arrived) { FXGL.inc("leben", -typ.lebenSchaden); schueler.removeFromWorld(); }
        });
        schueler.addComponent(navi);
        return schueler;
    }

    @Spawns("Lehrer1")
    public Entity newLehrer1(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.LEHRER)
                .viewWithBBox(lehrerView("Groebl.png", 48))
                .with(new LehrerComponent(0))
                .with(new TowerComponent(150, 1.0))
                .zIndex(10)
                .build();
    }

    @Spawns("Lehrer2")
    public Entity newLehrer2(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.LEHRER)
                .viewWithBBox(lehrerView("Feichtner.png", 48))
                .with(new LehrerComponent(1))
                .with(new TowerComponent(220, 1.8))
                .zIndex(10)
                .build();
    }

    @Spawns("Lehrer3")
    public Entity newLehrer3(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.LEHRER)
                .viewWithBBox(lehrerView("Winkler.png", 48))
                .with(new LehrerComponent(2))
                .with(new TowerComponent(100, 0.4))
                .zIndex(10)
                .build();
    }

    @Spawns("LehrerSchatten")
    public Entity newLehrerSchatten(SpawnData data) {
        Circle rangeCircle = new Circle(150, Color.color(1, 1, 1, 0.2));
        rangeCircle.setStroke(Color.WHITE);
        rangeCircle.setCenterX(24);
        rangeCircle.setCenterY(24);
        Rectangle body = new Rectangle(48, 48, Color.color(0, 0, 1, 0.5));
        return FXGL.entityBuilder(data)
                .view(rangeCircle)
                .viewWithBBox(body)
                .zIndex(100)
                .build();
    }

    // ============================================================
    // TEXTURE HELPER
    // ============================================================

    /** Lädt Schüler-Textur oder fällt auf gefärbtes Rechteck zurück */
    private Node schuelerView(SchuelerTyp typ) {
        String dateiname = switch (typ) {
            case TYP1 -> "Jonathan.png";
            case TYP2 -> "Gutmann.png";
            case TYP3 -> "Wojciech.png";
            case TYP4 -> "Maxi.png";
            case TYP5 -> "Toni.png";
            case TYP6 -> "Marko.png";
            default   -> null;
        };
        int spriteH = switch (typ) {
            case TYP5, TYP6 -> 72;
            case TYP7, TYP8 -> 88;
            default         -> 60;
        };
        int hitbox = switch (typ) {
            case TYP5, TYP6 -> HITBOX_MITTEL;
            case TYP7, TYP8 -> HITBOX_GROSS;
            default         -> HITBOX_KLEIN;
        };
        if (dateiname != null) {
            try {
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(FXGL.image(dateiname));
                iv.setFitHeight(spriteH);
                iv.setFitWidth(spriteH);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                // Sprite nach oben verschieben damit Füße auf Hitbox stehen
                iv.setTranslateY(-(spriteH - hitbox));
                return iv;
            } catch (Exception e) { /* Fallback */ }
        }
        return new Rectangle(hitbox, hitbox, typ.farbe);
    }

    /** Lädt Lehrer-Textur oder fällt auf blaues Rechteck zurück */
    private Node lehrerView(String dateiname, int groesse) {
        try {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(FXGL.image(dateiname));
            iv.setFitHeight(groesse); iv.setFitWidth(groesse);
            iv.setPreserveRatio(true); iv.setSmooth(false);
            return iv;
        } catch (Exception e) {
            return new Rectangle(groesse, groesse, Color.BLUE);
        }
    }

    // ── Groebl: Fisch-Boomerang ──────────────────────────────────
    @Spawns("ProjektilBoomerang")
    public Entity newProjektilBoomerang(SpawnData data) {
        Entity  target = data.get("target");
        int     damage = data.hasKey("damage") ? (int) data.get("damage") : 1;
        Point2D turret = new Point2D(data.getX(), data.getY());

        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(FXGL.image("projektil_fisch.png"));
        view.setFitWidth(64); view.setFitHeight(64);
        view.setPreserveRatio(true); view.setSmooth(false);

        javafx.animation.RotateTransition rot = new javafx.animation.RotateTransition(
                javafx.util.Duration.millis(600), view);
        rot.setByAngle(360); rot.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rot.play();

        return FXGL.entityBuilder(data)
                .type(EntityType.PROJEKTIL)
                .view(view)
                .bbox(new HitBox("body", new Point2D(-12, -12), BoundingShape.circle(12)))
                .with(new BoomerangComponent(target, damage, turret))
                .zIndex(150)
                .build();
    }

    // ── Feichtner: Alchemisten-Potion ────────────────────────────
    @Spawns("ProjektilPotion")
    public Entity newProjektilPotion(SpawnData data) {
        Entity  target   = data.get("target");
        int     damage   = data.hasKey("damage") ? (int) data.get("damage") : 1;
        Point2D startPos = target != null && target.isActive()
                ? target.getCenter()
                : new Point2D(data.getX(), data.getY());

        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(FXGL.image("projektil_potion.png"));
        view.setFitWidth(32); view.setFitHeight(32);
        view.setPreserveRatio(true); view.setSmooth(false);

        return FXGL.entityBuilder(data)
                .type(EntityType.PROJEKTIL)
                .view(view)
                .bbox(new HitBox("body", new Point2D(-10, -10), BoundingShape.circle(10)))
                .with(new PotionComponent(target, damage, startPos))
                .zIndex(150)
                .build();
    }

    // ── Winkler: Floppy Disk ─────────────────────────────────────
    @Spawns("ProjektilFloppy")
    public Entity newProjektilFloppy(SpawnData data) {
        Entity target = data.get("target");
        int    damage = data.hasKey("damage") ? (int) data.get("damage") : 1;

        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(FXGL.image("projektil_floppy.png"));
        view.setFitWidth(28); view.setFitHeight(28);
        view.setPreserveRatio(true); view.setSmooth(false);

        javafx.animation.RotateTransition rot = new javafx.animation.RotateTransition(
                javafx.util.Duration.millis(300), view);
        rot.setByAngle(360); rot.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rot.play();

        return FXGL.entityBuilder(data)
                .type(EntityType.PROJEKTIL)
                .view(view)
                .bbox(new HitBox("body", new Point2D(-10, -10), BoundingShape.circle(10)))
                .with(new FloppyComponent(target, damage))
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

    // ── City.tmx Hindernisse ─────────────────────────────────────
    @Spawns("HausFront")      public Entity newHausFront(SpawnData d)      { return baueHindernis(d); }
    @Spawns("Dach")           public Entity newDach(SpawnData d)           { return baueHindernis(d); }
    @Spawns("FoodTruck")      public Entity newFoodTruck(SpawnData d)      { return baueHindernis(d); }
    @Spawns("Bank")           public Entity newBank(SpawnData d)           { return baueHindernis(d); }
    @Spawns("Laterne")        public Entity newLaterne(SpawnData d)        { return baueHindernis(d); }
    @Spawns("Statue")         public Entity newStatue(SpawnData d)         { return baueHindernis(d); }
    @Spawns("Brunnen")        public Entity newBrunnen(SpawnData d)        { return baueHindernis(d); }
    @Spawns("Hydrant")        public Entity newHydrant(SpawnData d)        { return baueHindernis(d); }
    @Spawns("Bushaltestelle") public Entity newBushaltestelle(SpawnData d) { return baueHindernis(d); }
    @Spawns("Postkastl")      public Entity newPostkastl(SpawnData d)      { return baueHindernis(d); }
    @Spawns("Ampel")          public Entity newAmpel(SpawnData d)          { return baueHindernis(d); }
    @Spawns("Parkzähler")     public Entity newParkzaehler(SpawnData d)    { return baueHindernis(d); }
    @Spawns("Klima")          public Entity newKlima(SpawnData d)          { return baueHindernis(d); }
    @Spawns("Hintergarten")   public Entity newHintergarten(SpawnData d)   { return baueHindernisOhneSchuss(d); }
    @Spawns("Gemuese")        public Entity newGemuese(SpawnData d)        { return baueHindernisOhneSchuss(d); }
    @Spawns("Mistkübel")      public Entity newMistkuebel(SpawnData d)     { return baueHindernis(d); }

    // PFadAussen (City.tmx schreibt es mit großem F)
    @Spawns("PFadAussen")
    public Entity newPFadAussen(SpawnData data) {
        Entity e = baueHindernis(data);
        e.setProperty("usePip", true);
        e.setProperty("blockiertSchuss", false);
        return e;
    }

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