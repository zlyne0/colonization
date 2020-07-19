package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

class SettlementGoods {
	final Settlement settlement;
	final GoodsContainer goodsContainer = new GoodsContainer();
	
	public SettlementGoods(Settlement settlement) {
		this.settlement = settlement;
		settlement.getGoodsContainer().cloneTo(goodsContainer);
	}
	
	public int amount(GoodsType gt) {
		return goodsContainer.goodsAmount(gt);
	}
}

class SettlementWarehouseScoreGoods {
	private final GoodsContainer tmpCarrierGoodsContainer = new GoodsContainer();
	
	private final List<GoodsType> goodsType;
	private final Map map;
	private final PathFinder rangeCalculator;
	
	private final Market market;
	
	private final ObjectsListScore<GoodsType> settlementGoodsScore;
	private final ObjectsListScore<Settlement> settlementsScore;
	private final List<SettlementGoods> settlementsGoods;
	
	SettlementWarehouseScoreGoods(List<GoodsType> goodsType, Player player) {
		this(goodsType, player, null, null);
	}
	
	SettlementWarehouseScoreGoods(List<GoodsType> goodsType, Player player, Map map, PathFinder rangeCalculator) {
		this.goodsType = goodsType;
		this.map = map;
		this.rangeCalculator = rangeCalculator;
		this.market = player.market();
		
		settlementsGoods = createSettlementsGoods(player);
		
		settlementGoodsScore = new ObjectsListScore<GoodsType>(Specification.instance.goodsTypes.size());
		settlementsScore = new ObjectsListScore<Settlement>(player.settlements.size());
	}
	
	private List<SettlementGoods> createSettlementsGoods(Player player) {
		List<SettlementGoods> settlementsGoods = new ArrayList<SettlementGoods>(player.settlements.size());
		for (Settlement settlement : player.settlements) {
			settlementsGoods.add(new SettlementGoods(settlement));
		}
		return settlementsGoods;
	}
	
	ObjectsListScore<Settlement> score(Unit carrier) {
		settlementsScore.clear();
		for (SettlementGoods settlementGoods : settlementsGoods) {
			settlementsScore.add(settlementGoods.settlement, scoreSettlementGoods(settlementGoods, carrier));
		}
		settlementsScore.sortDescending();
		return settlementsScore;
	}

	ObjectsListScore<Settlement> score(Unit carrier, Tile startLocation) {
		rangeCalculator.generateRangeMap(map, startLocation, carrier, false);
		
		settlementsScore.clear();
		for (SettlementGoods settlementGoods : settlementsGoods) {
			int settlementScore = settlementTurnsScore(
				scoreSettlementGoods(settlementGoods, carrier), 
				rangeCalculator.turnsCost(settlementGoods.settlement.tile)
			);
			settlementsScore.add(settlementGoods.settlement, settlementScore);
		}
		settlementsScore.sortDescending();
		return settlementsScore;
	}
	
	int settlementTurnsScore(int settlementScore, int turns) {
		if (turns > 100) {
			turns = 100;
		}
		return settlementScore * (100 - (turns*15))/100;
	}
	
	private int scoreSettlementGoods(SettlementGoods settlementGoods, Unit carrier) {
		settlementAllGoodsScore(settlementGoods);
		
		int carierGoodsTotalSlots = carrier.unitType.getSpace() - carrier.unitContainerSpaceTaken();

		carrier.getGoodsContainer().cloneTo(tmpCarrierGoodsContainer);
		int score = 0;
		
		for (int i = 0; i < settlementGoodsScore.size(); i++) {
			GoodsType gt = settlementGoodsScore.get(i).getObj();
			int maxGoodsForContainer = tmpCarrierGoodsContainer.maxGoodsAmountToFillFreeSlots(gt.getId(), carierGoodsTotalSlots);
			int goodsToScore = Math.min(maxGoodsForContainer, settlementGoods.amount(gt));
			if (goodsToScore > 0) {
				score += market.getSalePrice(gt, goodsToScore);
				tmpCarrierGoodsContainer.increaseGoodsQuantity(gt, goodsToScore);
			}
		}
		return score;
	}
	
	private void settlementAllGoodsScore(SettlementGoods settlementGoods) {
		settlementGoodsScore.clear();
		for (GoodsType gt : goodsType) {
			int amount = settlementGoods.amount(gt);
			if (amount <= 0) {
				continue;
			}
			int salePrice = market.getSalePrice(gt, amount);
			settlementGoodsScore.add(gt, salePrice);
		}
		settlementGoodsScore.sortDescending();
	}

	public void settlementGoodsReduce(Settlement settlement, Unit carrierCargoSpace) {
		SettlementGoods settlementGoods = findSettlementGoods(settlement);
		
		settlementAllGoodsScore(settlementGoods);
		
		carrierCargoSpace.getGoodsContainer().cloneTo(tmpCarrierGoodsContainer);
		int cargoSlots = carrierCargoSpace.allGoodsCargoSlots();
		
		for (ObjectScore<GoodsType> scoreGoods : settlementGoodsScore) {
			GoodsType scoreGoodsType = scoreGoods.getObj();
			int goodsAmount = tmpCarrierGoodsContainer.maxGoodsAmountToFillFreeSlots(
				scoreGoodsType.getId(), 
				cargoSlots
			);
			
			settlementGoods.goodsContainer.transferGoods(scoreGoodsType, goodsAmount, tmpCarrierGoodsContainer);
		}
	}
	
	private SettlementGoods findSettlementGoods(Settlement settlement) {
		for (SettlementGoods settlementGoods : settlementsGoods) {
			if (settlement.equalsId(settlementGoods.settlement)) {
				return settlementGoods;
			}
		}
		throw new IllegalStateException("can not find settlements goods by settlement " + settlement.getId());
	}
	
}