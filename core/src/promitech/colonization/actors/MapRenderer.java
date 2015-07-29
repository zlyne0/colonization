package promitech.colonization.actors;

import java.util.HashMap;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.actors.MapRenderer.TileDrawer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.math.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;

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
		protected Batch batch;
		protected ShapeRenderer shapeRenderer;
		protected final Point screenPoint = new Point(); 
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
	
	private static class TerainBackgroundTileDrawer extends TileDrawer {
		@Override
		public void draw() {
			tileDrawModel.draw(batch, screenPoint.x, screenPoint.y);
		}
	}

	private static class TerainForegroundTileDrawer extends TileDrawer {
		@Override
		public void draw() {
			tileDrawModel.drawOverlay(batch, screenPoint.x, screenPoint.y);
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
			tileDrawModel.drawSettlementImage(batch, screenPoint.x, screenPoint.y);
			if (tileDrawModel.isFogOfWar()) {
				return;
			}
			
			if (tile.units.size() > 0 && !tile.hasSettlement()) {
				Unit firstUnit = tile.units.first();
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
			
			if (tile.units.size() > 1) {
				shapeRenderer.setColor(Color.WHITE);
				int max = tile.units.size() > 10 ? 10 : tile.units.size(); 
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
	
	private static class FogOfWarDrawer extends TileDrawer {
		public static final Color FOG_OF_WAR_COLOR = new Color(0f, 0f, 0f, 0.3f);

		private PolygonSpriteBatch polyBatch;
		private PolygonSprite poly;
		
		public FogOfWarDrawer() {
			int w = MapRenderer.TILE_WIDTH;
			int h = MapRenderer.TILE_HEIGHT;

//	         0
//	       /   \
//	      3-----1
//	       \   /
//	         2 
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(FOG_OF_WAR_COLOR); 
            pix.fill();
            Texture textureSolid = new Texture(pix);
            PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid),
            	new float[] {       
                  	w/2,  0,        
                  	w,  h/2,        
                  	w/2,  h,        
                  	0,  h/2         
                }, new short[] {
                	0, 1, 3,
                    3, 1, 2         
                }
            );
            poly = new PolygonSprite(polyReg);
            polyBatch = new PolygonSpriteBatch();
		}
		
		@Override
		public void draw() {
			if (tileDrawModel.isFogOfWar()) {
	            poly.setPosition(screenPoint.x, screenPoint.y);
	            poly.draw(polyBatch);
			}
		}
	}
	
	private final Map map; 
	private final MapDrawModel mapDrawModel;
	private final Player playerMap;
	private final GameResources gameResources;
	
    private final TerainBackgroundTileDrawer terainBackgroundTileDrawer = new TerainBackgroundTileDrawer();
    private final TerainForegroundTileDrawer terainForegroundTileDrawer = new TerainForegroundTileDrawer();
    private final RoadsTileDrawer roadsTileDrawer;
    private final ObjectsTileDrawer objectsTileDrawer = new ObjectsTileDrawer();
    private final FogOfWarDrawer fogOfWarDrawer = new FogOfWarDrawer();

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
    	this.playerMap = playerMap;
    	this.map = map;
    	this.mapDrawModel = mapDrawModel;
    	this.gameResources = gameResources;
    	
        roadsTileDrawer = new RoadsTileDrawer(gameResources);
        
    	terainBackgroundTileDrawer.map = map;
    	terainForegroundTileDrawer.map = map;
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
    
    private void drawLayer(TileDrawer tileDrawer) {
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
    	terainBackgroundTileDrawer.batch = batch;
    	terainForegroundTileDrawer.batch = batch;
    	roadsTileDrawer.batch = batch;
    	objectsTileDrawer.batch = batch;
    	fogOfWarDrawer.batch = batch;

    	preparePartOfMapToRender(map);
    	
    	drawLayer(terainBackgroundTileDrawer);
    	drawLayer(terainForegroundTileDrawer);
    	drawRoadLayer(batch);
    	drawLayer(objectsTileDrawer);
    	drawFogOfWarLayer(batch);
    }
    
    private void drawFogOfWarLayer(Batch batch) {
    	batch.end();
    	fogOfWarDrawer.polyBatch.setProjectionMatrix(batch.getProjectionMatrix());
    	fogOfWarDrawer.polyBatch.setTransformMatrix(batch.getTransformMatrix());
    	
    	fogOfWarDrawer.polyBatch.begin();
    	fogOfWarDrawer.polyBatch.enableBlending();
    	drawLayer(fogOfWarDrawer);
    	fogOfWarDrawer.polyBatch.end();
    	
    	batch.begin();
	}

    private void drawRoadLayer(Batch batch) {
    	batch.end();
    	roadsTileDrawer.shapeRenderer.begin(ShapeType.Line);
    	Gdx.gl20.glLineWidth(3);
    	drawLayer(roadsTileDrawer);
    	roadsTileDrawer.shapeRenderer.end();
    	Gdx.gl20.glLineWidth(1);
    	batch.begin();
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
    
}
