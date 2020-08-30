package promitech.colonization.ai.goodsToSell;

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
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;

class SettlementGoods {
	final Settlement settlement;
	final GoodsContainer goodsContainer = new GoodsContainer();
	final Market market;
	final List<GoodsType> goodsType;
	
	public SettlementGoods(Settlement settlement, Market market, List<GoodsType> goodsType) {
		this.settlement = settlement;
		this.market = market;
		this.goodsType = goodsType;
		settlement.getGoodsContainer().cloneTo(goodsContainer);
	}
	
	public int amount(GoodsType gt) {
		return goodsContainer.goodsAmount(gt);
	}
	
	public void score(ObjectsListScore<GoodsType> settlementGoodsScore) {
		settlementGoodsScore.clear();
		for (GoodsType gt : goodsType) {
			int amount = amount(gt);
			if (amount <= 0) {
				continue;
			}
			int salePrice = market.getSalePrice(gt, amount);
			settlementGoodsScore.add(gt, salePrice);
		}
		settlementGoodsScore.sortDescending();
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
		List<SettlementGoods> sGoods = new ArrayList<SettlementGoods>(player.settlements.size());
		for (Settlement settlement : player.settlements) {
			sGoods.add(new SettlementGoods(
				settlement, player.market(), goodsType
			));
		}
		return sGoods;
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
		rangeCalculator.generateRangeMap(map, startLocation, carrier, PathFinder.includeUnexploredTiles);
		
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
		settlementGoods.score(settlementGoodsScore);
		
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

	public void settlementGoodsReduce(Settlement settlement, Unit carrierCargoSpace) {
		SettlementGoods settlementGoods = findSettlementGoods(settlement);
		
		settlementGoods.score(settlementGoodsScore);
		
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