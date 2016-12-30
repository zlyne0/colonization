package net.sf.freecol.common.model.specification.options;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class SelectOption extends StringOption {

    public SelectOption(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            SelectOption option = new SelectOption(id);
            option.value = attr.getStrAttribute("value");
            option.defaultValue = attr.getStrAttribute("defaultValue");
            if (option.value == null) {
            	option.value = option.defaultValue;
            }
            nodeObject = option;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "selectOption";
        }
    }
}
