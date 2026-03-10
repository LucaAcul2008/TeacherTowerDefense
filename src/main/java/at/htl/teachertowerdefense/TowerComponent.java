package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

public class TowerComponent extends Component {
    private double range;
    private double shootDelay;
    private LocalTimer shootTimer;

    // Konstruktor: Jeder Lehrer kann eine eigene Reichweite und Schuss-Geschwindigkeit haben!
    public TowerComponent(double range, double shootDelay) {
        this.range = range;
        this.shootDelay = shootDelay;
    }

    @Override
    public void onAdded() {
        shootTimer = FXGL.newLocalTimer();
        shootTimer.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        // Wenn die Nachladezeit abgelaufen ist...
        if (shootTimer.elapsed(Duration.seconds(shootDelay))) {

            // ...suche den nächsten Schüler in Reichweite!
            Entity target = FXGL.getGameWorld()
                    .getClosestEntity(entity, e -> e.isType(EntityType.SCHUELER) && e.distance(entity) <= range)
                    .orElse(null);

            // Wenn ein Schüler gefunden wurde: FEUER!
            if (target != null) {
                shoot(target);
                shootTimer.capture(); // Timer zurücksetzen
            }
        }
    }

    private void shoot(Entity target) {
        // Spawnt ein Projektil exakt in der Mitte des Lehrers und übergibt das Ziel
        FXGL.spawn("Projektil",
                new SpawnData(entity.getCenter().getX(), entity.getCenter().getY())
                        .put("target", target)
        );
    }
}