package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class MonarchActionNotification implements Notification, Identifiable {
	
	private String id;
	private MonarchAction action;
	private GoodsType goodsType;
	private int goodsAmount;
	private String colonyId;
	private int tax;
	
	public MonarchActionNotification(MonarchAction action) {
		this.action = action;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	
	public String toString() {
		return  "id = " + getId() + 
				", action = " + action + 
				", goodsType = " + goodsType.getId() + 
				", action = " + action + 
				", tax = " + tax;
	}
	
	public static class Xml extends XmlNodeParser {
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(attr.getStrAttribute("goodsType"));
			
			MonarchActionNotification man = new MonarchActionNotification(attr.getEnumAttribute(MonarchAction.class, "action"));
			man.setId(attr.getStrAttribute("id"));
			man.setColonyId(attr.getStrAttribute("colonyId"));
			man.setGoodsType(goodsType);
			man.setGoodsAmount(attr.getIntAttribute("goodsAmount"));
			man.setTax(attr.getIntAttribute("tax"));
			
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
