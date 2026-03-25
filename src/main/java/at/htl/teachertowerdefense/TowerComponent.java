package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TowerComponent extends Component {

    private final double baseRange;
    private final double baseShootDelay;
    private LocalTimer shootTimer;

    public TowerComponent(double range, double shootDelay) {
        this.baseRange      = range;
        this.baseShootDelay = shootDelay;
    }

    @Override
    public void onAdded() {
        shootTimer = FXGL.newLocalTimer();
        shootTimer.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        LehrerComponent lc = entity.hasComponent(LehrerComponent.class)
                ? entity.getComponent(LehrerComponent.class) : null;

        double range      = lc != null ? lc.getRange()      : baseRange;
        double shootDelay = lc != null ? lc.getShootDelay() : baseShootDelay;
        int    damage     = lc != null ? lc.getDamage()     : 1;
        int    maxTargets = lc != null ? lc.getMultiTarget(): 1;

        if (!shootTimer.elapsed(Duration.seconds(shootDelay))) return;

        List<Entity> inRange = new ArrayList<>();
        for (Entity e : FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER)) {
            if (e.distance(entity) <= range && !wirdBlockiert(entity.getCenter(), e.getCenter())) {
                inRange.add(e);
            }
        }

        if (inRange.isEmpty()) return;

        inRange.sort(Comparator.comparingDouble(e -> e.distance(entity)));

        // Projektil-Typ je nach Lehrer
        String projektilTyp = "ProjektilFloppy"; // Winkler default
        if (lc != null) {
            projektilTyp = switch (lc.getLehrerTyp()) {
                case 0 -> "ProjektilBoomerang"; // Groebl → Fisch-Boomerang
                case 1 -> "ProjektilPotion";    // Feichtner → AoE Potion
                default -> "ProjektilFloppy";   // Winkler → Floppy Disk
            };
        }

        int schuesse = Math.min(maxTargets, inRange.size());
        for (int i = 0; i < schuesse; i++) {
            FXGL.spawn(projektilTyp,
                    new SpawnData(entity.getCenter().getX(), entity.getCenter().getY())
                            .put("target", inRange.get(i))
                            .put("damage", damage)
                            .put("spezial", lc != null && lc.isSpezialProjektil())
            );
        }
        shootTimer.capture();
    }

    // ============================================================
    // SICHTLINIEN-CHECK
    // Nur Hindernisse mit blockiertSchuss=true UND NICHT usePip blockieren!
    // ============================================================

    private boolean wirdBlockiert(Point2D von, Point2D bis) {
        for (Entity h : FXGL.getGameWorld().getEntitiesByType(EntityType.HINDERNIS)) {
            // usePip-Hindernisse (Teich, PfadAussen) blockieren NICHT
            if (h.getProperties().exists("usePip")) continue;
            // Nur explizit als blockierend markierte Hindernisse
            if (!h.getProperties().exists("blockiertSchuss")) continue;
            if (!h.getBoolean("blockiertSchuss")) continue;

            double hx = h.getBoundingBoxComponent().getMinXWorld();
            double hy = h.getBoundingBoxComponent().getMinYWorld();
            double hw = h.getWidth()  > 0 ? h.getWidth()  : 50;
            double hh = h.getHeight() > 0 ? h.getHeight() : 50;

            if (linieSchneidetRechteck(von, bis, hx, hy, hw, hh)) return true;
        }
        return false;
    }

    private boolean linieSchneidetRechteck(Point2D von, Point2D bis,
                                           double rx, double ry, double rw, double rh) {
        if (punktInRechteck(von, rx, ry, rw, rh)) return true;
        if (punktInRechteck(bis, rx, ry, rw, rh)) return true;
        return linieSchneidetLinie(von, bis, new Point2D(rx,    ry),    new Point2D(rx+rw, ry))
                || linieSchneidetLinie(von, bis, new Point2D(rx,    ry+rh), new Point2D(rx+rw, ry+rh))
                || linieSchneidetLinie(von, bis, new Point2D(rx,    ry),    new Point2D(rx,    ry+rh))
                || linieSchneidetLinie(von, bis, new Point2D(rx+rw, ry),    new Point2D(rx+rw, ry+rh));
    }

    private boolean punktInRechteck(Point2D p, double rx, double ry, double rw, double rh) {
        return p.getX() >= rx && p.getX() <= rx+rw && p.getY() >= ry && p.getY() <= ry+rh;
    }

    private boolean linieSchneidetLinie(Point2D a1, Point2D a2, Point2D b1, Point2D b2) {
        double d1x = a2.getX()-a1.getX(), d1y = a2.getY()-a1.getY();
        double d2x = b2.getX()-b1.getX(), d2y = b2.getY()-b1.getY();
        double cross = d1x*d2y - d1y*d2x;
        if (Math.abs(cross) < 1e-10) return false;
        double t = ((b1.getX()-a1.getX())*d2y - (b1.getY()-a1.getY())*d2x) / cross;
        double u = ((b1.getX()-a1.getX())*d1y - (b1.getY()-a1.getY())*d1x) / cross;
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
}