package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class IndianBringGiftMission extends AbstractMission {

	private String indianSettlementId;
	private String destinationColonyId;
	private String destinationColonyOwnerId;
	private String transportUnitId;
	private AbstractGoods gift;
	
	private IndianBringGiftMission(String id) {
		super(id);
	}
	
	public IndianBringGiftMission(
		Settlement indianSettlement, Colony colony, 
		Unit transportUnit, AbstractGoods gift
	) {
		super(Game.idGenerator.nextId(IndianBringGiftMission.class));
		this.indianSettlementId = indianSettlement.getId();
		
		this.destinationColonyId = colony.getId();
		this.destinationColonyOwnerId = colony.getOwner().getId();
		this.transportUnitId = transportUnit.getId();
		this.gift = gift;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(transportUnitId, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(transportUnitId, this);
	}

	public String getIndianSettlementId() {
		return indianSettlementId;
	}

	public static class Xml extends AbstractMission.Xml<IndianBringGiftMission> {

		private static final String ATTR_GIFT_AMOUNT = "giftAmount";
		private static final String ATTR_GIFT_GOODS_TYPE_ID = "giftGoodsTypeId";
		private static final String ATTR_TRANSPORT_UNIT_ID = "transportUnitId";
		private static final String ATTR_COLONY_OWNER_ID = "colonyOwnerId";
		private static final String ATTR_COLONY_ID = "colonyId";
		private static final String ATTR_INDIAN_SETTLEMENT_ID = "indianSettlementId";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			IndianBringGiftMission m = new IndianBringGiftMission(attr.getId());
			m.indianSettlementId = attr.getStrAttributeNotNull(ATTR_INDIAN_SETTLEMENT_ID);
			m.destinationColonyId = attr.getStrAttributeNotNull(ATTR_COLONY_ID);
			m.destinationColonyOwnerId = attr.getStrAttributeNotNull(ATTR_COLONY_OWNER_ID);
			m.transportUnitId = attr.getStrAttributeNotNull(ATTR_TRANSPORT_UNIT_ID);
			m.gift = new AbstractGoods(
				attr.getStrAttributeNotNull(ATTR_GIFT_GOODS_TYPE_ID), 
				attr.getIntAttribute(ATTR_GIFT_AMOUNT)
			);
			nodeObject = m;
		}

		@Override
		public void startWriteAttr(IndianBringGiftMission m, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(m);
			attr.set(ATTR_INDIAN_SETTLEMENT_ID, m.indianSettlementId);
			attr.set(ATTR_COLONY_ID, m.destinationColonyId);
			attr.set(ATTR_COLONY_OWNER_ID, m.destinationColonyOwnerId);
			attr.set(ATTR_TRANSPORT_UNIT_ID, m.transportUnitId);
			attr.set(ATTR_GIFT_GOODS_TYPE_ID, m.gift.getTypeId());
			attr.set(ATTR_GIFT_AMOUNT, m.gift.getQuantity());
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
			return "indianBringGiftMission";
		}
	}
	
}
