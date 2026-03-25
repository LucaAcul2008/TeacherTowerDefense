package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Feichtner's Alchemisten-Potion:
 * Verfolgt das Ziel solange es lebt.
 * Explodiert an der letzten bekannten Position → AoE-Schaden.
 */
public class PotionComponent extends Component {

    private static final double SPEED      = 260;
    private static final double AOE_RADIUS = 65;

    private Entity  target;
    private int     damage;
    private Point2D letzteZielPos; // wird jedes Frame aktualisiert

    public PotionComponent(Entity target, int damage, Point2D startZiel) {
        this.target       = target;
        this.damage       = damage;
        this.letzteZielPos = startZiel;
    }

    @Override
    public void onUpdate(double tpf) {
        // Zielposition live aktualisieren solange Schüler noch aktiv
        if (target != null && target.isActive()) {
            letzteZielPos = target.getCenter();
        }

        Point2D pos = entity.getCenter();
        Point2D dir = letzteZielPos.subtract(pos);
        double  dist = dir.magnitude();

        if (dist < 14) {
            explodieren(pos);
            return;
        }

        Point2D norm = dir.normalize();
        double  step = Math.min(SPEED * tpf, dist); // nicht überschießen
        entity.translate(norm.getX() * step, norm.getY() * step);
    }

    private void explodieren(Point2D pos) {
        // Alle Schüler im AoE-Radius treffen
        for (Entity s : FXGL.getGameWorld().getEntitiesByType(EntityType.SCHUELER)) {
            if (s.getCenter().distance(pos) <= AOE_RADIUS) {
                s.getComponent(SchuelerComponent.class).damage(damage);
            }
        }

        // Explosions-Effekt
        Circle kreis = new Circle(AOE_RADIUS, Color.color(0.5, 0.0, 0.8, 0.5));
        kreis.setStroke(Color.color(0.8, 0.2, 1.0, 0.9));
        kreis.setStrokeWidth(2);

        Entity explosion = FXGL.entityBuilder()
                .at(pos.getX(), pos.getY())
                .view(kreis)
                .zIndex(200)
                .buildAndAttach();

        // Explosion faden lassen
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                Duration.millis(350), kreis);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> { if (explosion.isActive()) explosion.removeFromWorld(); });
        fade.play();

        entity.removeFromWorld();
    }
}