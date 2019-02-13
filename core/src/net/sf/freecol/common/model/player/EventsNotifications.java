package net.sf.freecol.common.model.player;

import java.io.IOException;
import java.util.LinkedList;

import net.sf.freecol.common.model.Game;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class EventsNotifications {

    public static interface AddNotificationListener {
        public void onAddNotification(Notification notification);
    }
    
	private final LinkedList<Notification> notifications = new LinkedList<Notification>();
	private AddNotificationListener addNotificationListener;
    
    public void addMessageNotificationAsFirst(Notification notification) {
        notifications.addFirst(notification);
        if (addNotificationListener != null) {
            addNotificationListener.onAddNotification(notification);
        }
    }
	
	public void addMessageNotification(Notification notification) {
		notifications.add(notification);
		if (addNotificationListener != null) {
		    addNotificationListener.onAddNotification(notification);
		}
	}
	
	public void addMessageNotification(StringTemplate st) {
		System.out.println("message notification: " + st);
		this.addMessageNotification(new MessageNotification(Game.idGenerator, Messages.message(st)));
	}

	public Notification firstNotification() {
		if (notifications.isEmpty()) {
			throw new IllegalStateException("no notification, incorrect invocation");
		}
		return notifications.removeFirst();
	}

	public boolean hasNotifications() {
		return !notifications.isEmpty();
	}
	
	public void setAddNotificationListener(AddNotificationListener listener) {
	    this.addNotificationListener = listener;
	}

    public LinkedList<Notification> getNotifications() {
        return notifications;
    }
	
	public static final class Xml extends XmlNodeParser<EventsNotifications> {

		public Xml() {
			addNode(MessageNotification.class, new ObjectFromNodeSetter<EventsNotifications, MessageNotification>() {
				@Override
				public void set(EventsNotifications target, MessageNotification entity) {
					target.notifications.add(entity);
				}
				@Override
				public void generateXml(EventsNotifications source, ChildObject2XmlCustomeHandler<MessageNotification> xmlGenerator) throws IOException {
					for (Notification mn : source.notifications) {
						if (mn instanceof MessageNotification) {
							xmlGenerator.generateXml((MessageNotification)mn);
						}
					}
				}
			});
			addNode(MonarchActionNotification.class, new ObjectFromNodeSetter<EventsNotifications, MonarchActionNotification>() {
				@Override
				public void set(EventsNotifications target, MonarchActionNotification entity) {
					target.notifications.add(entity);
				}
				@Override
				public void generateXml(EventsNotifications source, ChildObject2XmlCustomeHandler<MonarchActionNotification> xmlGenerator) throws IOException {
					for (Notification mn : source.notifications) {
						if (mn instanceof MonarchActionNotification) {
							xmlGenerator.generateXml((MonarchActionNotification)mn);
						}
					}
				}
			});
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			EventsNotifications en = new EventsNotifications();
			
			nodeObject = en;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "eventsNotificatioins";
		}
	}
}
