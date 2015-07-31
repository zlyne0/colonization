package promitech.colonization.infrastructure;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ManyStageInputProcessor implements InputProcessor {
    private final Stage[] stages;
    private int i;
    
    public ManyStageInputProcessor(Stage ... stages) {
        this.stages = stages;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].keyDown(keycode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].keyUp(keycode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].keyTyped(character)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].touchDown(screenX, screenY, pointer, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].touchUp(screenX, screenY, pointer, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].touchDragged(screenX, screenY, pointer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].mouseMoved(screenX, screenY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        for (i=0; i<stages.length; i++) {
            if (stages[i].scrolled(amount)) {
                return true;
            }
        }
        return false;
    }
}
