package net.sf.freecol.common.model.specification.options;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class BooleanOption extends ObjectWithId {
	private boolean defaultValue;
    private boolean value;

    public BooleanOption(String id) {
        super(id);
    }

    public boolean getValue() {
        return value;
    }
    
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            BooleanOption option = new BooleanOption(id);
            option.defaultValue = attr.getBooleanAttribute("defaultValue", false);
            option.value = attr.getBooleanAttribute("value", option.defaultValue);
            nodeObject = option;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "booleanOption";
        }
    }
}
