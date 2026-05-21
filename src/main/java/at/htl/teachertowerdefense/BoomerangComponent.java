package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class BoomerangComponent extends Component {

    private static final double SPEED = 300;

    private Entity  target;
    private int     damage;
    private Point2D turretPos;
    private boolean rueckweg = false;
    private boolean hatZielGetroffen = false;
    private final java.util.Set<Entity> rueckwegGetroffen = new java.util.HashSet<>();

    public BoomerangComponent(Entity target, int damage, Point2D turretPos) {
        this.target    = target;
        this.damage    = damage;
        this.turretPos = turretPos;
    }

    @Override
    public void onUpdate(double tpf) {
        if (!rueckweg) {
            if (target == null || !target.isActive()) {
                rueckweg = true;
                return;
            }
            Point2D ziel = new Point2D(target.getCenter().getX(), target.getY() - 10);
            Point2D pos  = entity.getCenter();
            Point2D dir  = ziel.subtract(pos).normalize();

            entity.translate(dir.getX() * SPEED * tpf * GameConfig.speedMulti,
                    dir.getY() * SPEED * tpf * GameConfig.speedMulti);

            if (pos.distance(ziel) < 20) {
                if (!hatZielGetroffen) {
                    target.getComponent(SchuelerComponent.class).damage(damage);
                    addPopsToLehrer(damage);
                    hatZielGetroffen = true;
                }
                rueckweg = true;
            }
        } else {
            Point2D pos = entity.getCenter();
            Point2D dir = turretPos.subtract(pos).normalize();
            entity.translate(dir.getX() * SPEED * tpf * GameConfig.speedMulti,
                    dir.getY() * SPEED * tpf * GameConfig.speedMulti);

            for (Entity s : FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER)) {
                if (s == target) continue;
                if (rueckwegGetroffen.contains(s)) continue;
                Point2D brustS = new Point2D(s.getCenter().getX(), s.getY() - 10);
                if (brustS.distance(pos) < 22) {
                    s.getComponent(SchuelerComponent.class).damage(damage);
                    addPopsToLehrer(damage);
                    rueckwegGetroffen.add(s);
                }
            }

            if (pos.distance(turretPos) < 20) {
                entity.removeFromWorld();
            }
        }
    }

    private void addPopsToLehrer(int dmg) {
        if (!entity.getProperties().exists("lehrer")) return;
        Entity lEnt = entity.getObject("lehrer");
        if (lEnt != null && lEnt.isActive() && lEnt.hasComponent(LehrerComponent.class)) {
            lEnt.getComponent(LehrerComponent.class).addPops(dmg);
        }
    }
}