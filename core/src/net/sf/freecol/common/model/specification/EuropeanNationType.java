package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.ObjectWithFeatures;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class EuropeanNationType extends NationType {

	private boolean ref;
    
	public EuropeanNationType(String id) {
		super(id);
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
