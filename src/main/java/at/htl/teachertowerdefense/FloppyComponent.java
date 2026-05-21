package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class FloppyComponent extends Component {

    private static final double SPEED = 500;

    private Entity  target;
    private int     damage;
    private Point2D direction;

    public FloppyComponent(Entity target, int damage) {
        this.target = target;
        this.damage = damage;
    }

    @Override
    public void onAdded() {
        if (target != null && target.isActive()) {
            direction = target.getCenter().subtract(entity.getCenter()).normalize();
        } else {
            direction = new Point2D(1, 0);
        }
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translate(direction.getX() * SPEED * tpf * GameConfig.speedMulti,
                direction.getY() * SPEED * tpf * GameConfig.speedMulti);

        if (target != null && target.isActive()) {
            if (entity.getCenter().distance(target.getCenter()) < 18) {
                target.getComponent(SchuelerComponent.class).damage(damage);
                addPopsToLehrer(damage);
                entity.removeFromWorld();
                return;
            }
        }

        double x = entity.getX(), y = entity.getY();
        if (x < -50 || x > 1100 || y < -50 || y > 750) {
            entity.removeFromWorld();
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