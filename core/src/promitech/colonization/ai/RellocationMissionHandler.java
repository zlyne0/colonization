package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.map.path.TransportPathFinder;
import promitech.colonization.MoveLogic;
import promitech.colonization.gamelogic.MoveContext;

public class RellocationMissionHandler implements MissionHandler<RellocationMission> {
    
    private final PathFinder pathFinder;
    private final TransportPathFinder transportPathFinder;
    private final MoveLogic moveLogic;
    private final Game game;
    private Path carrierPath = null;
    
    public RellocationMissionHandler(PathFinder pathFinder, TransportPathFinder transportPathFinder, Game game, MoveLogic moveLogic) {
        this.pathFinder = pathFinder;
        this.transportPathFinder = transportPathFinder;
        this.moveLogic = moveLogic;
        this.game = game;
    }
    
    @Override
    public void handle(RellocationMission mission) {
        if (mission.isRequireGeneratePath()) {
            generatePathToRellocate(mission);
        }
        // unit and carrier can move in parallel. Sometimes it's necessary 

        carrierPath = null;
        if (mission.needCarrierMove()) {
        	moveCarrier(mission);
        }

        if (mission.isCarrierOnStepDestination() && mission.isUnitOnCarrier()) {
            if (mission.isUnitOnCarrier()) {
                disembarkUnit(mission);
            }
        } // else do nothing, wait until carrier will be on destination
        
        if (mission.needUnitMove()) {
            Path path = pathFinder.findToTile(game.map, mission.unit.getTile(), mission.unitDestination, mission.unit, false);
            MoveContext moveContext = new MoveContext(path);
            moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
        }
        if (mission.isUnitOnRellocationDestination()) {
            GlobalStrategyPlaner.unblockUnitsFromMission(mission);
            mission.setDone();
        } else {
            if (mission.isUnitOnStepDestination()) {
                if (mission.isCarrierOnStepDestination() && mission.unit.hasMovesPoints()) {
                    // embark
                    MoveContext moveContext = MoveContext.embarkUnit(mission.unit, mission.carrier);
                    moveLogic.forAiMoveOnlyReallocation(moveContext);

                    // after embark generate path for carrier
                    generatePathToRellocate(mission);
                    
                    // move carrier after embark
                    if (mission.needCarrierMove()) {
                    	moveCarrier(mission);
                    }
                } else {
                    // do nothing, wait for carrier
                }
            }
        }
    }

	private void moveCarrier(RellocationMission mission) {
		// after embark unit carrier use the same path like before embark
		if (carrierPath == null) {
			carrierPath = pathFinder.findToTile(game.map, mission.carrier.getTile(), mission.carrierDestination, mission.carrier, false);
		}
		MoveContext moveContext = new MoveContext(carrierPath);
		moveLogic.forAiMoveViaPathOnlyReallocation(moveContext);
	}

	private void disembarkUnit(RellocationMission mission) {
		if (mission.unit.hasMovesPoints()) {
		    MoveContext mc = new MoveContext(
		        mission.carrier.getTile(), 
		        mission.unitDestination, 
		        mission.unit
		    );
		    moveLogic.forAiMoveOnlyReallocation(mc);
		    
		    generatePathToRellocate(mission);
		}
	}

	private void generatePathToRellocate(RellocationMission mission) {
		Unit potentialCarrier = mission.carrier;
		if (potentialCarrier == null) {
		    potentialCarrier = GlobalStrategyPlaner.findCarrierToRellocateUnit(mission.unit);
		}
		if (potentialCarrier == null) {
		    System.out.println("can not find carrier to rellocate unit[" + mission.unit + "] for player " + mission.unit.getOwner());
		    return;
		}
		
		generatePath(mission, potentialCarrier);
		if (mission.isRequireCarrierToHandleMission()) {
		    GlobalStrategyPlaner.blockUnitForMission(potentialCarrier, mission);
		    mission.carrier = potentialCarrier;
		} else {
			GlobalStrategyPlaner.unblockUnitFromMission(mission.carrier, mission);
			mission.carrier = null;
			mission.carrierDestination = null;
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
            game.map, 
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
