package promitech.colonization.actors;

import java.util.Collections;
import java.util.LinkedList;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.SpiralIterator;
import promitech.colonization.gdx.Frame;
import promitech.colonization.gdx.SortableTexture;
import promitech.colonization.math.Point;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

class TileDrawModel {
	private LinkedList<SortableTexture> backgroundTerainTextures = new LinkedList<SortableTexture>();
	private LinkedList<Frame> foregroundTerainTextures = new LinkedList<Frame>();
	private boolean fogOfWar = true;
	Frame settlementImage;
	
	public void draw(Batch batch, float rx, float ry) {
		for (SortableTexture texture : backgroundTerainTextures) {
			batch.draw(texture.texture, rx, ry);
		}
	}
	
	public void drawOverlay(Batch batch, float rx, float ry) {
		for (Frame frame : foregroundTerainTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}

	public void drawSettlementImage(Batch batch, float rx, float ry) {
		if (settlementImage != null) {
			batch.draw(settlementImage.texture, rx + settlementImage.offsetX, ry + settlementImage.offsetY);
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

	public void clear() {
		backgroundTerainTextures.clear();
		foregroundTerainTextures.clear();
		fogOfWar = true;
	}
	
	public String toString() {
		return "drawModel backgroundTerains: " + backgroundTerainTextures.size() + 
				", foregroundTerains: " + foregroundTerainTextures.size();
	}

	public boolean isFogOfWar() {
		return fogOfWar;
	}

	public void withoutFogOfWar() {
		this.fogOfWar = false;
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
	private MapDrawModel mapDrawModel;
	
	private SpiralIterator spiralIterator;
	
	private Frame frame;
	private Texture img;
	
	public TileDrawModelInitializer(Map map, Player player, GameResources gameResources) {
		this.map = map;
		this.player = player;
		this.gameResources = gameResources;
		
		if (player == null) {
			throw new IllegalArgumentException("player should not be null");
		}
		
		spiralIterator = new SpiralIterator(map.width, map.height);
	}
	
	public void initMapTiles(MapDrawModel mapDrawModel) {
		this.mapDrawModel = mapDrawModel;
		
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
		
		fogOfWarForUnits();
		fogOfWarForSettlements();
	}

	private void fogOfWarForSettlements() {
		for (Settlement settlement : player.settlements.entities()) {
			x = settlement.tile.x;
			y = settlement.tile.y;
			int visibleRadius = settlement.settlementType.getVisibleRadius();
			initFogOfWarForNeighboursTiles(visibleRadius);
		}
	}

	private void fogOfWarForUnits() {
		for (Unit unit : player.units.entities()) {
			int radius = unit.lineOfSight();
			x = unit.tile.x;
			y = unit.tile.y;
			initFogOfWarForNeighboursTiles(radius);
		}
	}

	private void initFogOfWarForNeighboursTiles(int radius) {
		TileDrawModel t = mapDrawModel.getTileDrawModel(x, y);;
		t.withoutFogOfWar();
		spiralIterator.reset(x, y, true, radius);
		while (spiralIterator.hasNext()) {
			t = mapDrawModel.getTileDrawModel(spiralIterator.getX(), spiralIterator.getY());
			t.withoutFogOfWar();
			spiralIterator.next();
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
		if (tile.type.getId().equals("model.tile.hills")) {
			tileDrawModel.addForegroundTerainTexture(gameResources.hills());
		}
		if (tile.type.getId().equals("model.tile.mountains")) {
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
		
		if (tile.getSettlement() != null) {
			String key = tile.getSettlement().getImageKey();
			tileDrawModel.settlementImage = gameResources.getCenterAdjustFrameTexture(key);
		}
	}
}

public class MapDrawModel {
	private int width;
	private int height;
	
	private TileDrawModel[][] tilesDrawModel;
	final Point unitFocus = new Point(-1, -1);
	public Tile selectedTile;
	public Player playingPlayer;
	
	public void initialize(Map map, Player player, GameResources gameResources) {
		this.playingPlayer = player;
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
