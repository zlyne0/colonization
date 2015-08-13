package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import com.badlogic.gdx.graphics.Color;

import promitech.colonization.GameResources;
import promitech.colonization.savegame.XmlNodeParser;

public class Nation implements Identifiable {
	private final String id;
	public final NationType nationType;
	private Color color;

	public Nation(String id, NationType nationType) {
		this.id = id;
		this.nationType = nationType;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public Color getColor() {
		return color;
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml(XmlNodeParser parent) {
			super(parent);
		}
		
		@Override
		public void startElement(String qName, Attributes attributes) {
			String nationTypeStr = getStrAttribute(attributes, "nation-type");
			String id = getStrAttribute(attributes, "id");
			NationType type = game.specification.nationTypes.getById(nationTypeStr);
			
			String colorStrVal = getStrAttribute(attributes, "color");
			
            Nation nation = new Nation(id, type);
			nation.color = GameResources.colorFromValue(colorStrVal);
			
			nodeObject = nation;
		}
		
		@Override
		public String getTagName() {
			return "nation";
		}
		
	}

}
