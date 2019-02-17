package promitech.colonization;

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
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.MarketChangePrice;
import net.sf.freecol.common.model.player.MarketData;
import net.sf.freecol.common.model.player.MarketSnapshoot;
import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.MonarchLogic;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.orders.combat.CombatService;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.StringTemplate;

public class GameLogic {

	private final GUIGameModel guiGameModel;
	private final CombatService combatService;
	private final NewTurnContext newTurnContext = new NewTurnContext();
	private final MoveService moveService;
	
	private final List<Unit> playerUnits = new ArrayList<Unit>();
	
	public GameLogic(GUIGameModel guiGameModel, CombatService combatService, MoveService moveService) {
		this.guiGameModel = guiGameModel;
		this.combatService = combatService;
		this.moveService = moveService;
	}

	public void newTurn(Player player) {
		newTurnContext.restart();
		System.out.println("newTurn for player " + player);

		// copy units for safety remove
		playerUnits.clear();
		playerUnits.addAll(player.units.entities());
		for (Unit unit : playerUnits) {
			newTurnForUnit(unit);
		}
		
        for (Settlement settlement : player.settlements.sortedEntities()) {
			if (!settlement.isColony()) {
				continue;
			}
			Colony colony = (Colony)settlement;
			System.out.println("calculate new turn for colony " + colony);
			
			colony.updateColonyFeatures();
			colony.increaseWarehouseByProduction();
			colony.reduceTileResourceQuantity(newTurnContext);
			
			colony.increaseColonySize();
			colony.buildBuildings(newTurnContext);
			
			colony.removeExcessedStorableGoods();
			colony.handleLackOfResources(newTurnContext, guiGameModel.game);
			colony.calculateSonsOfLiberty();
			
			colony.increaseWorkersExperience();
		}
        
        if (player.isColonial()) {
        	MonarchLogic.generateMonarchAction(guiGameModel.game, player);
        	player.getEurope().handleImmigrationOnNewTurn();
        }
        if (player.isEuropean()) {
        	bombardEnemyShip(player);
        }
        
		player.fogOfWar.resetFogOfWar(guiGameModel.game, player);
	}

	private void bombardEnemyShip(Player player) {
		for (Settlement settlement : player.settlements.entities()) {
			Colony colony = settlement.getColony();
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

	public void newTurnForUnit(Unit unit) {
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
			default:
				break;
		}
		if (unit.isAtLocation(HighSeas.class)) {
		    sailOnHighSeas(unit);
		}
	}

    private void sailOnHighSeas(Unit unit) {
        unit.sailOnHighSea();
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
                Tile tile = guiGameModel.game.map.findFirstMovableHighSeasTile(unit, unit.getDestinationX(), unit.getDestinationY());
                unit.clearDestination();
                unit.changeUnitLocation(tile);
            }
        }
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
			
			LinkedList<Settlement> neighbouringSettlements = guiGameModel.game.map.findSettlements(improvingTile, unit.getOwner(), 2);
			
			TileTypeTransformation changedTileType = improvementType.changedTileType(improvingTile.getType());
			if (changedTileType != null) {
				improvingTile.changeTileType(changedTileType.getToType());
				
				Goods production = changedTileType.getProduction();
				int prodAmount = production.getAmount();
				if (unit.unitType.hasAbility(Ability.EXPERT_PIONEER)) {
					prodAmount *= 2;
				}
				if (!neighbouringSettlements.isEmpty()) {
					Settlement settlement = neighbouringSettlements.getFirst();
					prodAmount = settlement.applyModifiers(Modifier.TILE_TYPE_CHANGE_PRODUCTION, prodAmount);
					settlement.addGoods(production.getId(), prodAmount);
				}
			} else {
				TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, improvementType);
				improvingTile.addImprovement(tileImprovement);
				if (improvementType.isRoad()) {
					improvingTile.updateRoadConnections(guiGameModel.game.map);
				}
			}
			
			for (Settlement settlement : neighbouringSettlements) {
				if (settlement.isContainsTile(improvingTile)) {
					settlement.initMaxPossibleProductionOnTile(improvingTile);
					break;
				}
			}
			
			for (Unit u : improvingTile.getUnits().entities()) {
				if (u.getState() == UnitState.IMPROVING && u.getTileImprovementType().equalsId(improvementType)) {
					u.setState(UnitState.ACTIVE);
				}
			}
			
			// does improvement expose resource
			if (isExposedResourceAfterImprovement(improvingTile, improvementType)) {
				ResourceType resourceType = improvingTile.getType().exposeResource();
				int initQuantity = resourceType.initQuantity();
				improvingTile.addResource(new TileResource(Game.idGenerator, resourceType, initQuantity));
			}
			
			newTurnContext.setRequireUpdateMapModel();
			unit.setState(UnitState.ACTIVE);
		}
	}

	private boolean isExposedResourceAfterImprovement(Tile tile, TileImprovementType improvementType) {
		return !tile.hasTileResource() 
				&& Randomizer.instance().isHappen(improvementType.getExposedResourceAfterImprovement()) 
				&& tile.getType().allowedResourceTypes.isNotEmpty();
		
	}

	public NewTurnContext getNewTurnContext() {
		return newTurnContext;
	}

	public void comparePrices(Player playingPlayer, MarketSnapshoot marketSnapshoot) {
		for (MarketData md : playingPlayer.market().marketGoods.entities()) {
			MarketChangePrice mcp = marketSnapshoot.prices.getByIdOrNull(md.getId());
			if (mcp != null) {
				mcp.setPricesAfterTransaction(md);
				if (mcp.isMarketPriceChanged()) {
					MessageNotification goodsPriceChangeNotification = MessageNotification.createGoodsPriceChangeNotification(playingPlayer, mcp);
					playingPlayer.eventsNotifications.addMessageNotification(goodsPriceChangeNotification);
				}
			}
		}
	}
}

