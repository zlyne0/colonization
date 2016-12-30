package net.sf.freecol.common.model.specification.options;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitOption extends ObjectWithId {

	private String unitTypeId;
	private String roleId;
	private int number;
	
	public UnitOption(String id) {
		super(id);
	}

	public ArmyForceAbstractUnit createArmyForce() {
		return new ArmyForceAbstractUnit(
			Specification.instance.unitTypes.getById(unitTypeId), 
			Specification.instance.unitRoles.getById(roleId), 
			number
		);
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitOption uo = new UnitOption(attr.getStrAttribute("id"));
			nodeObject = uo;
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals("unitType")) {
				((UnitOption)nodeObject).unitTypeId = attr.getStrAttribute("value"); 
			}
			if (attr.isQNameEquals("role")) {
				((UnitOption)nodeObject).roleId = attr.getStrAttribute("value"); 
			}
			if (attr.isQNameEquals("number")) {
				((UnitOption)nodeObject).number = attr.getIntAttribute("value"); 
			}
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "unitOption";
		}
	}
}
