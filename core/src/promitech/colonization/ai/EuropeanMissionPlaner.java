package promitech.colonization.ai;

import java.util.List;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.buildcolony.ColonyPlaceGenerator;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

public class EuropeanMissionPlaner {

	private final TransportGoodsToSellMissionPlaner transportGoodsToSellMissionPlaner;
	private final ColonyPlaceGenerator colonyPlaceGenerator;
	private final ColonyWorkerRequestPlaner colonyWorkerRequestPlaner;
	
	public EuropeanMissionPlaner(Game game, PathFinder pathFinder) {
		this.transportGoodsToSellMissionPlaner = new TransportGoodsToSellMissionPlaner(game, pathFinder);
		this.colonyPlaceGenerator = new ColonyPlaceGenerator(pathFinder, game);
		this.colonyWorkerRequestPlaner = new ColonyWorkerRequestPlaner(game, pathFinder);
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		for (Unit unit : player.units.copy()) {
			if (unit.isAtLocation(Tile.class) || unit.isAtLocation(Unit.class) || unit.isAtLocation(Europe.class)) {
				if (unit.isColonist()) {
					colonyWorkerRequestPlaner.prepareMission(player, unit, playerMissionContainer);
				}
			}
		}
		
		for (Unit unit : player.units.copy()) {
			if (unit.isNaval() && !unit.isDamaged()) {
				navyUnitPlaner(unit, playerMissionContainer);
			}
		}
		
//		colonyProductionPlaner.createPlan(player, playerMissionContainer);
//		prepareFoundColonyMissions(player, playerMissionContainer);
//		transportGoodsToSellMissionPlaner.plan(player);
//		prepareExploreMissions(player, playerMissionContainer);
	}
	
	private void navyUnitPlaner(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		if (navyUnit.getUnitContainer() != null) {
			TransportUnitMission tum = null;
			
			for (Unit u : navyUnit.getUnitContainer().getUnits()) {
				List<ColonyWorkerMission> findMissions = playerMissionContainer.findMissions(ColonyWorkerMission.class, u);
				for (ColonyWorkerMission cwm : findMissions) {
					if (tum == null) {
						tum = new TransportUnitMission(navyUnit);
					}
					tum.addUnitDest(u, cwm.getTile());
				}
			}
			if (tum != null) {
				playerMissionContainer.addMission(tum);
			}
		}
		
		if (playerMissionContainer.isUnitBlockedForMission(navyUnit)) {
			return;
		}
		prepareExploreMissions(navyUnit, playerMissionContainer);
	}
	
	private void prepareExploreMissions(Unit navyUnit, PlayerMissionsContainer playerMissionContainer) {
		if (navyUnit.getTileLocationOrNull() != null) {
			ExplorerMission explorerMission = new ExplorerMission(navyUnit);
			playerMissionContainer.addMission(explorerMission);
		}
	}

	private void prepareFoundColonyMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		if (player.settlements.isNotEmpty() || playerMissionContainer.hasMissionType(FoundColonyMission.class)) {
			return;
		}
		
    	Unit ship = null;
    	MapIdEntities<Unit> colonists = new MapIdEntities<Unit>();
    	for (Unit unit : player.units.entities()) {
    		if (unit.isColonist()) {
    			colonists.add(unit);
    		}
    		if (unit.isNaval() && unit.isCarrier()) {
    			ship = unit;
    		}
    	}
    	if (ship == null) {
    		throw new IllegalStateException("can not find ship to generate start game ai missions");
    	}
    	if (colonists.isEmpty()) {
    		throw new IllegalStateException("can not find colonists to build colony");
    	}
    	
    	playerMissionContainer.interruptMission(ship);
    	
    	Tile tileToBuildColony = colonyPlaceGenerator.findTileToBuildColony(ship, ship.getTile());
    	if (tileToBuildColony != null) {
        	for (Unit colonist : colonists.entities()) {
        		RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, colonist, ship);
        		FoundColonyMission foundColonyMission = new FoundColonyMission(tileToBuildColony, colonist);
        		foundColonyMission.addDependMission(rellocationMission);
        		
        		playerMissionContainer.addMission(foundColonyMission);
        	}
    	} else {
    		System.out.println("can not find tile to found colony");
    	}
	}
	
}
