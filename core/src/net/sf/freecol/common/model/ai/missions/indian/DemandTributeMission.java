package net.sf.freecol.common.model.ai.missions.indian;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.player.Tension.Level;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.GoodsType;

import java.io.IOException;

import promitech.colonization.ai.MissionHandlerLogger;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class DemandTributeMission extends AbstractMission implements MissionFromIndianSettlement {

	private static final int CARGO_SIZE = 100;
	
	public enum Phase {
		MOVE_TO_COLONY,
		BACK_TO_SETTLEMENT, 
		ATTACK
	}

	private String indianSettlementId;
	private String unitToDemandTributeId;
	private String colonyId;
	private String colonyOwnerId;
	private Phase phase;
	
	private DemandTributeMission(String id) {
		super(id);
	}
	
	public DemandTributeMission(IndianSettlement indianSettlement, Unit unitToDemandTribute, Colony colony) {
		super(Game.idGenerator.nextId(DemandTributeMission.class));
		this.indianSettlementId = indianSettlement.getId();
		this.unitToDemandTributeId = unitToDemandTribute.getId();
		this.colonyId = colony.getId();
		this.colonyOwnerId = colony.getOwner().getId();
		this.phase = Phase.MOVE_TO_COLONY;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unitToDemandTributeId, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unitToDemandTributeId, this);
	}

	@Override
	public String getIndianSettlementId() {
		return indianSettlementId;
	}

	public Colony getDestinationColony(Game game) {
		Player colonyOwner = game.players.getById(colonyOwnerId);
		return colonyOwner.settlements.getById(colonyId).asColony();
	}

	public boolean canExecuteMission(Player indianPlayer) {
		if (indianPlayer.isDead()) {
			return false;
		}
		if (colonyOwnerId == null || !indianPlayer.settlements.containsId(indianSettlementId)) {
			return false;
		}
		return true;
	}

	public boolean isColonyOwnerChanged(Game game) {
		Player owner = game.players.getByIdOrNull(colonyOwnerId);
		return owner == null || owner.isDead() || !owner.settlements.containsId(colonyId);
	}
	
	public void exitFromSettlementWhenOnIt(Unit unit, IndianSettlement indianSettlement) {
		if (unit.getTileLocationOrNull() == null) {
			if (unit.isAtLocation(IndianSettlement.class)) {
				unit.changeUnitLocation(indianSettlement.tile);
			}
		}
	}
	
	public Goods selectGoods(Colony colony, Unit unit) {
		int demandModifier = Specification.options.getIntValue(GameOptions.NATIVE_DEMANDS) + 1;
		Level tensionLevel = unit.getOwner().getTension(colony.getOwner()).getLevel();
		
		GoodsType food = Specification.instance.goodsTypes.getById(GoodsType.FOOD);
		Goods goods = null;
		
		if (Level.CONTENT.isWorstOrEquals(tensionLevel) 
			&& colony.getGoodsContainer().hasGoodsQuantity(food, CARGO_SIZE)
		) {
			goods = new Goods(food, capacityAmount(
				colony.getGoodsContainer().goodsAmount(food), 
				demandModifier
			));
		} else if (Level.DISPLEASED.isWorstOrEquals(tensionLevel)) {
			goods = demandMostValuableGoods(colony, demandModifier);
		} else {
			goods = selectGoodsForAngryTension(colony, demandModifier);
		}
		if (goods == null) {
			goods = demandMostValuableGoods(colony, demandModifier);
		}
		return goods;
	}

	public int selectGold(Colony colony) {
		int gold = colony.getOwner().getGold() / 20;
		if (gold == 0) {
			gold = colony.getOwner().getGold();
		}
		return gold;
	}
	
	private Goods selectGoodsForAngryTension(Colony colony, int demandModifier) {
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isMilitary()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, capacityAmount(amount, demandModifier));
				}
			}
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isRawBuildingMaterial() && goodsType.isStorable()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, capacityAmount(amount, demandModifier));
				}
			}
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isTradeGoods()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, capacityAmount(amount, demandModifier));
				}
			}
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isRefined() && goodsType.isStorable()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, capacityAmount(amount, demandModifier));
				}
			}
		}
		return null;
	}

	private Goods demandMostValuableGoods(Colony colony, int demandModifier) {
		int maxGoodsValue = -1;
		GoodsType maxGoodsType = null;
		int maxGoodsAmount = 0;
		
		for (Entry<String> goodsEntry : colony.getGoodsContainer().entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsEntry.key);
			if (!goodsType.isStorable() || goodsType.isFood() || goodsType.isMilitary()) {
				continue;
			}
			int goodsValue = colony.getOwner().market().getSalePrice(goodsType, goodsEntry.value);
			if (goodsValue > maxGoodsValue) {
				maxGoodsValue = goodsValue;
				maxGoodsType = goodsType;
				maxGoodsAmount = goodsEntry.value;
			}
		}
		if (maxGoodsType != null) {
			return new Goods(maxGoodsType, capacityAmount(maxGoodsAmount, demandModifier));
		}
		return null;
	}
	
    private int capacityAmount(int amount, int difficulty) {
		return Math.min(
			Math.min(Math.max(amount * difficulty / 6, 30), CARGO_SIZE),
			amount
		);
    }
	
	public void acceptDemands(Colony colony, Goods goods, int goldAmount, Unit unit, IndianSettlement indianSettlement) {
		if (goods != null) {
			colony.getGoodsContainer().transferGoods(
				goods, 
				indianSettlement.getGoodsContainer()
			);
		} else {
			colony.getOwner().transferGoldToPlayer(
				goldAmount, indianSettlement.getOwner()
			);
		}
		int difficulty = Specification.options.getIntValue(GameOptions.NATIVE_DEMANDS);
		int tension = -(5 - difficulty) * 50;
		indianSettlement.modifyTensionWithOwnerTension(colony.getOwner(), tension);

		unit.reduceMovesLeftToZero();
		changePhase(Phase.BACK_TO_SETTLEMENT);
		
		acceptDemandLogMsg(goods, goldAmount, unit);
	}
    
	private void acceptDemandLogMsg(final Goods goods, final int goldAmount, Unit unit) {
		if (MissionHandlerLogger.logger.isDebug()) {
			MissionHandlerLogger.logger.debug(
				"player[%s].DemandTributeMission[%s] accept demand from player[%s] %s:%d", 
				unit.getOwner().getId(),
				getId(),
				colonyOwnerId,
				goods != null ? goods.getType() : "gold",
				goods != null ? goods.getAmount() : goldAmount
			);
		}
	}
	
	public void rejectDemands(Colony colony, Goods goods, int goldAmount, Unit unit, IndianSettlement indianSettlement) {
		Tension tension = Tension.worst(
			indianSettlement.getTension(colony.getOwner()), 
			indianSettlement.getOwner().getTension(colony.getOwner())
		);
		if (tension.isWorst(Level.CONTENT)) {
			phase = Phase.ATTACK;
		} else {
			phase = Phase.BACK_TO_SETTLEMENT;
		}
		rejectDemandsLogMsg(goods, goldAmount, unit);
	}
    
	private void rejectDemandsLogMsg(final Goods goods, final int goldAmount, Unit unit) {
		if (MissionHandlerLogger.logger.isDebug()) {
			MissionHandlerLogger.logger.debug(
				"player[%s].DemandTributeMission[%s] player[%s] reject demand %s:%d",
				unit.getOwner().getId(),
				getId(),
				colonyOwnerId,
				goods != null ? goods.getType() : "gold",
				goods != null ? goods.getAmount() : goldAmount
			);
		}
	}
	
	public String getUnitToDemandTributeId() {
		return unitToDemandTributeId;
	}

	public void changePhase(Phase newPhase) {
		this.phase = newPhase;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public void backToSettlementAfterSuccessfulAttack(Unit unit) {
    	if (!unit.isDisposed()) {
    		unit.reduceMovesLeftToZero();
    		changePhase(Phase.BACK_TO_SETTLEMENT);
    	} else {
    		// end mission
    		setDone();
    	}
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
			
			m.indianSettlementId = attr.getStrAttributeNotNull(ATTR_INDIAN_SETTLEMENT_ID);
			m.colonyOwnerId = attr.getStrAttributeNotNull(ATTR_COLONY_OWNER_ID);
			m.colonyId = attr.getStrAttributeNotNull(ATTR_COLONY_ID);
			m.unitToDemandTributeId = attr.getStrAttributeNotNull(ATTR_UNIT_ID);
			m.phase = attr.getEnumAttribute(Phase.class, ATTR_PHASE, Phase.BACK_TO_SETTLEMENT);
			
			nodeObject = m;
			super.startElement(attr);
		}

		@Override
		public void startWriteAttr(DemandTributeMission m, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(m, attr);
			attr.setId(m);
			attr.set(ATTR_INDIAN_SETTLEMENT_ID, m.indianSettlementId);
			attr.set(ATTR_COLONY_ID, m.colonyId);
			attr.set(ATTR_COLONY_OWNER_ID, m.colonyOwnerId);
			attr.set(ATTR_UNIT_ID, m.unitToDemandTributeId);
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
