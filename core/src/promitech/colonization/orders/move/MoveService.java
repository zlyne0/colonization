package promitech.colonization.orders.move;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import promitech.colonization.Direction;
import promitech.colonization.infrastructure.ThreadsResources;
import promitech.colonization.orders.LostCityRumourService;
import promitech.colonization.orders.combat.CombatController;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.Messages;

public class MoveService {

    public abstract static class AfterMoveProcessor {
    	
    	public static final AfterMoveProcessor DO_NOTHING = new AfterMoveProcessor() {
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

    // bombardment
	private MoveContext artilleryUnitBombardAnimation;
    
    public void inject(GUIGameController guiGameController, MoveController moveController, GUIGameModel guiGameModel, CombatService combatService) {
        this.guiGameController = guiGameController;
        this.moveController = moveController;
        this.guiGameModel = guiGameModel;
        this.combatController = new CombatController(guiGameController, combatService, guiGameModel);
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
    
    public MoveType aiConfirmedMovePath(MoveContext moveContext) {
    	moveContext.initNextPathStep();
    	while (moveContext.canHandleMove()) {
    		aiConfirmedMoveProcessor(moveContext);
    		moveContext.initNextPathStep();
    	}
    	return moveContext.moveType;
    }
    
    public void aiConfirmedMoveProcessor(MoveContext moveContext) {
        showMoveIfRequired(moveContext);
        aiPostMoveProcessor(moveContext);
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

    public void postMoveProcessor(MoveContext moveContext) {
        postMoveProcessor(moveContext, AfterMoveProcessor.DO_NOTHING);
    }
    
    private void postMoveProcessor(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        moveContext.handleMove();
        
        boolean exploredNewTiles = false;
        if (moveContext.isMoveTypeRevealMap()) {
            exploredNewTiles = moveContext.unit.getOwner().revealMapAfterUnitMove(guiGameModel.game.map, moveContext.unit);
        }
        if (exploredNewTiles) {
            guiGameController.resetUnexploredBorders();
            
            if (isNotDiscoveredNewLand(moveContext.unit)) {
                discoverNewLand(moveContext.unit.getOwner());
            }
        }
        
        firstContact(moveContext.destTile, moveContext.unit.getOwner());
        // TODO: budzenie jednostek sprawdzenie czy nie sa budzone przy nowej turze
        
        afterMoveProcessor.afterMove(moveContext);
    }
    
    private void firstContact(Tile tile, Player player) {
        // TODO: jak rusza sie ai i ma kontakt z human, jak rozwiazac pokazywanie okna podczas konca tury
        java.util.Map<String,Player> firstContactPlayers = null;
        if (tile.getType().isWater()) {
            return;
        }
        for (Direction direction : Direction.allDirections) {
            Tile neighbourTile = guiGameModel.game.map.getTile(tile, direction);
            if (neighbourTile == null || neighbourTile.getType().isWater()) {
                continue;
            }
            Player neighbourTilePlayer = null;
            if (neighbourTile.hasSettlement()) {
                neighbourTilePlayer = neighbourTile.getSettlement().getOwner();
            } else {
                if (neighbourTile.getUnits().isNotEmpty()) {
                    neighbourTilePlayer = neighbourTile.getUnits().first().getOwner();
                }
            }
            if (neighbourTilePlayer == null || player.equalsId(neighbourTilePlayer) || player.hasContacted(neighbourTilePlayer)) {
                continue;
            }
            if (firstContactPlayers == null) {
                firstContactPlayers = new HashMap<String, Player>();
            }
            firstContactPlayers.put(neighbourTilePlayer.getId(), neighbourTilePlayer);
        }
        
        if (firstContactPlayers != null) {
            for (Entry<String, Player> neighbourPlayerEntry : firstContactPlayers.entrySet()) {
                Player neighbour = neighbourPlayerEntry.getValue();
                if (player.isAi() && neighbour.isAi()) {
                    player.changeStance(neighbour, Stance.PEACE);
                } else {
                    if (player.isAi()) {
                        moveController.showFirstContactDialog(neighbour, player);
                    } else {
                        //moveController.showFirstContactDialog(player, neighbour);
                    }
                }
            }
        }
    }
    
    private boolean isNotDiscoveredNewLand(Unit unit) {
        return unit.getOwner().isEuropean() && unit.getOwner().getNewLandName() == null && unit.getTile().isNextToLand();
    }
    
    private void discoverNewLand(Player player) {
        String defaultLandName = Messages.msg(player.nation().getId() + ".newLandName");
        player.setNewLandName(defaultLandName);
        if (player.isHuman()) {
            moveController.showNewLandNameDialog(player, defaultLandName);
        }
    }
    
    private void aiPostMoveProcessor(MoveContext moveContext) {
    	moveContext.handleMove();
        if (moveContext.isMoveTypeRevealMap()) {
            moveContext.unit.getOwner().revealMapAfterUnitMove(guiGameModel.game.map, moveContext.unit);
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
            case ATTACK_SETTLEMENT: {
                combatController.confirmSettlementCombat(moveContext);
            } break;
            default:
                throw new IllegalStateException("not implemented required user interaction move type " + moveContext.moveType);
        }
    }
    
    private void showMoveIfRequired(MoveContext moveContext) {
        if (showMoveOnPlayerScreen(moveContext.sourceTile, moveContext.destTile)) {
            moveController.blockedShowMove(moveContext);
        }
    }
    
    public void blockedShowMove(MoveContext moveContext) {
        if (showMoveOnPlayerScreen(moveContext.sourceTile, moveContext.destTile)) {
            moveController.blockedShowMove(moveContext);
        }
    }
    
    public void blockedShowFailedAttackMove(MoveContext moveContext) {
        if (showMoveOnPlayerScreen(moveContext.sourceTile, moveContext.destTile)) {
            moveController.blockedShowFailedAttackMove(moveContext);
        }
    }
    
    public void blockedShowFailedBombardAttack(Player bombardingPlayer, Tile bombardingTile, Tile bombardedTile) {
        if (showMoveOnPlayerScreen(bombardingTile, bombardedTile)) {
        	createBombardArtillery();
    		artilleryUnitBombardAnimation.sourceTile = bombardingTile;
    		artilleryUnitBombardAnimation.destTile = bombardedTile;
    		artilleryUnitBombardAnimation.unit.setOwner(bombardingPlayer);
            moveController.blockedShowFailedAttackMove(artilleryUnitBombardAnimation);
        }
    }
    
	public void blockedShowSuccessfulBombardAttack(Colony bombardingColony, Tile bombardedTile, Unit bombardedUnit) {
		if (showMoveOnPlayerScreen(bombardingColony.tile, bombardedTile)) {
			createBombardArtillery();
    		artilleryUnitBombardAnimation.sourceTile = bombardingColony.tile;
    		artilleryUnitBombardAnimation.destTile = bombardedTile;
    		artilleryUnitBombardAnimation.unit.setOwner(bombardingColony.getOwner());
			moveController.blockedShowSuccessfulAttackWithMove(artilleryUnitBombardAnimation, bombardedUnit);
		}
	}
	
	private void createBombardArtillery() {
		if (artilleryUnitBombardAnimation != null) {
			return;
		}
		artilleryUnitBombardAnimation = new MoveContext();
    	artilleryUnitBombardAnimation.unit = new Unit(
			Game.idGenerator.nextId(Unit.class), 
			Specification.instance.unitTypes.getById(UnitType.ARTILLERY), 
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
			new Player("artilleryUnitBombardAnimation:player")
		);
	}

    public void blockedShowAttackRetreat(MoveContext moveContext) {
        if (showMoveOnPlayerScreen(moveContext.sourceTile, moveContext.destTile)) {
            moveController.blockedShowAttackRetreat(moveContext);
        }
    }
    
    public void blockedShowSuccessfulAttackWithMove(MoveContext moveContext, Unit loser) {
        if (showMoveOnPlayerScreen(moveContext.sourceTile, moveContext.destTile)) {
            moveController.blockedShowSuccessfulAttackWithMove(moveContext, loser);
        }
    }
    
    private boolean showMoveOnPlayerScreen(Tile sourceTile, Tile destTile) {
        return !guiGameModel.game.playingPlayer.fogOfWar.hasFogOfWar(sourceTile)
                || !guiGameModel.game.playingPlayer.fogOfWar.hasFogOfWar(destTile);
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
