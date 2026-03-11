package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;

/**
 * Registriert unser custom ESC-Menü bei FXGL.
 */
public class CustomSceneFactory extends SceneFactory {

    @Override
    public FXGLMenu newGameMenu() {
        return new CustomGameMenu();
    }
}
