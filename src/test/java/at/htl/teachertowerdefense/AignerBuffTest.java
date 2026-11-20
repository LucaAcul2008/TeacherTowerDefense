package at.htl.teachertowerdefense;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Aigner-Buffs dürfen nicht stapeln – sonst wird das Spiel viel zu einfach. */
class AignerBuffTest {

    @Test
    void ohneAignerGibtEsKeinenBuff() {
        AignerBuffComponent.Buff b = AignerBuffComponent.besterBuff(List.of());
        assertEquals(0, b.schaden());
        assertEquals(1.0, b.tempoFaktor());
    }

    @Test
    void mehrereAignersStapelnNicht() {
        var basis = new AignerBuffComponent.Buff(0, 0.90);
        AignerBuffComponent.Buff b = AignerBuffComponent.besterBuff(List.of(basis, basis, basis));
        assertEquals(0, b.schaden(), "3 gleiche Aigners dürfen keinen extra Schaden geben");
        assertEquals(0.90, b.tempoFaktor(), 1e-9, "3 gleiche Aigners dürfen nicht schneller machen als eine");
    }

    @Test
    void jeweilsDerBesteWertZaehlt() {
        var schadenAigner = new AignerBuffComponent.Buff(3, 0.90);
        var tempoAigner   = new AignerBuffComponent.Buff(0, 0.74);
        AignerBuffComponent.Buff b = AignerBuffComponent.besterBuff(List.of(schadenAigner, tempoAigner));
        assertEquals(3, b.schaden());
        assertEquals(0.74, b.tempoFaktor(), 1e-9);
    }

    @Test
    void tempoBuffIstMaximal32Prozent() {
        double[] f = AignerBuffComponent.TEMPO_FAKTOR;
        assertEquals(0.90, f[0], 1e-9, "Unverbessert: 10 % schneller");
        for (int i = 1; i < f.length; i++) assertTrue(f[i] < f[i - 1], "Jede Stufe muss schneller machen");
        assertTrue(f[f.length - 1] >= 0.68 - 1e-9, "Voll ausgebaut höchstens 32 % schneller");
    }
}
