package net.sf.freecol.common.model.player;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IdGenerator;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
	
	public MessageNotification(IdGenerator idGenerator, String body) {
		this(idGenerator.nextId(MessageNotification.class), body);
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
	
	public static class Xml extends XmlNodeParser<MessageNotification> {

		private static final String ATTR_BODY = "body";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttributeNotNull(ATTR_ID);
			String body = attr.getStrAttributeNotNull(ATTR_BODY);
			nodeObject = new MessageNotification(id, body);
		}

		@Override
		public void startWriteAttr(MessageNotification mn, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(mn);
			attr.set(ATTR_BODY, mn.getBody());
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
