package promitech.colonization.actors.map;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;

import promitech.colonization.actors.map.MapRenderer.TileDrawer;

class TileOwnerDrawer extends TileDrawer {

	private final java.util.Map<String,IsometricTilePolygonSprite> tilePolygonSpriteByPlayerId = new HashMap<String, IsometricTilePolygonSprite>();

	PolygonSpriteBatch polyBatch;
	
	protected TileOwnerDrawer(MapDrawModel mapDrawModel) {
		super(mapDrawModel);
		
		polyBatch = new PolygonSpriteBatch(); 
	}

	@Override
	public void draw() {
		if (!tileDrawModel.hasFogOfWar() && tile.getOwner() != null) {
			IsometricTilePolygonSprite isometricTilePolygonSprite = tilePolygonSpriteByPlayerId.get(tile.getOwner().getId());
			if (isometricTilePolygonSprite == null) {
				
				Color pc = tile.getOwner().nation().getColor();
				Color color = new Color(pc.r, pc.g, pc.b, 0.4f);
				
				isometricTilePolygonSprite = new IsometricTilePolygonSprite(color);
				tilePolygonSpriteByPlayerId.put(tile.getOwner().getId(), isometricTilePolygonSprite);
			}
			isometricTilePolygonSprite.polygonSprite.setPosition(screenPoint.x, screenPoint.y);
			isometricTilePolygonSprite.polygonSprite.draw(polyBatch);
		}
	}
	
	public void dispose() {
		for (Entry<String, IsometricTilePolygonSprite> entry : tilePolygonSpriteByPlayerId.entrySet()) {
			entry.getValue().dispose();
		}
		tilePolygonSpriteByPlayerId.clear();
	}
}