package promitech.colonization.screen.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;

import net.sf.freecol.common.model.Colony;
import promitech.colonization.screen.map.MapRenderer.TileDrawer;

class ColonyLockedTileDrawer extends TileDrawer {

	private static final Color LOCKED_TILE_COLOR = new Color(1f, 0f, 0f, 0.4f);
	
	private IsometricTilePolygonSprite polygonSprite;
	protected PolygonSpriteBatch polyBatch;	
	private Colony colony;
	
	protected ColonyLockedTileDrawer(MapDrawModel mapDrawModel) {
		super(mapDrawModel);
		
		polygonSprite = new IsometricTilePolygonSprite(LOCKED_TILE_COLOR);
		polyBatch = new PolygonSpriteBatch();
	}

	@Override
	public void draw() {
		if (colony != null && colony.isTileLocked(tile)) {
			polygonSprite.polygonSprite.setPosition(screenPoint.x, screenPoint.y);
			polygonSprite.polygonSprite.draw(polyBatch);
		}
	}
	
	public void update(Colony colony) {
		this.colony = colony;
	}
	
	public void dispose() {
		polygonSprite.dispose();
		polygonSprite = null;
	}	
}