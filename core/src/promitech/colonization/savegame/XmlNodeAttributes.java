package promitech.colonization.savegame;

import org.xml.sax.Attributes;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;

public class XmlNodeAttributes {
    String qName; 
    Attributes attributes;
    private final GridPoint2 tmpPoint = new GridPoint2();
    
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

    public byte getByteAttribute(String name, byte defaultValue) {
        String val = attributes.getValue(name);
        if (val == null) {
            return defaultValue;
        }
        return Byte.parseByte(val);
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
    
    public String getStrAttribute(String name, String defaultVal) {
    	String st = attributes.getValue(name);
    	if (st == null) {
    		return defaultVal;
    	}
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
    
    public <T extends Identifiable> T getEntity(String attrName, MapIdEntitiesReadOnly<T> entities) {
    	String entityId = getStrAttribute(attrName);
    	if (entityId != null) {
    		return entities.getById(entityId);
    	}
    	return null;
    }
    
    public String getId() {
        return getStrAttributeNotNull(XmlNodeParser.ATTR_ID);
    }

    public <T extends Identifiable> T getEntityId(MapIdEntitiesReadOnly<T> entities) {
    	String id = getStrAttributeNotNull(XmlNodeParser.ATTR_ID);
        return entities.getById(id);
    }
    
    public GridPoint2 getPoint(String name) {
    	String pointStr = getStrAttributeNotNull(name);
    	String[] t = pointStr.split(",");
    	tmpPoint.x = Integer.parseInt(t[0]);
    	tmpPoint.y = Integer.parseInt(t[1]);
    	return tmpPoint;
    }
    
    public GridPoint2 getPoint(String name, int defaultX, int defaultY) {
    	String pointStr = getStrAttribute(name);
    	if (pointStr == null) {
    		return tmpPoint.set(defaultX, defaultY);
    	}
    	String[] t = pointStr.split(",");
    	tmpPoint.x = Integer.parseInt(t[0]);
    	tmpPoint.y = Integer.parseInt(t[1]);
    	return tmpPoint;
    }

	public boolean hasAttr(String name) {
		return attributes.getValue(name) != null;
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
