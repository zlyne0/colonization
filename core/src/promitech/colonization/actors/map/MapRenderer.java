package promitech.colonization.actors.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.math.Point;

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
		
		protected Tile tile;
		protected TileDrawModel tileDrawModel;
		protected final MapDrawModel mapDrawModel;
		
		public abstract void draw();
		
		protected TileDrawer(MapDrawModel mapDrawModel) {
			this.mapDrawModel = mapDrawModel;
		}
	}
	
	private static class TerrainBackgroundTileDrawer extends TileDrawer {
		private final Frame unexploredFrame;
		
		public TerrainBackgroundTileDrawer(MapDrawModel mapDrawModel, GameResources gameResources) {
			super(mapDrawModel);
			unexploredFrame = gameResources.unexploredTile(1, 1);
		}
		
		@Override
		public void draw() {
			if (tileDrawModel.isExplored()) {
				tileDrawModel.draw(batch, screenPoint.x, screenPoint.y);
			} else {
				batch.draw(unexploredFrame.texture, screenPoint.x, screenPoint.y);
			}
		}
	}

	private static class TerrainForegroundTileDrawer extends TileDrawer {
		public TerrainForegroundTileDrawer(MapDrawModel mapDrawModel) {
			super(mapDrawModel);
		}
		
		@Override
		public void draw() {
			if (tileDrawModel.isExplored()) {
				tileDrawModel.drawOverlay(batch, screenPoint.x, screenPoint.y);
			}
		}
	}
	
	private final MapDrawModel mapDrawModel;
	private final GameResources gameResources;
	
    private final TerrainBackgroundTileDrawer terrainBackgroundTileDrawer;
    private final TerrainForegroundTileDrawer terrainForegroundTileDrawer;
    private final RoadsTileDrawer roadsTileDrawer;
    private final ObjectsTileDrawer objectsTileDrawer;
    private final FogOfWarDrawer fogOfWarDrawer;
    private TileOwnerDrawer tileOwnerDrawer;
    private TileDebugStringDrawer tileDebugStringDrawer;
    private ColonyLockedTileDrawer colonyLockedTileDrawer;

    private final ShapeRenderer shapeRenderer;
    public final Vector2 cameraPosition = new Vector2();
    private int screenWidth = -1;
    private int screenHeight = -1;
    
	private final Point screenMin = new Point();
	private final Point screenMax = new Point();
	private int x;
	private int y;
	
	private boolean drawGrids = false;
	
	public MapRenderer(MapDrawModel mapDrawModel, 
			GameResources gameResources, ShapeRenderer shapeRenderer ) 
	{
		fogOfWarDrawer = new FogOfWarDrawer(mapDrawModel);
		terrainForegroundTileDrawer = new TerrainForegroundTileDrawer(mapDrawModel);
		terrainBackgroundTileDrawer = new TerrainBackgroundTileDrawer(mapDrawModel, gameResources);
		roadsTileDrawer = new RoadsTileDrawer(mapDrawModel, gameResources);
		objectsTileDrawer = new ObjectsTileDrawer(mapDrawModel, gameResources);
		
    	this.mapDrawModel = mapDrawModel;
    	this.gameResources = gameResources;
    	this.shapeRenderer = shapeRenderer;
    	
    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	objectsTileDrawer.shapeRenderer = shapeRenderer;
    	fogOfWarDrawer.shapeRenderer = shapeRenderer;
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
    			tileDrawer.tile = mapDrawModel.map.getTile(x, y);
    			tileDrawer.tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
    			mapToScreenCords(x, y, tileDrawer.screenPoint);
    			tileDrawer.draw();
    		}
    	}
    }
    
    private void preparePartOfMapToRender() {
		// there is transformation screenY = screenHeight - screenY;
    	screenToMapCords(0-TILE_WIDTH, screenHeight + TILE_HEIGHT, screenMin);
    	screenToMapCords(screenWidth + TILE_WIDTH*2, -TILE_HEIGHT, screenMax);

    	if (screenMin.x < 0) {
    		screenMin.x = 0;
    	}
    	if (screenMin.y < 0) {
    		screenMin.y = 0;
    	}
    	if (screenMax.x >= mapDrawModel.map.width) {
    		screenMax.x = mapDrawModel.map.width;
    	}
    	if (screenMax.y >= mapDrawModel.map.height) {
    		screenMax.y = mapDrawModel.map.height;
    	}
    }
    
    public void render(Batch batch) {
    	objectsTileDrawer.batch = batch;
    	fogOfWarDrawer.batch = batch;

    	objectsTileDrawer.shapeRenderer = shapeRenderer;
    	fogOfWarDrawer.shapeRenderer = shapeRenderer;
    	
    	preparePartOfMapToRender();
  
    	drawLayer(batch, terrainBackgroundTileDrawer);
    	drawLayer(batch, terrainForegroundTileDrawer);
    	drawRoadLayer(batch);
    	drawLayer(batch, objectsTileDrawer);
    	drawFogOfWarLayer(batch);
    	drawTileOwners(batch);
    	drawPathLayer(batch);
    	
    	if (tileDebugStringDrawer != null) {
    		drawLayer(batch, tileDebugStringDrawer);
    	}
    }

    private void drawPathLayer(Batch batch) {
    	if (mapDrawModel.unitPath == null) {
    		return;
    	}
    	Frame pathStepImg = gameResources.unitPathStepImage(mapDrawModel.unitPath.unit);
    	
    	for (int i=0; i<mapDrawModel.unitPath.tiles.size; i++) {
    		Tile tile = mapDrawModel.unitPath.tiles.get(i);
    		if (isMapCoordinatesOnScreen(tile.x, tile.y)) {
    			Vector2 tileScreenCords = mapToScreenCords(tile.x, tile.y);
    			batch.draw(
						pathStepImg.texture, 
    					tileScreenCords.x + pathStepImg.offsetX, 
    					tileScreenCords.y + pathStepImg.offsetY
    			);
    			
    			int turns = mapDrawModel.unitPath.turns.get(i);
    			if (turns > 0) {
    				BitmapFont font = FontResource.getPathTurnsFont();
    				font.draw(batch, 
    						Integer.toString(turns), 
    						tileScreenCords.x + TILE_WIDTH/2 + pathStepImg.texture.getRegionWidth()/2, 
    						tileScreenCords.y + TILE_HEIGHT/2
    				);
    			}
    		}
    	}
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

    private void drawTileOwners(Batch batch) {
    	if (tileOwnerDrawer != null) {
    		batch.end();

    		tileOwnerDrawer.polyBatch.setProjectionMatrix(batch.getProjectionMatrix());
    		tileOwnerDrawer.polyBatch.setTransformMatrix(batch.getTransformMatrix());
    		
    		tileOwnerDrawer.polyBatch.begin();
    		tileOwnerDrawer.polyBatch.enableBlending();
    		drawLayer(batch, tileOwnerDrawer);
    		tileOwnerDrawer.polyBatch.end();
    		batch.begin();
    	}
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
	public Vector2 mapToScreenCords(int x, int y) {
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
	
	private boolean isMapCoordinatesOnScreen(int px, int py) {
		return screenMin.x <= px && px <= screenMax.x && screenMin.y <= py && py <= screenMax.y; 
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
		
    	drawSingleTile(terrainBackgroundTileDrawer, screenX, screenY, tile);
    	drawSingleTile(terrainForegroundTileDrawer, screenX, screenY, tile);
    	
    	if (tile.hasRoad()) {
	    	batch.end();
	    	roadsTileDrawer.shapeRenderer.begin(ShapeType.Line);
	    	Gdx.gl20.glLineWidth(3);
	    	drawSingleTile(roadsTileDrawer, screenX, screenY, tile);
	    	roadsTileDrawer.shapeRenderer.end();
	    	Gdx.gl20.glLineWidth(1);
	    	batch.begin();
    	}
	}

	private void drawColonyTilesRoadLayer(Tile colonyTile, float screenX, float screenY) {
		roadsTileDrawer.batch.end();
    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	roadsTileDrawer.shapeRenderer.begin(ShapeType.Line);
    	Gdx.gl20.glLineWidth(3);
    	drawColonyTilesLayer(colonyTile, screenX, screenY, roadsTileDrawer);
    	roadsTileDrawer.shapeRenderer.end();
    	Gdx.gl20.glLineWidth(1);
    	roadsTileDrawer.batch.begin();
	}
	
	private void drawSingleTile(TileDrawer tileDrawer, float px, float py, Tile tile) {
		tileDrawer.mapx = tile.x;
		tileDrawer.mapy = tile.y;
		tileDrawer.tile = tile;
		tileDrawer.tileDrawModel = mapDrawModel.getTileDrawModel(tile.x, tile.y);
		tileDrawer.screenPoint.x = px;
		tileDrawer.screenPoint.y = py;
		tileDrawer.draw();
	}

	public void drawColonyTiles(Tile colonyTile, Batch batch, ShapeRenderer shapeRenderer, float screenX, float screenY) {
    	terrainBackgroundTileDrawer.batch = batch;
    	terrainForegroundTileDrawer.batch = batch;
    	roadsTileDrawer.batch = batch;
    	objectsTileDrawer.batch = batch;
    	colonyLockedTileDrawer.batch = batch;

    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	objectsTileDrawer.shapeRenderer = shapeRenderer;

    	drawColonyTilesLayer(colonyTile, screenX, screenY, terrainBackgroundTileDrawer);
    	drawColonyTilesLayer(colonyTile, screenX, screenY, terrainForegroundTileDrawer);
    	drawColonyTilesRoadLayer(colonyTile, screenX, screenY);
    	drawColonyLockedTiles(colonyTile, screenX, screenY);
    	drawColonyTilesObjects(colonyTile, screenX, screenY);
    	
    	if (drawGrids) {
    	    drawColonyTilesGrids(colonyTile, batch, screenX, screenY);
    	}
	}
	
	private void drawColonyLockedTiles(Tile colonyTile, float screenX, float screenY) {
		colonyLockedTileDrawer.batch.end();

		colonyLockedTileDrawer.polyBatch.setProjectionMatrix(colonyLockedTileDrawer.batch.getProjectionMatrix());
		colonyLockedTileDrawer.polyBatch.setTransformMatrix(colonyLockedTileDrawer.batch.getTransformMatrix());
		
		colonyLockedTileDrawer.polyBatch.begin();
		colonyLockedTileDrawer.polyBatch.enableBlending();
		drawColonyTilesLayer(colonyTile, screenX, screenY, colonyLockedTileDrawer);
		colonyLockedTileDrawer.polyBatch.end();
		
		colonyLockedTileDrawer.batch.begin();
	}

	private void drawColonyTilesGrids(Tile colonyTile, Batch batch, float screenX, float screenY) {
	    batch.end();
	    Gdx.gl20.glLineWidth(3);
	    shapeRenderer.begin(ShapeType.Line);
	    shapeRenderer.setColor(Color.BLACK);
	    
	    Vector2 v = mapToScreenCords(colonyTile.x, colonyTile.y);
	    drawTileGrid(screenX, screenY, v);
	    
	    for (Direction direction : Direction.allDirections) {
            x = direction.stepX(colonyTile.x, colonyTile.y);
            y = direction.stepY(colonyTile.x, colonyTile.y);
            v = mapToScreenCords(x, y);
            drawTileGrid(screenX, screenY, v);
        }
	    
	    shapeRenderer.end();
	    Gdx.gl20.glLineWidth(1);
	    batch.begin();
	}

	private void drawTileGrid(float screenX, float screenY, Vector2 v) {
	    shapeRenderer.line(
	        screenX + v.x + TILE_WIDTH/2f, 
	        screenY + v.y, 
	        screenX + v.x, 
	        screenY + v.y + TILE_HEIGHT/2f
        );
	    shapeRenderer.line(
	        screenX + v.x + TILE_WIDTH/2f, 
	        screenY + v.y, 
	        screenX + v.x + TILE_WIDTH, 
	        screenY + v.y + TILE_HEIGHT/2f
        );
	}
	
	private void drawColonyTilesObjects(Tile colonyTile, float screenX, float screenY) {
		TileDrawModel tileDrawModel = mapDrawModel.getTileDrawModel(colonyTile.x, colonyTile.y);
		Vector2 v = mapToScreenCords(colonyTile.x, colonyTile.y);
		tileDrawModel.drawSettlementImage(objectsTileDrawer.batch, screenX + v.x, screenY + v.y);
		
		for (Direction direction : Direction.allDirections) {
			x = direction.stepX(colonyTile.x, colonyTile.y);
			y = direction.stepY(colonyTile.x, colonyTile.y);
			tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
			if (tileDrawModel.settlementImage != null) {
				v = mapToScreenCords(x, y);
				tileDrawModel.drawSettlementImage(objectsTileDrawer.batch, screenX + v.x, screenY + v.y);
			}
		}
	}

	private void drawColonyTilesLayer(Tile colonyTile, float screenX, float screenY, TileDrawer tileDrawer) {
    	Vector2 v = mapToScreenCords(colonyTile.x, colonyTile.y);
    	drawSingleTile(tileDrawer, screenX + v.x, screenY + v.y, colonyTile);
    	for (Direction direction : Direction.allDirections) {
    		Tile tile = mapDrawModel.map.getTile(colonyTile.x, colonyTile.y, direction);
    		v = mapToScreenCords(tile.x, tile.y);
    		drawSingleTile(tileDrawer, screenX + v.x, screenY + v.y, tile);
    	}
	}
	
	public Tile getColonyTileByScreenCords(Tile colonyTile, int screenX, int screenY) {
		Point p = screenToMapCords(screenX, screenY);
		if (p.equals(colonyTile.x, colonyTile.y)) {
			return colonyTile;
		}
		
		int x;
		int y;
		for (Direction direction : Direction.allDirections) {
			x = direction.stepX(colonyTile.x, colonyTile.y);
			y = direction.stepY(colonyTile.x, colonyTile.y);
			if (p.equals(x, y)) {
				return mapDrawModel.map.getTile(x, y);
			}
		}
		return null;
	}

	public void showColonyLockedTiles(Colony colony) {
		if (colonyLockedTileDrawer == null) {
			colonyLockedTileDrawer = new ColonyLockedTileDrawer(mapDrawModel);
		}
		colonyLockedTileDrawer.update(colony);
	}
	
	public void showTileOwners() {
		tileOwnerDrawer = new TileOwnerDrawer(mapDrawModel);
	}

	public void showTileDebugStrings(String strings[][]) {
		if (tileDebugStringDrawer == null) {
			tileDebugStringDrawer = new TileDebugStringDrawer(mapDrawModel);
		}
		tileDebugStringDrawer.initStrings(strings);
	}
	
	public void hideTileDebugStrings() {
	    tileDebugStringDrawer = null;
	}
	
	public void hideTileOwners() {
		tileOwnerDrawer.dispose();
		tileOwnerDrawer = null;
	}
}
