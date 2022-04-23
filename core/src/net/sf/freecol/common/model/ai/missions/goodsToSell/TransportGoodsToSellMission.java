package net.sf.freecol.common.model.ai.missions.goodsToSell;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.util.StringUtils;

import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TransportGoodsToSellMission extends AbstractMission {
	
	public enum Phase {
		MOVE_TO_COLONY,
		MOVE_TO_EUROPE
	}
	
	private Unit transporter;
	private String firstSettlementIdToVisit;
	private final Set<String> possibleSettlementToVisit = new HashSet<String>();
	private Phase phase;

	public TransportGoodsToSellMission(Unit transporter, Settlement firstToVisit, Set<String> possibleSettlementToVisit) {
		super(Game.idGenerator.nextId(TransportGoodsToSellMission.class));
		
		this.transporter = transporter;
		this.firstSettlementIdToVisit = firstToVisit.getId();
		this.possibleSettlementToVisit.addAll(possibleSettlementToVisit);
		this.phase = Phase.MOVE_TO_COLONY;
	}
	
	private TransportGoodsToSellMission(String id) {
		super(id);
	}

	public boolean isTransportUnitExists(Player player) {
		return CommonMissionHandler.isUnitExists(player, transporter);
	}
	
	public boolean isSettlementPossibleToVisit(Settlement settlement) {
		return possibleSettlementToVisit.contains(settlement.getId());
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
		possibleSettlementToVisit.remove(settlement.getId());
	}
	
	public void removeFirstSettlement() {
		if (firstSettlementIdToVisit != null) {
			possibleSettlementToVisit.remove(firstSettlementIdToVisit);
		}
		firstSettlementIdToVisit = null;
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(transporter, this);
	}
	
	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(transporter, this);
	}

	public boolean isTransporterOnSettlement(Settlement settlement) {
		return transporter.getTile().equalsCoordinates(settlement.tile);
	}

	public void loadGoodsFrom(Settlement settlement) {
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
	
	public boolean hasTransporterCargo() {
		return transporter.hasGoodsCargo();
	}
	
	public Unit getTransporter() {
		return transporter;
	}

	public Set<String> getPossibleSettlementToVisit() {
		return possibleSettlementToVisit;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public void changePhase(Phase phase) {
		this.phase = phase;
	}
	
	public void sellAllCargoInEurope(Game game, StringBuilder logStr) {
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
		String str = "unit[" + transporter.getId() + " " + transporter.unitType.toSmallIdStr() +  "] " + phase;
		if (firstSettlementIdToVisit != null) {
			str += ", next: " + firstSettlementIdToVisit;
		} else {
			str += ", next: no";
		}
		str += ", to visit[" + StringUtils.join(", ", possibleSettlementToVisit) + "]";
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
			m.transporter = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT));
			m.firstSettlementIdToVisit = attr.getStrAttribute(ATTR_FIRST_TO_VISIT); 
			m.phase = attr.getEnumAttribute(Phase.class, ATTR_PHASE);
			nodeObject = m;
			super.startElement(attr);
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals(ELEMENT_SETTLEMENT)) {
				nodeObject.possibleSettlementToVisit.add(attr.getId());
			}
		}
		
		@Override
		public void startWriteAttr(TransportGoodsToSellMission mission, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(mission, attr);
			attr.setId(mission);
			attr.set(ATTR_UNIT, mission.transporter);
			attr.set(ATTR_FIRST_TO_VISIT, mission.firstSettlementIdToVisit);
			attr.set(ATTR_PHASE, mission.phase);
			
			for (String settlementId : mission.possibleSettlementToVisit) {
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