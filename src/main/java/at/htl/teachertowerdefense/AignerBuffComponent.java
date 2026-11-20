package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;

import java.util.Collection;

/**
 * Aigner – passive Bufferin.
 * Lehrer in ihrem Aura-Radius schießen schneller und (mit Pfad B) stärker.
 *   Pfad A → getRange()  = Aura-Radius
 *   Pfad B → getDamage() = Schadens-Buff (startet bei 0)
 *   Pfad C → TEMPO_FAKTOR = Nachladezeit wird mit diesem Faktor multipliziert
 *
 * Buffs mehrerer Aigners STAPELN NICHT – es zählt jeweils der stärkste.
 * (Früher wurden alle addiert: 3 Aigners = +3 Schaden und bis zu 4x Feuerrate.)
 */
public class AignerBuffComponent extends Component {

    /** Faktor auf die Nachladezeit je Stufe von Pfad C (0.90 = 10 % schneller). */
    static final double[] TEMPO_FAKTOR = {0.90, 0.86, 0.82, 0.78, 0.74, 0.68};

    /** Ein Buff: Schadensbonus (flach) und Faktor auf die Nachladezeit. */
    public record Buff(int schaden, double tempoFaktor) {
        public static final Buff KEINER = new Buff(0, 1.0);
    }

    /** Aura-Radius in Pixeln (wächst mit Pfad-A-Upgrades via LehrerComponent). */
    public double getBuffRadius() {
        return entity.getComponent(LehrerComponent.class).getRange();
    }

    /** Schadensbuff der an Lehrer im Radius gegeben wird (wächst mit Pfad-B-Upgrades). */
    public int getDamageBonus() {
        return entity.getComponent(LehrerComponent.class).getDamage();
    }

    /** Faktor auf die Nachladezeit (wird mit Pfad-C-Upgrades kleiner = schneller). */
    public double getTempoFaktor() {
        int stufe = entity.getComponent(LehrerComponent.class).getStufePfadC();
        return TEMPO_FAKTOR[Math.min(stufe, TEMPO_FAKTOR.length - 1)];
    }

    /** Tempo-Buff in Prozent, z.B. 10 für 10 % schneller. */
    public int getTempoProzent() {
        return (int) Math.round((1 - getTempoFaktor()) * 100);
    }

    public Buff getBuff() {
        return new Buff(getDamageBonus(), getTempoFaktor());
    }

    /** Kombiniert die Buffs aller Aigners in Reichweite: kein Stapeln, jeweils der beste Wert zählt. */
    public static Buff besterBuff(Collection<Buff> buffs) {
        int schaden = 0;
        double faktor = 1.0;
        for (Buff b : buffs) {
            schaden = Math.max(schaden, b.schaden());
            faktor  = Math.min(faktor, b.tempoFaktor());
        }
        return new Buff(schaden, faktor);
    }
}
