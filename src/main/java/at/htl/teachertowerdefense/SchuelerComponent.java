package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

public class SchuelerComponent extends Component {

    private final SchuelerTyp typ;
    private final int startWaypoint; // ab welchem Wegpunkt dieser Schüler gestartet ist
    private int hp;
    private boolean sterbend = false;

    public SchuelerComponent(SchuelerTyp typ) {
        this(typ, 0);
    }

    public SchuelerComponent(SchuelerTyp typ, int startWaypoint) {
        this.typ          = typ;
        this.startWaypoint = startWaypoint;
        this.hp           = (int)(typ.maxHp * GameConfig.getSchuelerHpMulti());
    }

    public void damage(int amount) {
        if (sterbend) return;
        hp -= amount;
        if (hp <= 0) {
            sterbend = true;
            // Schüler sofort verstecken damit er nicht noch einen Frame nach vorne springt
            entity.setVisible(false);
            int overkill = Math.abs(hp);
            sterben(overkill);
        }
    }

    private void sterben(int overkillDamage) {
        FXGL.inc("geld", typ.belohnung);

        // Münzen-Animation
        javafx.scene.text.Text coinText = new javafx.scene.text.Text("+" + typ.belohnung + " 💰");
        coinText.setFill(javafx.scene.paint.Color.GOLD);
        coinText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
        coinText.setStroke(javafx.scene.paint.Color.BLACK);
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
                .to(animationEntity.getPosition().subtract(0, 40))
                .buildAndPlay();

        // XP an platzierte Lehrer
        int xpGewinn = typ.maxHp;
        java.util.Set<Integer> bereitsGezaehlt = new java.util.HashSet<>();
        for (com.almasb.fxgl.entity.Entity lehrer :
                FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)) {
            if (lehrer.hasComponent(LehrerComponent.class)) {
                int idx = lehrer.getComponent(LehrerComponent.class).getLehrerTyp();
                if (bereitsGezaehlt.add(idx)) {
                    SaveData.lehrerXP[idx] += xpGewinn;
                }
            }
        }
        SaveData.speichern();

        final double x           = entity.getX() + typ.groesse / 2.0;
        final double y           = entity.getY() + typ.groesse / 2.0;
        // Nur vorwärts suchen ab dem Waypoint wo dieser Schüler gestartet ist
        final int naechsterIndex = WaypointData.naechsterWaypointIndexAb(x, y, startWaypoint);
        final int overkill       = overkillDamage;

        FXGL.getGameTimer().runOnceAfter(() -> {
            if (!entity.isActive()) return;

            if (typ.kindTyp != null) {
                for (int i = 0; i < typ.kindAnzahl; i++) {
                    var kindEntity = FXGL.spawn("Schueler",
                            new com.almasb.fxgl.entity.SpawnData(x, y)
                                    .put("typ", typ.kindTyp)
                                    .put("startWaypoint", naechsterIndex)
                    );
                    if (overkill > 0) {
                        kindEntity.getComponent(SchuelerComponent.class).damage(overkill);
                    }
                }
            }

            entity.removeFromWorld();

        }, javafx.util.Duration.ZERO);
    }

    public SchuelerTyp getTyp()  { return typ; }
    public int getHp()           { return hp; }
    public int getMaxHp()        { return (int)(typ.maxHp * GameConfig.getSchuelerHpMulti()); }
}
