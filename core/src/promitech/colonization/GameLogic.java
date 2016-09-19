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
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import net.sf.freecol.common.model.player.HighSeas;
import net.sf.freecol.common.model.player.MarketChangePrice;
import net.sf.freecol.common.model.player.MarketData;
import net.sf.freecol.common.model.player.MarketSnapshoot;
import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.Monarch;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.player.MonarchActionNotification;
import net.sf.freecol.common.model.player.MonarchLogic;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class GameLogic {

	private final Game game;
	private final NewTurnContext newTurnContext = new NewTurnContext();
	
	public GameLogic(Game game) {
		this.game = game;
	}

	public void newTurn(Player player) {
		newTurnContext.restart();
		
		for (Unit unit : player.units.entities()) {
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
			colony.handleLackOfResources(newTurnContext);
			colony.calculateSonsOfLiberty();
			colony.calculateImmigration();
			colony.increaseWorkersExperience();
		}
        
        if (player.isColonial()) {
        	generateMonarchAction(player);
        }
        
		player.fogOfWar.resetFogOfWar(player);
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
                
                StringTemplate st = StringTemplate.template("model.unit.arriveInEurope")
                    .addKey("%europe%", unit.getOwner().getEuropeNameKey());
                unit.getOwner().eventsNotifications.addMessageNotification(st);
            }
            
            if (unit.isDestinationTile()) {
                Tile tile = game.map.findFirstMovableHighSeasTile(unit, unit.getDestinationX(), unit.getDestinationY());
                unit.clearDestination();
                unit.changeUnitLocation(tile);
            }
        }
    }

	private void workOnImprovement(Unit unit) {
		if (unit.workOnImprovement()) {
			Tile improvingTile = unit.getTile();
			TileImprovementType improvementType = unit.getTileImprovementType();
			
			LinkedList<Settlement> neighbouringSettlements = game.map.findSettlements(improvingTile, unit.getOwner(), 2);
			
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
					improvingTile.updateRoadConnections(game.map);
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
	
	private void generateMonarchAction(Player player) {
    	WithProbability<MonarchAction> randomMonarchAction = Randomizer.instance().randomOne(player.getMonarch().getActionChoices(game));
    	if (randomMonarchAction == null) {
    		return;
    	}
    	MonarchAction action = randomMonarchAction.probabilityObject();
    	
    	handleMonarchAction(player, action);
	}

	protected void handleMonarchAction(Player player, MonarchAction action) {
    	Monarch monarch = player.getMonarch();
		MonarchActionNotification man;
		StringTemplate st;
		
		switch (action) {
	    	case NO_ACTION:
	    		break;
	    	case RAISE_TAX_ACT:
	    	case RAISE_TAX_WAR:
	    		man = new MonarchActionNotification(action);
	    		
	            player.market().findMostValuableGoods(player, man);
	            if (man.getGoodsType() == null) {
	            	System.out.println("Ignoring tax raise, no goods to boycott.");
	            	return;
	            }
	            man.setTax(monarch.potentialTaxRaiseValue(game));
	            
	            player.eventsNotifications.notifications.addFirst(man);
	            
	    		break;
	    	case LOWER_TAX_WAR:
	    	case LOWER_TAX_OTHER:
	    		man = new MonarchActionNotification(action);
	    		man.setTax(monarch.generateLowerTaxValue());
	    		
	            player.eventsNotifications.notifications.addFirst(man);
	    		
	    		break;
	    	case WAIVE_TAX:
	    		man = new MonarchActionNotification(action);
	    		player.eventsNotifications.notifications.addFirst(man);
	    		break;
	    	case ADD_TO_REF:
	    		ArmyForceAbstractUnit royalAdditions = monarch.chooseForAddRoyalExpedition();
	            if (royalAdditions == null) {
	            	break;
	            }
	            MonarchLogic.riseExpeditionaryForce(monarch, royalAdditions);
	    		
	            man = new MonarchActionNotification(action);
	            st = StringTemplate.template(action.msgKey())
	            		.addAmount("%number%", royalAdditions.getAmount())
	            		.addName("%unit%", royalAdditions.getUnitType());
	            man.setMsgBody(Messages.message(st));
	            player.eventsNotifications.notifications.addFirst(man);
	            break;
	    	case DECLARE_PEACE:
	    		List<Player> friends = monarch.collectPotentialFriends(game);
	    		if (friends.isEmpty()) {
	    			break;
	    		}
	    		Player friend = Randomizer.instance().randomMember(friends);
	    		
	    		player.changeStance(friend, Stance.PEACE);
	    		
	    		st = StringTemplate.template(action.msgKey()).addName("%nation%", friend.nation());
	    		man = new MonarchActionNotification(action);
	    		man.setMsgBody(Messages.message(st));
	    		player.eventsNotifications.notifications.addFirst(man);
	    		break;
	    	case DECLARE_WAR:
	            List<Player> enemies = monarch.collectPotentialEnemies(game);
	            if (enemies.isEmpty()) {
	            	break;
	            }
	            Player enemy = Randomizer.instance().randomMember(enemies);
	            player.changeStance(enemy, Stance.WAR);
	            
	    		st = StringTemplate.template(action.msgKey()).addName("%nation%", enemy.nation());
	    		man = new MonarchActionNotification(action);
	    		man.setMsgBody(Messages.message(st));
	    		player.eventsNotifications.notifications.addFirst(man);
	    		break;
	    	case SUPPORT_LAND:
	    	case SUPPORT_SEA:
	    		List<ArmyForceAbstractUnit> supportUnits = monarch.chooseForSupport(action);
	    		if (supportUnits.isEmpty()) {
	    			break;
	    		}
	    		String unitsLabel = "";
	    		for (ArmyForceAbstractUnit af : supportUnits) {
	    			for (int i=0; i<af.getAmount(); i++) {
	    				Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), af.getUnitType(), af.getUnitRole(), player);
	    				unit.changeUnitLocation(player.getEurope());
	    			}
	    			if (!unitsLabel.isEmpty()) {
	    				unitsLabel += ", ";
	    			}
	    			unitsLabel += Messages.message(UnitLabel.getLabelWithAmount(af.getUnitType(), af.getUnitRole(), af.getAmount()));
	    		}
	    		st = StringTemplate.template(action.msgKey()).add("%addition%", unitsLabel);
	    		
	    		man = new MonarchActionNotification(action);
	    		man.setMsgBody(Messages.message(st));
	    		player.eventsNotifications.notifications.addFirst(man);
	    		break;
	    	case MONARCH_MERCENARIES:
	    		List<ArmyForceAbstractUnit> mercenaries = new ArrayList<ArmyForceAbstractUnit>(); 
	    		int price = monarch.chooseMercenaries(mercenaries);
	    		if (mercenaries.isEmpty()) {
	    			break;
	    		}
	    		man = new MonarchActionNotification(action, mercenaries, price);
	    		player.eventsNotifications.notifications.addFirst(man);
	    		break;
	    	case HESSIAN_MERCENARIES:
	    	case DISPLEASURE:
	    	    MonarchLogic.generateDispleasureMessageNotification(player);
	    		break;
		default:
			break;
		}
	}
	
}

