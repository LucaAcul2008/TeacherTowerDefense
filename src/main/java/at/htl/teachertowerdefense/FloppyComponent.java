package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

/**
 * Winkler's Floppy Disk:
 * Sehr schnelles Single-Target Projektil.
 * Fliegt geradeaus in Richtung Ziel und trifft es einmalig.
 */
public class FloppyComponent extends Component {

    private static final double SPEED = 500; // sehr schnell

    private Entity  target;
    private int     damage;
    private Point2D direction; // feste Flugrichtung beim Spawn

    public FloppyComponent(Entity target, int damage) {
        this.target    = target;
        this.damage    = damage;
    }

    @Override
    public void onAdded() {
        // Richtung beim Spawn einmalig berechnen (fliegt dann geradeaus)
        if (target != null && target.isActive()) {
            direction = target.getCenter()
                    .subtract(entity.getCenter())
                    .normalize();
        } else {
            direction = new Point2D(1, 0);
        }
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translate(direction.getX() * SPEED * tpf,
                         direction.getY() * SPEED * tpf);

        // Ziel getroffen?
        if (target != null && target.isActive()) {
            if (entity.getCenter().distance(target.getCenter()) < 18) {
                target.getComponent(SchuelerComponent.class).damage(damage);
                entity.removeFromWorld();
                return;
            }
        }

        // Aus dem Spielfeld → entfernen
        double x = entity.getX(), y = entity.getY();
        if (x < -50 || x > 1100 || y < -50 || y > 750) {
            entity.removeFromWorld();
        }
    }
}
