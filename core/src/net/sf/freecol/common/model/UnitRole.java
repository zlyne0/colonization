package net.sf.freecol.common.model;

import net.sf.freecol.common.util.StringUtils;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitRole extends ObjectWithFeatures {
	private static final String MODEL_ROLE_DEFAULT = "model.role.default";
	
	private final String roleSuffix;
	
	public UnitRole(String id) {
		super(id);
		
		if (MODEL_ROLE_DEFAULT.equals(id)) {
			this.roleSuffix = ""; 
		} else {
			this.roleSuffix = "." + StringUtils.lastPart(id, ".");
		}
	}
	
	public String getRoleSuffix() {
		return roleSuffix;
	}
	
    public boolean isOffensive() {
        return hasModifier(Modifier.OFFENCE);
    }
	
	public static class Xml extends XmlNodeParser {
		public Xml(XmlNodeParser parent) {
			super(parent);
			
            addNode(new MapIdEntities.Xml(this, "modifiers", Modifier.class));
            addNode(new MapIdEntities.Xml(this, "abilities", Ability.class));
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String idStr = attr.getStrAttribute("id");
			nodeObject = new UnitRole(idStr);
		}

		@Override
		public String getTagName() {
			return "role";
		}
	}
}
