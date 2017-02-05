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
		xml.attribute("id", identifiable.getId());
	}

	public void set(String attrName, String val) throws IOException {
		xml.attribute(attrName, val);
	}

	public void set(String attrName, int val) throws IOException {
		xml.attribute(attrName, Integer.toString(val));
	}
}
