package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class DemandTributeMission extends AbstractMission implements MissionFromIndianSettlement {

	public enum Phase {
		MOVE_TO_COLONY,
		BACK_TO_SETTLEMENT
	}
	
	private IndianSettlement indianSettlement;
	private Unit unitToDemandTribute;
	private Colony colony;
	private String colonyOwnerId;
	private Phase phase;
	
	private DemandTributeMission(String id) {
		super(id);
	}
	
	public DemandTributeMission(IndianSettlement indianSettlement, Unit unitToDemandTribute, Colony colony) {
		super(Game.idGenerator.nextId(DemandTributeMission.class));
		this.indianSettlement = indianSettlement;
		this.unitToDemandTribute = unitToDemandTribute;
		this.colony = colony;
		this.colonyOwnerId = colony.getOwner().getId();
		this.phase = Phase.MOVE_TO_COLONY;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unitToDemandTribute, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unitToDemandTribute, this);
	}

	@Override
	public IndianSettlement getIndianSettlement() {
		return indianSettlement;
	}

	public Colony getColony() {
		return colony;
	}

	public Unit getUnitToDemandTribute() {
		return unitToDemandTribute;
	}

	public static class Xml extends AbstractMission.Xml<DemandTributeMission> {

		private static final String ATTR_PHASE = "phase";
		private static final String ATTR_UNIT_ID = "unitId";
		private static final String ATTR_COLONY_OWNER_ID = "colonyOwnerId";
		private static final String ATTR_COLONY_ID = "colonyId";
		private static final String ATTR_INDIAN_SETTLEMENT_ID = "indianSettlementId";
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			DemandTributeMission m = new DemandTributeMission(attr.getId());
			
			m.indianSettlement = PlayerMissionsContainer.Xml.getPlayerIndianSettlement(
				attr.getStrAttributeNotNull(ATTR_INDIAN_SETTLEMENT_ID)
			);
			
			Player colonyOwner = attr.getEntity(ATTR_COLONY_OWNER_ID, XmlNodeParser.game.players);
			m.colonyOwnerId = colonyOwner.getId();
			
			Settlement colonySettlement = colonyOwner.settlements.getByIdOrNull(attr.getStrAttributeNotNull(ATTR_COLONY_ID));
			if (colonySettlement != null) {
				m.colony = colonySettlement.asColony();
			}
			
			m.unitToDemandTribute = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttributeNotNull(ATTR_UNIT_ID));
			m.phase = attr.getEnumAttribute(Phase.class, ATTR_PHASE, Phase.BACK_TO_SETTLEMENT);
			
			nodeObject = m;
		}

		@Override
		public void startWriteAttr(DemandTributeMission m, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(m);
			attr.set(ATTR_INDIAN_SETTLEMENT_ID, m.indianSettlement);
			attr.set(ATTR_COLONY_ID, m.colony);
			attr.set(ATTR_COLONY_OWNER_ID, m.colonyOwnerId);
			attr.set(ATTR_UNIT_ID, m.unitToDemandTribute);
			attr.set(ATTR_PHASE, m.phase);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "demandTributeMission";
		}
		
	}
}
