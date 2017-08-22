package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianNationType extends NationType {

	private final Set<String> regionNames = new HashSet<String>(); 
	
    public IndianNationType(String id) {
		super(id);
	}

	public boolean isREF() {
        return false;
    }
	
	public boolean isIndian() {
		return true;
	}

	public Set<String> getRegionNames() {
		return regionNames;
	}
	
    public static class Xml extends XmlNodeParser<IndianNationType> {
        private static final String ATTR_REGION = "region";

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
