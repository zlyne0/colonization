package promitech.colonization.savegame;

import org.xml.sax.Attributes;
import promitech.colonization.savegame.XmlNodeParser;

public class XmlNodeAttributes {
    String qName; 
    Attributes attributes;
    
    public boolean isQNameEquals(String qname) {
        return qName.equals(qname);
    }
    
    public float getFloatAttribute(String name) {
        return Float.parseFloat(attributes.getValue(name));
    }
    
    public float getFloatAttribute(String name, float defaultValue) {
        String val = attributes.getValue(name);
        if (val == null) {
            return defaultValue;
        }
        return Float.parseFloat(val);
    }
    
    public int getIntAttribute(String name) {
        return Integer.parseInt(attributes.getValue(name));
    }
    
    public int getIntAttribute(String name, int defaultVal) {
        String val = attributes.getValue(name);
        if (val == null) {
            return defaultVal;
        }
        return Integer.parseInt(val);
    }
    
    public String getStrAttribute(String name) {
        return attributes.getValue(name);
    }

    public String getStrAttributeNotNull(String name) {
        String st = attributes.getValue(name);
        if (st == null) {
        	throw new IllegalStateException("null attribute while reading '" + name + "'");
        }
        return st;
    }
    
    public boolean getBooleanAttribute(String name) {
        String val = attributes.getValue(name);
        return val != null && "true".equals(val.toLowerCase());
    }
    
    public boolean getBooleanAttribute(String name, boolean defaultValue) {
        String val = attributes.getValue(name);
        if (val == null) {
        	return defaultValue;
        }
        return "true".equals(val.toLowerCase());
    }
    
    public <T extends Enum<T>> T getEnumAttribute(Class<T> enumClass, String name, T defaultValue) {
    	String val = attributes.getValue(name);
    	if (val == null) {
    		return defaultValue;
    	}
    	return Enum.valueOf(enumClass, val.toUpperCase());
    }
    
    public <T extends Enum<T>> T getEnumAttribute(Class<T> enumClass, String name) {
    	return getEnumAttribute(enumClass, name, null);
    }
    
    public String getId() {
        return getStrAttributeNotNull(XmlNodeParser.ATTR_ID);
    }
    
    public String toString() {
        String st = "";
        for (int i=0; i<attributes.getLength(); i++) {
            st += "[";
            st += attributes.getQName(i) + " = " + attributes.getValue(i);
            st += "], ";
        }
        return "qName = " + qName + ", attributes = {" + st + "}"; 
    }
}
