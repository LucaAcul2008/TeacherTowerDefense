package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

/**
 * Gassner – Geldgenerator (Bananen-Farm-Stil).
 *   Pfad A → schnellere Generierung
 *   Pfad B → höherer Betrag pro Auszahlung
 *   Pfad C → Synergie: je mehr Gassners platziert, desto mehr generiert jeder
 */
public class GassnerComponent extends Component {

    private static final double[] INTERVALLE = {10.0, 9.0, 7.5, 6.0, 4.5, 3.0};
    private static final int[]    BETRAEGE   = {25, 35, 50, 70, 100, 150};

    private double timer = 0;

    @Override
    public void onUpdate(double tpf) {
        timer += tpf;
        double interval = getGenInterval() / GameConfig.speedMulti;
        if (timer >= interval) {
            timer = 0;
            int betrag = (int)(getGenBetrag() * getSynergie());
            FXGL.inc("geld", betrag);
            zeigeMuenzenAnimation(betrag);
        }
    }

    public double getGenInterval() {
        int stufe = entity.getComponent(LehrerComponent.class).getStufePfadA();
        return INTERVALLE[Math.min(stufe, INTERVALLE.length - 1)];
    }

    public int getGenBetrag() {
        int stufe = entity.getComponent(LehrerComponent.class).getStufePfadB();
        return BETRAEGE[Math.min(stufe, BETRAEGE.length - 1)];
    }

    public double getSynergie() {
        int stufe = entity.getComponent(LehrerComponent.class).getStufePfadC();
        long anzahl = FXGL.getGameWorld().getEntitiesByType(EntityType.LEHRER)
                .stream().filter(e -> e.hasComponent(GassnerComponent.class)).count();
        double bonus = 0.1 * stufe * (anzahl - 1);
        return 1.0 + Math.max(0, bonus);
    }

    private void zeigeMuenzenAnimation(int betrag) {
        javafx.scene.text.Text txt = new javafx.scene.text.Text("+" + betrag + "€");
        txt.setFill(javafx.scene.paint.Color.GOLD);
        txt.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
        txt.setStroke(javafx.scene.paint.Color.BLACK);
        txt.setStrokeWidth(0.5);

        com.almasb.fxgl.entity.Entity animEnt = FXGL.entityBuilder()
                .at(entity.getCenter())
                .view(txt)
                .zIndex(200)
                .buildAndAttach();

        FXGL.animationBuilder()
                .duration(javafx.util.Duration.seconds(0.8))
                .onFinished(animEnt::removeFromWorld)
                .translate(animEnt)
                .from(animEnt.getPosition())
                .to(animEnt.getPosition().subtract(0, 30))
                .buildAndPlay();
    }
}
