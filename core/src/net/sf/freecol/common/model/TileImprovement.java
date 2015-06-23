package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovement {
	public final TileImprovementType type;
	public final String style;
	public int magnitude = 0;
	
	public TileImprovement(TileImprovementType type, String style) {
		this.type = type;
		if (style != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < style.length(); i++) {
				char c = style.charAt(i);
				if (Character.digit(c, Character.MAX_RADIX) < 0) {
					break;
				}
				sb.append((c == '0') ? "0" : "1");
			}
			this.style = sb.toString();		
		} else {
			this.style = style;
		}
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			String typeStr = getStrAttribute(attributes, "type");
			String style = getStrAttribute(attributes, "style");
			TileImprovementType type = specification.getTileImprovementTypeBy(typeStr);
			TileImprovement tileImprovement = new TileImprovement(type, style);
			tileImprovement.magnitude = getIntAttribute(attributes, "magnitude", 0);
			
			((Tile.Xml)parentXmlNodeParser).tile.tileImprovements.add(tileImprovement);
		}

		@Override
		public String getTagName() {
			return "tileimprovement";
		}
		
	}
	
}
