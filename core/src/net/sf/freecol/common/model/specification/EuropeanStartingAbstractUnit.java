package net.sf.freecol.common.model.specification;

import java.io.IOException;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class EuropeanStartingAbstractUnit implements Identifiable {

	private final String id;
	private final UnitType type;
	private final UnitRole role;
	private final boolean startingAsExpertUnit;
	
	public EuropeanStartingAbstractUnit(String id, UnitType type, UnitRole role, boolean expert) {
		this.id = id;
		this.type = type;
		this.role = role;
		this.startingAsExpertUnit = expert;
	}

	@Override
	public String getId() {
		return id;
	}

	public UnitType getType() {
		return type;
	}

	public UnitRole getRole() {
		return role;
	}

	public boolean isStartingAsExpertUnit() {
		return startingAsExpertUnit;
	}
	
	public String toString() {
		return "id = " + id + ", type = " + type.getId() + ", role = " + role.getId() + ", startingAsExpertUnit = " + startingAsExpertUnit;
	}
	
	public static class Xml extends XmlNodeParser<EuropeanStartingAbstractUnit> {

		private static final String ATTR_EXPERT_STARTING_UNITS = "expert-starting-units";
		private static final String ATTR_ROLE = "role";
		private static final String ATTR_TYPE = "type";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitType type = Specification.instance.unitTypes.getById(attr.getStrAttribute(ATTR_TYPE));
			String roleStr = attr.getStrAttribute(ATTR_ROLE);
			if (roleStr == null) {
				roleStr = UnitRole.DEFAULT_ROLE_ID;
			}
			UnitRole role = Specification.instance.unitRoles.getById(roleStr);
			
			EuropeanStartingAbstractUnit u = new EuropeanStartingAbstractUnit(
				attr.getStrAttribute(ATTR_ID),
				type,
				role,
				attr.getBooleanAttribute(ATTR_EXPERT_STARTING_UNITS, false)
			);
			nodeObject = u;
		}

		@Override
		public void startWriteAttr(EuropeanStartingAbstractUnit u, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(u);
			attr.set(ATTR_TYPE, u.type.getId());
			attr.set(ATTR_ROLE, u.role.getId());
			attr.set(ATTR_EXPERT_STARTING_UNITS, u.startingAsExpertUnit);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "unit";
		}
	}
}
