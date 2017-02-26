package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class SelectOption extends StringOption {

    public SelectOption(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser<SelectOption> {
        private static final String ATTR_DEFAULT_VALUE = "defaultValue";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            SelectOption option = new SelectOption(id);
            option.value = attr.getStrAttribute(ATTR_VALUE);
            option.defaultValue = attr.getStrAttribute(ATTR_DEFAULT_VALUE);
            if (option.value == null) {
            	option.value = option.defaultValue;
            }
            nodeObject = option;
        }

		@Override
		public void startWriteAttr(SelectOption so, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(so);
			attr.set(ATTR_VALUE, so.value);
			attr.set(ATTR_DEFAULT_VALUE, so.defaultValue);
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
