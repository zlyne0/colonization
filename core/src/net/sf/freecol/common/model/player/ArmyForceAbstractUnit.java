package net.sf.freecol.common.model.player;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ArmyForceAbstractUnit extends ObjectWithId {
    private UnitType unitType;
    private UnitRole unitRole;
    private int amount;
    
    public ArmyForceAbstractUnit(UnitType unitType, UnitRole unitRole, int amount) {
        super(unitRole.getId());
        this.unitType = unitType;
        this.unitRole = unitRole;
        this.amount = amount;
    }

	public UnitType getUnitType() {
		return unitType;
	}

	public UnitRole getUnitRole() {
		return unitRole;
	}

	public int getAmount() {
		return amount;
	}

	public void addAmount(ArmyForceAbstractUnit army) {
		this.amount += army.amount;
	}
	
	public void increaseAmount() {
		this.amount++;
	}
	
	public String toString() {
		return "type: " + unitType.getId() + ", role: " + unitRole.getId() + ", amount: " + amount; 
	}
	
    public static class Xml extends XmlNodeParser<ArmyForceAbstractUnit> {
        private static final String ATTR_NUMBER = "number";
		private static final String ATTR_ROLE = "role";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            UnitType unitType = Specification.instance.unitTypes.getById(attr.getStrAttribute(ATTR_ID));
            UnitRole unitRole = Specification.instance.unitRoles.getById(attr.getStrAttribute(ATTR_ROLE));
            int amount = attr.getIntAttribute(ATTR_NUMBER);
            
            ArmyForceAbstractUnit u = new ArmyForceAbstractUnit(unitType, unitRole, amount);
            nodeObject = u;
        }

        @Override
        public void startWriteAttr(ArmyForceAbstractUnit u, XmlNodeAttributesWriter attr) throws IOException {
        	attr.set(ATTR_ID, u.unitType);
        	attr.set(ATTR_ROLE, u.unitRole);
        	attr.set(ATTR_NUMBER, u.amount);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "abstractUnit";
        }
    }

}