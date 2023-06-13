package net.sf.freecol.common.model.ai.missions.indian;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
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
import promitech.colonization.savegame.XmlNodeParser;

public class DemandTributeMission extends AbstractMission implements MissionFromIndianSettlement {

	private static final int CARGO_SIZE = 100;
	
	public enum Phase {
		MOVE_TO_COLONY,
		BACK_TO_SETTLEMENT, 
		ATTACK
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

	public boolean canExecuteMission(Player indianPlayer) {
		if (indianPlayer.isDead()) {
			return false;
		}
		if (indianSettlement == null || !indianPlayer.settlements.containsId(indianSettlement)) {
			return false;
		}
		if (unitToDemandTribute == null 
				|| unitToDemandTribute.isDisposed() 
				|| !indianPlayer.units.containsId(unitToDemandTribute)
		) {
			return false;
		}
		if (colonyOwnerId == null) {
			return false;
		}
		return true;
	}

	public boolean isColonyOwnerChanged(Game game) {
		Player owner = game.players.getById(colonyOwnerId);
		if (owner.isDead()) {
			return true;
		}
		if (colony == null || !colony.getOwner().equalsId(owner)
				|| !owner.settlements.containsId(colony)) {
			return true;
		}
		return false;
	}
	
	public void exitFromSettlementWhenOnIt() {
		if (unitToDemandTribute.getTileLocationOrNull() == null) {
			if (unitToDemandTribute.isAtLocation(IndianSettlement.class)) {
				unitToDemandTribute.changeUnitLocation(indianSettlement.tile);
			}
		}
	}
	
	public Goods selectGoods() {
		int demandModifier = Specification.options.getIntValue(GameOptions.NATIVE_DEMANDS) + 1;
		Level tensionLevel = unitToDemandTribute.getOwner().getTension(colony.getOwner()).getLevel();
		
		GoodsType food = Specification.instance.goodsTypes.getById(GoodsType.FOOD);
		Goods goods = null;
		
		if (Level.CONTENT.isWorstOrEquals(tensionLevel) 
			&& colony.getGoodsContainer().hasGoodsQuantity(food, CARGO_SIZE)
		) {
			goods = new Goods(food, cappacityAmount(
				colony.getGoodsContainer().goodsAmount(food), 
				demandModifier
			));
		} else if (Level.DISPLEASED.isWorstOrEquals(tensionLevel)) {
			goods = demandMostValuableGoods(demandModifier);
		} else {
			goods = selectGoodsForAngryTension(demandModifier);
		}
		if (goods == null) {
			goods = demandMostValuableGoods(demandModifier);
		}
		return goods;
	}

	public int selectGold() {
		int gold = colony.getOwner().getGold() / 20;
		if (gold == 0) {
			gold = colony.getOwner().getGold();
		}
		return gold;
	}
	
	private Goods selectGoodsForAngryTension(int demandModifier) {
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isMilitary()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, cappacityAmount(amount, demandModifier));
				}
			}
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isRawBuildingMaterial() && goodsType.isStorable()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, cappacityAmount(amount, demandModifier));
				}
			}
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isTradeGoods()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, cappacityAmount(amount, demandModifier));
				}
			}
		}
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isRefined() && goodsType.isStorable()) {
				int amount = colony.getGoodsContainer().goodsAmount(goodsType);
				if (amount > 0) {
					return new Goods(goodsType, cappacityAmount(amount, demandModifier));
				}
			}
		}
		return null;
	}

	private Goods demandMostValuableGoods(int demandModifier) {
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
			return new Goods(maxGoodsType, cappacityAmount(maxGoodsAmount, demandModifier));
		}
		return null;
	}
	
    private int cappacityAmount(int amount, int difficulty) {
        return Math.min(Math.max(amount * difficulty / 6, 30), CARGO_SIZE);
    }
	
	public void acceptDemands(Goods goods, int goldAmount) {
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
		
		unitToDemandTribute.reduceMovesLeftToZero();
		changePhase(Phase.BACK_TO_SETTLEMENT);
		
		acceptDemandLogMsg(goods, goldAmount);
	}
    
	private void acceptDemandLogMsg(final Goods goods, final int goldAmount) {
		if (MissionHandlerLogger.logger.isDebug()) {
			MissionHandlerLogger.logger.debug(
				"player[%s].DemandTributeMission[%s] accept demand from player[%s] %s:%d", 
				unitToDemandTribute.getOwner().getId(),
				getId(),
				colony.getOwner().getId(),
				goods != null ? goods.getType() : "gold",
				goods != null ? goods.getAmount() : goldAmount
			);
		}
	}
	
	public void rejectDemands(final Goods goods, final int goldAmount) {
		Tension tension = Tension.worst(
			indianSettlement.getTension(colony.getOwner()), 
			indianSettlement.getOwner().getTension(colony.getOwner())
		);
		if (tension.isWorst(Level.CONTENT)) {
			phase = Phase.ATTACK;
		}
		rejectDemandsLogMsg(goods, goldAmount);
	}
    
	private void rejectDemandsLogMsg(final Goods goods, final int goldAmount) {
		if (MissionHandlerLogger.logger.isDebug()) {
			MissionHandlerLogger.logger.debug(
				"player[%s].DemandTributeMission[%s] player[%s] reject demand %s:%d", 
				unitToDemandTribute.getOwner().getId(),
				getId(),
				colony.getOwner().getId(),
				goods != null ? goods.getType() : "gold",
				goods != null ? goods.getAmount() : goldAmount
			);
		}
	}
	
	public Colony getColony() {
		return colony;
	}

	public Unit getUnitToDemandTribute() {
		return unitToDemandTribute;
	}

	public void changePhase(Phase newPhase) {
		this.phase = newPhase;
	}

	public Phase getPhase() {
		return phase;
	}
	
	public void backToSettlementAfterSuccessfulAtack() {
    	if (!unitToDemandTribute.isDisposed()) {
    		unitToDemandTribute.reduceMovesLeftToZero();
    		changePhase(Phase.BACK_TO_SETTLEMENT);
    	} else {
    		// end mission
    		setDone();
    	}
	}
	
	public void backUnitToSettlement() {
		unitToDemandTribute.changeUnitLocation(indianSettlement);
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
			super.startElement(attr);
		}

		@Override
		public void startWriteAttr(DemandTributeMission m, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(m, attr);
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
