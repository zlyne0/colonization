package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Goods;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileTypeTransformation implements Identifiable {
	private String from;
	private String to;

	private TileType toType;
	private Goods production;
	
	@Override
	public String getId() {
		return from;
	}

	public TileType getToType() {
		return toType;
	}

	public static class Xml extends XmlNodeParser {

		public Xml() {
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			TileTypeTransformation c = new TileTypeTransformation();
			c.from = attr.getStrAttribute("from");
			c.to = attr.getStrAttribute("to");
			c.toType = Specification.instance.tileTypes.getById(c.to);
			c.production = new Goods(attr.getStrAttribute("goods-type"), attr.getIntAttribute("value"));
			nodeObject = c;
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "change";
		}
		
	}

	public Goods getProduction() {
		return production;
	}
}
