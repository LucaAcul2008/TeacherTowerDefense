package at.htl.teachertowerdefense;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;

public class CustomSceneFactory extends SceneFactory {

    @Override
    public FXGLMenu newMainMenu() {
        return new CustomMainMenu();
    }

    @Override
    public FXGLMenu newGameMenu() {
        return new CustomGameMenu();
    }
}