package promitech.colonization.actors;

import java.util.Collections;
import java.util.LinkedList;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.gdx.SortableTexture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

class TileDrawModel {
	private LinkedList<SortableTexture> backgroundTerainTextures = new LinkedList<SortableTexture>();
	private LinkedList<Frame> foregroundTerainTextures = new LinkedList<Frame>();
	private LinkedList<Frame> objectTextures = new LinkedList<Frame>();

	public void draw(Batch batch, int rx, int ry) {
		for (SortableTexture texture : backgroundTerainTextures) {
			batch.draw(texture.texture, rx, ry);
		}
	}
	
	public void drawOverlay(Batch batch, int rx, int ry) {
		for (Frame frame : foregroundTerainTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}

	public void drawObjects(Batch batch, int rx, int ry) {
		for (Frame frame : objectTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}
	
	public void addBackgroundTerainTexture(SortableTexture texture) {
		backgroundTerainTextures.add(texture);
		Collections.sort(backgroundTerainTextures);
	}

	public void addForegroundTerainTexture(Frame frame) {
		if (frame == null) {
			throw new NullPointerException();
		}
		foregroundTerainTextures.add(frame);
	}
	
	public void addObjectTexture(Frame frame) {
		objectTextures.add(frame);
	}

	public void clear() {
		backgroundTerainTextures.clear();
		foregroundTerainTextures.clear();
		objectTextures.clear();
	}
	
	public String toString() {
		return "drawModel backgroundTerains: " + backgroundTerainTextures.size() + 
				", foregroundTerains: " + foregroundTerainTextures.size() +
				", objectTextures: " + objectTextures.size();
	}
	
}

class TileDrawModelInitializer {
	private GameResources gameResources;
	private Map map; 
	private Player player;
	
	private int x, y;
	private Tile tile;
	private Tile borderTile;
	private TileDrawModel tileDrawModel;
	private TileDrawModel borderTileDrawModel;
	
	private Frame frame;
	private Texture img;
	
	public TileDrawModelInitializer(Map map, Player player, GameResources gameResources) {
		this.map = map;
		this.player = player;
		this.gameResources = gameResources;
		
		if (player == null) {
			throw new IllegalArgumentException("player should not be null");
		}
	}
	
	public void initMapTiles(MapDrawModel mapDrawModel) {
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
				renderTerainAndBeaches();
			}
		}
		
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				if (tile.isUnexplored(player)) {
					continue;
				}
				tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
				
	            for (Direction direction : Direction.values()) {
					borderTile = map.getTile(x, y, direction);
					if (borderTile == null) {
						continue;
					}
					borderTileDrawModel = mapDrawModel.getTileDrawModel(x, y, direction);
					renderBordersAndRivers(direction);
				}
			}
		}
		
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
				renderResources();
			}
		}
		
	}

	private void renderTerainAndBeaches() {
		if (tile.isUnexplored(player)) {
			img = gameResources.unexploredTile(x, y);
			tileDrawModel.addBackgroundTerainTexture(new SortableTexture(img));
			return;
		}
		
		img = gameResources.tile(tile.type, x, y);
		tileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
		
		// add images for beach
		if (tile.type.isWater() && tile.style > 0) {
			int edgeStyle = tile.style >> 4;
    		if (edgeStyle > 0) {
    			img = gameResources.tileEdge(edgeStyle, x, y);
    			tileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
    		}
    		int cornerStyle = tile.style & 15;
    		if (cornerStyle > 0) {
    			img = gameResources.tileCorner(cornerStyle, x, y);
    			tileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
    		}
		}
	}
	
	private void renderBordersAndRivers(Direction direction) {
		if (borderTile.isUnexplored(player)) {
			img = gameResources.unexploredBorder(direction, x, y);
			tileDrawModel.addBackgroundTerainTexture(new SortableTexture(img));
			return;
		}
		
		if (tile.type.hasTheSameTerain(borderTile.type)) {
			return;
		}
		
		if (tile.type.isWater() || borderTile.type.isWater()) {
			if (!tile.type.isWater() && borderTile.type.isWater()) {
				direction = direction.getReverseDirection();
				img = gameResources.tileBorder(tile.type, direction, x, y);
				borderTileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
			}
			if (tile.type.isWater() && !tile.type.isHighSea() && borderTile.type.isHighSea()) {
				direction = direction.getReverseDirection();
				img = gameResources.tileBorder(tile.type, direction, x, y);
				borderTileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
			}
		} else {
			direction = direction.getReverseDirection();
			img = gameResources.tileBorder(tile.type, direction, x, y);
			borderTileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, tile.type.getOrder()));
		}
		
		if (Direction.longSides.contains(direction) && tile.type.isWater()) {
			TileImprovement riverTileImprovement = borderTile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			if (riverTileImprovement != null) {
				img = gameResources.riverDelta(direction, riverTileImprovement);
				tileDrawModel.addBackgroundTerainTexture(new SortableTexture(img, -1));
			}
		}
	}
	
	private void renderResources() {
		if (tile.isUnexplored(player)) {
			return;
		}
		
		if (tile.isPlowed()) {
			tileDrawModel.addForegroundTerainTexture(gameResources.plowed());
		}
		if (tile.type.getTypeStr().equals("model.tile.hills")) {
			tileDrawModel.addForegroundTerainTexture(gameResources.hills());
		}
		if (tile.type.getTypeStr().equals("model.tile.mountains")) {
			tileDrawModel.addForegroundTerainTexture(gameResources.mountainsKey());
		}
		// draw forest with river
		if (tile.type.isForested()) {
			TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			frame = gameResources.forestImg(tile.type, riverTileImprovement);
			if (frame != null) {
				tileDrawModel.addForegroundTerainTexture(frame);
			}
		}
		for (TileImprovement tileImprovement : tile.tileImprovements) {
			if (tileImprovement.type.isRiver()) {
				tileDrawModel.addForegroundTerainTexture(gameResources.river(tileImprovement.style));
			}
		}
		for (TileResource tileResource : tile.tileResources) {
			frame = gameResources.tileResource(tileResource.getResourceType());
			tileDrawModel.addForegroundTerainTexture(frame);
		}
		
		if (tile.lostCityRumour) {
			tileDrawModel.addForegroundTerainTexture(gameResources.tileLastCityRumour());
		}
		
		
		if (tile.indianSettlement != null) {
		    String key = tile.indianSettlement.getImageKey();
		    tileDrawModel.addObjectTexture(gameResources.getCenterAdjustFrameTexture(key));
		}
		if (tile.colony != null) {
            String key = tile.colony.getImageKey();
            tileDrawModel.addObjectTexture(gameResources.getCenterAdjustFrameTexture(key));
		}
	}
}

public class MapDrawModel {
	private int width;
	private int height;
	
	private TileDrawModel[][] tilesDrawModel;
	
	public void initialize(Map map, Player player, GameResources gameResources) {
		width = map.width;
		height = map.height;
		
		if (tilesDrawModel == null) {
			tilesDrawModel = new TileDrawModel[height][width];
			
			for (int i=0; i<height; i++) {
				TileDrawModel row[] = tilesDrawModel[i];
				for (int j=0; j<width; j++) {
					row[j] = new TileDrawModel();
				}
			}
		} else {
			for (int i=0; i<height; i++) {
				TileDrawModel row[] = tilesDrawModel[i];
				for (int j=0; j<width; j++) {
					row[j].clear();
				}
			}
		}

		TileDrawModelInitializer initializer = new TileDrawModelInitializer(map, player, gameResources);
		initializer.initMapTiles(this);
	}

	public TileDrawModel getTileDrawModel(int x, int y, Direction direction) {
		return getTileDrawModel(direction.stepX(x, y), direction.stepY(x, y));
	}
	
	public TileDrawModel getTileDrawModel(int x, int y) {
		if (!isCoordinateValid(x, y)) {
			return null;
		}
		TileDrawModel tile = tilesDrawModel[y][x];
		if (tile == null) {
			throw new RuntimeException("not implementd, shoud return empty tile or default");
		}
		return tile;
	}

	public boolean isCoordinateValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	
}
