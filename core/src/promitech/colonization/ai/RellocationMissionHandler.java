package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import promitech.colonization.GUIGameModel;
import promitech.colonization.MoveLogic;
import promitech.colonization.gamelogic.MoveContext;

public class RellocationMissionHandler implements MissionHandler<RellocationMission> {
    
    private final PathFinder pathFinder;
    private final TransportPathFinder transportPathFinder;
    private final MoveLogic moveLogic;
    private final GUIGameModel gameModel;
    private final TileDebugView tileDebugView;
    
    public RellocationMissionHandler(PathFinder pathFinder, TransportPathFinder transportPathFinder, GUIGameModel gameModel, MoveLogic moveLogic, TileDebugView tileDebugView) {
        this.pathFinder = pathFinder;
        this.transportPathFinder = transportPathFinder;
        this.moveLogic = moveLogic;
        this.gameModel = gameModel;
        this.tileDebugView = tileDebugView;
    }

    @Override
    public void handle(RellocationMission mission) {
        if (mission.isRequireGeneratePath()) {
            Unit potentialCarrier = mission.carrier;
            if (potentialCarrier == null) {
                potentialCarrier = GlobalStrategyPlaner.findCarrierToRellocateUnit(mission.unit);
            }
            if (potentialCarrier == null) {
                System.out.println("can not find carrier to rellocate unit[" + mission.unit + "] for player " + mission.unit.getOwner());
                return;
            }
            
            Path generatePath = generatePath(mission, potentialCarrier);
            if (mission.isRequireCarrierToHandleMission()) {
                GlobalStrategyPlaner.blockUnitForMission(potentialCarrier, mission);
                mission.carrier = potentialCarrier;
            }
            
            if (tileDebugView.isDebug()) {
                tileDebugView.showPath(generatePath);
            }
        }
        // unit and carrier can move in parallel. Sometimes it's necessary 
        
        if (mission.needCarrierMove()) {
            Path path = pathFinder.findToTile(gameModel.game.map, mission.carrier.getTile(), mission.carrierDestination, mission.carrier, false);
            MoveContext moveContext = new MoveContext(path);
            moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
        }

        if (mission.isCarrierOnStepDestination()) {
            if (mission.isUnitOnCarrier()) {
                // disembark
                if (mission.unit.hasMovesPoints()) {
                    MoveContext mc = new MoveContext(
                        mission.carrier.getTile(), 
                        mission.unitDestination, 
                        mission.unit
                    );
                    moveLogic.forAiMoveOnlyReallocation(mc);
                    
                    if (!mission.isUnitOnRellocationDestination()) {
                        generatePath(mission, mission.carrier);
                        if (!mission.isRequireCarrierToHandleMission()) {
                            GlobalStrategyPlaner.unblockUnitFromMission(mission.carrier, mission);
                            mission.carrier = null;
                            mission.carrierDestination = null;
                        }
                    }
                }
            } else {
                // do nothing, wait for unit
            }
        }
        
        if (mission.needUnitMove()) {
            Path path = pathFinder.findToTile(gameModel.game.map, mission.unit.getTile(), mission.unitDestination, mission.unit, false);
            MoveContext moveContext = new MoveContext(path);
            moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
        }
        if (mission.isUnitOnRellocationDestination()) {
            GlobalStrategyPlaner.unblockUnitsFromMission(mission);
            mission.setDone();
        } else {
            if (mission.isUnitOnStepDestination()) {
                if (mission.isCarrierOnStepDestination()) {
                    // embark
                    MoveContext moveContext = MoveContext.embarkUnit(mission.unit, mission.carrier);
                    moveLogic.forAiMoveOnlyReallocation(moveContext);
                    //moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
                    
                    generatePath(mission, mission.carrier);
                    if (!mission.isRequireCarrierToHandleMission()) {
                        GlobalStrategyPlaner.unblockUnitFromMission(mission.carrier, mission);
                        
                        mission.carrier = null;
                    }
                } else {
                    // do nothing, wait for carrier
                }
            }
        }
    }

    /**
     * {@link RellocationMission} should have already set unit and destination
     * @param mission {@link RellocationMission}
     * @param carrier {@link Unit}
     */
    private Path generatePath(RellocationMission mission, Unit carrier) {
        Tile sourceTile = null;
        if (mission.unit.getTileLocationOrNull() == null) {
            sourceTile = carrier.getTile();
        } else {
            sourceTile = mission.unit.getTile();
        }
        
        pathFinder.generateRangeMap(
            gameModel.game.map, 
            carrier.getTile(), 
            carrier, 
            false
        );
        Path pathToDestination = transportPathFinder.findToTile(
            sourceTile, 
            mission.rellocationDestination, 
            mission.unit, 
            carrier, 
            pathFinder
        );
        
        // assumption start from land 
        if (pathToDestination.tiles.size <= 1) {
            throw new IllegalStateException("can not find path do destination " + mission.rellocationDestination);
        }
        mission.initNextStepDestinationFromPath(pathToDestination);
        
        return pathToDestination;
    }
}
