package net.sf.freecol.common.model;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileTypeTransformation implements Identifiable {
	private String from;
	private String to;
	private ProductionInfo productionInfo = new ProductionInfo() ;

	private TileType toType;
	
	@Override
	public String getId() {
		return from;
	}

	public TileType getToType() {
		return toType;
	}

	public ProductionInfo getProductionInfo() {
		return productionInfo;
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml() {
            addNode(Production.class, new ObjectFromNodeSetter<TileTypeTransformation, Production>() {
				@Override
				public void set(TileTypeTransformation target, Production entity) {
					target.productionInfo.addProduction(entity);
				}
			});
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			TileTypeTransformation c = new TileTypeTransformation();
			c.from = attr.getStrAttribute("from");
			c.to = attr.getStrAttribute("to");
			c.toType = Specification.instance.tileTypes.getById(c.to);
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
}
