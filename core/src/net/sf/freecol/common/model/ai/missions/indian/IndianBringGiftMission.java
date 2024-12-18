package net.sf.freecol.common.model.ai.missions.indian;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.AbstractGoods;

import java.io.IOException;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class IndianBringGiftMission extends AbstractMission implements MissionFromIndianSettlement {

	public enum Phase {
		TAKE_GOODS,
		MOVE_TO_COLONY,
		BACK_TO_SETTLEMENT
	}
	
	private String indianSettlementId;
	private String destinationColonyId;
	private String destinationColonyOwnerId;
	private String transportUnitId;
	private AbstractGoods gift;
	private Phase phase;
	
	private IndianBringGiftMission(String id) {
		super(id);
	}
	
	public IndianBringGiftMission(
		IndianSettlement indianSettlement, Colony colony, 
		Unit transportUnit, AbstractGoods gift
	) {
		super(Game.idGenerator.nextId(IndianBringGiftMission.class));
		this.indianSettlementId = indianSettlement.getId();
		
		this.destinationColonyId = colony.getId();
		this.destinationColonyOwnerId = colony.getOwner().getId();
		this.transportUnitId = transportUnit.getId();
		this.gift = gift;
		this.phase = Phase.TAKE_GOODS;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(transportUnitId, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(transportUnitId, this);
	}

	public boolean canExecuteMission(Player player) {
		if (player.isDead() || !player.settlements.containsId(indianSettlementId) || destinationColonyOwnerId == null) {
			return false;
		}
		return true;
	}

	public boolean isColonyOwnerChanged(Game game) {
		Player owner = game.players.getById(destinationColonyOwnerId);
		return owner == null || owner.isDead() || !owner.settlements.containsId(destinationColonyId);
	}
	
	public boolean correctTransportUnitLocation(Unit unit, IndianSettlement indianSettlement) {
		if (unit.getTileLocationOrNull() == null) {
			if (unit.isAtLocation(IndianSettlement.class)) {
				unit.changeUnitLocation(indianSettlement.tile);
			} else {
				return false;
			}
		}
		return true;
	}
	
	public void backUnitToSettlement(Unit unit, IndianSettlement indianSettlement) {
		if (gift.getQuantity() > 0) {
			indianSettlement.getGoodsContainer().increaseGoodsQuantity(gift);
		}
		unit.changeUnitLocation(indianSettlement);
	}
	
	public AbstractGoods transferGoodsToColony(Unit unit, Colony destinationColony) {
		AbstractGoods transferedGoods = gift.clone();
		unit.reduceMovesLeftToZero();
		destinationColony.getGoodsContainer().increaseGoodsQuantity(gift);
		gift.setQuantity(0);
		changePhase(Phase.BACK_TO_SETTLEMENT);
		return transferedGoods;
	}
	
	@Override
	public String getIndianSettlementId() {
		return indianSettlementId;
	}

	public String getTransportUnitId() {
		return transportUnitId;
	}

	public Colony getDestinationColony(Game game) {
		Player colonyOwner = game.players.getById(destinationColonyOwnerId);
		return colonyOwner.settlements.getById(destinationColonyId).asColony();
	}

	public AbstractGoods getGift() {
		return gift;
	}
	
	public void changePhase(Phase newPhase) {
		this.phase = newPhase;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public static class Xml extends AbstractMission.Xml<IndianBringGiftMission> {

		private static final String ATTR_PHASE = "phase";
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
			m.destinationColonyOwnerId = attr.getStrAttributeNotNull(ATTR_COLONY_OWNER_ID);
			m.destinationColonyId = attr.getStrAttributeNotNull(ATTR_COLONY_ID);
			m.transportUnitId = attr.getStrAttributeNotNull(ATTR_TRANSPORT_UNIT_ID);
			m.gift = new AbstractGoods(
				attr.getStrAttributeNotNull(ATTR_GIFT_GOODS_TYPE_ID), 
				attr.getIntAttribute(ATTR_GIFT_AMOUNT)
			);
			
			m.phase = attr.getEnumAttribute(Phase.class, ATTR_PHASE, Phase.BACK_TO_SETTLEMENT);
			nodeObject = m;
			super.startElement(attr);
		}

		@Override
		public void startWriteAttr(IndianBringGiftMission m, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(m, attr);
			attr.setId(m);
			attr.set(ATTR_INDIAN_SETTLEMENT_ID, m.indianSettlementId);
			attr.set(ATTR_COLONY_ID, m.destinationColonyId);
			attr.set(ATTR_COLONY_OWNER_ID, m.destinationColonyOwnerId);
			attr.set(ATTR_TRANSPORT_UNIT_ID, m.transportUnitId);
			attr.set(ATTR_GIFT_GOODS_TYPE_ID, m.gift.getTypeId());
			attr.set(ATTR_GIFT_AMOUNT, m.gift.getQuantity());
			attr.set(ATTR_PHASE, m.phase);
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
