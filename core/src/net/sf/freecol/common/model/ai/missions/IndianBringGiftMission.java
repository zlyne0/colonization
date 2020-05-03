package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianBringGiftMission extends AbstractMission implements MissionFromIndianSettlement {

	public enum Phase {
		TAKE_GOODS,
		MOVE_TO_COLONY,
		BACK_TO_SETTLEMENT
	}
	
	private IndianSettlement indianSettlement;
	private Colony destinationColony;
	private String destinationColonyOwnerId;
	private Unit transportUnit;
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
		this.indianSettlement = indianSettlement;
		
		this.destinationColony = colony;
		this.destinationColonyOwnerId = colony.getOwner().getId();
		this.transportUnit = transportUnit;
		this.gift = gift;
		this.phase = Phase.TAKE_GOODS;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(transportUnit, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(transportUnit, this);
	}

	public boolean canExecuteMission(Player player) {
		if (player.isDead()) {
			return false;
		}
		if (indianSettlement == null || !player.settlements.containsId(indianSettlement)) {
			return false;
		}
		if (transportUnit == null || transportUnit.isDisposed() || !player.units.containsId(transportUnit)) {
			return false;
		}
		if (destinationColonyOwnerId == null) {
			return false;
		}
		return true;
	}

	public boolean isColonyOwnerChanged(Game game) {
		Player owner = game.players.getById(destinationColonyOwnerId);
		if (owner.isDead()) {
			return true;
		}
		if (destinationColony == null || !destinationColony.getOwner().equalsId(owner)
				|| !owner.settlements.containsId(destinationColony)) {
			return true;
		}
		return false;
	}
	
	public boolean correctTransportUnitLocation() {
		if (transportUnit.getTileLocationOrNull() == null) {
			if (transportUnit.isAtLocation(IndianSettlement.class)) {
				transportUnit.changeUnitLocation(indianSettlement.tile);
			} else {
				return false;
			}
		}
		return true;
	}
	
	public void backUnitToSettlement() {
		transportUnit.changeUnitLocation(indianSettlement);
	}
	
	public void transferGoods() {
		transportUnit.reduceMovesLeftToZero();
		destinationColony.getGoodsContainer().increaseGoodsQuantity(gift);
		changePhase(Phase.BACK_TO_SETTLEMENT);
	}
	
	@Override
	public IndianSettlement getIndianSettlement() {
		return indianSettlement;
	}

	public Colony getDestinationColony() {
		return destinationColony;
	}

	public Unit getTransportUnit() {
		return transportUnit;
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
			
			m.indianSettlement = PlayerMissionsContainer.Xml.getPlayerIndianSettlement(
				attr.getStrAttributeNotNull(ATTR_INDIAN_SETTLEMENT_ID)
			);
			
			Player colonyOwner = attr.getEntity(ATTR_COLONY_OWNER_ID, XmlNodeParser.game.players);
			m.destinationColonyOwnerId = colonyOwner.getId();
			
			Settlement colonySettlement = colonyOwner.settlements.getByIdOrNull(attr.getStrAttributeNotNull(ATTR_COLONY_ID));
			if (colonySettlement != null) {
				m.destinationColony = colonySettlement.asColony();
			}
			
			m.transportUnit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttributeNotNull(ATTR_TRANSPORT_UNIT_ID));
			m.gift = new AbstractGoods(
				attr.getStrAttributeNotNull(ATTR_GIFT_GOODS_TYPE_ID), 
				attr.getIntAttribute(ATTR_GIFT_AMOUNT)
			);
			
			m.phase = attr.getEnumAttribute(Phase.class, ATTR_PHASE, Phase.BACK_TO_SETTLEMENT);
			nodeObject = m;
		}

		@Override
		public void startWriteAttr(IndianBringGiftMission m, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(m);
			attr.set(ATTR_INDIAN_SETTLEMENT_ID, m.indianSettlement);
			attr.set(ATTR_COLONY_ID, m.destinationColony);
			attr.set(ATTR_COLONY_OWNER_ID, m.destinationColonyOwnerId);
			attr.set(ATTR_TRANSPORT_UNIT_ID, m.transportUnit);
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
