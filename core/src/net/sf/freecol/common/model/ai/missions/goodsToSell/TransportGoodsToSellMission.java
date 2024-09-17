package net.sf.freecol.common.model.ai.missions.goodsToSell;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TransportGoodsToSellMission extends AbstractMission {
	
	public enum Phase {
		MOVE_TO_COLONY,
		MOVE_TO_EUROPE
	}
	
	private String transporterId;
	private String firstSettlementIdToVisit;
	private final Set<String> visitedSettlements = new HashSet<String>();
	private Phase phase;

	public static TransportGoodsToSellMission sellTransportedCargoInEurope(Unit transporter) {
		TransportGoodsToSellMission mission = new TransportGoodsToSellMission(Game.idGenerator.nextId(TransportGoodsToSellMission.class));
		mission.transporterId = transporter.getId();
		mission.phase = Phase.MOVE_TO_EUROPE;
		return mission;
	}

	public TransportGoodsToSellMission(Unit transporter, Settlement firstToVisit) {
		super(Game.idGenerator.nextId(TransportGoodsToSellMission.class));
		
		this.transporterId = transporter.getId();
		this.firstSettlementIdToVisit = firstToVisit.getId();
		this.phase = Phase.MOVE_TO_COLONY;
	}
	
	private TransportGoodsToSellMission(String id) {
		super(id);
	}

	public void addVisitedSettlement(Settlement settlement) {
		visitedSettlements.add(settlement.getId());
	}

	public boolean isSettlementPossibleToVisit(Settlement settlement) {
		return !visitedSettlements.contains(settlement.getId());
	}
	
	public Settlement firstSettlementToVisit(Player player) {
		if (firstSettlementIdToVisit == null) {
			return null;
		}
		Settlement settlement = player.settlements.getByIdOrNull(firstSettlementIdToVisit);
		if (settlement != null && settlement.getOwner().notEqualsId(player)) {
			return null;
		}
		return settlement;
	}
	
	public void visitSettlementAsFirst(Settlement settlement) {
		firstSettlementIdToVisit = settlement.getId();
	}
	
	public void removeFirstSettlement() {
		firstSettlementIdToVisit = null;
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(transporterId, this);
	}
	
	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(transporterId, this);
	}

	public void loadGoodsFrom(Settlement settlement, Unit transporter) {
		logger.debug(
			"player[%s].TransportGoodsToSellMissionHandler load goods in %s",
			transporter.getOwner().getId(), settlement.getId()
		);
		
		GoodsLoader goodsLoader = new GoodsLoader(
			transporter.getOwner(),
			Specification.instance.goodsTypeToScoreByPrice
		);
		goodsLoader.load(settlement, transporter);
	}
	
	public String getTransporterId() {
		return transporterId;
	}

	public Set<String> getVisitedSettlements() {
		return visitedSettlements;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public void changePhase(Phase phase) {
		this.phase = phase;
	}
	
	public void sellAllCargoInEurope(Game game, StringBuilder logStr, Unit transporter) {
		Market market = transporter.getOwner().market();
		
		// ai can sell without arrears
		market.repayAllArrears();
		
		int sumPrice = 0;
		
		for (Entry<String> cargoEntry : transporter.getGoodsContainer().entries()) {
			GoodsType cargoGoodsType = Specification.instance.goodsTypes.getById(cargoEntry.key);
			
			int price = market.sellGoods(
				game, 
				transporter.getOwner(), 
				cargoGoodsType, cargoEntry.value
			).netPrice;
			sumPrice += price;
			
			if (logStr.length() != 0) {
				logStr.append(", ");
			}
			logStr.append("[" + cargoGoodsType.getId() + ":" + cargoEntry.value + ", price: " + price + "]");
		}
		logStr.append(", sumPrice: " + sumPrice);
		transporter.getGoodsContainer().decreaseAllToZero();
	}

	@Override
	public String toString() {
		String str = "unit[" + transporterId + "] " + phase;
		if (firstSettlementIdToVisit != null) {
			str += ", next: " + firstSettlementIdToVisit;
		} else {
			str += ", next: no";
		}
		str += ", visited[" + StringUtils.join(", ", visitedSettlements) + "]";
		return str;
	}

	public static class Xml extends AbstractMission.Xml<TransportGoodsToSellMission> {

		private static final String ELEMENT_SETTLEMENT = "settlement";
		private static final String ATTR_UNIT = "unit";
		private static final String ATTR_PHASE = "phase";
		private static final String ATTR_FIRST_TO_VISIT = "firstToVisit";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			TransportGoodsToSellMission m = new TransportGoodsToSellMission(attr.getId());
			m.transporterId = attr.getStrAttribute(ATTR_UNIT);
			m.firstSettlementIdToVisit = attr.getStrAttribute(ATTR_FIRST_TO_VISIT);
			m.phase = attr.getEnumAttribute(Phase.class, ATTR_PHASE);
			nodeObject = m;
			super.startElement(attr);
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals(ELEMENT_SETTLEMENT)) {
				nodeObject.visitedSettlements.add(attr.getId());
			}
		}
		
		@Override
		public void startWriteAttr(TransportGoodsToSellMission mission, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(mission, attr);
			attr.setId(mission);
			attr.set(ATTR_UNIT, mission.transporterId);
			attr.set(ATTR_FIRST_TO_VISIT, mission.firstSettlementIdToVisit);
			attr.set(ATTR_PHASE, mission.phase);
			
			for (String settlementId : mission.visitedSettlements) {
				attr.xml.element(ELEMENT_SETTLEMENT);
                attr.set(XmlNodeParser.ATTR_ID, settlementId);
                attr.xml.pop();
			}
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "transportGoodsToSellMission";
		}
		
	}
}