package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.util.Predicate;
import static promitech.colonization.ai.MissionHandlerLogger.logger;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.colonization.ai.Units;

public class ColonyWorkerRequestPlaner {

//	1col +0
//	2col +5
//	3col +7
//	4col +10
//	5col +12
//	6col +15
	int [][] colNumberWeights = new int[][] {
		{0, 0},
		{1, 0},
		{2, 5},
		{3, 7},
		{4, 10},
		{5, 12},
		{6, 15}
	};
	
	private final Game game;
	private final PathFinder pathFinder;
	private final MapIdEntities<GoodsType> goodsTypeToScoreByPrice;
	private final ObjectsListScore<TileUnitType> tileScore; 
	
	public ColonyWorkerRequestPlaner(Game game, PathFinder pathFinder) {
		this.game = game;
		this.pathFinder = pathFinder;
		this.goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice;
		this.tileScore = new ObjectsListScore<TileUnitType>(20);
	}
	
	/**
	 * Prepare mission for create new colony or put worker in colony.
	 * Unit can be on europe dock, on carrier on tile. 
	 */
	public void prepareMission(Player player, Unit landUnit, PlayerMissionsContainer playerMissionContainer) {
		if (playerMissionContainer.isUnitBlockedForMission(landUnit)) {
			return;
		}
		
		Unit transporter = Units.findCarrier(player);
		if (transporter == null) {
			logger.debug("player[%s] no carrier unit", player.getId());
			return;
		}
		score(player, transporter);
		
		for (final ObjectScore<TileUnitType> objectScore : tileScore) {
			if (!hasMissionToTile(playerMissionContainer, objectScore.getObj().tile)) {
				ColonyWorkerMission mission = new ColonyWorkerMission(objectScore.getObj().tile, landUnit);
				playerMissionContainer.addMission(mission);
				
				break;
			}
		}
	}
	
	private boolean hasMissionToTile(final PlayerMissionsContainer playerMissionContainer, final Tile tile) {
		return playerMissionContainer.hasMission(ColonyWorkerMission.class, new Predicate<ColonyWorkerMission>() {
			@Override
			public boolean test(ColonyWorkerMission obj) {
				return obj.getTile().equalsCoordinates(tile);
			}
		});
	}
	
	public ObjectsListScore<TileUnitType> score(Player player, Unit transporter) {
		tileScore.clear();
		if (player.settlements.isEmpty()) {
			workerForCreateColony(player, transporter, tileScore);
			// tileScore already sorted
		} else {
			workerForColony(player, tileScore);
			workerForCreateColony(player, transporter, tileScore);
			tileScore.sortDescending();
		}
		return tileScore;
	}
	
	public void debug(MapTileDebugInfo mapDebugInfo) {
		printTileScores(tileScore);
		
		for (ObjectScore<TileUnitType> ts : tileScore) {
			mapDebugInfo.str(
				ts.getObj().tile.x, 
				ts.getObj().tile.y, 
				"" + ts.getScore()
			);
		}
	}
	
	private void printTileScores(ObjectsListScore<TileUnitType> tileScore) {
		for (ObjectScore<TileUnitType> objectScore : tileScore) {
			System.out.println("" 
				+ objectScore.getScore() + ", " 
				+ objectScore.getObj().unitType + ", " 
				+ objectScore.getObj().tile.getId() + ", "
				+ (objectScore.getObj().tile.hasSettlement() ? "settlement" : "")  
			);
		}
	}
	
	private void workerForCreateColony(Player player, Unit transporter, ObjectsListScore<TileUnitType> tileScore) {
		ColonyPlaceGenerator colonyPlaceGenerator = new ColonyPlaceGenerator(pathFinder, game);
		
		Tile playerEntryTile = game.map.getSafeTile(player.getEntryLocation());
		Tile[] theBestTiles = colonyPlaceGenerator.theBestTiles(transporter, playerEntryTile);
		
		CreateColonyReqScore newColonyReqScore = new CreateColonyReqScore(game.map, player, goodsTypeToScoreByPrice);
		
		for (Tile tile : theBestTiles) {
			if (tile == null) {
				continue;
			}
			newColonyReqScore.score(tileScore, tile);
		}
		tileScore.sortDescending();
	}
	
	private void workerForColony(Player player, ObjectsListScore<TileUnitType> tileScore) {
		for (Settlement settlement : player.settlements) {
			ColonyWorkerReqScore colonyWorkerReq = new ColonyWorkerReqScore(settlement.asColony(), goodsTypeToScoreByPrice);
			ObjectsListScore<UnitType> score = colonyWorkerReq.simulate();
			if (!score.isEmpty()) {
				tileScore.add(
					new TileUnitType(settlement.tile, score.firstObj()), 
					score.scoreSum()
				);
			}
		}
	}
}
