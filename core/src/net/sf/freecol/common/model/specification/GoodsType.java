package net.sf.freecol.common.model.specification;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;
import net.sf.freecol.common.model.ObjectWithId;

public class GoodsType extends ObjectWithId {

    boolean farmed;
    boolean food;
    boolean military;
    boolean ignoreLimit;
    boolean newWorldGoods;
    boolean tradeGoods;
    boolean storable;

    public boolean isStorable() {
        return storable;
    }
    
	public GoodsType(String id) {
		super(id);
	}

	public static class Xml extends XmlNodeParser {

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            GoodsType gt = new GoodsType(id);
            gt.farmed = attr.getBooleanAttribute("is-farmed", false);
            gt.food = attr.getBooleanAttribute("is-food", false);
            gt.military = attr.getBooleanAttribute("is-military", false);
            gt.ignoreLimit = attr.getBooleanAttribute("ignore-limit", false);
            gt.newWorldGoods = attr.getBooleanAttribute("new-world-goods", false);
            gt.tradeGoods = attr.getBooleanAttribute("trade-goods", false);
            gt.storable = attr.getBooleanAttribute("storable", true);
            
            nodeObject = gt;
        }

        @Override
        public String getTagName() {
            return tagName();
        }
	    
        public static String tagName() {
            return "goods-type";
        }
	}
	
}
