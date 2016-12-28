package net.sf.freecol.common.model.specification.options;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

class RangeValue {
	private String value;
	private String label;
	
	public RangeValue(String value, String label) {
		this.value = value;
		this.label = label;
	}
}

public class RangeOption extends ObjectWithId {

    private String value;
    private final List<RangeValue> rangeValues = new ArrayList<RangeValue>(5);
    
    public RangeOption(String id) {
        super(id);
    }

    public String getValue() {
        return value;
    }
    
    public int getValueAsInt() {
    	return Integer.parseInt(value);
    }
    
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
        	RangeOption ro = new RangeOption(attr.getStrAttribute("id"));
        	ro.value = attr.getStrAttribute("value");
        	nodeObject = ro;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("rangeValue")) {
        		((RangeOption)nodeObject).rangeValues.add(new RangeValue(
    				attr.getStrAttribute("value"), 
    				attr.getStrAttribute("label"))
        		);
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
