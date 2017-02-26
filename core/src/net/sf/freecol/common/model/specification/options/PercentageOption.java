package net.sf.freecol.common.model.specification.options;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class PercentageOption extends ObjectWithId {

    private int value;
    private int defaultValue;
    
    public PercentageOption(String id) {
        super(id);
    }

    public int getValue() {
        return value;
    }

    public static class Xml extends XmlNodeParser<PercentageOption> {
		private static final String ATTR_DEFAULT_VALUE = "defaultValue";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            PercentageOption option = new PercentageOption(id);
            
            option.defaultValue = attr.getIntAttribute(ATTR_DEFAULT_VALUE, 0);
            option.value = attr.getIntAttribute(ATTR_VALUE, option.defaultValue);
            
            nodeObject = option;
        }

		@Override
		public void startWriteAttr(PercentageOption option, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(option);

			attr.set(ATTR_DEFAULT_VALUE, option.defaultValue);
			attr.set(ATTR_VALUE, option.value);
		}
		
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "percentageOption";
        }
    }

}
