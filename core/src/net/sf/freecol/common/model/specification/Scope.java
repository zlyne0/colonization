package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.Identifiable;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

class Scope implements Identifiable {

	boolean matchNegated = false;
	boolean matchesNull = true;
	public String abilityId = null;
	public boolean abilityValue = true;
	public String type;
	
	@Override
	public String getId() {
		throw new IllegalStateException("there is no id for feature scope");
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Scope scope = new Scope();
			scope.matchNegated = attr.getBooleanAttribute("matchNegated", false);
			scope.matchesNull = attr.getBooleanAttribute("matchesNull", true);
			scope.abilityId = attr.getStrAttribute("ability-id");
			scope.abilityValue = attr.getBooleanAttribute("ability-value", true);
			scope.type = attr.getStrAttribute("type");
			
			nodeObject = scope;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "scope";
		}
	}

}
