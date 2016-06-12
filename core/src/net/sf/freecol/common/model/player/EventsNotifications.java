package net.sf.freecol.common.model.player;

import java.util.LinkedList;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class EventsNotifications implements Identifiable {

	public final LinkedList<Notification> notifications = new LinkedList<Notification>();
	
	@Override
	public String getId() {
		throw new IllegalStateException("there is no id");
	}

	public void addPriceChangeNotification(GoodsType goodsType, int beforePrice, int afterPrice) {
		GoodsPriceChangeNotification n = new GoodsPriceChangeNotification();
		n.type = goodsType;
		n.from = beforePrice;
		n.to = afterPrice;
		notifications.add(n);
	}

	public void addMessageNotification(StringTemplate st) {
		System.out.println("message notification: " + st);
		String nextId = Game.idGenerator.nextId(MessageNotification.class);
		notifications.add(new MessageNotification(nextId, Messages.message(st)));
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
	
	public static final class Xml extends XmlNodeParser {

		public Xml() {
			addNode(GoodsPriceChangeNotification.class, new ObjectFromNodeSetter<EventsNotifications, GoodsPriceChangeNotification>() {
				@Override
				public void set(EventsNotifications target, GoodsPriceChangeNotification entity) {
					target.notifications.add(entity);
				}
			});
			addNode(MessageNotification.class, new ObjectFromNodeSetter<EventsNotifications, MessageNotification>() {
				@Override
				public void set(EventsNotifications target, MessageNotification entity) {
					target.notifications.add(entity);
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
