package net.sf.freecol.common.model.specification.options;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitListOption extends ObjectWithId {

	public final MapIdEntities<UnitOption> unitOptions = new MapIdEntities<UnitOption>();	
	
	private int maximumNumber;
	
	public UnitListOption(String id) {
		super(id);
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml() {
			addNodeForMapIdEntities("unitOptions", UnitOption.class);
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitListOption ulo = new UnitListOption(attr.getStrAttribute("id"));
			ulo.maximumNumber = attr.getIntAttribute("maximumNumber", 1);
			nodeObject = ulo;
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
