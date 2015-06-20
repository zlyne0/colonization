package promitech.colonization;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.TileType;
import promitech.colonization.gdx.Frame;
import promitech.colonization.math.Point;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MapRenderer {
	public static final int TILE_WIDTH = 128;
	public static final int TILE_HEIGHT = 64;
	
	public abstract class TileDrawer {
		protected SpriteBatch batch;
		protected final Point p = new Point();
		protected int mapx;
		protected int mapy;
		
		protected Map map;
		protected Tile tile;
		
		public boolean areCoordinatesOnMap() {
			return mapx >= 0 && mapx < map.width && mapy >= 0 && mapy < map.height;
		}
		
		public abstract void draw();
		
		public void drawLayerTile() {
		}
	}
	
	private class BackgroundTileDrawer extends TileDrawer {
		@Override
		public void draw() {
			tile = map.getTile(mapx, mapy);
			drawLayerTile();
		}
	}
	
    private final GameResources gameResources;
    private final BackgroundTileDrawer backgroundTileDrawer = new BackgroundTileDrawer();

    public Vector2 cameraPosition = new Vector2();
    
    private DebugInformationRenderer debugInformationRenderer; 
	public boolean debug = true;
    
    public MapRenderer(GameResources gameResources) {
    	this.gameResources = gameResources;
        debugInformationRenderer = new DebugInformationRenderer(this);
        debugInformationRenderer.gameResources = gameResources;
    }
    
    
    public void render(SpriteBatch batch, final Map map) {
    	backgroundTileDrawer.batch = batch;
    	backgroundTileDrawer.map = map;
    	
    	int x, y;
    	int rx = 0;
    	int ry = 0;
    	Tile tile;
    	for (y=0; y<map.height; y++) {
    		for (x=0; x<map.width; x++) {
    			//yy = map.height - y - 1;
                rx = (TILE_WIDTH * x) + ((y % 2 == 1) ? TILE_HEIGHT : 0);
                ry = (TILE_HEIGHT / 2) * y;
                
                rx += cameraPosition.x;
                ry += cameraPosition.y;
                
                tile = map.getTile(x, y);
                tile.draw(batch, rx, 500 - ry);
    		}
    	}

    	for (y=0; y<map.height; y++) {
    		for (x=0; x<map.width; x++) {
    			//yy = map.height - y - 1;
                rx = (TILE_WIDTH * x) + ((y % 2 == 1) ? TILE_HEIGHT : 0);
                ry = (TILE_HEIGHT / 2) * y;
                
                rx += cameraPosition.x;
                ry += cameraPosition.y;
                
                tile = map.getTile(x, y);
                tile.drawOverlay(batch, rx, 500 - ry);
    		}
    	}
    	
        if (debug) {
        	debugInformationRenderer.render(batch, map);
        }
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
    
    public boolean isEven(int x, int y) {
        return ((y % 8 <= 2) || ((x + y) % 2 == 0 ));
    }
    
    public String tileEdgeKey(int edgeStyle, int x, int y) {
    	return "model.tile.beach.edge" + edgeStyle + ((isEven(x, y)) ? "_even" : "_odd");    
    }


	public String tileCornerKey(int cornerStyle, int x, int y) {
		return "model.tile.beach.corner" + cornerStyle + ((isEven(x, y)) ? "_even" : "_odd");
	}

	public String tileResourceKey(ResourceType resourceType) {
		return resourceType.getResourceTypeId() + ".image";
	}
	
	public String tileKey(TileType type, int x, int y) {
		if (isEven(x, y)) {
			return type.getTypeStr() + ".center0.image";
		} else {
			return type.getTypeStr() + ".center1.image";
		}
	}

	public String tileBorder(TileType type, Direction direction, int x, int y) {
		return type.getTypeStr() + ".border_" + direction + ((isEven(x, y)) ?  "_even" : "_odd") + ".image";
	}


	public String tileLastCityRumour() {
		return "lostCityRumour.image";
	}
    
	public String riverDelta(Direction direction, TileImprovement tileImprovement) {
		return "model.tile.delta_" + direction + (tileImprovement.magnitude == 1 ? "_small" : "_large");		
	}

	public String hillsKey() {
		Randomizer randomizer = Randomizer.getInstance();		
		String keyPrefix = "model.tile.hills.overlay";
		int countForPrefix = gameResources.getCountForPrefix(keyPrefix);
		return keyPrefix + Integer.toString(randomizer.randomInt(countForPrefix)) + ".image";
		
	}

	public String mountainsKey() {
		Randomizer randomizer = Randomizer.getInstance();		
		String keyPrefix = "model.tile.mountains.overlay";
		int countForPrefix = gameResources.getCountForPrefix(keyPrefix);
		return keyPrefix + Integer.toString(randomizer.randomInt(countForPrefix)) + ".image";
	}
	
	public String forestImgKey(TileType type, TileImprovement riverTileImprovement) {
		if (riverTileImprovement != null) {
			return type.getTypeStr() + ".forest" + riverTileImprovement.style;
		} else {
			return type.getTypeStr() + ".forest";
		}
	}

	public void initMapTiles(Map map) {
		Texture terainImage;
		String key;
		
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				Tile tile = map.getTile(x, y);
				
				key = tileKey(tile.type, x, y);
				terainImage = gameResources.getImage(key);
				tile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
				
				// add images for beach
				if (tile.type.isWater() && tile.style > 0) {
					int edgeStyle = tile.style >> 4;
					if (edgeStyle > 0) {
						key = tileEdgeKey(edgeStyle, x, y);
						terainImage = gameResources.getImage(key);
						tile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
					}
					int cornerStyle = tile.style & 15;
					if (cornerStyle > 0) {
						key = tileCornerKey(cornerStyle, x, y);
						terainImage = gameResources.getImage(key);
						tile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
					}
				}
			}
		}
		
		List<Direction> borderDirections = new ArrayList<Direction>();
		borderDirections.addAll(Direction.longSides);
		borderDirections.addAll(Direction.corners);
		
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				Tile tile = map.getTile(x, y);
	            for (Direction direction : Direction.values()) {
					Tile borderTile = map.getTile(x, y, direction);
					if (borderTile == null) {
						continue;
					}
					if (tile.type.hasTheSameTerain(borderTile.type)) {
						continue;
					}
					if (tile.type.isWater() || borderTile.type.isWater()) {
						if (!tile.type.isWater() && borderTile.type.isWater()) {
							direction = direction.getReverseDirection();
							key = tileBorder(tile.type, direction, x, y);
							terainImage = gameResources.getImage(key);
							borderTile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
						}
						if (tile.type.isWater() && !tile.type.isHighSea() && borderTile.type.isHighSea()) {
							direction = direction.getReverseDirection();
							key = tileBorder(tile.type, direction, x, y);
							terainImage = gameResources.getImage(key);
							borderTile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
						}
					} else {
						direction = direction.getReverseDirection();
						key = tileBorder(tile.type, direction, x, y);
						terainImage = gameResources.getImage(key);
						borderTile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
					}
					
					if (Direction.longSides.contains(direction) && tile.type.isWater()) {
						TileImprovement riverTileImprovement = borderTile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
						if (riverTileImprovement != null) {
							key = riverDelta(direction, riverTileImprovement);
							terainImage = gameResources.getImage(key);
							tile.addTexture(new SortableTexture(terainImage, -1));
						}
					}
				}
			}
		}
		
		Frame frame;
		
		for (int y=0; y<map.height; y++) {
			for (int x=0; x<map.width; x++) {
				Tile tile = map.getTile(x, y);
				if (tile.type.getTypeStr().equals("model.tile.hills")) {
					key = hillsKey();
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				if (tile.type.getTypeStr().equals("model.tile.mountains")) {
					key = mountainsKey();
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				if (tile.type.isForested()) {
					TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
					key = forestImgKey(tile.type, riverTileImprovement);
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				for (TileResource tileResource : tile.tileResources) {
					key = tileResourceKey(tileResource.getResourceType());
					frame = gameResources.getCenterAdjustFrameTexture(key);
					tile.addOverlayTexture(frame);
				}
				
				for (TileImprovement tileImprovement : tile.tileImprovements) {
					key = "model.tile.river" + tileImprovement.style;
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				
				if (tile.lostCityRumour) {
					key = tileLastCityRumour();
					frame = gameResources.getCenterAdjustFrameTexture(key);
					tile.addOverlayTexture(frame);
				}
			}
		}
		
	}
	
	
}
