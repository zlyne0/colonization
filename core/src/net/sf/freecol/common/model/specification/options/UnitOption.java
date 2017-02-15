package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
	
	public UnitType getUnitType() {
		return Specification.instance.unitTypes.getById(unitTypeId);
	}
	
	public static class Xml extends XmlNodeParser<UnitOption> {

		private static final String ELEMENT_NUMBER = "number";
		private static final String ELEMENT_ROLE = "role";
		private static final String ELEMENT_UNIT_TYPE = "unitType";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			UnitOption uo = new UnitOption(attr.getStrAttribute(ATTR_ID));
			nodeObject = uo;
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals(ELEMENT_UNIT_TYPE)) {
				nodeObject.unitTypeId = attr.getStrAttribute(ATTR_VALUE); 
			}
			if (attr.isQNameEquals(ELEMENT_ROLE)) {
				nodeObject.roleId = attr.getStrAttribute(ATTR_VALUE); 
			}
			if (attr.isQNameEquals(ELEMENT_NUMBER)) {
				nodeObject.number = attr.getIntAttribute(ATTR_VALUE); 
			}
		}
		
		@Override
		public void startWriteAttr(UnitOption uo, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(uo);
			
			if (uo.unitTypeId != null) {
				attr.xml.element(ELEMENT_UNIT_TYPE);
				attr.set(ATTR_VALUE, uo.unitTypeId);
				attr.xml.pop();
			}
			if (uo.roleId != null) {
				attr.xml.element(ELEMENT_ROLE);
				attr.set(ATTR_VALUE, uo.roleId);
				attr.xml.pop();
			}
			attr.xml.element(ELEMENT_NUMBER);
			attr.set(ATTR_VALUE, uo.number);
			attr.xml.pop();
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
