package promitech.colonization.move;

import java.util.List;

import net.sf.freecol.common.model.Unit.UnitState;
import promitech.colonization.GUIGameController;
import promitech.colonization.GUIGameModel;
import promitech.colonization.LostCityRumourService;
import promitech.colonization.actors.map.unitanimation.MoveView;
import promitech.colonization.gamelogic.combat.CombatController;
import promitech.colonization.infrastructure.ThreadsResources;

public class MoveService {

    public abstract static class AfterMoveProcessor {
    	
    	public static AfterMoveProcessor DO_NOTHING = new AfterMoveProcessor() {
		};
    	
        public void afterMove(MoveContext moveContext) {
        }
        void afterMove(List<MoveContext> moveContextList) {
        }
    }
    
    private abstract class RunnableMoveContext implements Runnable {
        protected MoveContext moveContext;
        protected AfterMoveProcessor afterMovePorcessor;
        protected List<MoveContext> moveContextList;
    }
    
    private GUIGameModel guiGameModel;
    private GUIGameController guiGameController;
    private MoveController moveController;
    private CombatController combatController;

    public void inject(GUIGameController guiGameController, MoveController moveController, GUIGameModel guiGameModel, MoveView moveView) {
        this.guiGameController = guiGameController;
        this.moveController = moveController;
        this.guiGameModel = guiGameModel;
        this.combatController = new CombatController(guiGameController, moveView);
    }

    private final RunnableMoveContext moveHandlerThread = new RunnableMoveContext() {
        @Override
        public void run() {
            preMoveProcessor(moveContext, afterMovePorcessor);
        }
    };
    public void preMoveProcessorInNewThread(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        if (!moveContext.canHandleMove()) {
            return;
        }
        moveHandlerThread.moveContext = moveContext;
        moveHandlerThread.afterMovePorcessor = afterMoveProcessor;
        ThreadsResources.instance.executeMovement(moveHandlerThread);
    }

    private final RunnableMoveContext confirmedMoveHandlerThread = new RunnableMoveContext() {
        @Override
        public void run() {
            confirmedMoveProcessor(moveContext, afterMovePorcessor);
        }
    };
    public void confirmedMoveProcessorInNewThread(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        confirmedMoveHandlerThread.moveContext = moveContext;
        confirmedMoveHandlerThread.afterMovePorcessor = afterMoveProcessor;
        ThreadsResources.instance.executeMovement(confirmedMoveHandlerThread);
    }

    private final RunnableMoveContext confirmedMultipleMoveHandlerThread = new RunnableMoveContext() {
        @Override
        public void run() {
            confirmedMultipleMoveProcessor(moveContextList, afterMovePorcessor);
        }
    };
    public void confirmedMultipleMoveProcessorInNewThread(List<MoveContext> moveContextList, AfterMoveProcessor afterMoveProcessor) {
    	confirmedMultipleMoveHandlerThread.moveContextList = moveContextList;
    	confirmedMultipleMoveHandlerThread.afterMovePorcessor = afterMoveProcessor;
        ThreadsResources.instance.executeMovement(confirmedMultipleMoveHandlerThread);
    }

    private void confirmedMultipleMoveProcessor(List<MoveContext> moveContextList, AfterMoveProcessor afterMoveProcessor) {
    	for (MoveContext mc : moveContextList) {
            showMoveIfRequired(mc);
            postMoveProcessor(mc, AfterMoveProcessor.DO_NOTHING);
    	}
    	afterMoveProcessor.afterMove(moveContextList);
    }
    
    public void confirmedMoveProcessor(MoveContext moveContext) {
    	confirmedMoveProcessor(moveContext, AfterMoveProcessor.DO_NOTHING);
    }
    
    private void confirmedMoveProcessor(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        showMoveIfRequired(moveContext);
        postMoveProcessor(moveContext, afterMoveProcessor);
    }
    
    private void preMoveProcessor(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        if (!moveContext.canHandleMove()) {
            return;
        }
        if (moveContext.isMoveViaPath()) {
            handlePathMoveContext(moveContext, afterMoveProcessor);
        } else {
            
            if (moveContext.isRequireUserInteraction()) {
                userInterationRequestProcessor(moveContext);
                return;
            }
            showMoveIfRequired(moveContext);
            postMoveProcessor(moveContext, afterMoveProcessor);
        }
    }

    private void postMoveProcessor(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        moveContext.handleMove();
        
        if (moveContext.isAi()) {
            if (moveContext.isMoveTypeRevealMap()) {
                moveContext.unit.getOwner().revealMapAfterUnitMove(guiGameModel.game.map, moveContext.unit);
            }
        } else {
            boolean exloredNewTiles = false;
            if (moveContext.isMoveTypeRevealMap()) {
                exloredNewTiles = moveContext.unit.getOwner().revealMapAfterUnitMove(guiGameModel.game.map, moveContext.unit);
            }
            if (exloredNewTiles) {
                guiGameController.resetUnexploredBorders();
            }
            afterMoveProcessor.afterMove(moveContext);
        }
    }
    
    private void userInterationRequestProcessor(MoveContext moveContext) {
        switch (moveContext.moveType) {
            case EXPLORE_LOST_CITY_RUMOUR: {
                new LostCityRumourService(guiGameController, this, guiGameModel.game)
                    .showLostCityRumourConfirmation(moveContext);
            } break;
            case DISEMBARK: {
                if (moveContext.unit.getUnitContainer().getUnits().size() == 1) {
                    MoveContext mc = new MoveContext(
                        moveContext.unit.getTileLocationOrNull(), 
                        moveContext.destTile, 
                        moveContext.unit.getUnitContainer().getUnits().first()
                    );
                    confirmedMoveProcessor(mc, AfterMoveProcessor.DO_NOTHING);
                } else {
                	moveController.showDisembarkConfirmation(moveContext);
                }
            } break;
            case MOVE_HIGH_SEAS: {
                moveController.showHighSeasQuestion(moveContext);
            } break;
            case ATTACK_UNIT: {
                combatController.confirmCombat(moveContext);
            } break;
            default:
                throw new IllegalStateException("not implemented required user interaction move type " + moveContext.moveType);
        }
    }
    
    private void showMoveIfRequired(MoveContext moveContext) {
        if (showMoveOnPlayerScreen(moveContext)) {
            moveController.waitForUnitDislocationAnimation(moveContext);
        }
    }
    
    private boolean showMoveOnPlayerScreen(MoveContext moveContext) {
        return !guiGameModel.game.playingPlayer.fogOfWar.hasFogOfWar(moveContext.sourceTile)
                || !guiGameModel.game.playingPlayer.fogOfWar.hasFogOfWar(moveContext.destTile);
    }

    private void handlePathMoveContext(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        boolean runAfterMoveProcessor = true;
        while (true) {
            if (!moveContext.canHandleMove()) {
                // move via path but no move points so next unit
                moveContext.unit.setState(UnitState.SKIPPED);
                break;
            }

            if (moveContext.isRequireUserInteraction()) {
                userInterationRequestProcessor(moveContext);
                runAfterMoveProcessor = false;
                break;
            }

            showMoveIfRequired(moveContext);
            postMoveProcessor(moveContext, AfterMoveProcessor.DO_NOTHING);
            
            moveContext.initNextPathStep();
            
            if (guiGameModel.game.map.isUnitSeeHostileUnit(moveContext.unit)) {
                System.out.println("unit: " + moveContext.unit + " see hostile unit");
                break;
            }
                
            if (moveContext.isEndOfPath()) {
                if (moveContext.unit.isDestinationEurope() && moveContext.unit.getTile().getType().isHighSea()) {
                    moveContext.unit.moveUnitToHighSea();
                } else {
                    moveContext.unit.clearDestination();
                }
                break;
            }
        }
        if (runAfterMoveProcessor) {
            afterMoveProcessor.afterMove(moveContext);
        }
    }
    
    
}
