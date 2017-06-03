package promitech.colonization.ai;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveLogic;

public class AILogicDebugRun {

    private final PathFinder pathFinder = new PathFinder();
    private final ExplorerMissionHandler explorerMissionHandler;
    private GUIGameModel gameModel;
    
    public AILogicDebugRun(GUIGameModel gameModel, MoveLogic moveLogic) {
        this.gameModel = gameModel;
        explorerMissionHandler = new ExplorerMissionHandler(gameModel.game, pathFinder, moveLogic);
    }
    
    public void run() {
        Unit unit = gameModel.getActiveUnit();
//        ExplorerMission explorerMission = new ExplorerMission(unit);
//        explorerMissionHandler.executeMission(explorerMission);
//        explorerMissionHandler.exploreByOneMove(unit);
        explorerMissionHandler.exploreByAllMoves(unit);
    }
}
