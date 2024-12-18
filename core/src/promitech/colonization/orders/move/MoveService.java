package promitech.colonization.orders.move;

import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.LostCityRumour;
import net.sf.freecol.common.model.player.MoveExploredTiles;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.PlayerExploredTiles;

import promitech.colonization.orders.LostCityRumourService;
import promitech.colonization.orders.combat.CombatController;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.diplomacy.FirstContactController;
import promitech.colonization.orders.diplomacy.FirstContactService;
import promitech.colonization.orders.diplomacy.SpeakToChiefResult;
import promitech.colonization.orders.diplomacy.TradeController;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.Messages;
import promitech.map.isometric.IterableSpiral;
import promitech.map.isometric.NeighbourIterableTile;

public class MoveService {

    public abstract static class AfterMoveProcessor {
    	
    	public static final AfterMoveProcessor DO_NOTHING = new AfterMoveProcessor() {
		};
    	
        public void afterMove(MoveContext moveContext) {
        }
        void afterMove(List<MoveContext> moveContextList) {
        }
        public void afterMove(Unit unit) {
        }
    }
    
    private GUIGameModel guiGameModel;
    private GUIGameController guiGameController;
    private MoveController moveController;
    private CombatController combatController;
    private FirstContactController firstContactController;
    private FirstContactService firstContactService;
    
    // bombardment
	private MoveContext artilleryUnitBombardAnimation;
    
    private final MoveExploredTiles exploredTiles = new MoveExploredTiles();
    private final IterableSpiral<Tile> spiralIterator = new IterableSpiral<Tile>();
    
    public void inject(
    		GUIGameController guiGameController, 
    		MoveController moveController, 
    		GUIGameModel guiGameModel, 
    		CombatService combatService,
    		FirstContactController firstContactController
	) {
        this.guiGameController = guiGameController;
        this.moveController = moveController;
        this.guiGameModel = guiGameModel;
        this.combatController = new CombatController(guiGameController, combatService, guiGameModel, firstContactController);
        this.firstContactController = firstContactController;
        this.firstContactService = new FirstContactService(firstContactController, guiGameModel);
    }

    public void confirmedMultipleMoveProcessor(List<MoveContext> moveContextList, AfterMoveProcessor afterMoveProcessor) {
    	for (MoveContext mc : moveContextList) {
            showMoveIfRequired(mc);
            processMove(mc);
    	}
    	afterMoveProcessor.afterMove(moveContextList);
    }
    
    public void confirmedMoveProcessor(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        showMoveIfRequired(moveContext);
        processMove(moveContext);
        afterMoveProcessor.afterMove(moveContext);
    }
    
    public MoveType aiConfirmedMovePath(MoveContext moveContext) {
        moveContext.setMoveViaHighSea();
    	moveContext.initNextPathStep();
    	while (moveContext.canAiHandleMove()) {
    		aiConfirmedMoveProcessor(moveContext);
    		moveContext.initNextPathStep();
    	}
    	return moveContext.moveType;
    }
    
    public void aiConfirmedMoveProcessor(MoveContext moveContext) {
        showMoveIfRequired(moveContext);
        processMove(moveContext);

        if (moveContext.moveType == MoveType.EXPLORE_LOST_CITY_RUMOUR) {
            LostCityRumourService lostCityRumourService = new LostCityRumourService(guiGameController, guiGameModel.game);
            LostCityRumour.RumourType explorationResult = lostCityRumourService.processExploration(moveContext);
            System.out.println(String.format("player[%s].explorationLostCityRumourResult %s", moveContext.unit.getOwner().getId(), explorationResult));
        } else if (moveContext.moveType == MoveType.ENTER_INDIAN_SETTLEMENT_WITH_SCOUT) {
            IndianSettlement indianSettlement = moveContext.destTile.getSettlement().asIndianSettlement();
            SpeakToChiefResult speakResult = firstContactService.scoutSpeakWithIndianSettlementChief(indianSettlement, moveContext.unit);
            System.out.println(String.format("player[%s].speakToChiefResult %s", moveContext.unit.getOwner().getId(), speakResult));
        }
    }
    
    public void preMoveProcessor(MoveContext moveContext, AfterMoveProcessor afterMoveProcessor) {
        if (!moveContext.canHandleMove()) {
            return;
        }
        if (moveContext.isMoveViaPath()) {
            boolean requireUserInteraction = handlePathMoveContext(moveContext);
            if (!requireUserInteraction) {
                if (moveContext.unit.isAtLocation(moveContext.destTile) && moveContext.destTile.hasSettlementOwnedBy(moveContext.unit.getOwner())) {
                    moveContext.unit.disembarkUnitsToLocation(moveContext.destTile);
                }
                afterMoveProcessor.afterMove(moveContext);
            }
        } else {
            if (moveContext.isRequireUserInteraction()) {
                userInterationRequestProcessor(moveContext);
                return;
            }
            showMoveIfRequired(moveContext);
            processMove(moveContext);
            if (moveContext.destTile.hasSettlementOwnedBy(moveContext.unit.getOwner())) {
        		moveContext.unit.disembarkUnitsToLocation(moveContext.destTile);
            }
            afterMoveProcessor.afterMove(moveContext);
        }
    }
    
    public void processMove(MoveContext moveContext) {
        moveContext.handleMove();
        if (moveContext.isMoveTypeRevealMap()) {
        	exploredTiles.clear();
            moveContext.unit.getOwner().getPlayerExploredTiles().revealMap(
        		moveContext.unit,
        		exploredTiles,
                guiGameModel.game.getTurn()
    		);
        }
        if (exploredTiles.isExploredNewTiles() && moveContext.unit.getOwner().equalsId(guiGameModel.game.playingPlayer)) {
            guiGameController.resetUnexploredBorders(exploredTiles);
            exploredTiles.clear();
            
            if (isNotDiscoveredNewLand(moveContext.unit)) {
                discoverNewLand(moveContext.unit.getOwner());
            }
        }
        firstContactService.firstContact(moveContext.destTile, moveContext.unit.getOwner());
        wakeUpSentryUnits(moveContext.unit.getOwner(), moveContext.destTile);
        
        if (moveContext.destTile.hasSettlementOwnedBy(moveContext.unit.getOwner())) {
        	checkCashInTreasureInCarrier(moveContext.unit, moveContext.destTile);
        }
    }
    
    /**
     * Wake up neighbour sentry units. Not only in newturn but in MoveService too 
     * because unit with lots of moves can move close and move further. NewTurn check
     * only on sign radius. Unit can move in radius and go out. 
     * One tile range not ideal but enough.   
     */
    private void wakeUpSentryUnits(Player player, Tile tile) {
    	for (NeighbourIterableTile<Tile> neighbourTile : guiGameModel.game.map.neighbourTiles(tile)) {
    		for (Unit u : neighbourTile.tile.getUnits().entities()) {
    			if (u.isSentry() && u.getOwner().notEqualsId(player)) {
    				u.setState(UnitState.ACTIVE);
    			}
    		}
		}
    }

	private void checkCashInTreasureInCarrier(Unit carrier, Tile tile) {
		Unit treasureWagon = null;

        if (carrier.canCarryTreasure()) {
		    treasureWagon = carrier;
        }

		// check treasure in unit carrier
		if (carrier.getUnitContainer() != null) {
			for (Unit unit : carrier.getUnitContainer().getUnits().entities()) {
				if (unit.canCarryTreasure()) {
					treasureWagon = unit;
				}
			}
		}
		if (treasureWagon != null && Unit.canCashInTreasureInLocation(treasureWagon.getOwner(), tile)) {
			if (carrier.getOwner().isHuman()) {
				moveController.showCashInTreasureConfirmation(treasureWagon);
			}
			if (carrier.getOwner().isAi()) {
				MoveService.this.cashInTreasure(treasureWagon);
			}
		}
	}
    
    private boolean isNotDiscoveredNewLand(Unit unit) {
        return unit.getOwner().isEuropean() && !unit.getOwner().hasGivenNewWorldName() && unit.getTile().isNextToLand();
    }
    
    private void discoverNewLand(Player player) {
        String defaultLandName = Messages.msg(player.nation().getId() + ".newLandName");
        player.setNewLandName(defaultLandName);
        if (player.isHuman()) {
            moveController.showNewLandNameDialog(player, defaultLandName);
        }
    }
    
    private void userInterationRequestProcessor(MoveContext moveContext) {
        switch (moveContext.moveType) {
            case EXPLORE_LOST_CITY_RUMOUR: {
                moveController.showLostCityRumourConfirmation(moveContext);
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
            case MOVE_CASH_IN_TREASURE: {
            	showMoveIfRequired(moveContext);
            	processMove(moveContext);
            	moveController.showCashInTreasureConfirmation(moveContext.unit);
            } break;
            case ENTER_FOREIGN_COLONY_WITH_SCOUT: {
            	firstContactController.showScoutMoveToForeignColonyQuestion(
        			moveContext.destTile.getSettlement().asColony(),
        			moveContext.unit
    			);
            } break;
            case ENTER_INDIAN_SETTLEMENT_WITH_SCOUT: {
            	firstContactController.showScoutMoveToIndianSettlementQuestion(
        			moveContext.destTile.getSettlement().asIndianSettlement(),
        			moveContext.unit
    			);
            } break;
            case ENTER_INDIAN_SETTLEMENT_WITH_FREE_COLONIST: {
            	firstContactController.learnSkill(
        			moveContext.unit,
        			moveContext.destTile.getSettlement().asIndianSettlement()
    			);
            } break;
            case ENTER_INDIAN_SETTLEMENT_WITH_MISSIONARY: {
            	firstContactController.indianSettlementMissionary(
        			moveContext.unit, 
        			moveContext.destTile.getSettlement().asIndianSettlement()
    			);
            } break;
            case ENTER_SETTLEMENT_WITH_CARRIER_AND_GOODS: {
            	new TradeController(guiGameModel.game.map, guiGameController, moveContext).trade();
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
    
    public void showMoveIfRequired(MoveContext moveContext) {
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
        PlayerExploredTiles playerExploredTiles = guiGameModel.game.playingPlayer.getPlayerExploredTiles();
        return !playerExploredTiles.hasFogOfWar(sourceTile, guiGameModel.game.getTurn())
            || !playerExploredTiles.hasFogOfWar(destTile, guiGameModel.game.getTurn());
    }

    public boolean disembarkUnits(Unit carrier, List<Unit> units, Tile sourceTile, Tile destTile) {
    	boolean canDisembark = true;
    	
    	if (sourceTile.equalsCoordinates(destTile)) {
    		// colony no show move
    		for (Unit u : units) {
    			carrier.disembarkUnitToLocation(destTile, u);
    		}
    	} else {
    		MoveContext mc = new MoveContext();
    		for (Unit u : units) {
    			mc.init(sourceTile, destTile, u);
    			if (mc.isMoveType()) {
    				aiConfirmedMoveProcessor(mc);
    			} else {
    				canDisembark = false;
    			}
    		}
    	}
    	return canDisembark;
    }
    
    /**
     * Return true when user interation request
     */
    public boolean handlePathMoveContext(MoveContext moveContext) {
        while (true) {
            if (!moveContext.canHandleMove()) {
                // move via path but no move points so next unit
                moveContext.unit.setState(UnitState.SKIPPED);
                break;
            }

            if (moveContext.isRequireUserInteraction()) {
                userInterationRequestProcessor(moveContext);
                return true;
            }

            showMoveIfRequired(moveContext);
            processMove(moveContext);
            
            moveContext.initNextPathStep();
            
            if (moveContext.unit.isSeeHostile(spiralIterator, guiGameModel.game.map)) {
                System.out.println("unit: " + moveContext.unit + " see hostile unit");
                break;
            }
                
            if (moveContext.isEndOfPath()) {
                if (moveContext.unit.isDestinationEurope() && moveContext.unit.getTile().getType().isHighSea()) {
                    moveContext.unit.sailUnitToEurope(moveContext.destTile);
                } else {
                    moveContext.unit.clearDestination();
                }
                break;
            }
        }
        return false;
    }
    
    public void cashInTreasure(Unit unit) {
        LostCityRumourService.cashInTreasure(guiGameModel.game, unit, guiGameController);
    }

}
