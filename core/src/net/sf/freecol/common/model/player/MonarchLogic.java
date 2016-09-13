package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class MonarchLogic {

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
			
			StringTemplate st = StringTemplate.template("model.monarch.action.FORCE_TAX")
					.addAmount("%amount%", ntf.getTax() + extraTax);
			
			MessageNotification msgNtf = new MessageNotification(
				Game.idGenerator.nextId(MessageNotification.class), 
				Messages.message(st)
			);
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

			MessageNotification msg = new MessageNotification(
				Game.idGenerator.nextId(MessageNotification.class), 
				Messages.message(st)
			);
			player.eventsNotifications.addMessageNotification(msg);
		}
	}

	public static void lowerRax(Player player, MonarchActionNotification ntf) {
		player.setTax(ntf.getTax());
	}
	
	
}
