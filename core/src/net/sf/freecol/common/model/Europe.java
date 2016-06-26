package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Europe extends ObjectWithFeatures implements UnitLocation {

    /** The initial recruit price. */
    private static final int RECRUIT_PRICE_INITIAL = 200;

    /** The initial lower bound on recruitment price. */
    private static final int LOWER_CAP_INITIAL = 80;
    
    private int recruitPrice;
    private int recruitLowerCap;
    private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    protected final List<UnitType> recruitables = new ArrayList<UnitType>();
    
    public Europe(String id) {
        super(id);
    }

	@Override
	public MapIdEntities<Unit> getUnits() {
		return units;
	}
    
	public List<UnitType> getRecruitables() {
		return recruitables;
	}
	
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            
            addNode(Unit.class, new ObjectFromNodeSetter<Europe,Unit>() {
                @Override
                public void set(Europe europe, Unit unit) {
                    unit.changeUnitLocation(europe);
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            Europe eu = new Europe(id);
            eu.recruitPrice = attr.getIntAttribute("recruitPrice", RECRUIT_PRICE_INITIAL);
            eu.recruitLowerCap = attr.getIntAttribute("recruitLowerCap", LOWER_CAP_INITIAL);
            nodeObject = eu;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("recruit")) {
        		UnitType unitType = Specification.instance.unitTypes.getById(attr.getStrAttribute("id"));
        		((Europe)nodeObject).recruitables.add(unitType);
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "europe";
        }
    }
}
