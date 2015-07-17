package net.sf.freecol.common.model;

import net.sf.freecol.common.util.StringUtils;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class UnitRole implements Identifiable {
	private static final String MODEL_ROLE_DEFAULT = "model.role.default";
	
	private final String id;
	private final String roleSuffix;
	
	public UnitRole(String id) {
		this.id = id;
		if (MODEL_ROLE_DEFAULT.equals(id)) {
			this.roleSuffix = ""; 
		} else {
			this.roleSuffix = "." + StringUtils.lastPart(id, ".");
		}
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public String getRoleSuffix() {
		return roleSuffix;
	}
	
	public static class Xml extends XmlNodeParser {

		public Xml(XmlNodeParser parent) {
			super(parent);
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			String idStr = getStrAttribute(attributes, "id");
			
			UnitRole role = new UnitRole(idStr);
			specification.unitRoles.add(role);
		}

		@Override
		public String getTagName() {
			return "role";
		}

	}
}
