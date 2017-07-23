package promitech.colonization.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.PlayerMissionsContainer;
import net.sf.freecol.common.model.player.Player;

public class EuropeanMissionPlaner {

	private final FoundColonyMissionHandler foundColonyMissionHandler;
	
	public EuropeanMissionPlaner(FoundColonyMissionHandler foundColonyMissionHandler) {
		this.foundColonyMissionHandler = foundColonyMissionHandler;
	}

	public void xxx(Player player) {
		// create missions
		MapIdEntities<ExplorerMission> missions = new MapIdEntities<ExplorerMission>();
		for (Unit unit : player.units.entities()) {
			if (unit.isNaval() && unit.getTileLocationOrNull() != null) {
				missions.add(new ExplorerMission(unit));
			}
		}
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
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
		
    	Tile tileToBuildColony = foundColonyMissionHandler.findTileToBuildColony(ship.getOwner(), ship, ship.getTile());
    	if (tileToBuildColony == null) {
    		System.out.println("can not find tile to found colony");
    	}
    	
    	for (Unit colonist : colonists.entities()) {
    		RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, colonist, ship);
            GlobalStrategyPlaner.blockUnitsForMission(rellocationMission);
            
    		FoundColonyMission foundColonyMission = new FoundColonyMission(tileToBuildColony, colonist);
    		foundColonyMission.addDependMission(rellocationMission);
    		GlobalStrategyPlaner.blockUnitsForMission(foundColonyMission);
    		
    		System.out.println("create mission: " + foundColonyMission);
    		playerMissionContainer.addMission(foundColonyMission);
    	}
	}
	
}
