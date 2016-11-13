package net.sf.freecol.common.model.specification;

import java.util.HashSet;
import java.util.Set;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianNationType extends NationType {

	private final Set<String> regionNames = new HashSet<String>(); 
	
    public IndianNationType(String id) {
		super(id);
	}

	public boolean isREF() {
        return false;
    }

	public Set<String> getRegionNames() {
		return regionNames;
	}
	
    public static class Xml extends XmlNodeParser {
        public Xml() {
        	NationType.Xml.abstractAddNodes(this);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            NationType nationType = new IndianNationType(attr.getStrAttribute("id"));
            nationType.european = false;
            
            NationType.Xml.abstractStartElement(attr, nationType);
            nodeObject = nationType;
        }
        
        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("region")) {
        		((IndianNationType)nodeObject).regionNames.add(attr.getStrAttributeNotNull("id"));
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
