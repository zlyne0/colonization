package promitech.colonization.screen.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;

import promitech.colonization.screen.map.MapRenderer.TileDrawer;

class FogOfWarDrawer extends TileDrawer {
	public static final Color FOG_OF_WAR_COLOR = new Color(0f, 0f, 0f, 0.3f);

	PolygonSpriteBatch polyBatch;
	private final IsometricTilePolygonSprite tilePolygonSprite;
	
	public FogOfWarDrawer(MapDrawModel mapDrawModel) {
		super(mapDrawModel);
        polyBatch = new PolygonSpriteBatch();
        tilePolygonSprite = new IsometricTilePolygonSprite(FOG_OF_WAR_COLOR);
	}
	
	@Override
	public void draw() {
		if (tileDrawModel.hasFogOfWar()) {
			tilePolygonSprite.polygonSprite.setPosition(screenPoint.x, screenPoint.y);
			tilePolygonSprite.polygonSprite.draw(polyBatch);
		}
	}
}
