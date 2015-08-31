package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Europe extends ObjectWithFeatures {

    /** The initial recruit price. */
    private static final int RECRUIT_PRICE_INITIAL = 200;

    /** The initial lower bound on recruitment price. */
    private static final int LOWER_CAP_INITIAL = 80;
    
    private int recruitPrice;
    private int recruitLowerCap;
    
    public Europe(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNodeForMapIdEntities("abilities", Ability.class);
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
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "europe";
        }
        
    }
    
}
