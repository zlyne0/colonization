package promitech.colonization.actors.colony;

import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.GameResources;

class BuildingQueueWindow extends Dialog {

	private final Colony colony;
	
	private Table tableLayout;
	
	BuildingQueueWindow(Colony colony) {
		super("", GameResources.instance.getUiSkin());
		this.colony = colony;
		
		//getContentTable().add(verticalListScrollPane);
		
		
        //getButtonTable().add(cancelButton);
        addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (Keys.ENTER == keycode2) {
                	hideWithFade();
                }
                if (Keys.ESCAPE == keycode2) {
                	hideWithFade();
                }
                return false;
            }
        });
        
        List<BuildingType> buildableBuildings = colony.buildableBuildings();
        for (BuildingType bt : buildableBuildings) {
        	System.out.println("bt " + bt);
        }
	}
	
    void hideWithFade() {
    	hide();
//    	if (unitActionOrdersDialogOnCloseListener != null) {
//    		unitActionOrdersDialogOnCloseListener.handle(null);
//    	}
    }
    
    void hideWithoutFade() {
    	hide(null);
//    	if (unitActionOrdersDialogOnCloseListener != null) {
//    		unitActionOrdersDialogOnCloseListener.handle(null);
//    	}
    }
}
