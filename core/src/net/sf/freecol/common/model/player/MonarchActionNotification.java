package net.sf.freecol.common.model.player;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
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
					throw new RuntimeException("not implemented");
				}
			});
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			
			MonarchActionNotification man = new MonarchActionNotification(
				attr.getStrAttribute("id"),
				attr.getEnumAttribute(MonarchAction.class, "action")
			);
			man.setColonyId(attr.getStrAttribute("colonyId"));
			
			String goodsTypeId = attr.getStrAttribute("goodsType");
			if (goodsTypeId != null) {
				GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsTypeId);
				man.setGoodsType(goodsType);
			}
			
			man.setGoodsAmount(attr.getIntAttribute("goodsAmount", 0));
			man.setTax(attr.getIntAttribute("tax", 0));
			man.setMsgBody(attr.getStrAttribute("msgBody"));
			man.price = attr.getIntAttribute("price", 0);
			
			nodeObject = man;
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
