package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import javafx.util.Duration;

/**
 * Verwaltet HP und spawnt beim Tod die richtigen Kinder.
 *
 * WICHTIG: removeFromWorld() wird NICHT direkt im Collision-Callback aufgerufen,
 * sondern mit runOnceAfter(Duration.ZERO) auf den nächsten Frame verschoben.
 * Sonst kann FXGL während der Kollisionsverarbeitung abstürzen und
 * weitere Kollisionen (und damit das Schießen) blockieren.
 */
public class SchuelerComponent extends Component {

    private final SchuelerTyp typ;
    private int hp;
    private boolean sterbend = false; // verhindert doppeltes Sterben

    public SchuelerComponent(SchuelerTyp typ) {
        this.typ = typ;
        this.hp  = typ.maxHp;
    }

    public void damage(int amount) {
        if (sterbend) return; // schon am Sterben, ignorieren
        hp -= amount;
        if (hp <= 0) {
            sterbend = true;
            sterben();
        }
    }

    private void sterben() {
        // Belohnung sofort auszahlen
        FXGL.inc("geld", typ.belohnung);

        // Position merken BEVOR die Entity entfernt wird
        final double x = entity.getX() + typ.groesse / 2.0;
        final double y = entity.getY() + typ.groesse / 2.0;
        final int naechsterIndex = WaypointData.naechsterWaypointIndex(x, y);

        // Auf nächsten Frame verschieben → sicher außerhalb des Collision-Callbacks
        FXGL.getGameTimer().runOnceAfter(() -> {
            if (!entity.isActive()) return; // Sicherheitscheck

            // Kinder spawnen
            if (typ.kindTyp != null) {
                for (int i = 0; i < typ.kindAnzahl; i++) {
                    FXGL.spawn("Schueler",
                            new SpawnData(x, y)
                                    .put("typ", typ.kindTyp)
                                    .put("startWaypoint", naechsterIndex)
                    );
                }
            }

            entity.removeFromWorld();

        }, Duration.ZERO);
    }

    public SchuelerTyp getTyp() { return typ; }
    public int getHp()          { return hp; }
}