package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
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
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitType type = Specification.instance.unitTypes.getById(attr.getStrAttribute("type"));
			String roleStr = attr.getStrAttribute("role");
			if (roleStr == null) {
				roleStr = UnitRole.DEFAULT_ROLE_ID;
			}
			UnitRole role = Specification.instance.unitRoles.getById(roleStr);
			
			EuropeanStartingAbstractUnit u = new EuropeanStartingAbstractUnit(
				attr.getStrAttribute("id"),
				type,
				role,
				attr.getBooleanAttribute("expert-starting-units", false)
			);
			nodeObject = u;
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
