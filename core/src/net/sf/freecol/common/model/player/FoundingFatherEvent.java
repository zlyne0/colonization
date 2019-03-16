package net.sf.freecol.common.model.player;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

class FoundingFatherEvent extends ObjectWithId {

    private String value;
    
    public FoundingFatherEvent(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser<FoundingFatherEvent> {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            FoundingFatherEvent ffe = new FoundingFatherEvent(attr.getStrAttributeNotNull(ATTR_ID));
            ffe.value = attr.getStrAttribute(ATTR_VALUE);
            nodeObject = ffe;
        }

        @Override
        public void startWriteAttr(FoundingFatherEvent ffe, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(ffe);
        	attr.set(ATTR_VALUE, ffe.value);
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
