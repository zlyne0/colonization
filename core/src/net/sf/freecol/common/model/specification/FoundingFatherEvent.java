package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

class FoundingFatherEvent extends ObjectWithId {

    private String value;
    
    public FoundingFatherEvent(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            FoundingFatherEvent ffe = new FoundingFatherEvent(attr.getStrAttributeNotNull("id"));
            ffe.value = attr.getStrAttribute("value");
            nodeObject = ffe;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "event";
        }
    }
    
}
