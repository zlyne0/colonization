package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;

public interface CloseableDialog {
	void clickOnDialogStage(InputEvent event, float x, float y);
	void addOnCloseEvent(EventListener dialogOnCloseListener);
	Dialog show(Stage stage);	
}
