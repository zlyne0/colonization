package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TradeRouteDefinition extends ObjectWithId {

	private String name;
	private final List<TradeRouteStop> tradeRouteStops = new ArrayList<TradeRouteStop>();
	
	private TradeRouteDefinition(String id, String name) {
	    super(id);
	    this.name = name;
	}
	
	public TradeRouteDefinition(IdGenerator idGenerator, String name) {
		super(idGenerator.nextId(TradeRouteDefinition.class));
		this.name = name;
	}

	public TradeRouteStop addRouteStop(Colony colony) {
		TradeRouteStop stop = new TradeRouteStop(colony.getId());
		tradeRouteStops.add(stop);
		return stop;
	}
	
	public void removeStop(TradeRouteStop stop) {
		tradeRouteStops.remove(stop);
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

		private static final String ATTR_NAME = "name";

		public Xml() {
		    addNode(TradeRouteStop.class, new ObjectFromNodeSetter<TradeRouteDefinition, TradeRouteStop>() {
                @Override
                public void set(TradeRouteDefinition target, TradeRouteStop entity) {
                    target.tradeRouteStops.add(entity);
                }
                @Override
                public void generateXml(TradeRouteDefinition source, ChildObject2XmlCustomeHandler<TradeRouteStop> xmlGenerator) throws IOException {
                    xmlGenerator.generateXmlFromCollection(source.tradeRouteStops);
                }
		    });
		}
		
        @Override
		public void startElement(XmlNodeAttributes attr) {
		    TradeRouteDefinition def = new TradeRouteDefinition(attr.getId(), attr.getStrAttributeNotNull(ATTR_NAME));
		    nodeObject = def;
		}

        @Override
        public void startWriteAttr(TradeRouteDefinition node, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(node);
            attr.set(ATTR_NAME, node.getName());
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
