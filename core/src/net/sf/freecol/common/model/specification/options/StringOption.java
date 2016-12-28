package net.sf.freecol.common.model.specification.options;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class StringOption extends ObjectWithId {

    protected String value;
    
    public StringOption(String id) {
        super(id);
    }

    public String getValue() {
        return value;
    }
    
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            StringOption option = new StringOption(id);
            option.value = attr.getStrAttribute("value");
            nodeObject = option;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "stringOption";
        }
    }
}
