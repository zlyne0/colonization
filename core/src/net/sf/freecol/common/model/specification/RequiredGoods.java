package net.sf.freecol.common.model.specification;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;

public class RequiredGoods extends ObjectWithId {
    public final int amount;
    public final GoodsType goodsType;
    
    public RequiredGoods(GoodsType goodsType, int amount) {
        super(goodsType.getId());
        this.goodsType = goodsType;
        this.amount = amount;
    }

    public static class Xml extends XmlNodeParser {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            int amount = attr.getIntAttribute("value");
            
            GoodsType goodsType = Specification.instance.goodsTypes.getById(id);
            RequiredGoods rg = new RequiredGoods(goodsType, amount);
            nodeObject = rg;
        }

        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "required-goods";
        }
    }
}
