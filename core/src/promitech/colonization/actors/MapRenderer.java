package promitech.colonization.actors;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.math.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class MapRenderer {
	public static final int TILE_WIDTH = 128;
	public static final int TILE_HEIGHT = 64;
	
	private static final float HORIZONTAL_SCREEN_EDGE_SIZE = TILE_WIDTH;
	private static final float VERTICAL_SCREEN_EDGE_SIZE = TILE_HEIGHT;
	
	public static abstract class TileDrawer {
		protected Batch batch;
		protected ShapeRenderer shapeRenderer;
		protected final Vector2 screenPoint = new Vector2(); 
		protected int mapx;
		protected int mapy;
		
		protected Map map;
		protected Tile tile;
		protected TileDrawModel tileDrawModel;
		protected MapDrawModel mapDrawModel;
		
		public boolean areCoordinatesOnMap() {
			return mapx >= 0 && mapx < map.width && mapy >= 0 && mapy < map.height;
		}
		
		public abstract void draw();		
	}
	
	private static class TerrainBackgroundTileDrawer extends TileDrawer {
		private final Frame unexploredFrame;
		private final Player player;
		
		public TerrainBackgroundTileDrawer(GameResources gameResources, Player player) {
			unexploredFrame = gameResources.unexploredTile(1, 1);
			this.player = player;
		}
		
		@Override
		public void draw() {
			if (player.isTileExplored(mapx, mapy)) {
				tileDrawModel.draw(batch, screenPoint.x, screenPoint.y);
			} else {
				batch.draw(unexploredFrame.texture, screenPoint.x, screenPoint.y);
			}
		}
	}

	private static class TerrainForegroundTileDrawer extends TileDrawer {
		private final Player player;
		
		public TerrainForegroundTileDrawer(Player player) {
			this.player = player;
		}
		
		@Override
		public void draw() {
			if (player.isTileExplored(mapx, mapy)) {
				tileDrawModel.drawOverlay(batch, screenPoint.x, screenPoint.y);
			}
		}
	}
	
	private final Map map; 
	private final MapDrawModel mapDrawModel;
	private final Player playerMap;
	private final GameResources gameResources;
	
    private final TerrainBackgroundTileDrawer terrainBackgroundTileDrawer;
    private final TerrainForegroundTileDrawer terrainForegroundTileDrawer;
    private final RoadsTileDrawer roadsTileDrawer;
    private final ObjectsTileDrawer objectsTileDrawer;
    private final FogOfWarDrawer fogOfWarDrawer;

    private final ShapeRenderer shapeRenderer;
    public final Vector2 cameraPosition = new Vector2();
    private int screenWidth = -1;
    private int screenHeight = -1;
    
	private final Point screenMin = new Point();
	private final Point screenMax = new Point();
	private int x;
	private int y;
	
	
	public MapRenderer(Player playerMap, Map map, MapDrawModel mapDrawModel, 
			GameResources gameResources, ShapeRenderer shapeRenderer ) 
	{
		fogOfWarDrawer = new FogOfWarDrawer(playerMap);
		terrainForegroundTileDrawer = new TerrainForegroundTileDrawer(playerMap);
		terrainBackgroundTileDrawer = new TerrainBackgroundTileDrawer(gameResources, playerMap);
		roadsTileDrawer = new RoadsTileDrawer(gameResources);
		objectsTileDrawer = new ObjectsTileDrawer(gameResources);
		
    	this.playerMap = playerMap;
    	this.map = map;
    	this.mapDrawModel = mapDrawModel;
    	this.objectsTileDrawer.mapDrawModel = mapDrawModel;
    	this.gameResources = gameResources;
    	this.shapeRenderer = shapeRenderer;
        
    	terrainBackgroundTileDrawer.map = map;
    	terrainForegroundTileDrawer.map = map;
    	roadsTileDrawer.map = map;
    	objectsTileDrawer.map = map;
    	fogOfWarDrawer.map = map;
    	
    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	objectsTileDrawer.shapeRenderer = shapeRenderer;
    	fogOfWarDrawer.shapeRenderer = shapeRenderer;
    	
    	roadsTileDrawer.renderForPlayer = playerMap;
    	objectsTileDrawer.player = playerMap;
    	
    }
    
    public void setMapRendererSize(int width, int height) {
    	this.screenWidth = width;
    	this.screenHeight = height;
    }
    
    private void drawLayer(Batch batch, TileDrawer tileDrawer) {
    	tileDrawer.batch = batch;
    	for (y=screenMin.y; y<screenMax.y; y++) {
    		for (x=screenMin.x; x<screenMax.x; x++) {
    			tileDrawer.mapx = x;
    			tileDrawer.mapy = y;
    			tileDrawer.tile = tileDrawer.map.getTile(x, y);
    			tileDrawer.tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
    			mapToScreenCords(x, y, tileDrawer.screenPoint);
    			tileDrawer.draw();
    		}
    	}
    }
    
    private void preparePartOfMapToRender(final Map map) {
		// there is transformation screenY = screenHeight - screenY;
    	screenToMapCords(0-TILE_WIDTH, screenHeight + TILE_HEIGHT, screenMin);
    	screenToMapCords(screenWidth + TILE_WIDTH*2, -TILE_HEIGHT, screenMax);

    	if (screenMin.x < 0) {
    		screenMin.x = 0;
    	}
    	if (screenMin.y < 0) {
    		screenMin.y = 0;
    	}
    	if (screenMax.x >= map.width) {
    		screenMax.x = map.width;
    	}
    	if (screenMax.y >= map.height) {
    		screenMax.y = map.height;
    	}
    }
    
    public void render(Batch batch) {
    	objectsTileDrawer.batch = batch;
    	fogOfWarDrawer.batch = batch;

    	objectsTileDrawer.shapeRenderer = shapeRenderer;
    	fogOfWarDrawer.shapeRenderer = shapeRenderer;
    	
    	preparePartOfMapToRender(map);
  
    	drawLayer(batch, terrainBackgroundTileDrawer);
    	drawLayer(batch, terrainForegroundTileDrawer);
    	drawRoadLayer(batch);
    	drawLayer(batch, objectsTileDrawer);
    	drawFogOfWarLayer(batch);
    }

    private void drawFogOfWarLayer(Batch batch) {
    	batch.end();
    	fogOfWarDrawer.polyBatch.setProjectionMatrix(batch.getProjectionMatrix());
    	fogOfWarDrawer.polyBatch.setTransformMatrix(batch.getTransformMatrix());
    	
    	fogOfWarDrawer.polyBatch.begin();
    	fogOfWarDrawer.polyBatch.enableBlending();
    	drawLayer(batch, fogOfWarDrawer);
    	fogOfWarDrawer.polyBatch.end();
    	
    	batch.begin();
	}

    private void drawRoadLayer(Batch batch) {
    	batch.end();
    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	roadsTileDrawer.shapeRenderer.begin(ShapeType.Line);
    	Gdx.gl20.glLineWidth(3);
    	drawLayer(batch, roadsTileDrawer);
    	roadsTileDrawer.shapeRenderer.end();
    	Gdx.gl20.glLineWidth(1);
    	batch.begin();
    }
    
    private final Vector2 oneUseVector2 = new Vector2();
	private Vector2 mapToScreenCords(int x, int y) {
		mapToScreenCords(x, y, oneUseVector2);
		return oneUseVector2;
	}
    
	public void mapToScreenCords(int x, int y, Vector2 p) {
        p.x = (TILE_WIDTH * x) + ((y % 2 == 1) ? TILE_HEIGHT : 0);
        p.y = (TILE_HEIGHT / 2) * y;
        
        p.x += cameraPosition.x;
        p.y += cameraPosition.y;
    	
        p.y = screenHeight - p.y;
    }

	private final Point oneUsePoint = new Point();
    public Point screenToMapCords(int screenX, int screenY) {
    	screenToMapCords(screenX, screenY, oneUsePoint);
    	return oneUsePoint;
    }
	
    public void screenToMapCords(int screenX, int screenY, Point p) {
        double x, y, pX, pY;
        screenY = screenHeight - screenY;
        
        int hWidth = TILE_WIDTH / 2;
        int hHeight = TILE_HEIGHT / 2;

        pX = screenX - TILE_WIDTH - cameraPosition.x;
        pY = screenY - cameraPosition.y;

        x = Math.floor((pX + (pY - hHeight) * 2) / TILE_WIDTH);
        y = Math.floor((pY - (pX - hWidth) * 0.5) / TILE_HEIGHT);

        p.x = (int) Math.floor((x - y) / 2) + 1 /* + this.camera.x */;
        p.y = (int) (y + x + 2) /* + this.camera.y */;
    }

	public void centerCameraOnTileCords(int mapx, int mapy) {
		if (screenWidth == -1 && screenHeight == -1) {
			throw new IllegalStateException("screen size not set");
		}
		cameraPosition.set(0, 0);
		// there is transformation screenY = screenHeight - screenY;
    	screenToMapCords(0-TILE_WIDTH, screenHeight + TILE_HEIGHT, screenMin);
    	screenToMapCords(screenWidth + TILE_WIDTH*2, -TILE_HEIGHT, screenMax);
		
    	int halfX = (screenMax.x - screenMin.x) / 2; 
    	int halfY = (screenMax.y - screenMin.y) / 2; 
    	
		cameraPosition.set(-(mapx - halfX/2) * TILE_WIDTH, -(mapy - halfY) * TILE_HEIGHT/2 );
		cameraPosition.add(TILE_WIDTH/2, 0);
	}

	public Point getCenterOfScreen() {
		return screenToMapCords(screenWidth/2, screenHeight/2);
	}
	
	public boolean isPointOnScreenEdge(int px, int py) {
		Vector2 sc = mapToScreenCords(px, py);
		if (	sc.x <= HORIZONTAL_SCREEN_EDGE_SIZE ||
				sc.x >= screenWidth - HORIZONTAL_SCREEN_EDGE_SIZE ||
				sc.y <= VERTICAL_SCREEN_EDGE_SIZE || 
				sc.y >= screenHeight - VERTICAL_SCREEN_EDGE_SIZE) {
			return true;
		}
		return false;
	}
	
	public void drawSelectedTileOnInfoPanel(Batch batch, ShapeRenderer shapeRenderer, float screenX, float screenY) {
		if (mapDrawModel.selectedTile == null) {
			return;
		}
		Tile tile = mapDrawModel.selectedTile;

    	terrainBackgroundTileDrawer.batch = batch;
    	terrainForegroundTileDrawer.batch = batch;
    	roadsTileDrawer.batch = batch;

    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	objectsTileDrawer.shapeRenderer = shapeRenderer;
    	fogOfWarDrawer.shapeRenderer = shapeRenderer;
		
    	drawInfoPanelTile(terrainBackgroundTileDrawer, screenX, screenY, tile);
    	drawInfoPanelTile(terrainForegroundTileDrawer, screenX, screenY, tile);
    	
    	if (tile.hasRoad()) {
	    	batch.end();
	    	roadsTileDrawer.shapeRenderer.begin(ShapeType.Line);
	    	Gdx.gl20.glLineWidth(3);
	    	drawInfoPanelTile(roadsTileDrawer, screenX, screenY, tile);
	    	roadsTileDrawer.shapeRenderer.end();
	    	Gdx.gl20.glLineWidth(1);
	    	batch.begin();
    	}
	}
    
	private void drawInfoPanelTile(TileDrawer tileDrawer, float px, float py, Tile tile) {
		tileDrawer.mapx = tile.x;
		tileDrawer.mapy = tile.y;
		tileDrawer.tile = tile;
		tileDrawer.tileDrawModel = mapDrawModel.getTileDrawModel(tile.x, tile.y);
		tileDrawer.screenPoint.x = px;
		tileDrawer.screenPoint.y = py;
		tileDrawer.draw();
	}
	
}
