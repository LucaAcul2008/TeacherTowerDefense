package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

public class SchuelerComponent extends Component {

    private final SchuelerTyp typ;
    private final int startWaypoint; // ab welchem Wegpunkt dieser Schüler gestartet ist
    private int hp;
    private boolean sterbend = false;

    // HP-Balken (nur für Schüler mit mehr als 1 HP, erscheint sobald sie Schaden nehmen)
    private javafx.scene.shape.Rectangle hpBalkenBg;
    private javafx.scene.shape.Rectangle hpBalken;

    public SchuelerComponent(SchuelerTyp typ) {
        this(typ, 0);
    }

    public SchuelerComponent(SchuelerTyp typ, int startWaypoint) {
        this.typ          = typ;
        this.startWaypoint = startWaypoint;
        this.hp           = (int)(typ.maxHp * GameConfig.getSchuelerHpMulti());
    }

    @Override
    public void onAdded() {
        if (getMaxHp() <= 1) return;
        int breite = TeacherTowerDefenseFactory.hitboxGroesse(typ);
        double y   = -(TeacherTowerDefenseFactory.spriteHoehe(typ) - breite) - 7;

        // Über dem Sprite zentrieren (das Sprite ist breiter als die Hitbox)
        double spriteBreite = entity.getViewComponent().getChildren().stream()
                .filter(n -> n instanceof javafx.scene.image.ImageView)
                .mapToDouble(n -> n.getBoundsInLocal().getWidth())
                .findFirst().orElse(breite);
        double x = (spriteBreite - breite) / 2.0;

        hpBalkenBg = new javafx.scene.shape.Rectangle(breite, 5, javafx.scene.paint.Color.color(0, 0, 0, 0.7));
        hpBalkenBg.setTranslateX(x);
        hpBalkenBg.setTranslateY(y);
        hpBalkenBg.setArcWidth(3); hpBalkenBg.setArcHeight(3);
        hpBalken = new javafx.scene.shape.Rectangle(breite, 5, javafx.scene.paint.Color.LIMEGREEN);
        hpBalken.setTranslateX(x);
        hpBalken.setTranslateY(y);
        hpBalken.setArcWidth(3); hpBalken.setArcHeight(3);

        hpBalkenBg.setVisible(false);
        hpBalken.setVisible(false);
        entity.getViewComponent().addChild(hpBalkenBg);
        entity.getViewComponent().addChild(hpBalken);
    }

    private void aktualisiereHpBalken() {
        if (hpBalken == null) return;
        double anteil = Math.max(0, (double) hp / getMaxHp());
        hpBalken.setWidth(hpBalkenBg.getWidth() * anteil);
        hpBalken.setFill(anteil > 0.5 ? javafx.scene.paint.Color.LIMEGREEN
                : anteil > 0.25 ? javafx.scene.paint.Color.ORANGE
                : javafx.scene.paint.Color.RED);
        boolean sichtbar = hp < getMaxHp();
        hpBalkenBg.setVisible(sichtbar);
        hpBalken.setVisible(sichtbar);
    }

    public void damage(int amount) {
        if (sterbend) return;
        hp -= amount;
        aktualisiereHpBalken();
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
        Sounds.play("pop");

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
        // Gespeichert wird am Rundenende / Spielende – nicht bei jedem Kill (zu teuer)

        // Kinder spawnen genau dort, wo der Schüler gestorben ist, und laufen zum Ende
        // des Wegabschnitts, auf dem er gerade war (nicht zum "nächsten Eckpunkt" – das hat Ecken übersprungen).
        final javafx.geometry.Point2D pos = entity.getPosition();
        final int abschnitt      = WaypointData.naechsterAbschnitt(pos, startWaypoint);
        final int naechsterIndex = abschnitt + 1;
        final javafx.geometry.Point2D abschnittStart = WaypointData.getROUTE().get(abschnitt);
        final int overkill       = overkillDamage;

        FXGL.getGameTimer().runOnceAfter(() -> {
            if (!entity.isActive()) return;

            if (typ.kindTyp != null) {
                // Mehrere Kinder leicht hintereinander (Richtung Abschnitt-Anfang), nicht alle auf einem Punkt
                javafx.geometry.Point2D zurueck = abschnittStart.subtract(pos);
                double platz = zurueck.magnitude();
                javafx.geometry.Point2D richtung = platz > 0 ? zurueck.normalize() : javafx.geometry.Point2D.ZERO;
                for (int i = 0; i < typ.kindAnzahl; i++) {
                    javafx.geometry.Point2D p = pos.add(richtung.multiply(Math.min(i * 14.0, platz)));
                    var kindEntity = FXGL.spawn("Schueler",
                            new com.almasb.fxgl.entity.SpawnData(p.getX(), p.getY())
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
    public int getStartWaypoint() { return startWaypoint; }
    public int getHp()           { return hp; }
    public int getMaxHp()        { return (int)(typ.maxHp * GameConfig.getSchuelerHpMulti()); }
}
