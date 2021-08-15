package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.util.Predicate;

import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.colonization.ai.Units;
import promitech.colonization.screen.debug.TileDebugView;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

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
	
	private final Map map;
	private final PathFinder pathFinder;
	private final MapIdEntities<GoodsType> goodsTypeToScoreByPrice;
	private final ObjectsListScore<WorkerRequestScoreValue> tileScore;
	
	public ColonyWorkerRequestPlaner(Map map, PathFinder pathFinder) {
		this.map = map;
		this.pathFinder = pathFinder;
		this.goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice;
		this.tileScore = new ObjectsListScore<WorkerRequestScoreValue>(20);
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
		
		for (final ObjectScore<WorkerRequestScoreValue> objectScore : tileScore) {
			if (!hasMissionToTile(playerMissionContainer, objectScore.getObj().getLocation())) {
				ColonyWorkerMission mission = new ColonyWorkerMission(objectScore.getObj().getLocation(), landUnit);
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
	
	public ObjectsListScore<WorkerRequestScoreValue> score(Player player, Unit transporter) {
		tileScore.clear();
		if (player.settlements.isEmpty()) {
			workerForCreateColony(player, transporter, tileScore);
			// tileScore already sorted
		} else {
			workerForColony(player, tileScore);
			workerForCreateColony(player, transporter, tileScore);
			tileScore.sortDescending();
		}
		// maybe it should return list of list to score pack of workers for one colony
		return tileScore;
	}
	
	public void debug(MapTileDebugInfo mapDebugInfo) {
		printTileScores(tileScore);
		
		for (ObjectScore<WorkerRequestScoreValue> ts : tileScore) {
			mapDebugInfo.str(
				ts.getObj().getLocation().x,
				ts.getObj().getLocation().y,
				"" + ts.getScore()
			);
		}
	}
	
	private void printTileScores(ObjectsListScore<WorkerRequestScoreValue> tileScore) {
		for (ObjectScore<WorkerRequestScoreValue> objectScore : tileScore) {
			System.out.println("" 
				+ objectScore.getScore() + ", " 
				+ objectScore.getObj().getWorkerType() + ", "
				+ "[" + objectScore.getObj().getLocation().x + "," + objectScore.getObj().getLocation().y + "]"
				+ (objectScore.getObj().getLocation().hasSettlement() ? " settlement(" + objectScore.getObj().getLocation().getSettlement().getName() + ")" : "")
			);
		}
	}
	
	private void workerForCreateColony(Player player, Unit transporter, ObjectsListScore<WorkerRequestScoreValue> tileScore) {
		ColonyPlaceGenerator colonyPlaceGenerator = new ColonyPlaceGenerator(pathFinder, map);
		
		Tile playerEntryTile = map.getSafeTile(player.getEntryLocation());
		Tile[] theBestTiles = colonyPlaceGenerator.theBestTiles(transporter, playerEntryTile);
		
		CreateColonyReqScore newColonyReqScore = new CreateColonyReqScore(map, player, goodsTypeToScoreByPrice);
		newColonyReqScore.score(tileScore, theBestTiles);
		tileScore.sortDescending();
	}
	
	private void workerForColony(Player player, ObjectsListScore<WorkerRequestScoreValue> tileScore) {
		for (Settlement settlement : player.settlements) {
			ColonyWorkerReqScore colonyWorkerReq = new ColonyWorkerReqScore(settlement.asColony(), goodsTypeToScoreByPrice);
			ObjectsListScore<WorkerRequestScoreValue> score = colonyWorkerReq.simulate();
			if (!score.isEmpty()) {
				if (score.firstScore().getScore() != 0) {
					tileScore.add(score.firstObj(), score.firstScore().getScore());
				} else {
					tileScore.add(score.firstObj(), firstNotZero(score));
				}
			}
		}
	}

	private int firstNotZero(ObjectsListScore<WorkerRequestScoreValue> score) {
		for (ObjectScore<WorkerRequestScoreValue> value : score) {
			if (value.getScore() != 0) {
				return value.getScore();
			}
		}
		return 0;
	}

	public void debugTheBestBlaceToBuildColony(TileDebugView tileDebugView, Unit galleon, Tile rangeSource) {
		ColonyPlaceGenerator colonyPlaceGenerator = new ColonyPlaceGenerator(pathFinder, map);
		colonyPlaceGenerator.theBestTiles(galleon, rangeSource);
		colonyPlaceGenerator.theBestTilesWeight(tileDebugView);
	}

	public void debugGenerateTileScoresForNewColony(TileDebugView tileDebugView, Player playingPlayer) {
		ColonyPlaceGenerator colonyPlaceGenerator = new ColonyPlaceGenerator(pathFinder, map);
		colonyPlaceGenerator.generateWeights(playingPlayer);
		colonyPlaceGenerator.tilesWeights(tileDebugView);
	}
}
