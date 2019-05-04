package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitConsumption extends AbstractGoods implements Identifiable {

	private UnitConsumption(String typeId, int quantity) {
		super(typeId, quantity);
	}
	
	@Override
	public String getId() {
		return getTypeId();
	}
	
	public static class Xml extends XmlNodeParser<UnitConsumption> {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitConsumption uc = new UnitConsumption(
				attr.getId(), 
				attr.getIntAttribute(ATTR_VALUE, 0)
			);
			nodeObject = uc;
		}

		@Override
		public void startWriteAttr(UnitConsumption uc, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(uc);
			attr.set(ATTR_VALUE, uc.getQuantity());
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
