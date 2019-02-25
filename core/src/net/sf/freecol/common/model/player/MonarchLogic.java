package net.sf.freecol.common.model.player;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.specification.WithProbability;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class MonarchLogic {

    public static void generateMonarchAction(Game game, Player player) {
        WithProbability<MonarchAction> randomMonarchAction = Randomizer.instance().randomOne(player.getMonarch().getActionChoices(game));
        if (randomMonarchAction == null) {
            return;
        }
        MonarchAction action = randomMonarchAction.probabilityObject();
        
        MonarchLogic.handleMonarchAction(game, player, action);
    }
    
    public static void handleMonarchAction(final Game game, final Player player, final MonarchAction action) {
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
                
                player.eventsNotifications.addMessageNotificationAsFirst(man);
                
                break;
            case LOWER_TAX_WAR:
            case LOWER_TAX_OTHER:
                man = new MonarchActionNotification(action);
                man.setTax(monarch.generateLowerTaxValue());
                
                player.eventsNotifications.addMessageNotificationAsFirst(man);
                
                break;
            case WAIVE_TAX:
                man = new MonarchActionNotification(action);
                player.eventsNotifications.addMessageNotificationAsFirst(man);
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
                player.eventsNotifications.addMessageNotificationAsFirst(man);
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
                player.eventsNotifications.addMessageNotificationAsFirst(man);
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
                player.eventsNotifications.addMessageNotificationAsFirst(man);
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
                		player.units.add(unit);
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
                player.eventsNotifications.addMessageNotificationAsFirst(man);
                break;
            case HESSIAN_MERCENARIES:
            case MONARCH_MERCENARIES:
                List<ArmyForceAbstractUnit> mercenaries = new ArrayList<ArmyForceAbstractUnit>(); 
                int price = monarch.chooseMercenaries(mercenaries);
                if (mercenaries.isEmpty()) {
                    break;
                }
                man = new MonarchActionNotification(action, mercenaries, price);
                player.eventsNotifications.addMessageNotificationAsFirst(man);
                break;
            case DISPLEASURE:
                MonarchLogic.generateDispleasureMessageNotification(player);
                break;
        default:
            break;
        }
    }
    
	public static void acceptRiseTax(Player player, MonarchActionNotification ntf) {
		player.setTax(ntf.getTax());
	}
	
	public static void refuseRiseTax(final Player player, MonarchActionNotification ntf) {
		Colony colony = (Colony)player.settlements.getById(ntf.getColonyId());

		if (colony.getGoodsContainer().goodsAmount(ntf.getGoodsType()) < ntf.getGoodsAmount()) {
			// Player has removed the goods from the colony,
			// so raise the tax anyway.
			final int extraTax = 3;
			player.setTax(ntf.getTax() + extraTax);
			
			StringTemplate st = StringTemplate.template(MonarchAction.FORCE_TAX.msgKey())
					.addAmount("%amount%", ntf.getTax() + extraTax);
			
			MessageNotification msgNtf = new MessageNotification(Game.idGenerator, Messages.message(st));
			player.eventsNotifications.addMessageNotification(msgNtf);
		} else { // Tea party
			colony.getGoodsContainer().decreaseGoodsQuantity(ntf.getGoodsType(), ntf.getGoodsAmount());
			colony.updateModelOnWorkerAllocationOrGoodsTransfer();
			
			player.market().createArrears(ntf.getGoodsType());

			StringTemplate st = StringTemplate
				.template((colony.isCoastland()) ? "model.monarch.colonyGoodsParty.harbour" : "model.monarch.colonyGoodsParty.landLocked")
				.add("%colony%", colony.getName())
				.addAmount("%amount%", ntf.getGoodsAmount())
				.addName("%goods%", ntf.getGoodsType());

			MessageNotification msg = new MessageNotification(Game.idGenerator, Messages.message(st));
			player.eventsNotifications.addMessageNotification(msg);
		}
	}

	public static void lowerRax(Player player, MonarchActionNotification ntf) {
		player.setTax(ntf.getTax());
	}

	public static void riseExpeditionaryForce(Monarch monarch, ArmyForceAbstractUnit royalAdditions) {
        monarch.getExpeditionaryForce().addArmy(royalAdditions);
	}
	
	public static void buyMercenaries(final Player player, final MonarchActionNotification ntf) {
		if (player.hasGold(ntf.getPrice())) {
			player.subtractGold(ntf.getPrice());
			for (ArmyForceAbstractUnit af : ntf.getMercenaries()) {
				for (int i=0; i<af.getAmount(); i++) {
					Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), af.getUnitType(), af.getUnitRole(), player);
					player.units.add(unit);
					unit.changeUnitLocation(player.getEurope());
				}
			}
		} else {
		    generateDispleasureMessageNotification(player);
		}
	}
	
	public static void generateDispleasureMessageNotification(Player player) {
	    player.getMonarch().setDispleasure(true);
	    MonarchActionNotification man = new MonarchActionNotification(MonarchAction.DISPLEASURE);
	    man.setMsgBody(Messages.msg(MonarchAction.DISPLEASURE.msgKey()));
	    player.eventsNotifications.addMessageNotificationAsFirst(man);
	}

	
}
