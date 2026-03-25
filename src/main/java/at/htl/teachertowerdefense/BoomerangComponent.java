package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

/**
 * Groebl's Fisch-Boomerang:
 * Fliegt zum Ziel, trifft es, kehrt dann zum Turm zurück
 * und trifft dabei alle Schüler auf dem Rückweg.
 */
public class BoomerangComponent extends Component {

    private static final double SPEED = 300;

    private Entity  target;
    private int     damage;
    private Point2D turretPos;   // Ursprungsposition zum Zurückfliegen
    private boolean rueckweg = false;
    private boolean hatZielGetroffen = false;

    public BoomerangComponent(Entity target, int damage, Point2D turretPos) {
        this.target     = target;
        this.damage     = damage;
        this.turretPos  = turretPos;
    }

    @Override
    public void onUpdate(double tpf) {
        if (!rueckweg) {
            if (target == null || !target.isActive()) {
                rueckweg = true;
                return;
            }
            // Brust = Y-Position des Schülers (oberkante Hitbox) minus etwas nach oben
            // Sprite geht über Hitbox hinaus → Brust liegt ca bei entity.getY() - 10
            Point2D ziel = new Point2D(target.getCenter().getX(), target.getY() - 10);
            Point2D pos  = entity.getCenter();
            Point2D dir  = ziel.subtract(pos).normalize();

            entity.translate(dir.getX() * SPEED * tpf, dir.getY() * SPEED * tpf);

            if (pos.distance(ziel) < 20) {
                if (!hatZielGetroffen) {
                    target.getComponent(SchuelerComponent.class).damage(damage);
                    hatZielGetroffen = true;
                }
                rueckweg = true;
            }
        } else {
            Point2D pos = entity.getCenter();
            Point2D dir = turretPos.subtract(pos).normalize();
            entity.translate(dir.getX() * SPEED * tpf, dir.getY() * SPEED * tpf);

            for (Entity s : FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER)) {
                if (s == target) continue;
                // Auch auf dem Rückweg auf Brusthöhe treffen
                Point2D brustS = new Point2D(s.getCenter().getX(), s.getY() - 10);
                if (brustS.distance(pos) < 22) {
                    s.getComponent(SchuelerComponent.class).damage(damage);
                }
            }

            if (pos.distance(turretPos) < 20) {
                entity.removeFromWorld();
            }
        }
    }
}