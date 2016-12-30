package net.sf.freecol.common.model.specification.options;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IntegerOption extends ObjectWithId {

    private int value;
    private int defaultValue;
    private int maximumValue;
    private int minimumValue;
    
    public IntegerOption(String id) {
        super(id);
    }

    public int getValue() {
        return value;
    }

    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            IntegerOption option = new IntegerOption(id);
            
            option.defaultValue = attr.getIntAttribute("defaultValue", 0);
            option.value = attr.getIntAttribute("value", option.defaultValue);
            option.maximumValue = attr.getIntAttribute("maximumValue", Integer.MAX_VALUE);
            option.minimumValue = attr.getIntAttribute("minimumValue", Integer.MIN_VALUE);
            
            nodeObject = option;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "integerOption";
        }
    }

}
