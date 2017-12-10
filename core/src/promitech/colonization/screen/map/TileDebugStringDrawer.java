package promitech.colonization.screen.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.screen.map.MapRenderer.TileDrawer;

class TileDebugStringDrawer extends TileDrawer {
	private String tileStrings[][];
	
	protected TileDebugStringDrawer(MapDrawModel mapDrawModel) {
		super(mapDrawModel);
	}

	public void initStrings(String tileStrings[][]) {
		this.tileStrings = tileStrings;
	}
	
	@Override
	public void draw() {
//		if (!mapDrawModel.playingPlayer.isTileExplored(mapx, mapy)) {
//			return;
//		}
		if (tileStrings == null) {
			return;
		}
		String str = tileStrings[mapy][mapx];
		if (str == null) {
			return;
		}
		
		BitmapFont unitBoxFont = FontResource.getUnitBoxFont();
		unitBoxFont.setColor(Color.RED);
		unitBoxFont.draw(batch, str, 
			screenPoint.x + MapRenderer.TILE_WIDTH/2 - FontResource.strWidth(unitBoxFont, str)/2, 
			screenPoint.y + MapRenderer.TILE_HEIGHT/2 + FontResource.fontHeight(unitBoxFont)/2
		);
	}

}
