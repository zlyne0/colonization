package net.sf.freecol.common.model;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
	
	public static class Xml extends XmlNodeParser<UnitConsumption> {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitConsumption uc = new UnitConsumption();
			uc.id = attr.getStrAttribute(ATTR_ID);
			uc.quantity = attr.getIntAttribute(ATTR_VALUE, 0);
			
			nodeObject = uc;
		}

		@Override
		public void startWriteAttr(UnitConsumption uc, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(uc);
			attr.set(ATTR_VALUE, uc.quantity);
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
