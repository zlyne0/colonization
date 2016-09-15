package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

class ArmyForceAbstractUnit extends ObjectWithId {
    private UnitType unitType;
    private UnitRole unitRole;
    private int amount;
    
    public ArmyForceAbstractUnit(UnitType unitType, UnitRole unitRole, int amount) {
        super(unitType.getId());
        this.unitType = unitType;
        this.unitRole = unitRole;
    }
    
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            UnitType unitType = Specification.instance.unitTypes.getById(attr.getStrAttribute("id"));
            UnitRole unitRole = Specification.instance.unitRoles.getById(attr.getStrAttribute("role"));
            int amount = attr.getIntAttribute("number", 0);
            
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