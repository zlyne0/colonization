package net.sf.freecol.common.model.specification;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import net.sf.freecol.common.model.ObjectWithId;

public class RequiredGoods extends ObjectWithId {

    public final int amount;
    
    public RequiredGoods(String id, int amount) {
        super(id);
        this.amount = amount;
    }

    public static class Xml extends XmlNodeParser {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            int amount = attr.getIntAttribute("value");
            RequiredGoods rg = new RequiredGoods(id, amount);
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
