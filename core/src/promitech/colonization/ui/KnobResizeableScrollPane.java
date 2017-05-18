package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Enlarge ScrollPane component when need show knob 
 */
public class KnobResizeableScrollPane extends ScrollPane {
	private float knobHeight = 0;

	public KnobResizeableScrollPane(Actor widget, Skin skin) {
		super(widget, skin);
	}
	
	@Override
	public float getPrefHeight() {
		return super.getPrefHeight() + knobHeight;
	}
	
	@Override
	public void layout() {
		super.layout();
		
		// this mean that need scroll 
		if (getMaxX() > 0) {
			if (knobHeight == 0) {
				invalidateHierarchy();
			}
			knobHeight = getStyle().hScrollKnob.getMinHeight();
		} else {
			if (knobHeight == getStyle().hScrollKnob.getMinHeight()) {
				invalidateHierarchy();
			}
			knobHeight = 0;
		}
	}
	

}
