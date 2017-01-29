package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.specification.options.UnitListOption;
import net.sf.freecol.common.model.specification.options.UnitOption;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ArmyForce implements Identifiable {

    public final MapIdEntities<ArmyForceAbstractUnit> navalUnits = new MapIdEntities<ArmyForceAbstractUnit>();
    public final MapIdEntities<ArmyForceAbstractUnit> landUnits = new MapIdEntities<ArmyForceAbstractUnit>();

    public ArmyForce() {
	}
    
    public ArmyForce(UnitListOption unitListOption) {
    	for (UnitOption unitOption : unitListOption.unitOptions.entities()) {
    		addArmy(unitOption.createArmyForce());
    	}
    }
    
    @Override
    public String getId() {
        throw new IllegalStateException("object do not has id");
    }

    public int spaceRequired() {
        int spaceRequired = 0;
        for (ArmyForceAbstractUnit lu : landUnits.entities()) {
            spaceRequired += lu.getUnitType().getSpaceTaken() * lu.getAmount();
        }
    	return spaceRequired;    			
    }
    
    public int capacity() {
    	int capacity = 0;
    	for (ArmyForceAbstractUnit a : navalUnits.entities()) {
    		if (a.getUnitType().canCarryUnits()) {
    			capacity += a.getUnitType().getSpace() * a.getAmount();
    		}
    	}
    	return capacity;
    }

	public void addArmy(ArmyForceAbstractUnit army) {
		ArmyForceAbstractUnit existed;
		if (army.getUnitType().isNaval()) {
			existed = navalUnits.getByIdOrNull(army.getUnitRole().getId());
			if (existed != null) {
				existed.addAmount(army);
			} else {
				navalUnits.add(army);
			}
		} else {
			existed = landUnits.getByIdOrNull(army.getUnitRole().getId());
			if (existed != null) {
				existed.addAmount(army);
			} else {
				landUnits.add(army);
			}
		}
	}
    
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNodeForMapIdEntities("navalUnits", "navalUnits", ArmyForceAbstractUnit.class);
            addNodeForMapIdEntities("landUnits", "landUnits", ArmyForceAbstractUnit.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            nodeObject = new ArmyForce();
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "not set, xml node parent set name";
        }
        
    }

}
