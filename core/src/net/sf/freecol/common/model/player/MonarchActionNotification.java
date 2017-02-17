package net.sf.freecol.common.model.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class MonarchActionNotification implements Notification, Identifiable {
	
	private final String id;
	private MonarchAction action;
	private GoodsType goodsType;
	private int goodsAmount;
	private String colonyId;
	private int tax;
	private String msgBody;
	private int price;
	private List<ArmyForceAbstractUnit> mercenaries;
	
	private MonarchActionNotification(String id, MonarchAction anAction) {
		this.id = id;
		this.action = anAction;
	}

	public MonarchActionNotification(MonarchAction anAction) {
		this(Game.idGenerator.nextId(MonarchActionNotification.class), anAction);
	}
	
	public MonarchActionNotification(MonarchAction anAction, List<ArmyForceAbstractUnit> mercenaries, int price) {
		this(anAction);
		this.price = price;
		this.mercenaries = mercenaries;
	}

	@Override
	public String getId() {
		return id;
	}

	public MonarchAction getAction() {
		return action;
	}

	public int getTax() {
		return tax;
	}

	public GoodsType getGoodsType() {
		return goodsType;
	}

	public int getGoodsAmount() {
		return goodsAmount;
	}

	public String getColonyId() {
		return colonyId;
	}

	public void setGoodsAmount(int goodsAmount) {
		this.goodsAmount = goodsAmount;
	}

	public void setGoodsType(GoodsType goodsType) {
		this.goodsType = goodsType;
	}

	public void setColonyId(String colonyId) {
		this.colonyId = colonyId;
	}

	public void setTax(int tax) {
		this.tax = tax;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}
	
	public int getPrice() {
		return price;
	}
	
	public List<ArmyForceAbstractUnit> getMercenaries() {
		return mercenaries;
	}
	
	public String toString() {
		return  "id = " + getId() + 
				", action = " + action + 
				", goodsType = " + goodsType.getId() + 
				", action = " + action + 
				", tax = " + tax;
	}
	
	public static class Xml extends XmlNodeParser<MonarchActionNotification> {
		
		private static final String ATTR_PRICE = "price";
		private static final String ATTR_MSG_BODY = "msgBody";
		private static final String ATTR_TAX = "tax";
		private static final String ATTR_GOODS_AMOUNT = "goodsAmount";
		private static final String ATTR_GOODS_TYPE = "goodsType";
		private static final String ATTR_COLONY_ID = "colonyId";
		private static final String ATTR_ACTION = "action";

		public Xml() {
			addNode(ArmyForceAbstractUnit.class, new ObjectFromNodeSetter<MonarchActionNotification, ArmyForceAbstractUnit>() {
				@Override
				public void set(MonarchActionNotification target, ArmyForceAbstractUnit entity) {
					if (target.mercenaries == null) {
						target.mercenaries = new ArrayList<ArmyForceAbstractUnit>();
					}
					target.mercenaries.add(entity);
				}
				@Override
				public List<ArmyForceAbstractUnit> get(MonarchActionNotification source) {
					if (source.mercenaries == null) {
						return Collections.emptyList();
					}
					return source.mercenaries;
				}
			});
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			
			MonarchActionNotification man = new MonarchActionNotification(
				attr.getStrAttribute(ATTR_ID),
				attr.getEnumAttribute(MonarchAction.class, ATTR_ACTION)
			);
			man.setColonyId(attr.getStrAttribute(ATTR_COLONY_ID));
			
			String goodsTypeId = attr.getStrAttribute(ATTR_GOODS_TYPE);
			if (goodsTypeId != null) {
				GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsTypeId);
				man.setGoodsType(goodsType);
			}
			
			man.setGoodsAmount(attr.getIntAttribute(ATTR_GOODS_AMOUNT, 0));
			man.setTax(attr.getIntAttribute(ATTR_TAX, 0));
			man.setMsgBody(attr.getStrAttribute(ATTR_MSG_BODY));
			man.price = attr.getIntAttribute(ATTR_PRICE, 0);
			
			nodeObject = man;
		}

		@Override
		public void startWriteAttr(MonarchActionNotification man, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(man);
			
			attr.set(ATTR_ACTION, man.action);
			attr.set(ATTR_COLONY_ID, man.colonyId);
			attr.set(ATTR_GOODS_TYPE, man.goodsType);
			attr.set(ATTR_GOODS_AMOUNT, man.goodsAmount);
			attr.set(ATTR_TAX, man.tax);
			attr.set(ATTR_MSG_BODY, man.msgBody);
			attr.set(ATTR_PRICE, man.price);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "monarchActionNotification";
		}
		
	}

}
