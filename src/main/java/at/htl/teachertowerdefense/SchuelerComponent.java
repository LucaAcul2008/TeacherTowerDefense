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
        this.hp  = (int)(typ.maxHp * GameConfig.getSchuelerHpMulti());
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

        // --- NEU: MÜNZEN ANIMATION ---
        javafx.scene.text.Text coinText = new javafx.scene.text.Text("+" + typ.belohnung + " \uD83D\uDCB0");
        coinText.setFill(javafx.scene.paint.Color.GOLD);
        coinText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
        coinText.setStroke(javafx.scene.paint.Color.BLACK); // Outline für bessere Lesbarkeit
        coinText.setStrokeWidth(1);

        com.almasb.fxgl.entity.Entity animationEntity = FXGL.entityBuilder()
                .at(entity.getX(), entity.getY() - 10)
                .view(coinText)
                .zIndex(200)
                .buildAndAttach();

        FXGL.animationBuilder()
                .duration(javafx.util.Duration.seconds(0.8))
                .onFinished(() -> animationEntity.removeFromWorld())
                .translate(animationEntity)
                .from(animationEntity.getPosition())
                .to(animationEntity.getPosition().subtract(0, 40)) // 40px nach oben floaten
                .buildAndPlay();
        // -----------------------------

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
                            com.almasb.fxgl.entity.SpawnData.class.cast(
                                    new com.almasb.fxgl.entity.SpawnData(x, y)
                                            .put("typ", typ.kindTyp)
                                            .put("startWaypoint", naechsterIndex)
                            )
                    );

                    // Overkill-Schaden ans Kind weitergeben (falls > 0)
                    if (overkill > 0) {
                        kindEntity.getComponent(SchuelerComponent.class).damage(overkill);
                    }
                }
            }

            entity.removeFromWorld();

        }, javafx.util.Duration.ZERO);
    }

    public SchuelerTyp getTyp() { return typ; }
    public int getHp()          { return hp; }
    public int getMaxHp()       { return typ.maxHp; }
}