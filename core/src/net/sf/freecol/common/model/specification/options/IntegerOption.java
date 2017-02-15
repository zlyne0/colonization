package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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

    public static class Xml extends XmlNodeParser<IntegerOption> {
        private static final String ATTR_MINIMUM_VALUE = "minimumValue";
		private static final String ATTR_MAXIMUM_VALUE = "maximumValue";
		private static final String ATTR_DEFAULT_VALUE = "defaultValue";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            IntegerOption option = new IntegerOption(id);
            
            option.defaultValue = attr.getIntAttribute(ATTR_DEFAULT_VALUE, 0);
            option.value = attr.getIntAttribute(ATTR_VALUE, option.defaultValue);
            option.maximumValue = attr.getIntAttribute(ATTR_MAXIMUM_VALUE, Integer.MAX_VALUE);
            option.minimumValue = attr.getIntAttribute(ATTR_MINIMUM_VALUE, Integer.MIN_VALUE);
            
            nodeObject = option;
        }

		@Override
		public void startWriteAttr(IntegerOption option, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(option);

			attr.set(ATTR_DEFAULT_VALUE, option.defaultValue);
			attr.set(ATTR_VALUE, option.value);
			attr.set(ATTR_MAXIMUM_VALUE, option.maximumValue, Integer.MAX_VALUE);
			attr.set(ATTR_MINIMUM_VALUE, option.minimumValue, Integer.MIN_VALUE);
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
