package promitech.colonization;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.MapRenderer.TileDrawer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.math.Point;

class RoadsTileDrawer extends TileDrawer {
	private int SAMPLE_POINTS = 100;
	private float SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS;
	
	// tile width and height
	private int w = MapRenderer.TILE_WIDTH;
	private int h = MapRenderer.TILE_HEIGHT;
	
	private java.util.Map<Direction,Vector2> edgePoints = new HashMap<Direction, Vector2>();
	private final Color roadColor;
	
	// paths from direction to direction
	private Path<Vector2>[][] paths = null;
	private Vector2 centerOfTile = new Vector2(w/2, h/2);
	Player renderForPlayer;

	private final Vector2 p1 = new Vector2();
	private final Vector2 p2 = new Vector2();
	private boolean tmpPointInited = false;
	private int borderRoadCount = 0;
	
	public RoadsTileDrawer(GameResources gameResources) {
		roadColor = gameResources.getColor("road.color");
		
		edgePoints.put(Direction.N,  new Vector2(w / 2, h));
		edgePoints.put(Direction.NE, new Vector2(w * 0.75f, h * 0.75f ));
		edgePoints.put(Direction.E,  new Vector2(w, h / 2));
		edgePoints.put(Direction.SE, new Vector2(w * 0.75f, h * 0.25f ));
		edgePoints.put(Direction.S,  new Vector2(w / 2, 0));
		edgePoints.put(Direction.SW, new Vector2(w * 0.25f, h * 0.25f ));
		edgePoints.put(Direction.W,  new Vector2(0, h / 2));
		edgePoints.put(Direction.NW, new Vector2(w * 0.25f, h * 0.75f ));
		
		initPaths();
	}
	
	private void initPaths() {
		paths = new Path[Direction.values().length][Direction.values().length];
		
		for (Direction dSrc : Direction.values()) {
			for (Direction dDest : Direction.values()) {
				paths[dSrc.ordinal()][dDest.ordinal()] = new Bezier<Vector2>(edgePoints.get(dSrc), centerOfTile, edgePoints.get(dDest));
			}
		}
	}
	
	private void draw(Direction src, Direction dest) {
		tmpPointInited = false;
		if (src == dest) {
			throw new IllegalArgumentException("can not draw road from the same source and destination direction (" + src + ")");
		} 
		Path<Vector2> road = paths[src.ordinal()][dest.ordinal()];
		
		float val = 0f;
		while (val <= 1f) {
			if (!tmpPointInited) {
				road.valueAt(p1, val);
				val += SAMPLE_POINT_DISTANCE;
				road.valueAt(p2, val);
				tmpPointInited = true;
			} else {
				p1.set(p2);
				road.valueAt(p2, val);
			}
			shapeRenderer.line(
					p1.x + screenPoint.x, p1.y + screenPoint.y, 
					p2.x + screenPoint.x, p2.y + screenPoint.y
			);
			val += SAMPLE_POINT_DISTANCE;
		}
	}

	@Override
	public void draw() {
		if (tile.isUnexplored(renderForPlayer) || !tile.hasRoad()) {
			return;
		}
		borderRoadCount = 0;
		shapeRenderer.setColor(roadColor);
		Direction directionForOneRoad = null;
		
		for (int iSrc = 0; iSrc < Direction.values().length; iSrc++) {
			Direction srcDirection = Direction.values()[iSrc];
			Tile srcTile = map.getTile(mapx, mapy, srcDirection);
			if (srcTile == null || !srcTile.hasRoad()) {
				continue;
			}
			borderRoadCount++;
			directionForOneRoad = srcDirection;
			
			for (int iDest = iSrc + 1; iDest < Direction.values().length; iDest++) {
				Direction destDirection = Direction.values()[iDest];
				Tile destTile = map.getTile(mapx, mapy, destDirection);
				if (destTile == null) {
					continue;
				}
				if (!destTile.hasRoad()) {
					continue;
				}
				draw(srcDirection, destDirection);
			}
		}
		if (borderRoadCount == 0) {
			if (!tile.hasSettlement()) {
				shapeRenderer.ellipse(
						centerOfTile.x + screenPoint.x - 25, centerOfTile.y + screenPoint.y - 12, 
						50, 25
				);
			}
		} else {
			if (borderRoadCount == 1) {
				Vector2 p2 = edgePoints.get(directionForOneRoad);
				shapeRenderer.line(
						centerOfTile.x + screenPoint.x, centerOfTile.y + screenPoint.y,
						p2.x + screenPoint.x, p2.y + screenPoint.y
				);
			}
		}
	}
}

public class MapRenderer {
	public static final int TILE_WIDTH = 128;
	public static final int TILE_HEIGHT = 64;
	
	public static abstract class TileDrawer {
		protected SpriteBatch batch;
		protected ShapeRenderer shapeRenderer;
		protected final Point screenPoint = new Point(); 
		protected int mapx;
		protected int mapy;
		
		protected Map map;
		protected Tile tile;
		
		public boolean areCoordinatesOnMap() {
			return mapx >= 0 && mapx < map.width && mapy >= 0 && mapy < map.height;
		}
		
		public abstract void draw();		
	}
	
	private class TerainBackgroundTileDrawer extends TileDrawer {
		@Override
		public void draw() {
			tile.draw(batch, screenPoint.x, screenPoint.y);
		}
	}

	private class TerainForegroundTileDrawer extends TileDrawer {
		@Override
		public void draw() {
			tile.drawOverlay(batch, screenPoint.x, screenPoint.y);
		}
	}
	
	private class ObjectsTileDrawer extends TileDrawer {
		
		private static final int UNIT_IMAGE_OFFSET = 20;
		
		private BitmapFont font = new BitmapFont(false);
		protected Player player;
		
		private int w = MapRenderer.TILE_WIDTH;
		private int h = MapRenderer.TILE_HEIGHT;

		@Override
		public void draw() {
			if (tile.isUnexplored(player)) {
				return;
			}
			tile.drawObjects(batch, screenPoint.x, screenPoint.y);
			
			if (tile.unitsCount() > 0 && !tile.hasSettlement()) {
				Unit firstUnit = tile.firstUnit();
				Frame frame = gameResources.getCenterAdjustFrameTexture(firstUnit.resourceImageKey());
				drawUnit(firstUnit, frame);
			}
		}
		
		private void drawUnit(Unit unit, Frame frame) {
			batch.draw(frame.texture, 
					screenPoint.x + frame.offsetX, screenPoint.y + frame.offsetY + UNIT_IMAGE_OFFSET
			);
			
			drawUnitChip(unit);
		}
		
		private void drawUnitChip(Unit unit) {
			int cx = screenPoint.x + 35;
			int cy = screenPoint.y + h - 5;
			
			batch.end();
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(unit.getOwner().getNation().getColor());
			shapeRenderer.rect(cx, cy, 17, 20);
			shapeRenderer.end();

			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.rect(cx, cy, 17, 20);
			
			if (tile.unitsCount() > 1) {
				shapeRenderer.setColor(Color.WHITE);
				int max = tile.unitsCount() > 10 ? 10 : tile.unitsCount(); 
				for (int i=0; i<max; i++) {
					shapeRenderer.line(cx - 7, cy - (i*2) + 19, cx - 1, cy - (i*2) + 19);
				}
			}
			
			shapeRenderer.end();
			
			batch.begin();
			font.setColor(Color.BLACK);
			font.draw(batch, "G", cx + 2, cy + 20 - 2);
		}
	}
	
	private final SpriteBatch batch; 
	private final Map map; 
	private final GameResources gameResources;
	
    private final TerainBackgroundTileDrawer terainBackgroundTileDrawer = new TerainBackgroundTileDrawer();
    private final TerainForegroundTileDrawer terainForegroundTileDrawer = new TerainForegroundTileDrawer();
    private final RoadsTileDrawer roadsTileDrawer;
    private final ObjectsTileDrawer objectsTileDrawer = new ObjectsTileDrawer();

    public final Vector2 cameraPosition = new Vector2();
    private final int screenWidth;
    private final int screenHeight;
    
    private DebugInformationRenderer debugInformationRenderer; 
	public boolean debug = true;
	private final Point screenMin = new Point();
	private final Point screenMax = new Point();
	private int x;
	private int y;
	
	
    public MapRenderer(Map map, GameResources gameResources, int screenWidth, int screenHeight, 
    		SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) 
    {
    	this.screenWidth = screenWidth;
    	this.screenHeight = screenHeight;
    	this.gameResources = gameResources;
        debugInformationRenderer = new DebugInformationRenderer(this);
        debugInformationRenderer.gameResources = gameResources;
        
        roadsTileDrawer = new RoadsTileDrawer(gameResources);
        
        this.map = map;
        this.batch = spriteBatch;
        
    	terainBackgroundTileDrawer.batch = batch;
    	terainBackgroundTileDrawer.map = map;
    	
    	terainForegroundTileDrawer.batch = batch;
    	terainForegroundTileDrawer.map = map;
    	
    	roadsTileDrawer.map = map;
    	roadsTileDrawer.batch = batch;
    	roadsTileDrawer.shapeRenderer = shapeRenderer;
    	
    	objectsTileDrawer.batch = batch;
    	objectsTileDrawer.map = map;
    	objectsTileDrawer.shapeRenderer = shapeRenderer;
    }
    
    private void drawLayer(TileDrawer tileDrawer) {
    	for (y=screenMin.y; y<screenMax.y; y++) {
    		for (x=screenMin.x; x<screenMax.x; x++) {
    			tileDrawer.mapx = x;
    			tileDrawer.mapy = y;
    			tileDrawer.tile = tileDrawer.map.getTile(x, y);
    			mapToScreenCords(x, y, tileDrawer.screenPoint);
    			tileDrawer.draw();
    		}
    	}
    }
    
    private void preparePartOfMapToRender(final Map map) {
    	screenToMapCords(0-TILE_WIDTH, 0-TILE_HEIGHT, screenMin);
    	screenToMapCords(screenWidth + TILE_WIDTH*2, screenHeight + TILE_HEIGHT, screenMax);
    	
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
    
    public void render(final Player player) {
    	
    	preparePartOfMapToRender(map);
    	drawLayer(terainBackgroundTileDrawer);

    	
    	drawLayer(terainForegroundTileDrawer);
    	
    	batch.end();
    	roadsTileDrawer.renderForPlayer = player;
    	roadsTileDrawer.shapeRenderer.begin(ShapeType.Line);
    	Gdx.gl20.glLineWidth(3);
    	drawLayer(roadsTileDrawer);
    	roadsTileDrawer.shapeRenderer.end();
    	Gdx.gl20.glLineWidth(1);
    	batch.begin();
    	
    	objectsTileDrawer.player = player;
    	drawLayer(objectsTileDrawer);
    	
        if (debug) {
        	debugInformationRenderer.render(batch, map);
        }
    }
    
    private void mapToScreenCords(int x, int y, Point p) {
        p.x = (TILE_WIDTH * x) + ((y % 2 == 1) ? TILE_HEIGHT : 0);
        p.y = (TILE_HEIGHT / 2) * y;
        
        p.x += cameraPosition.x;
        p.y += cameraPosition.y;
    	
        p.y = screenHeight - p.y;
    }
    
    public void screenToMapCords(int screenX, int screenY, Point p) {
        double x, y, pX, pY;

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
		cameraPosition.set(0, 0);
    	screenToMapCords(0-TILE_WIDTH, 0-TILE_HEIGHT, screenMin);
    	screenToMapCords(screenWidth + TILE_WIDTH*2, screenHeight + TILE_HEIGHT, screenMax);
		
    	int halfX = (screenMax.x - screenMin.x) / 2; 
    	int halfY = (screenMax.y - screenMin.y) / 2; 
    	
		cameraPosition.set(-(mapx - halfX/2) * TILE_WIDTH, -(mapy - halfY) * TILE_HEIGHT/2 );
		cameraPosition.add(TILE_WIDTH/2, 0);
	}
    
	public void initMapTiles(Map map, Player player) {
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				Tile tile = map.getTile(x, y);
				renderTerainAndBeaches(tile, player, x, y);
			}
		}
		
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				Tile tile = map.getTile(x, y);
				if (tile.isUnexplored(player)) {
					continue;
				}
	            for (Direction direction : Direction.values()) {
					Tile borderTile = map.getTile(x, y, direction);
					if (borderTile == null) {
						continue;
					}
					renderBordersAndRivers(tile, borderTile, direction, x, y, player);
				}
			}
		}
		
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				Tile tile = map.getTile(x, y);
				renderResources(tile, x, y, player);
			}
		}
		
	}

	private void renderTerainAndBeaches(Tile tile, Player player, int x, int y) {
		Texture img;
		
		if (tile.isUnexplored(player)) {
			img = gameResources.unexploredTile(x, y);
			tile.addBackgroundTerainTexture(new SortableTexture(img));
			return;
		}
		
		img = gameResources.tile(tile.type, x, y);
		tile.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
		
		// add images for beach
		if (tile.type.isWater() && tile.style > 0) {
			int edgeStyle = tile.style >> 4;
    		if (edgeStyle > 0) {
    			img = gameResources.tileEdge(edgeStyle, x, y);
    			tile.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
    		}
    		int cornerStyle = tile.style & 15;
    		if (cornerStyle > 0) {
    			img = gameResources.tileCorner(cornerStyle, x, y);
    			tile.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
    		}
		}
	}
	
	private void renderBordersAndRivers(Tile tile, Tile borderTile, Direction direction, int x, int y, Player player) {
		Texture img;
		
		if (borderTile.isUnexplored(player)) {
			img = gameResources.unexploredBorder(direction, x, y);
			tile.addBackgroundTerainTexture(new SortableTexture(img));
			return;
		}
		
		if (tile.type.hasTheSameTerain(borderTile.type)) {
			return;
		}
		
		if (tile.type.isWater() || borderTile.type.isWater()) {
			if (!tile.type.isWater() && borderTile.type.isWater()) {
				direction = direction.getReverseDirection();
				img = gameResources.tileBorder(tile.type, direction, x, y);
				borderTile.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
			}
			if (tile.type.isWater() && !tile.type.isHighSea() && borderTile.type.isHighSea()) {
				direction = direction.getReverseDirection();
				img = gameResources.tileBorder(tile.type, direction, x, y);
				borderTile.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
			}
		} else {
			direction = direction.getReverseDirection();
			img = gameResources.tileBorder(tile.type, direction, x, y);
			borderTile.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
		}
		
		if (Direction.longSides.contains(direction) && tile.type.isWater()) {
			TileImprovement riverTileImprovement = borderTile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			if (riverTileImprovement != null) {
				img = gameResources.riverDelta(direction, riverTileImprovement);
				tile.addBackgroundTerainTexture(new SortableTexture(img, -1));
			}
		}
	}
	
	private void renderResources(Tile tile, int x, int y, Player player) {
		if (tile.isUnexplored(player)) {
			return;
		}
		
		Frame frame;
		
		if (tile.isPlowed()) {
			tile.addForegroundTerainTexture(gameResources.plowed());
		}
		if (tile.type.getTypeStr().equals("model.tile.hills")) {
			tile.addForegroundTerainTexture(gameResources.hills());
		}
		if (tile.type.getTypeStr().equals("model.tile.mountains")) {
			tile.addForegroundTerainTexture(gameResources.mountainsKey());
		}
		// draw forest with river
		if (tile.type.isForested()) {
			TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			frame = gameResources.forestImg(tile.type, riverTileImprovement);
			if (frame != null) {
				tile.addForegroundTerainTexture(frame);
			}
		}
		for (TileImprovement tileImprovement : tile.tileImprovements) {
			if (tileImprovement.type.isRiver()) {
				tile.addForegroundTerainTexture(gameResources.river(tileImprovement.style));
			}
		}
		for (TileResource tileResource : tile.tileResources) {
			frame = gameResources.tileResource(tileResource.getResourceType());
			tile.addForegroundTerainTexture(frame);
		}
		
		if (tile.lostCityRumour) {
			tile.addForegroundTerainTexture(gameResources.tileLastCityRumour());
		}
		
		
		if (tile.indianSettlement != null) {
		    String key = tile.indianSettlement.getImageKey();
		    tile.addObjectTexture(gameResources.getCenterAdjustFrameTexture(key));
		}
		if (tile.colony != null) {
            String key = tile.colony.getImageKey();
            tile.addObjectTexture(gameResources.getCenterAdjustFrameTexture(key));
		}
	}
}
