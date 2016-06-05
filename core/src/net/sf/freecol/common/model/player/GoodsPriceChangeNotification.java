package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsPriceChangeNotification implements Notification, Identifiable {

	GoodsType type;
	int from;
	int to;
	
	public GoodsPriceChangeNotification() {
	}

	@Override
	public String getId() {
		return type.getId();
	}
	
	public String toString() {
		return "GoodsPriceChangeNotification[type: " + type + ", from: " + from + ", to: " + to + "]";
	}
	
	public static class Xml extends XmlNodeParser {
		@Override
		public void startElement(XmlNodeAttributes attr) {
			String typeStr = attr.getStrAttributeNotNull("type");
			
			GoodsPriceChangeNotification n = new GoodsPriceChangeNotification();
			n.type = Specification.instance.goodsTypes.getById(typeStr);
			n.from = attr.getIntAttribute("from");
			n.to = attr.getIntAttribute("to");
			
			nodeObject = n;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "goodsPriceChange";
		}
	}
}
