package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;

/**
 * Aigner – passive Bufferin.
 * Benachbarte Lehrer in ihrem Aura-Radius bekommen Boni auf Schaden und Schussgeschwindigkeit.
 * Die Werte wachsen automatisch mit LehrerComponent-Upgrades:
 *   Pfad A → getRange()      = Aura-Radius
 *   Pfad B → getDamage()     = Schadensbuff
 *   Pfad C → eigene Tabelle  = Tempobuff (Shoot-Delay-Reduktion)
 */
public class AignerBuffComponent extends Component {

    private static final double[] SPEED_BONI = {0.0, 0.05, 0.10, 0.17, 0.24, 0.30};

    /** Aura-Radius in Pixeln (wächst mit Pfad-A-Upgrades via LehrerComponent). */
    public double getBuffRadius() {
        return entity.getComponent(LehrerComponent.class).getRange();
    }

    /** Schadensbuff der an Lehrer im Radius gegeben wird (wächst mit Pfad-B-Upgrades). */
    public int getDamageBonus() {
        return entity.getComponent(LehrerComponent.class).getDamage();
    }

    /** Shoot-Delay-Reduktion in Sekunden (wächst mit Pfad-C-Upgrades). */
    public double getShootDelayBonus() {
        int stufe = entity.getComponent(LehrerComponent.class).getStufePfadC();
        return SPEED_BONI[Math.min(stufe, SPEED_BONI.length - 1)];
    }
}
