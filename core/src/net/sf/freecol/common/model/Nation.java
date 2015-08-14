package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.GameResources;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

import com.badlogic.gdx.graphics.Color;

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
        public void startElement(XmlNodeAttributes attr) {
			String nationTypeStr = attr.getStrAttribute("nation-type");
			String id = attr.getStrAttribute("id");
			NationType type = game.specification.nationTypes.getById(nationTypeStr);
			
			String colorStrVal = attr.getStrAttribute("color");
			
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
