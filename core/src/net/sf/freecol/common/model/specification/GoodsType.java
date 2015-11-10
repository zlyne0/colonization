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
    private String storedAs;
    private int breedingNumber;

    public boolean isStorable() {
        return storable;
    }
    
	public GoodsType(String id) {
		super(id);
	}

    public String getStoredAs() {
        if (storedAs != null) {
            return storedAs;
        } else {
            return id;
        }
    }
	
    public int getBreedingNumber() {
        return breedingNumber;
    }
    
	@Override
	public boolean equals(Object obj) {
	    GoodsType gObj = (GoodsType)obj;
	    return id.equals(gObj.id);
	}
	
	@Override
	public int hashCode() {
	    return id.hashCode();
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
            gt.storedAs = attr.getStrAttribute("stored-as");
            gt.breedingNumber = attr.getIntAttribute("breeding-number", 0);
            
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
