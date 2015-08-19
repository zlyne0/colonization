package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileImprovement implements Identifiable {
    
    private String id;
    
	public final TileImprovementType type;
	public final String style;
	public int magnitude = 0;
	private int turns = 0;
	
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

	@Override
	public String getId() {
	    return id;
	}
	
	public boolean isComplete() {
		return turns <= 0;
	}
	
	public static class Xml extends XmlNodeParser {

	    public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String typeStr = attr.getStrAttribute("type");
			String style = attr.getStrAttribute("style");
			TileImprovementType type = game.specification.tileImprovementTypes.getById(typeStr);
			TileImprovement tileImprovement = new TileImprovement(type, style);
			tileImprovement.magnitude = attr.getIntAttribute("magnitude", 0);
			tileImprovement.turns = attr.getIntAttribute("turns");
			tileImprovement.id = attr.getStrAttribute("id");
			
			nodeObject = tileImprovement;
		}

		@Override
		public String getTagName() {
		    return tagName();
		}
		
		public static String tagName() {
		    return "tileimprovement";
		}
	}
}
