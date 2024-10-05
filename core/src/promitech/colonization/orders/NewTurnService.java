package promitech.colonization.orders;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.ai.PlayerAiContainer;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.MonarchLogic;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.StringTemplate;
import promitech.map.isometric.IterableSpiral;
import static promitech.colonization.orders.NewTurnLogger.logger;

public class NewTurnService {

	private final GUIGameModel guiGameModel;
	private final CombatService combatService;
	private final MoveService moveService;

	private final List<Unit> playerUnits = new ArrayList<Unit>();
	private final IterableSpiral<Tile> spiralIterator = new IterableSpiral<Tile>();

	private final NewTurnIndianSettlement newTurnIndianSettlement;
	private final NewTurnColony newTurnColony;

	public NewTurnService(GUIGameModel guiGameModel, CombatService combatService, MoveService moveService) {
		this.guiGameModel = guiGameModel;
		this.combatService = combatService;
		this.moveService = moveService;

		this.newTurnIndianSettlement = new NewTurnIndianSettlement(guiGameModel);
		this.newTurnColony = new NewTurnColony(guiGameModel);
	}

	public void newTurn(Player player) {
		if (player.isEuropean()) {
			player.foundingFathers.checkFoundingFathers(guiGameModel.game);
		}
		// copy units for safety remove
		playerUnits.clear();
		playerUnits.addAll(player.units.entities());
		for (Unit unit : playerUnits) {
			newTurnForUnit(unit);
		}
		
        for (Settlement settlement : player.settlements.entities()) {
			if (settlement.isIndianSettlement()) {
				newTurnIndianSettlement.newTurn(settlement.asIndianSettlement());
        	} else if (settlement.isColony()) {
				newTurnColony.newTurn(settlement.asColony());
			}
		}
        
        if (player.isColonial()) {
        	MonarchLogic.generateMonarchAction(guiGameModel.game, player);
        	player.getEurope().handleImmigrationOnNewTurn();
        }
        if (player.isEuropean()) {
        	bombardEnemyShip(player);
        }

		if (player.isAi()) {
        	player.eventsNotifications.clearNotifications();
			PlayerAiContainer playerAiContainer = guiGameModel.game.aiContainer.playerAiContainer(player);
			playerAiContainer.removeOutdatedReservations(guiGameModel.game.aiContainer.missionContainer(player));
			playerAiContainer.removeSupplyGoodsWhenNoColony();
		}
	}

	private void bombardEnemyShip(Player player) {
		for (Settlement settlement : player.settlements.entities()) {
			Colony colony = settlement.asColony();
			if (!colony.canBombardEnemyShip()) {
				continue;
			}
			
			for (Direction direction : Direction.allDirections) {
				Tile neighbourTile = guiGameModel.game.map.getTile(colony.tile, direction);
				if (neighbourTile.getType().isWater()) {
					Unit firstUnit = neighbourTile.getUnits().first();
					if (combatService.canBombardTile(colony, neighbourTile, firstUnit)) {
						combatService.bombardTileCombat(colony, neighbourTile, firstUnit);
						// colony can bombard only one tile per turn
						break;
					}
				}
			}
		}
	}

	private void newTurnForUnit(Unit unit) {
		unit.resetMovesLeftOnNewTurn();
		switch (unit.getState()) {
			case IMPROVING:
				workOnImprovement(unit);
				break;
			case FORTIFYING:
				unit.setState(UnitState.FORTIFIED);
				break;
			case SKIPPED:
				unit.setState(UnitState.ACTIVE);
				break;
			case SENTRY:
				wakeUpWhenSeeEnemy(unit);
				break;
			default:
				break;
		}
		if (unit.isAtLocation(HighSeas.class)) {
		    sailOnHighSeas(unit);
		}
	}

    private void wakeUpWhenSeeEnemy(Unit unit) {
    	if (unit.isAtLocation(Tile.class)) {
    		if (unit.isSeeEnemy(spiralIterator, guiGameModel.game.map)) {
    			unit.setState(UnitState.ACTIVE);
    		}
    	}
	}

	private void sailOnHighSeas(Unit unit) {
        unit.sailOnHighSea();
        if (logger.isDebug()) {
        	String dest = "new world";
        	if (unit.isDestinationEurope()) {
        		dest = "europe";
        	}
        	logger.debug("player[%s].unit[%s].sailOnHighSeas.turns %s dest %s", unit.getOwner().getId(), unit.getId(), unit.workTurnsToComplete(), dest);
        }
        if (unit.isWorkComplete()) {
            if (unit.isDestinationEurope()) {
                unit.clearDestination();
                unit.changeUnitLocation(unit.getOwner().getEurope());
                unit.disembarkUnitsToLocation(unit.getOwner().getEurope());
                
                StringTemplate st = StringTemplate.template("model.unit.arriveInEurope")
                    .addKey("%europe%", unit.getOwner().getEuropeNameKey());
                unit.getOwner().eventsNotifications.addMessageNotification(st);
                
    		    checkAndCashInTreasureInEurope(unit.getOwner());
            }
            
            if (unit.isDestinationTile()) {
                Tile tile = findFirstMovableHighSeasTile(unit, unit.getDestinationX(), unit.getDestinationY());
                unit.clearDestination();
                unit.changeUnitLocation(tile);
            }
        }
    }

	private Tile findFirstMovableHighSeasTile(final Unit unit, int x, int y) {
		Tile tile = guiGameModel.game.map.getSafeTile(x, y);
		if (tile.getUnits().isEmpty()) {
			return tile;
		}
		Unit firstUnit = tile.getUnits().first();
		if (firstUnit.isOwner(unit.getOwner())) {
			return tile;
		}
		for (promitech.map.isometric.NeighbourIterableTile<Tile> neighbourIterableTile : guiGameModel.game.map.neighbourTiles(x, y)) {
			if (neighbourIterableTile.tile.getType().isHighSea()) {
				if (neighbourIterableTile.tile.getUnits().isEmpty()) {
					return neighbourIterableTile.tile;
				}
				firstUnit = neighbourIterableTile.tile.getUnits().first();
				if (firstUnit.isOwner(unit.getOwner())) {
					return neighbourIterableTile.tile;
				}
			}
		}
		return null;
	}

    private void checkAndCashInTreasureInEurope(Player player) {
    	List<Unit> units = null; 
        for (Unit u : player.getEurope().getUnits().entities()) {
        	if (u.canCarryTreasure()) {
        		units = new ArrayList<Unit>(player.getEurope().getUnits().entities());
        	}
        }
        if (units != null) {
        	for (Unit u : units) {
        		if (u.canCarryTreasure()) {
        			moveService.cashInTreasure(u);
        		}
        	}
        }
    }
    
	private void workOnImprovement(Unit unit) {
		if (unit.workOnImprovement()) {
			Tile improvingTile = unit.getTile();
			TileImprovementType improvementType = unit.getTileImprovementType();
			
			LinkedList<Settlement> neighbouringSettlements = guiGameModel.game.map.findSettlements(improvingTile, 2);
			
			TileTypeTransformation changedTileType = improvementType.changedTileType(improvingTile.getType());
			if (changedTileType != null) {
				improvingTile.changeTileType(changedTileType.getToType());
				
				for (Settlement settlement : neighbouringSettlements) {
					if (settlement.getOwner().equalsId(unit.getOwner())) {
						changedTileType.addTransformationProductionToSettlement(unit, settlement);
						break;
					}
				}
			} else {
				TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, improvementType);
				improvingTile.addImprovement(tileImprovement);
				if (improvementType.isRoad()) {
					improvingTile.updateRoadConnections(guiGameModel.game.map);
				}
			}

			// does improvement expose resource
			if (isExposedResourceAfterImprovement(improvingTile, improvementType)) {
				ResourceType resourceType = improvingTile.getType().exposeResource();
				int initQuantity = resourceType.initQuantity();
				improvingTile.addResource(new TileResource(Game.idGenerator, resourceType, initQuantity));
			}

			for (Settlement settlement : neighbouringSettlements) {
				if (settlement.isColony()) {
					settlement.asColony().updateProductionToMaxPossible(improvingTile);
				}
			}
			
			for (Unit u : improvingTile.getUnits().entities()) {
				if (u.getState() == UnitState.IMPROVING && u.getTileImprovementType().equalsId(improvementType)) {
					u.setState(UnitState.ACTIVE);
				}
			}
			unit.setState(UnitState.ACTIVE);
		}
	}

	private boolean isExposedResourceAfterImprovement(Tile tile, TileImprovementType improvementType) {
		return !tile.hasTileResource() 
				&& Randomizer.instance().isHappen(improvementType.getExposedResourceAfterImprovement()) 
				&& tile.getType().allowedResourceTypes.isNotEmpty();
		
	}
}

