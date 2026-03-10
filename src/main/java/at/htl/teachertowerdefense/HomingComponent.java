package at.htl.teachertowerdefense;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;

public class HomingComponent extends Component {
    private Entity target;
    private double speed;

    public HomingComponent(Entity target, double speed) {
        this.target = target;
        this.speed = speed;
    }

    @Override
    public void onUpdate(double tpf) {
        // Wenn der Schüler noch lebt, fliege auf ihn zu!
        if (target.isActive()) {
            entity.translateTowards(target.getCenter(), speed * tpf);
        } else {
            // Wenn der Schüler schon besiegt wurde, lösche das Projektil, damit es nicht ewig weiterfliegt
            entity.removeFromWorld();
        }
    }
}