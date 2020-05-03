package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TileTypeTransformation implements Identifiable {
	private String from;
	private String to;

	private TileType toType;
	private AbstractGoods production;
	
	@Override
	public String getId() {
		return from;
	}

	public TileType getToType() {
		return toType;
	}

	public AbstractGoods getProduction() {
		return production;
	}

	public void addTransformationProductionToSettlement(Unit unit, Settlement settlement) {
		int prodAmount = production.getQuantity();
		if (unit.unitType.hasAbility(Ability.EXPERT_PIONEER)) {
			prodAmount *= 2;
		}
		prodAmount = settlement.applyModifiers(Modifier.TILE_TYPE_CHANGE_PRODUCTION, prodAmount);
		settlement.addGoods(production.getTypeId(), prodAmount);
	}
	
	public static class Xml extends XmlNodeParser<TileTypeTransformation> {

		private static final String ATTR_VALUE = "value";
		private static final String ATTR_GOODS_TYPE = "goods-type";
		private static final String ATTR_TO = "to";
		private static final String ATTR_FROM = "from";

		public Xml() {
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			TileTypeTransformation c = new TileTypeTransformation();
			c.from = attr.getStrAttribute(ATTR_FROM);
			c.to = attr.getStrAttribute(ATTR_TO);
			c.toType = Specification.instance.tileTypes.getById(c.to);
			c.production = new AbstractGoods(attr.getStrAttribute(ATTR_GOODS_TYPE), attr.getIntAttribute(ATTR_VALUE));
			nodeObject = c;
		}

		@Override
		public void startWriteAttr(TileTypeTransformation c, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_FROM, c.from);
			attr.set(ATTR_TO, c.to);
			attr.set(ATTR_GOODS_TYPE, c.production.getTypeId());
			attr.set(ATTR_VALUE, c.production.getQuantity());
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
