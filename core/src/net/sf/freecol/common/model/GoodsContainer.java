package net.sf.freecol.common.model;

import java.util.HashMap;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsContainer extends ObjectWithId {

    private final java.util.Map<String,Integer> goods = new HashMap<String, Integer>();
    
    public GoodsContainer(String id) {
        super(id);
    }

    public int goodsAmount(GoodsType type) {
        Integer amount = goods.get(type.id);
        if (amount == null) {
            return 0;
        }
        return amount;
    }


    public java.util.Map<String, Integer> getGoods() {
        return goods;
    }
    
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            GoodsContainer goodsContainer = new GoodsContainer(id);
            
            nodeObject = goodsContainer;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals("goods")) {
                String typeStr = attr.getStrAttribute("type");
                int amount = attr.getIntAttribute("amount", 0);
                ((GoodsContainer)nodeObject).goods.put(typeStr, amount);
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
