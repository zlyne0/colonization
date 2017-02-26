package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class StringOption extends ObjectWithId {

    protected String value;
	protected String defaultValue;
    
    public StringOption(String id) {
        super(id);
    }

    public String getValue() {
        return value;
    }
    
    public static class Xml extends XmlNodeParser<StringOption> {
        private static final String ATTR_DEFAULT_VALUE = "defaultValue";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            StringOption option = new StringOption(id);
            option.defaultValue = attr.getStrAttribute(ATTR_DEFAULT_VALUE);
            option.value = attr.getStrAttribute(ATTR_VALUE);
            if (option.value == null) {
            	option.value = option.defaultValue;
            }
            nodeObject = option;
        }

        @Override
        public void startWriteAttr(StringOption option, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(option);
        	attr.set(ATTR_VALUE, option.value);
        	attr.set(ATTR_DEFAULT_VALUE, option.defaultValue);
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
