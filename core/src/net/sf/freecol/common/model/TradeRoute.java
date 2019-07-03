package net.sf.freecol.common.model;

import java.io.IOException;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TradeRoute implements Identifiable {

	private String tradeRouteDefinitionId;
	private int nextStopLocationIndex;
	
	public TradeRoute(String tradeRouteDefinitionId) {
		this.tradeRouteDefinitionId = tradeRouteDefinitionId;
		this.nextStopLocationIndex = 0;
	}

	private TradeRoute(String tradeRouteDefinitionId, int nextStopLocationIndex) {
		this.tradeRouteDefinitionId = tradeRouteDefinitionId;
		this.nextStopLocationIndex = nextStopLocationIndex;
	}
	
	@Override
	public String getId() {
		return tradeRouteDefinitionId;
	}

	public String getTradeRouteDefinitionId() {
		return tradeRouteDefinitionId;
	}

	public int getNextStopLocationIndex() {
		return nextStopLocationIndex;
	}

	public void setNextStopLocationIndex(int nextStopLocationIndex) {
		this.nextStopLocationIndex = nextStopLocationIndex;
	}

	public boolean containsStop(Player player, Identifiable stopLocation) {
		TradeRouteDefinition tradeRouteDef = player.tradeRoutes.getByIdOrNull(tradeRouteDefinitionId);
		if (tradeRouteDef == null) {
			return false;
		}
		return tradeRouteDef.stopDefinitionByLocationOrNull(stopLocation) != null;
	}
	
    public Colony nextStopLocation(Player player) {
        TradeRouteDefinition tradeRoute = player.tradeRoutes.getByIdOrNull(tradeRouteDefinitionId);
        if (tradeRoute == null) {
            System.out.println("player[" + player.getId() + "]"
                + ".noTradeRouteDefinition " + tradeRouteDefinitionId);
            return null;
        }
        if (nextStopLocationIndex >= tradeRoute.getTradeRouteStops().size()) {
            System.out.println(
                "player[" + player.getId() + "]"
                + ".tradeRouteDefinition[" + tradeRouteDefinitionId + "]"
                + ".noStopLocation"
            );
            return null;
        }
        TradeRouteStop tradeRouteStop = tradeRoute.getTradeRouteStops().get(nextStopLocationIndex);
        Settlement settlement = player.settlements.getByIdOrNull(tradeRouteStop.getTradeLocationId());
        if (settlement == null || !settlement.isColony() || settlement.getOwner().notEqualsId(player)) {
            System.out.println(
                "player[" + player.getId() + "]"
                + ".tradeRouteDefinition[" + tradeRouteDefinitionId + "]"
                + ".noStopLocation[" + tradeRouteStop.getTradeLocationId() + "]"
            );
            return null;
        }
        return settlement.asColony();
    }
    
    public void increaseNextStop(Player player) {
        TradeRouteDefinition tradeRoute = player.tradeRoutes.getById(tradeRouteDefinitionId);
        
        nextStopLocationIndex++;
        if (nextStopLocationIndex >= tradeRoute.getTradeRouteStops().size()) {
            nextStopLocationIndex = 0;
        }
    }

	public void unloadCargo(Unit wagon, Colony stopLocation) {
		System.out.println(
			"tradeRoute[" + tradeRouteDefinitionId + "]"
			+ ".location[" + stopLocation.getId() + "].unloadGoods"
		);
		TradeRouteDefinition tradeRouteDef = wagon.getOwner().tradeRoutes.getById(tradeRouteDefinitionId);
		TradeRouteStop stopDef = tradeRouteDef.stopDefinitionByLocation(stopLocation);

		boolean unloadDone = false;
		// clone because nested modified objectMap
		for (Entry<String> wagonGoodsEntry : wagon.getGoodsContainer().cloneGoods().entries()) {
			String goodsId = wagonGoodsEntry.key;
			int wagonGoodsAmount = wagonGoodsEntry.value;
			if (stopDef.hasNotGoods(goodsId) && wagonGoodsAmount > 0) {
				System.out.println(
					"tradeRoute[" + tradeRouteDefinitionId + "]"
					+ ".location[" + stopLocation.getId() + "]"
					+ ".unloadGoods " + goodsId + " " + wagonGoodsAmount
				);
				wagon.getGoodsContainer().transferGoods(goodsId, stopLocation.getGoodsContainer());
				unloadDone = true;
			}
		}
		
		if (unloadDone && !wagon.hasFullMovesPoints()) {
			wagon.reduceMovesLeftToZero();
			wagon.setState(UnitState.ACTIVE);
		}
	}
    
	public void loadCargo(Unit wagon, Colony stopLocation) {
		System.out.println(
			"tradeRoute[" + tradeRouteDefinitionId + "]"
			+ ".location[" + stopLocation.getId() + "].loadGoods"
		);

		TradeRouteDefinition tradeRouteDef = wagon.getOwner().tradeRoutes.getById(tradeRouteDefinitionId);
		TradeRouteStop stopDef = tradeRouteDef.stopDefinitionByLocation(stopLocation);
		
		boolean loadDone = false;
		for (GoodsType goodsType : stopDef.getGoodsType()) {
			int stopGoodsAmount = stopLocation.getGoodsContainer().goodsAmount(goodsType);
			if (stopGoodsAmount > 0) {
				int transferAmount = Math.min(
					stopGoodsAmount, 
					wagon.maxGoodsAmountToFillFreeSlots(goodsType.getId())
				);
				if (transferAmount > 0) {
					System.out.println(
						"tradeRoute[" + tradeRouteDefinitionId + "]"
						+ ".location[" + stopLocation.getId() + "]"
						+ ".load " + goodsType.getId() + " " + transferAmount
					);
					stopLocation.getGoodsContainer().transferGoods(goodsType, transferAmount, wagon.getGoodsContainer());
					loadDone = true;
				}
			}
		}
		if (loadDone && !wagon.hasFullMovesPoints()) {
			wagon.reduceMovesLeftToZero();
			wagon.setState(UnitState.ACTIVE);
		}
	}
	
    public static class Xml extends XmlNodeParser<TradeRoute> {

		private static final String ATTR_NEXT_STOP_INDEX = "index";
		private static final String ATTR_ROUTE_DEFINITION_ID = "defId";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			nodeObject = new TradeRoute(
				attr.getStrAttributeNotNull(ATTR_ROUTE_DEFINITION_ID),
				attr.getIntAttribute(ATTR_NEXT_STOP_INDEX)
			);
		}

		@Override
		public void startWriteAttr(TradeRoute node, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_ROUTE_DEFINITION_ID, node.tradeRouteDefinitionId);
			attr.set(ATTR_NEXT_STOP_INDEX, node.nextStopLocationIndex);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "tradeRoute";
		}
    	
    }
}
