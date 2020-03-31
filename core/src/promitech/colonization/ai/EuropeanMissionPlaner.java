package promitech.colonization.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.player.Player;

public class EuropeanMissionPlaner {

	private final FoundColonyMissionHandler foundColonyMissionHandler;
	
	public EuropeanMissionPlaner(FoundColonyMissionHandler foundColonyMissionHandler) {
		this.foundColonyMissionHandler = foundColonyMissionHandler;
	}

	public void prepareMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
		prepareFoundColonyMissions(player, playerMissionContainer);
		prepareExploreMissions(player, playerMissionContainer);
	}

	private void prepareExploreMissions(Player player, PlayerMissionsContainer playerMissionContainer) {
    	for (Unit unit : player.units.entities()) {
    		if (unit.isNaval() && unit.isCarrier() && unit.getTileLocationOrNull() != null) {
    			if (!playerMissionContainer.isUnitBlockedForMission(unit)) {
    				ExplorerMission explorerMission = new ExplorerMission(unit);
    				playerMissionContainer.blockUnitForMission(unit, explorerMission);
    				playerMissionContainer.addMission(explorerMission);
    			}
    		}
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
    	
    	Tile tileToBuildColony = foundColonyMissionHandler.findTileToBuildColony(ship.getOwner(), ship, ship.getTile());
    	if (tileToBuildColony == null) {
    		System.out.println("can not find tile to found colony");
    	}
    	
    	for (Unit colonist : colonists.entities()) {
    		RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, colonist, ship);
            playerMissionContainer.blockUnitsForMission(rellocationMission);
            
    		FoundColonyMission foundColonyMission = new FoundColonyMission(tileToBuildColony, colonist);
    		foundColonyMission.addDependMission(rellocationMission);
    		playerMissionContainer.blockUnitsForMission(foundColonyMission);
    		
    		System.out.println("create mission: " + foundColonyMission);
    		playerMissionContainer.addMission(foundColonyMission);
    	}
	}
	
}
