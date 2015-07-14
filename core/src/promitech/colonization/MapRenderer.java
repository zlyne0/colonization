package promitech.colonization;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import promitech.colonization.gdx.Frame;
import promitech.colonization.math.Point;

public class MapRenderer {
	public static final int TILE_WIDTH = 128;
	public static final int TILE_HEIGHT = 64;
	
	public abstract class TileDrawer {
		protected SpriteBatch batch;
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
	
    private final GameResources gameResources;
    private final TerainBackgroundTileDrawer terainBackgroundTileDrawer = new TerainBackgroundTileDrawer();
    private final TerainForegroundTileDrawer terainForegroundTileDrawer = new TerainForegroundTileDrawer();

    public final Vector2 cameraPosition = new Vector2();
    private final int screenWidth;
    private final int screenHeight;
    
    private DebugInformationRenderer debugInformationRenderer; 
	public boolean debug = true;
	private final Point screenMin = new Point();
	private final Point screenMax = new Point();
	private int x;
	private int y;
	
    public MapRenderer(GameResources gameResources, int screenWidth, int screenHeight) {
    	this.screenWidth = screenWidth;
    	this.screenHeight = screenHeight;
    	this.gameResources = gameResources;
        debugInformationRenderer = new DebugInformationRenderer(this);
        debugInformationRenderer.gameResources = gameResources;
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
    
    public void render(SpriteBatch batch, final Map map) {
    	terainBackgroundTileDrawer.batch = batch;
    	terainBackgroundTileDrawer.map = map;
    	
    	terainForegroundTileDrawer.batch = batch;
    	terainForegroundTileDrawer.map = map;
    	
    	preparePartOfMapToRender(map);
    	drawLayer(terainBackgroundTileDrawer);
    	drawLayer(terainForegroundTileDrawer);
    	
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
			tile.addTexture(new SortableTexture(img));
			return;
		}
		
		img = gameResources.tile(tile.type, x, y);
		tile.addTexture(new SortableTexture(img, tile.type.getOrder()));
		
		// add images for beach
		if (tile.type.isWater() && tile.style > 0) {
			int edgeStyle = tile.style >> 4;
    		if (edgeStyle > 0) {
    			img = gameResources.tileEdge(edgeStyle, x, y);
    			tile.addTexture(new SortableTexture(img, tile.type.getOrder()));
    		}
    		int cornerStyle = tile.style & 15;
    		if (cornerStyle > 0) {
    			img = gameResources.tileCorner(cornerStyle, x, y);
    			tile.addTexture(new SortableTexture(img, tile.type.getOrder()));
    		}
		}
	}
	
	private void renderBordersAndRivers(Tile tile, Tile borderTile, Direction direction, int x, int y, Player player) {
		Texture img;
		
		if (borderTile.isUnexplored(player)) {
			img = gameResources.unexploredBorder(direction, x, y);
			tile.addTexture(new SortableTexture(img));
			return;
		}
		
		if (tile.type.hasTheSameTerain(borderTile.type)) {
			return;
		}
		
		if (tile.type.isWater() || borderTile.type.isWater()) {
			if (!tile.type.isWater() && borderTile.type.isWater()) {
				direction = direction.getReverseDirection();
				img = gameResources.tileBorder(tile.type, direction, x, y);
				borderTile.addTexture(new SortableTexture(img, tile.type.getOrder()));
			}
			if (tile.type.isWater() && !tile.type.isHighSea() && borderTile.type.isHighSea()) {
				direction = direction.getReverseDirection();
				img = gameResources.tileBorder(tile.type, direction, x, y);
				borderTile.addTexture(new SortableTexture(img, tile.type.getOrder()));
			}
		} else {
			direction = direction.getReverseDirection();
			img = gameResources.tileBorder(tile.type, direction, x, y);
			borderTile.addTexture(new SortableTexture(img, tile.type.getOrder()));
		}
		
		if (Direction.longSides.contains(direction) && tile.type.isWater()) {
			TileImprovement riverTileImprovement = borderTile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			if (riverTileImprovement != null) {
				img = gameResources.riverDelta(direction, riverTileImprovement);
				tile.addTexture(new SortableTexture(img, -1));
			}
		}
	}
	
	private void renderResources(Tile tile, int x, int y, Player player) {
		if (tile.isUnexplored(player)) {
			return;
		}
		
		Frame frame;
		
		if (tile.type.getTypeStr().equals("model.tile.hills")) {
			tile.addOverlayTexture(gameResources.hills());
		}
		if (tile.type.getTypeStr().equals("model.tile.mountains")) {
			tile.addOverlayTexture(gameResources.mountainsKey());
		}
		// draw forest with river
		if (tile.type.isForested()) {
			TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			frame = gameResources.forestImg(tile.type, riverTileImprovement);
			if (frame != null) {
				tile.addOverlayTexture(frame);
			}
		}
		for (TileImprovement tileImprovement : tile.tileImprovements) {
			if (tileImprovement.type.isRiver()) {
				tile.addOverlayTexture(gameResources.river(tileImprovement.style));
			}
		}
		for (TileResource tileResource : tile.tileResources) {
			frame = gameResources.tileResource(tileResource.getResourceType());
			tile.addOverlayTexture(frame);
		}
		
		if (tile.lostCityRumour) {
			tile.addOverlayTexture(gameResources.tileLastCityRumour());
		}
		
		if (tile.indianSettlement != null) {
		    String key = tile.indianSettlement.getImageKey();
		    tile.addOverlayTexture(gameResources.getCenterAdjustFrameTexture(key));
		}
		if (tile.colony != null) {
            String key = tile.colony.getImageKey();
            tile.addOverlayTexture(gameResources.getCenterAdjustFrameTexture(key));
		}
	}
}
