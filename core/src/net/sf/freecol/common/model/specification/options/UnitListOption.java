package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitListOption extends ObjectWithId {

	public final MapIdEntities<UnitOption> unitOptions = new MapIdEntities<UnitOption>();	
	
	private int maximumNumber;
	
	public UnitListOption(String id) {
		super(id);
	}
	
	public static class Xml extends XmlNodeParser<UnitListOption> {

		private static final String ATTR_MAXIMUM_NUMBER = "maximumNumber";

		public Xml() {
			addNodeForMapIdEntities("unitOptions", UnitOption.class);
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitListOption ulo = new UnitListOption(attr.getStrAttribute(ATTR_ID));
			ulo.maximumNumber = attr.getIntAttribute(ATTR_MAXIMUM_NUMBER, 1);
			nodeObject = ulo;
		}

		@Override
		public void startWriteAttr(UnitListOption ulo, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(ulo);
			attr.set(ATTR_MAXIMUM_NUMBER, ulo.maximumNumber);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "unitListOption";
		}
		
	}
	
}
