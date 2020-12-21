package promitech.colonization.ai;

import net.sf.freecol.common.model.ColonyFactory;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.ai.missions.buildcolony.ColonyPlaceGenerator;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.orders.BuildColonyOrder;
import promitech.colonization.orders.BuildColonyOrder.OrderStatus;

class FoundColonyMissionHandler implements MissionHandler<FoundColonyMission> {
    
    private final PathFinder pathFinder;
    private final Game game;
    private final ColonyPlaceGenerator colonyPlaceGenerator;
    
    public FoundColonyMissionHandler(PathFinder pathFinder, Game game) {
        this.pathFinder = pathFinder;
        this.game = game;
        this.colonyPlaceGenerator = new ColonyPlaceGenerator(pathFinder, game);
    }
    
    @Override
    public void handle(PlayerMissionsContainer playerMissionsContainer, FoundColonyMission mission) {
    	if (mission.unit == null || mission.unit.isDisposed()) {
    		mission.setDone();
    		return;
    	}
        if (!mission.isUnitInDestination()) {
            // do nothing, wait when unit will be on destination
            return;
        }
        BuildColonyOrder buildColonyOrder = new BuildColonyOrder(game.map);
        OrderStatus check = buildColonyOrder.check(mission.unit, mission.destTile);
        if (check == OrderStatus.OK) {
            ColonyFactory colonyFactory = new ColonyFactory(game, pathFinder);
            colonyFactory.buildColonyByAI(mission.unit, mission.destTile);
            
            playerMissionsContainer.unblockUnitsFromMission(mission);
            mission.setDone();
        } else {
            if (check == OrderStatus.NO_MOVE_POINTS) {
                // do nothing wait on next turn for move points
            } else {
                Tile tileToBuildColony = colonyPlaceGenerator.findTileToBuildColony(mission.unit, mission.unit.getTile());
                if (tileToBuildColony != null) {
                    mission.destTile = tileToBuildColony;
                    RellocationMission rellocationMission = new RellocationMission(tileToBuildColony, mission.unit);
                    mission.addDependMission(rellocationMission);
                } else {
                    // can not find tile to build colony, do nothing
                	playerMissionsContainer.unblockUnitsFromMission(mission);
                }
            }
        }
    }
}