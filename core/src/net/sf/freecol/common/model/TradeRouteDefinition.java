package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TradeRouteDefinition extends ObjectWithId {

	private String name;
	private final List<TradeRouteStop> tradeRouteStops = new ArrayList<TradeRouteStop>();
	
	public TradeRouteDefinition(IdGenerator idGenerator, String name) {
		super(idGenerator.nextId(TradeRouteDefinition.class));
		this.name = name;
	}

	public TradeRouteStop addRouteStop(Colony colony) {
		TradeRouteStop stop = new TradeRouteStop(colony.getId());
		tradeRouteStops.add(stop);
		return stop;
	}
	
	public List<TradeRouteStop> getTradeRouteStops() {
		return tradeRouteStops;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static class Xml extends XmlNodeParser<TradeRouteDefinition> {

		@Override
		public void startElement(XmlNodeAttributes attr) {
		}

		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
			return "tradeRouteDef";
		}
	}
}
