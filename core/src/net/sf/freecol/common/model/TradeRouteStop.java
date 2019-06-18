package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TradeRouteStop {
	private String tradeLocationId;
	private List<GoodsType> goodsType = new ArrayList<GoodsType>();

	public TradeRouteStop(String tradeLocationId) {
		this.tradeLocationId = tradeLocationId;
	}
	
	public String getTradeLocationId() {
		return tradeLocationId;
	}

	public List<GoodsType> getGoodsType() {
		return goodsType;
	}
	
    public static class Xml extends XmlNodeParser<TradeRouteStop> {

        private static final String GOODS_ELEMENT = "goods";
        private static final String ATTR_LOCATION = "location";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TradeRouteStop stop = new TradeRouteStop(attr.getStrAttributeNotNull(ATTR_LOCATION));
            nodeObject = stop;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(GOODS_ELEMENT)) {
                nodeObject.goodsType.add(Specification.instance.goodsTypes.getById(attr.getId()));
            }
        }
        
        @Override
        public void startWriteAttr(TradeRouteStop node, XmlNodeAttributesWriter attr) throws IOException {
            attr.set(ATTR_LOCATION, node.tradeLocationId);
            for (GoodsType gt : node.goodsType) {
                attr.xml.element(GOODS_ELEMENT);
                attr.setId(gt);
                attr.xml.pop();
            }
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "stop";
        }
    }
}
