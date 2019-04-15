package promitech.colonization.screen.map;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.player.MoveExploredTiles;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.map.isometric.NeighbourIterableTile;

class TileDrawModelInitializer {
	private final GameResources gameResources;
	private Game game;
	private Map map; 
	private Player player;
	
	private int x, y;
	private Tile tile;
	private Tile borderTile;
	private TileDrawModel tileDrawModel;
	private TileDrawModel borderTileDrawModel;
	private MapDrawModel mapDrawModel;
	
	private Frame frame;
	
	public TileDrawModelInitializer(GameResources gameResources) {
		this.gameResources = gameResources;
	}
	
	public void initMapTiles(MapDrawModel mapDrawModel, Game game) {
		this.mapDrawModel = mapDrawModel;
		this.game = game;
		this.map = game.map;
		this.player = game.playingPlayer;
		
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getSafeTile(x, y);
				tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
				renderTerainAndBeaches();
			}
		}
		
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getSafeTile(x, y);
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
				tile = map.getSafeTile(x, y);
				tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
				renderResources();
			}
		}
		
		initBordersForUnexploredTiles();
	}
	
	public void initBordersForUnexploredTiles() {
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				determineUnexploredBordersForCords(x, y);
			}
		}
	}

	public void initBordersForUnexploredTiles(MoveExploredTiles exploredTiles) {
		for (int i=0; i<exploredTiles.removeFogOfWarX.size; i++) {
			tileDrawModel = mapDrawModel.getTileDrawModel(
				exploredTiles.removeFogOfWarX.get(i),
				exploredTiles.removeFogOfWarY.get(i)
			);
			tileDrawModel.setFogOfWar(false);
		}
		
		for (int i=0; i<exploredTiles.exploredTilesX.size; i++) {
			x = exploredTiles.exploredTilesX.get(i);
			y = exploredTiles.exploredTilesY.get(i);

			determineUnexploredBordersForCords(x, y);
			// when one explore, border moves so reset neighbours 
			for (NeighbourIterableTile<Tile> it : map.neighbourTiles(x, y)) {
				determineUnexploredBordersForCords(it.tile.x, it.tile.y);
			}
		}
	}

	private void determineUnexploredBordersForCords(int x, int y) {
		tileDrawModel = mapDrawModel.getTileDrawModel(x, y);
		tileDrawModel.resetUnexploredBorders();
		tileDrawModel.setTileVisibility(
			player.isTileExplored(x, y),
			player.fogOfWar.hasFogOfWar(x, y)
		);
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
	
	private void renderTerainAndBeaches() {
		
		frame = gameResources.tile(tile.getType(), x, y);
		tileDrawModel.addBackgroundTerainTexture(frame);
		
		// add images for beach
		if (tile.getType().isWater() && tile.getStyle() > 0) {
			int edgeStyle = tile.getStyle() >> 4;
    		if (edgeStyle > 0) {
    			frame = gameResources.tileEdge(edgeStyle, x, y, tile.getType().getInsertOrder());
    			tileDrawModel.addBackgroundTerainTexture(frame);
    		}
    		int cornerStyle = tile.getStyle() & 15;
    		if (cornerStyle > 0) {
    			frame = gameResources.tileCorner(cornerStyle, x, y, tile.getType().getInsertOrder());
    			tileDrawModel.addBackgroundTerainTexture(frame);
    		}
		}
	}
	
	private void renderBordersAndRivers(Direction direction) {
		
		if (tile.getType().hasTheSameTerain(borderTile.getType())) {
			return;
		}
		
		if (tile.getType().isWater() || borderTile.getType().isWater()) {
			if (!tile.getType().isWater() && borderTile.getType().isWater()) {
				direction = direction.getReverseDirection();
				frame = gameResources.tileBorder(tile.getType(), direction, x, y, tile.getType().getInsertOrder());
				borderTileDrawModel.addBackgroundTerainTexture(frame);
			}
			if (tile.getType().isWater() && !tile.getType().isHighSea() && borderTile.getType().isHighSea()) {
				direction = direction.getReverseDirection();
				frame = gameResources.tileBorder(tile.getType(), direction, x, y, tile.getType().getInsertOrder());
				borderTileDrawModel.addBackgroundTerainTexture(frame);
			}
		} else {
			direction = direction.getReverseDirection();
			frame = gameResources.tileBorder(tile.getType(), direction, x, y, tile.getType().getInsertOrder());
			borderTileDrawModel.addBackgroundTerainTexture(frame);
		}
		
		if (Direction.longSides.contains(direction) && tile.getType().isWater()) {
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
		if (tile.getType().getId().equals("model.tile.hills")) {
			tileDrawModel.addForegroundTerainTexture(gameResources.hills(tile.x, tile.y));
		}
		if (tile.getType().getId().equals("model.tile.mountains")) {
			tileDrawModel.addForegroundTerainTexture(gameResources.mountainsKey(tile.x, tile.y));
		}
		// draw forest with river
		if (tile.getType().isForested()) {
			TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
			frame = gameResources.forestImg(tile.getType(), riverTileImprovement);
			if (frame != null) {
				tileDrawModel.addForegroundTerainTexture(frame);
			}
		}
		for (TileImprovement tileImprovement : tile.getTileImprovements()) {
			if (tileImprovement.type.isRiver()) {
				tileDrawModel.addForegroundTerainTexture(gameResources.river(tileImprovement.getStyle()));
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
			if (tile.getSettlement().isIndianSettlement()) {
				tileDrawModel.settlementChip = new IndianSettlementChip(
					game.players,
					game.playingPlayer,
					tile.getSettlement().getIndianSettlement()
				);
			}
		}
	}
}