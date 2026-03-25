package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import javafx.util.Duration;

/**
 * Verwaltet HP und spawnt beim Tod die richtigen Kinder.
 * Überschuss-Schaden (Overkill) wird an alle Kinder weitergegeben.
 *
 * Beispiel: Schüler hat 1 HP, Turm macht 3 Damage → 2 Overkill-Damage
 * → Kinder werden mit damage(2) getroffen sobald sie spawnen.
 */
public class SchuelerComponent extends Component {

    private final SchuelerTyp typ;
    private int hp;
    private boolean sterbend = false;

    public SchuelerComponent(SchuelerTyp typ) {
        this.typ = typ;
        this.hp  = typ.maxHp;
    }

    public void damage(int amount) {
        if (sterbend) return;
        hp -= amount;
        if (hp <= 0) {
            sterbend = true;
            // Überschuss-Schaden = wie viel "über 0" wir gegangen sind
            int overkill = Math.abs(hp); // hp ist negativ oder 0
            sterben(overkill);
        }
    }

    private void sterben(int overkillDamage) {
        FXGL.inc("geld", typ.belohnung);

        // XP an alle platzierten Lehrer vergeben (jeder Typ bekommt seinen eigenen XP-Pool)
        int xpGewinn = typ.maxHp;
        java.util.Set<Integer> bereitsGezaehlt = new java.util.HashSet<>();
        for (com.almasb.fxgl.entity.Entity lehrer :
                FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
            if (lehrer.hasComponent(LehrerComponent.class)) {
                int idx = lehrer.getComponent(LehrerComponent.class).getLehrerTyp();
                if (bereitsGezaehlt.add(idx)) { // jeden Typ nur einmal
                    SaveData.lehrerXP[idx] += xpGewinn;
                }
            }
        }
        SaveData.speichern();

        final double x            = entity.getX() + typ.groesse / 2.0;
        final double y            = entity.getY() + typ.groesse / 2.0;
        final int naechsterIndex  = WaypointData.naechsterWaypointIndex(x, y);
        final int overkill        = overkillDamage;

        FXGL.getGameTimer().runOnceAfter(() -> {
            if (!entity.isActive()) return;

            if (typ.kindTyp != null) {
                for (int i = 0; i < typ.kindAnzahl; i++) {
                    // Kind spawnen
                    var kindEntity = FXGL.spawn("Schueler",
                            new SpawnData(x, y)
                                    .put("typ", typ.kindTyp)
                                    .put("startWaypoint", naechsterIndex)
                    );

                    // Overkill-Schaden ans Kind weitergeben (falls > 0)
                    if (overkill > 0) {
                        kindEntity.getComponent(SchuelerComponent.class).damage(overkill);
                    }
                }
            }

            entity.removeFromWorld();

        }, Duration.ZERO);
    }

    public SchuelerTyp getTyp() { return typ; }
    public int getHp()          { return hp; }
    public int getMaxHp()       { return typ.maxHp; }
}