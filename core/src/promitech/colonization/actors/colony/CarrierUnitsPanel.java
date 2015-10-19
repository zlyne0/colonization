package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapRenderer;

class CarrierUnitsPanel extends ScrollPane {

	private final Table widgets = new Table();
	
	public CarrierUnitsPanel() {
		super(null, GameResources.instance.getUiSkin());
		setWidget(widgets);
		
        setForceScroll(false, false);
        setFadeScrollBars(false);
        setOverscroll(true, true);
	}
	
	void initUnits(Tile colonyTile) {
		widgets.clear();
		for (Unit unit : colonyTile.units.entities()) {
		    if (!unit.isCarrier()) {
		        continue;
		    }
			TextureRegion tr = GameResources.instance.getFrame(unit.resourceImageKey()).texture;
			widgets.add(new Image(tr))
				.spaceLeft(tr.getRegionWidth()/2) 
				.spaceRight(tr.getRegionWidth()/2);
		}
	}
	
	@Override
	public float getPrefWidth() {
		return MapRenderer.TILE_WIDTH*3;
	}
	@Override
	public float getPrefHeight() {
		return MapRenderer.TILE_HEIGHT*2;
	}
	
}
