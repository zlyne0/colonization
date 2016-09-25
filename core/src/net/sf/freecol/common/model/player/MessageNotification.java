package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class MessageNotification extends ObjectWithId implements Notification {
	
	private final String body;

	public static MessageNotification createGoodsPriceChangeNotification(Player player, MarketChangePrice transaction) {
		String strTempCode = transaction.isPriceIncrease() ? "model.market.priceIncrease" : "model.market.priceDecrease";
		
		StringTemplate st = StringTemplate.template(strTempCode)
			.addStringTemplate("%market%", player.getNationName())
			.addName("%goods%", transaction.goodsTypeId)
			.addAmount("%buy%", transaction.buyPriceAfterTransaction)
			.addAmount("%sell%", transaction.sellPriceAfterTransaction);
		return new MessageNotification(
			Game.idGenerator.nextId(MessageNotification.class), 
			Messages.message(st)
		);
	}
	
	public MessageNotification(String id, String body) {
		super(id);
		this.body = body;
	}

	public String getBody() {
		return body;
	}
	
	public String toString() {
		return "MessageNotification[id: " + id + ", body: " + body + "]";
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttributeNotNull("id");
			String body = attr.getStrAttributeNotNull("body");
			nodeObject = new MessageNotification(id, body);
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "messageNotification";
		}
		
	}

}
