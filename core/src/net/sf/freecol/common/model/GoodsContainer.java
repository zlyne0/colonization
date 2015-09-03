package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsContainer extends ObjectWithId {

    public final List<AbstractGoods> goods = new ArrayList<AbstractGoods>(20);
    
    public GoodsContainer(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser {

        boolean storedGoodsSection = false;
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            GoodsContainer goodsContainer = new GoodsContainer(id);
            
            nodeObject = goodsContainer;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals("storedGoods")) {
                storedGoodsSection = true;
            }
            if (storedGoodsSection && attr.isQNameEquals("goods")) {
                String typeStr = attr.getStrAttribute("type");
                int amount = attr.getIntAttribute("amount", 0);
                
                GoodsType goodsType = game.specification.goodsTypes.getById(typeStr);
                AbstractGoods goods = new AbstractGoods(goodsType, amount);
                
                ((GoodsContainer)nodeObject).goods.add(goods);
            }
        }
        
        @Override
        public void endReadChildren(String qName) {
            if (qName.equals("storedGoods")) {
                storedGoodsSection = false;
            }
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "goodsContainer";
        }
    }
}
