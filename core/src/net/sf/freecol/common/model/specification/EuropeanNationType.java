package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
    
    public static class Xml extends XmlNodeParser<EuropeanNationType> {
        private static final String ATTR_REF = "ref";

		public Xml() {
        	NationType.Xml.abstractAddNodes(this);
            
            addNode(EuropeanStartingAbstractUnit.class, new ObjectFromNodeSetter<EuropeanNationType, EuropeanStartingAbstractUnit>() {
				@Override
				public void set(EuropeanNationType target, EuropeanStartingAbstractUnit entity) {
					target.addStartingUnit(entity);
				}
				@Override
				public void generateXml(EuropeanNationType source, ChildObject2XmlCustomeHandler<EuropeanStartingAbstractUnit> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.expertStartingUnits);
					xmlGenerator.generateXmlFromCollection(source.startingUnits);
				}
			});
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            EuropeanNationType nationType = new EuropeanNationType(attr.getStrAttribute(ATTR_ID));
            nationType.european = true;
            nationType.ref = attr.getBooleanAttribute(ATTR_REF);
            
            NationType.Xml.abstractStartElement(attr, nationType);
            
            if (nationType.parentNationTypeId != null) {
            	EuropeanNationType parent = (EuropeanNationType)Specification.instance.nationTypes.getById(nationType.parentNationTypeId);
            	nationType.startingUnits.addAll(parent.startingUnits);
            	nationType.expertStartingUnits.addAll(parent.expertStartingUnits);
            }            
            nodeObject = nationType;
        }
        
        @Override
        public void startWriteAttr(EuropeanNationType nationType, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(nationType);
        	attr.set(ATTR_REF, nationType.ref);
        	NationType.Xml.abstractStartWriteAttr(nationType, attr);
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
