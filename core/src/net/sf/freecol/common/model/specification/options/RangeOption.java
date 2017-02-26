package net.sf.freecol.common.model.specification.options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

class RangeValue {
	private String value;
	private String label;
	
	public RangeValue(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}
}

public class RangeOption extends ObjectWithId {

    private String value;
    private final List<RangeValue> rangeValues = new ArrayList<RangeValue>(5);
	private String defaultValue;
    
    public RangeOption(String id) {
        super(id);
    }

    public String getValue() {
        return value;
    }
    
    public int getValueAsInt() {
    	return Integer.parseInt(value);
    }
    
    public static class Xml extends XmlNodeParser<RangeOption> {
        private static final String ATTR_LABEL = "label";
		private static final String TAG_RANGE_VALUE = "rangeValue";
		private static final String ATTR_DEFAULT_VALUE = "defaultValue";

		@Override
        public void startElement(XmlNodeAttributes attr) {
        	RangeOption ro = new RangeOption(attr.getStrAttribute(ATTR_ID));
        	ro.value = attr.getStrAttribute(ATTR_VALUE);
        	ro.defaultValue = attr.getStrAttribute(ATTR_DEFAULT_VALUE);
        	if (ro.value == null) {
        		ro.value = ro.defaultValue;
        	}
        	nodeObject = ro;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(TAG_RANGE_VALUE)) {
        		nodeObject.rangeValues.add(new RangeValue(
    				attr.getStrAttribute(ATTR_VALUE), 
    				attr.getStrAttribute(ATTR_LABEL))
        		);
        	}
        }
        
        @Override
        public void startWriteAttr(RangeOption ro, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(ro);
        	attr.set(ATTR_VALUE, ro.value);
        	attr.set(ATTR_DEFAULT_VALUE, ro.defaultValue);
        	
        	for (RangeValue rv : ro.rangeValues) {
        		attr.xml.element(TAG_RANGE_VALUE);
        		attr.set(ATTR_LABEL, rv.getLabel());
        		attr.set(ATTR_VALUE, rv.getValue());
        		attr.xml.pop();
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "rangeOption";
        }
    }
}
