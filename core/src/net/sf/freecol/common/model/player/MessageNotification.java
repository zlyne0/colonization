package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class MessageNotification extends ObjectWithId implements Notification, Identifiable {
	
	private final String body;

	public MessageNotification(String id, String body) {
		super(id);
		this.body = body;
	}

	public String getBody() {
		return body;
	}
	
	public String toString() {
		return "MessageNotification[id: " + id + ", body: " + body + "]";
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttributeNotNull("id");
			String body = attr.getStrAttributeNotNull("body");
			nodeObject = new MessageNotification(id, body);
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "messageNotification";
		}
		
	}

}
