package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
    
    public static class Xml extends XmlNodeParser<BooleanOption> {
        private static final String ATTR_DEFAULT_VALUE = "defaultValue";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            BooleanOption option = new BooleanOption(id);
            option.defaultValue = attr.getBooleanAttribute(ATTR_DEFAULT_VALUE, false);
            option.value = attr.getBooleanAttribute(ATTR_VALUE, option.defaultValue);
            nodeObject = option;
        }

		@Override
		public void startWriteAttr(BooleanOption option, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(option);
			attr.set(ATTR_VALUE, option.value);
			attr.set(ATTR_DEFAULT_VALUE, option.defaultValue);
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
