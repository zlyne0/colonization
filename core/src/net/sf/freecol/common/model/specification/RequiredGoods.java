package net.sf.freecol.common.model.specification;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

import java.io.IOException;

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

    @Override
    public String toString() {
    	return goodsType.getId() + " " + amount;
    }
    
    public static class Xml extends XmlNodeParser<RequiredGoods> {

		@Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute(ATTR_ID);
            int amount = attr.getIntAttribute(ATTR_VALUE);
            
            GoodsType goodsType = Specification.instance.goodsTypes.getById(id);
            RequiredGoods rg = new RequiredGoods(goodsType, amount);
            nodeObject = rg;
        }

        @Override
        public void startWriteAttr(RequiredGoods rg, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(rg);
        	attr.set(ATTR_VALUE, rg.amount);
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
