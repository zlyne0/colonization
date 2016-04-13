package promitech.colonization.actors.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.Path;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class TileDrawModel {
	private final LinkedList<Frame> backgroundTerainTextures = new LinkedList<Frame>();
	private final LinkedList<Frame> foregroundTerainTextures = new LinkedList<Frame>();
	private final LinkedList<Frame> unexploredBorders = new LinkedList<Frame>();
	Frame settlementImage;
	
	public void draw(Batch batch, float rx, float ry) {
		for (Frame frame : backgroundTerainTextures) {
			batch.draw(frame.texture, rx, ry);
		}
		for (Frame frame : unexploredBorders) {
			batch.draw(frame.texture, rx, ry);
		}
	}
	
	public void drawOverlay(Batch batch, float rx, float ry) {
		for (Frame frame : foregroundTerainTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}

	public void drawSettlementImage(Batch batch, float rx, float ry) {
		batch.draw(settlementImage.texture, rx + settlementImage.offsetX, ry + settlementImage.offsetY);
	}
	
	public void addBackgroundTerainTexture(Frame texture) {
		backgroundTerainTextures.add(texture);
		Collections.sort(backgroundTerainTextures);
	}

	public void addForegroundTerainTexture(Frame frame) {
		if (frame == null) {
			throw new IllegalArgumentException("frame should not be null");
		}
		foregroundTerainTextures.add(frame);
	}

	public void addUnexploredBorders(Frame frame) {
		unexploredBorders.add(frame);
	}
	
	public void clear() {
		backgroundTerainTextures.clear();
		foregroundTerainTextures.clear();
	}

	public void resetUnexploredBorders() {
		unexploredBorders.clear();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("drawModel backgroundTerains: " + backgroundTerainTextures.size() + "\n");
		for (Frame f : backgroundTerainTextures){
			sb.append(f.toString()).append("\n");
		}
		return sb.toString() + ", foregroundTerains: " + foregroundTerainTextures.size();
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
	private final MapDrawModel mapDrawModel;
	
	private Frame frame;
	
	public TileDrawModelInitializer(MapDrawModel mapDrawModel, Map map, Player player, GameResources gameResources) {
		this.mapDrawModel = mapDrawModel;
		this.map = map;
		this.player = player;
		this.gameResources = gameResources;
		
		if (player == null) {
			throw new IllegalArgumentException("player should not be null");
		}
	}
	
	public void initMapTiles() {
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
		
		initBordersForUnexploredTiles();
	}
	
	public void initBordersForUnexploredTiles() {
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
				tileDrawModel.resetUnexploredBorders();
				
				for (Direction direction : Direction.values()) {
					borderTile = map.getTile(x, y, direction);
					if (borderTile == null) {
						continue;
					}
					if (!player.isTileExplored(borderTile.x, borderTile.y)) {
						frame = gameResources.unexploredBorder(direction, x, y);
						tileDrawModel.addUnexploredBorders(frame);
					}
				}
			}
		}
	}
	
	private void renderTerainAndBeaches() {
		
		frame = gameResources.tile(tile.type, x, y);
		tileDrawModel.addBackgroundTerainTexture(frame);
		
		// add images for beach
		if (tile.type.isWater() && tile.style > 0) {
			int edgeStyle = tile.style >> 4;
    		if (edgeStyle > 0) {
    			frame = gameResources.tileEdge(edgeStyle, x, y, tile.type.getInsertOrder());
    			tileDrawModel.addBackgroundTerainTexture(frame);
    		}
    		int cornerStyle = tile.style & 15;
    		if (cornerStyle > 0) {
    			frame = gameResources.tileCorner(cornerStyle, x, y, tile.type.getInsertOrder());
    			tileDrawModel.addBackgroundTerainTexture(frame);
    		}
		}
	}
	
	private void renderBordersAndRivers(Direction direction) {
		
		if (tile.type.hasTheSameTerain(borderTile.type)) {
			return;
		}
		
		if (tile.type.isWater() || borderTile.type.isWater()) {
			if (!tile.type.isWater() && borderTile.type.isWater()) {
				direction = direction.getReverseDirection();
				frame = gameResources.tileBorder(tile.type, direction, x, y, tile.type.getInsertOrder());
				borderTileDrawModel.addBackgroundTerainTexture(frame);
			}
			if (tile.type.isWater() && !tile.type.isHighSea() && borderTile.type.isHighSea()) {
				direction = direction.getReverseDirection();
				frame = gameResources.tileBorder(tile.type, direction, x, y, tile.type.getInsertOrder());
				borderTileDrawModel.addBackgroundTerainTexture(frame);
			}
		} else {
			direction = direction.getReverseDirection();
			frame = gameResources.tileBorder(tile.type, direction, x, y, tile.type.getInsertOrder());
			borderTileDrawModel.addBackgroundTerainTexture(frame);
		}
		
		if (Direction.longSides.contains(direction) && tile.type.isWater()) {
			TileImprovement riverTileImprovement = borderTile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			if (riverTileImprovement != null) {
				frame = gameResources.riverDelta(direction, riverTileImprovement);
				tileDrawModel.addBackgroundTerainTexture(frame);
			}
		}
	}
	
	private void renderResources() {
		
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
		for (TileImprovement tileImprovement : tile.getTileImprovements()) {
			if (tileImprovement.type.isRiver()) {
				tileDrawModel.addForegroundTerainTexture(gameResources.river(tileImprovement.style));
			}
		}
		for (TileResource tileResource : tile.getTileResources()) {
			frame = gameResources.tileResource(tileResource.getResourceType());
			tileDrawModel.addForegroundTerainTexture(frame);
		}
		
		if (tile.hasLostCityRumour()) {
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
	
	private final List<ChangeSelectedUnitListener> changeSelectedUnitListeners = new ArrayList<ChangeSelectedUnitListener>();
	
	private TileDrawModel[][] tilesDrawModel;
	public Tile selectedTile;
	private Unit selectedUnit;
	public Player playingPlayer;
	public Map map;
	protected final UnitDislocationAnimation unitDislocationAnimation = new UnitDislocationAnimation();
	public Path unitPath;
	
	public void initialize(Map map, Player player, GameResources gameResources) {
		this.map = map;
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

		TileDrawModelInitializer initializer = new TileDrawModelInitializer(this, map, player, gameResources);
		initializer.initMapTiles();
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

	public void resetUnexploredBorders(Map map, GameResources gameResources) {
		TileDrawModelInitializer initializer = new TileDrawModelInitializer(this, map, playingPlayer, gameResources);
		initializer.initBordersForUnexploredTiles();
	}
	
	public void addChangeSelectedUnitListener(ChangeSelectedUnitListener listener) {
		this.changeSelectedUnitListeners.add(listener);
	}

	public Unit getSelectedUnit() {
		return selectedUnit;
	}
	
	public void setSelectedUnit(Unit selectedUnit) {
		this.selectedUnit = selectedUnit;
		for (ChangeSelectedUnitListener l : changeSelectedUnitListeners) {
			l.changeSelectedUnitAction(selectedUnit);
		}
	}
}
