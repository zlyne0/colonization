package promitech.colonization.ai;

import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameModel;

class TransportUnitMissionHandler {

    private final PathFinder pathFinder;
    private final GUIGameModel gameModel;
    private final MoveService moveService;

    public TransportUnitMissionHandler(PathFinder pathFinder, GUIGameModel gameModel, MoveService moveService) {
        this.pathFinder = pathFinder;
        this.gameModel = gameModel;
        this.moveService = moveService;
    }

    public void transportUnitMissionHandler(TransportUnitMission mission) {
    	if (mission.carrier == null || mission.carrier.isDisposed() || mission.carrier.isDamaged() || mission.units.isEmpty()) {
    		return;
    	}
        Path path = pathFinder.findToTile(
            gameModel.game.map,
            mission.carrier.getTile(),
            mission.dest,
            mission.carrier,
            PathFinder.includeUnexploredAndExcludeNavyThreatTiles
        );
        
        MoveContext moveContext = new MoveContext(path);
        
        moveService.aiConfirmedMovePath(moveContext);
        
        if (moveContext.isMoveType(MoveType.DISEMBARK)) {
            mission.setDone();
            for (Unit unitToDisembark : mission.units.entities()) {
                MoveContext mc = new MoveContext(
                    mission.carrier.getTile(), 
                    mission.dest, 
                    unitToDisembark
                );
                moveService.aiConfirmedMoveProcessor(mc);
            }
        }
    }
}