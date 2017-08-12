package promitech.colonization.savegame;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlWriter;

import net.sf.freecol.common.model.Identifiable;

public class XmlNodeAttributesWriter {
    public final XmlWriter xml;

    public XmlNodeAttributesWriter(XmlWriter xml) {
    	this.xml = xml;
    }

	public void setId(Identifiable identifiable) throws IOException {
		xml.attribute(XmlNodeParser.ATTR_ID, identifiable.getId());
	}

	public void set(String attrName, Identifiable identifiable) throws IOException {
		if (identifiable != null) {
			xml.attribute(attrName, identifiable.getId());
		}
	}
	
	public void set(String attrName, String val) throws IOException {
		if (val != null) {
			xml.attribute(attrName, val);
		}
	}

	public void set(String attrName, int val) throws IOException {
		xml.attribute(attrName, Integer.toString(val));
	}

	public void set(String attrName, int val, int defaultVal) throws IOException {
		if (val != defaultVal) {
			xml.attribute(attrName, Integer.toString(val));
		}
	}
	
	public void set(String attrName, boolean val) throws IOException {
		xml.attribute(attrName, Boolean.toString(val));
	}

	public void set(String attrName, float val) throws IOException {
		xml.attribute(attrName, Float.toString(val));
	}

	public void set(String attrName, float val, float defaultVal) throws IOException {
		if (val != defaultVal) {
			xml.attribute(attrName, Float.toString(val));
		}
	}
	
	public <T extends Enum<T>> void set(String attrName, T val) throws IOException {
		if (val != null) {
			xml.attribute(attrName, val.name());
		}
	}

	public void setPoint(String attrName, int x, int y) throws IOException {
		xml.attribute(attrName, Integer.toString(x) + "," + Integer.toString(y));
	}
}
