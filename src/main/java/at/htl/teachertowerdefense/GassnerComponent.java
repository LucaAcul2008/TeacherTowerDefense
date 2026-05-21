package at.htl.teachertowerdefense;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

public class GassnerComponent extends Component {

    private static final int[] BETRAEGE = {25, 35, 50, 70, 100, 150};
    private static final int[] AUSZAHLUNGEN_PRO_RUNDE = {2, 2, 2, 3, 3, 4};

    private int generatedGeld = 0;
    private int payoutsThisRound = 0;

    public void notifyRoundPayout() {
        int stufeA = entity.getComponent(LehrerComponent.class).getStufePfadA();
        int maxPayouts = AUSZAHLUNGEN_PRO_RUNDE[Math.min(stufeA, AUSZAHLUNGEN_PRO_RUNDE.length - 1)];

        if (payoutsThisRound >= maxPayouts) return;
        payoutsThisRound++;

        int betrag = (int) (getGenBetrag() * getSynergie());
        FXGL.inc("geld", betrag);
        generatedGeld += betrag;
        zeigeMuenzenAnimation(betrag);

        int stufeC = entity.getComponent(LehrerComponent.class).getStufePfadC();
        int lebenBonus = 0;
        if (stufeC >= 5) lebenBonus = 2;
        else if (stufeC >= 4) lebenBonus = 1;

        if (lebenBonus > 0) {
            FXGL.inc("leben", lebenBonus);
            zeigeLebensAnimation(lebenBonus);
        }
    }

    public void onRundeStart() {
        payoutsThisRound = 0;
    }

    public int getGenBetrag() {
        int stufe = entity.getComponent(LehrerComponent.class).getStufePfadB();
        return BETRAEGE[Math.min(stufe, BETRAEGE.length - 1)];
    }

    public int getAuszahlungenProRunde() {
        int stufeA = entity.getComponent(LehrerComponent.class).getStufePfadA();
        return AUSZAHLUNGEN_PRO_RUNDE[Math.min(stufeA, AUSZAHLUNGEN_PRO_RUNDE.length - 1)];
    }

    public double getSynergie() {
        int stufeC = entity.getComponent(LehrerComponent.class).getStufePfadC();
        if (stufeC <= 0) return 1.0;

        int synStufe = Math.min(stufeC, 3);
        double rate;
        if (synStufe == 1)      rate = 0.10;
        else if (synStufe == 2) rate = 0.15;
        else                    rate = 0.20;

        long anzahlMitStufe = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.LEHRER)
                .stream()
                .filter(e -> e.hasComponent(GassnerComponent.class))
                .filter(e -> {
                    LehrerComponent lc = e.getComponent(LehrerComponent.class);
                    return lc.getStufePfadC() >= 1;
                })
                .count();

        long synAnzahl = Math.max(0, anzahlMitStufe - 1);
        return 1.0 + rate * synAnzahl;
    }

    public int getGeneratedGeld() {
        return generatedGeld;
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

    private void zeigeLebensAnimation(int lebenBonus) {
        javafx.scene.text.Text txt = new javafx.scene.text.Text("+" + lebenBonus + " ❤");
        txt.setFill(javafx.scene.paint.Color.HOTPINK);
        txt.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 13));
        txt.setStroke(javafx.scene.paint.Color.BLACK);
        txt.setStrokeWidth(0.5);

        com.almasb.fxgl.entity.Entity animEnt = FXGL.entityBuilder()
                .at(entity.getCenter().add(20, 0))
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