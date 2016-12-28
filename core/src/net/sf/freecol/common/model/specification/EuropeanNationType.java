package net.sf.freecol.common.model.specification;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class EuropeanNationType extends NationType {

	private boolean ref;
	private List<EuropeanStartingAbstractUnit> startingUnits = new ArrayList<EuropeanStartingAbstractUnit>();
	private List<EuropeanStartingAbstractUnit> expertStartingUnits = new ArrayList<EuropeanStartingAbstractUnit>();
    
	public EuropeanNationType(String id) {
		super(id);
	}
    
	public MapIdEntities<EuropeanStartingAbstractUnit> getStartedUnits(boolean experts) {
		MapIdEntities<EuropeanStartingAbstractUnit> units = new MapIdEntities<EuropeanStartingAbstractUnit>();
		for (EuropeanStartingAbstractUnit u : startingUnits) {
			units.add(u);
		}
		if (experts) {
			for (EuropeanStartingAbstractUnit u : expertStartingUnits) {
				units.add(u);
			}
		}
		return units;
	}

	protected void addStartingUnit(EuropeanStartingAbstractUnit abstractUnit) {
		if (abstractUnit.isStartingAsExpertUnit()) {
			expertStartingUnits.add(abstractUnit);
		} else {
			startingUnits.add(abstractUnit);
		}
	}
	
    @Override
    public boolean isREF() {
        return ref;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
        	NationType.Xml.abstractAddNodes(this);

            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            
            addNode(EuropeanStartingAbstractUnit.class, new ObjectFromNodeSetter<EuropeanNationType, EuropeanStartingAbstractUnit>() {
				@Override
				public void set(EuropeanNationType target, EuropeanStartingAbstractUnit entity) {
					target.addStartingUnit(entity);
				}
			});
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            EuropeanNationType nationType = new EuropeanNationType(attr.getStrAttribute("id"));
            nationType.european = true;
            nationType.ref = attr.getBooleanAttribute("ref");
            
            NationType.Xml.abstractStartElement(attr, nationType);
            nodeObject = nationType;
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "european-nation-type";
        }
    }
}
