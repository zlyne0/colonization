package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.StringTemplate;

public class TradeRouteDefinition extends ObjectWithId {

	private String name;
	// TODO: MapIdEntities
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

	public TradeRouteStop stopDefinitionByLocation(Identifiable stopLocation) {
		TradeRouteStop stop = stopDefinitionByLocationOrNull(stopLocation);
		if (stop == null) {
			throw new IllegalArgumentException(
				"can not find trade stop by id: " + stopLocation.getId() 
				+ " in route def " + getId() + " " + name
			);
		}
		return stop;
	}

	public TradeRouteStop stopDefinitionByLocationOrNull(Identifiable stopLocation) {
		for (TradeRouteStop stop : tradeRouteStops) {
			if (stopLocation.getId().equals(stop.getTradeLocationId())) {
				return stop;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public StringTemplate canBeAssignedMsg(Player player) {
        if (tradeRouteStops.size() < 2) {
            return StringTemplate.template("tradeRoute.notEnoughStops");
        }
        boolean empty = true;
        for (TradeRouteStop tradeRouteStop : tradeRouteStops) {
            if (!player.settlements.containsId(tradeRouteStop.getTradeLocationId())) {
                return StringTemplate.template("tradeRoute.invalidStop")
                        .add("%name%", "");
            }
            if (!tradeRouteStop.getGoodsType().isEmpty()) {
                empty = false;
            }
        }
        if (empty) {
            return StringTemplate.template("tradeRoute.allEmpty");
        }
        return null;
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
