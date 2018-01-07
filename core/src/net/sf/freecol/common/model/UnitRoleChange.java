package net.sf.freecol.common.model;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class UnitRoleChange implements Identifiable {

    /*
     * from the source role identifier
     */
	private final String from;
	/*
	 * capture the identifier for the role to capture
	 */
	private final String capture;
	
	public UnitRoleChange(String from, String capture) {
		this.from = from;
		this.capture = capture;
	}
	
	@Override
	public String getId() {
		return from + ":" + capture;
	}

	boolean match(UnitRole fromRole, UnitRole toRole) {
		return fromRole.equalsId(from) && toRole.equalsId(capture);
	}
	
	public static class Xml extends XmlNodeParser<UnitRoleChange> {

		private static final String ATTR_FROM = "from";
		private static final String ATTR_CAPTURE = "capture";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			nodeObject = new UnitRoleChange(
				attr.getStrAttribute(ATTR_FROM), 
				attr.getStrAttribute(ATTR_CAPTURE)
			); 
		}
		
		@Override
		public void startWriteAttr(UnitRoleChange node, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_FROM, node.from);
			attr.set(ATTR_CAPTURE, node.capture);
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "role-change";
		}
	}
}
