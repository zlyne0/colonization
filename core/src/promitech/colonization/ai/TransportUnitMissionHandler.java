package promitech.colonization.ai;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveLogic;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.gamelogic.MoveType;

class TransportUnitMissionHandler {

    private final PathFinder pathFinder;
    private final GUIGameModel gameModel;
    private final MoveLogic moveLogic;

    public TransportUnitMissionHandler(PathFinder pathFinder, GUIGameModel gameModel, MoveLogic moveLogic) {
        this.pathFinder = pathFinder;
        this.gameModel = gameModel;
        this.moveLogic = moveLogic;
    }

    public void transportUnitMissionHandler(TransportUnitMission mission) {
        Path path = pathFinder.findToTile(gameModel.game.map, mission.carrier.getTile(), mission.dest, mission.carrier);
        
        MoveContext moveContext = new MoveContext(path);
        
        moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
        
        if (moveContext.isMoveType(MoveType.DISEMBARK)) {
            mission.setDone();
            for (Unit unitToDisembark : mission.units.entities()) {
                MoveContext mc = new MoveContext(
                    mission.carrier.getTile(), 
                    mission.dest, 
                    unitToDisembark
                );
                moveLogic.forAiMoveOnlyReallocation(mc);
            }
        }
    }
}