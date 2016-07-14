package promitech.colonization.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;

import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapRenderer;

public class UnitsPanel extends ScrollPane {
    private final HorizontalGroup widgets = new HorizontalGroup();
    
    public UnitsPanel() {
        super(null, GameResources.instance.getUiSkin());
        setWidget(widgets);
        
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
        setScrollBarPositions(false, true);
        
        widgets.align(Align.center);
        widgets.space(15);
    }

    public void clear() {
        widgets.clear();
    }
    
    void disableAllUnitActors() {
        for (Actor a : widgets.getChildren()) {
            if (a instanceof UnitActor) {
                ((UnitActor)a).disableFocus();
            }
        }
    }
    
    public void addUnit(UnitActor unitActor) {
        widgets.addActor(unitActor);
    }
    
    @Override
    public float getPrefWidth() {
        return MapRenderer.TILE_WIDTH*2;
    }
    @Override
    public float getPrefHeight() {
        return MapRenderer.TILE_HEIGHT*2;
    }
}