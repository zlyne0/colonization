package net.sf.freecol.common.model;

import java.io.IOException;

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
