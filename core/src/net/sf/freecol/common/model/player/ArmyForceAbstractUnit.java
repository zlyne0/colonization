package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
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
	
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            UnitType unitType = Specification.instance.unitTypes.getById(attr.getStrAttribute("id"));
            UnitRole unitRole = Specification.instance.unitRoles.getById(attr.getStrAttribute("role"));
            int amount = attr.getIntAttribute("number");
            
            ArmyForceAbstractUnit u = new ArmyForceAbstractUnit(unitType, unitRole, amount);
            nodeObject = u;
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