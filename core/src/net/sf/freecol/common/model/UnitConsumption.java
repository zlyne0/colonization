package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitConsumption implements Identifiable {

	private String id;
	private int quantity;
	
	@Override
	public String getId() {
		return id;
	}

	public int getQuantity() {
		return quantity;
	}
	
	public static class Xml extends XmlNodeParser {
		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitConsumption uc = new UnitConsumption();
			uc.id = attr.getStrAttribute("id");
			uc.quantity = attr.getIntAttribute("value", 0);
			
			nodeObject = uc;
		}

		@Override
		public String getTagName() {
			return tagName();
		}
		
        public static String tagName() {
        	return "consumes";
        }
	}
}
