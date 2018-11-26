package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianNationType extends NationType {

	private final Set<String> regionNames = new HashSet<String>();
	private final List<RandomChoice<UnitType>> skills = new ArrayList<RandomChoice<UnitType>>();
	
    public IndianNationType(String id) {
		super(id);
	}

	public boolean isREF() {
        return false;
    }
	
	public boolean isIndian() {
		return true;
	}

	public List<RandomChoice<UnitType>> getSkills() {
		return skills;
	}
	
	public Set<String> getRegionNames() {
		return regionNames;
	}
	
    public static class Xml extends XmlNodeParser<IndianNationType> {
		private static final String ATTR_REGION = "region";
        private static final String TAG_SKILL = "skill";
        private static final String ATTR_PROBABILITY = "probability";

		public Xml() {
			NationType.Xml.abstractAddNodes(this);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
        	IndianNationType nationType = new IndianNationType(attr.getStrAttribute(ATTR_ID));
            nationType.european = false;
            
            NationType.Xml.abstractStartElement(attr, nationType);
            nodeObject = nationType;
        }
        
        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(ATTR_REGION)) {
        		nodeObject.regionNames.add(attr.getStrAttributeNotNull(ATTR_ID));
        	}
        	if (attr.isQNameEquals(TAG_SKILL)) {
        		nodeObject.skills.add(new RandomChoice<UnitType>(
    				attr.getEntityId(Specification.instance.unitTypes), 
    				attr.getIntAttribute(ATTR_PROBABILITY)
				));
        	}
        }
        
        @Override
        public void startWriteAttr(IndianNationType nationType, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(nationType);
        	NationType.Xml.abstractStartWriteAttr(nationType, attr);
        	
        	for (String regionName : nationType.regionNames) {
        		attr.xml.element(ATTR_REGION);
        		attr.set(ATTR_ID, regionName);
        		attr.xml.pop();
        	}
        	for (RandomChoice<UnitType> skill : nationType.skills) {
        		attr.xml.element(TAG_SKILL);
        		attr.setId(skill.probabilityObject());
        		attr.set(ATTR_PROBABILITY, skill.getOccureProbability());
        		attr.xml.pop();
			}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "indian-nation-type";
        }
    }
}
