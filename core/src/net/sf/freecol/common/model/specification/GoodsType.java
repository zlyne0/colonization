package net.sf.freecol.common.model.specification;

import java.io.IOException;

import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.Market;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsType extends ObjectWithFeatures {

    public static final String FISH = "model.goods.fish";
    public static final String FOOD = "model.goods.food";
    public static final String GRAIN = "model.goods.grain";
    public static final String CROSSES = "model.goods.crosses";
    public static final String BELLS = "model.goods.bells";
    
    public static boolean isFoodGoodsType(String goodsTypeId) {
        return GRAIN.equals(goodsTypeId) || FISH.equals(goodsTypeId);
    }
    
    boolean farmed;
    private boolean food;
    boolean military;
    boolean ignoreLimit;
    boolean newWorldGoods;
    boolean tradeGoods;
    boolean storable;
    private String storedAs;
    private int breedingNumber;
    private int price;
    private GoodsType madeFrom;

    /** The initial market price difference for this type of goods. */
    private int priceDiff = 1;
    /** The initial amount of this goods type in a market. */
    private int initialAmount = 0;
    /** The initial <em>minimum</em> sales price for this type of goods. */
    private int initialPrice = 1;
    
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

    public boolean isFarmed() {
        return farmed;
    }
	
	public int getPrice() {
		return price;
	}

    /**
     * The default initial price difference (between purchase and sale price)
     * for this type of goods.
     *
     * @return The default initial price difference.
     */
    public int getPriceDifference() {
        return priceDiff;
    }
	
    public int getInitialAmount() {
        return initialAmount;
    }
    
    /**
     * The default initial purchase price for this goods type.
     *
     * @return The default initial purchase price.
     */
    public int getInitialBuyPrice() {
        return initialPrice + priceDiff;
    }
    
    /**
     * Get the initial <em>minimum</em> sales price for this type
     * of goods.  The actual initial sales price in a particular
     * Market may be higher.  This method is only used for initializing
     * Markets.
     *
     * @return The initial sell price.
     * @see Market
     */
    public int getInitialSellPrice() {
        return initialPrice;
    }
    
    /**
     * Is this a goods type native to the New World?
     *
     * @return True if this goods type is native to the New World.
     */
    public boolean isNewWorldGoodsType() {
        return newWorldGoods;
    }
    
    public boolean isNewWorldOrigin() {
    	return isNewWorldGoodsType() || getMadeFrom() != null && getMadeFrom().isNewWorldGoodsType();
    }
    
    public boolean isImmigrationType() {
        return hasModifier(Modifier.IMMIGRATION);
    }
    
    public GoodsType getMadeFrom() {
        return madeFrom;
    }
    
    public boolean isFood() {
        return food;
    }
    
	public static class Xml extends XmlNodeParser<GoodsType> {
		private static final String MARKET_ELEMENT = "market";
		private static final String ATTR_MADE_FROM = "made-from";
		private static final String ATTR_PRICE = "price";
		private static final String ATTR_BREEDING_NUMBER = "breeding-number";
		private static final String ATTR_STORED_AS = "stored-as";
		private static final String ATTR_STORABLE = "storable";
		private static final String ATTR_TRADE_GOODS = "trade-goods";
		private static final String ATTR_NEW_WORLD_GOODS = "new-world-goods";
		private static final String ATTR_IGNORE_LIMIT = "ignore-limit";
		private static final String ATTR_IS_MILITARY = "is-military";
		private static final String ATTR_IS_FOOD = "is-food";
		private static final String ATTR_IS_FARMED = "is-farmed";
		private static final String PRICE_DIFFERENCE_TAG = "price-difference";
		private static final String INITIAL_AMOUNT_TAG = "initial-amount";
		private static final String INITIAL_PRICE_TAG = "initial-price";

		public Xml() {
		    ObjectWithFeatures.Xml.abstractAddNodes(this);
		}
		
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            GoodsType gt = new GoodsType(id);
            gt.farmed = attr.getBooleanAttribute(ATTR_IS_FARMED, false);
            gt.food = attr.getBooleanAttribute(ATTR_IS_FOOD, false);
            gt.military = attr.getBooleanAttribute(ATTR_IS_MILITARY, false);
            gt.ignoreLimit = attr.getBooleanAttribute(ATTR_IGNORE_LIMIT, false);
            gt.newWorldGoods = attr.getBooleanAttribute(ATTR_NEW_WORLD_GOODS, false);
            gt.tradeGoods = attr.getBooleanAttribute(ATTR_TRADE_GOODS, false);
            gt.storable = attr.getBooleanAttribute(ATTR_STORABLE, true);
            gt.storedAs = attr.getStrAttribute(ATTR_STORED_AS);
            gt.breedingNumber = attr.getIntAttribute(ATTR_BREEDING_NUMBER, 0);
            gt.price = attr.getIntAttribute(ATTR_PRICE, 0);
            
            String madeFromStr = attr.getStrAttribute(ATTR_MADE_FROM);
            if (madeFromStr != null) {
            	gt.madeFrom = Specification.instance.goodsTypes.getById(madeFromStr);
            }
            
            nodeObject = gt;
        }

        @Override
        public void startWriteAttr(GoodsType gt, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(gt);

        	attr.set(ATTR_IS_FARMED, gt.farmed);
        	attr.set(ATTR_IS_FOOD, gt.food);
        	attr.set(ATTR_IS_MILITARY, gt.military);
        	attr.set(ATTR_IGNORE_LIMIT, gt.ignoreLimit);
        	attr.set(ATTR_NEW_WORLD_GOODS, gt.newWorldGoods);
        	attr.set(ATTR_TRADE_GOODS, gt.tradeGoods);
        	attr.set(ATTR_STORABLE, gt.storable);
        	attr.set(ATTR_STORED_AS, gt.storedAs);
        	attr.set(ATTR_BREEDING_NUMBER, gt.breedingNumber, 0);
        	attr.set(ATTR_PRICE, gt.price, 0);
        	if (gt.madeFrom != null) {
        		attr.set(ATTR_MADE_FROM, gt.madeFrom.getId());
        	}
        	
        	if (gt.initialAmount != 0 || gt.initialPrice != 1 || gt.priceDiff != 1) {
        		attr.xml.element(MARKET_ELEMENT);
        		attr.set(INITIAL_AMOUNT_TAG, gt.initialAmount);
                attr.set(INITIAL_PRICE_TAG, gt.initialPrice);
                attr.set(PRICE_DIFFERENCE_TAG, gt.priceDiff);
        		attr.xml.pop();
        	}
        }
        
        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals(MARKET_ELEMENT)) {
                GoodsType gt = (GoodsType)nodeObject;
                gt.initialAmount = attr.getIntAttribute(INITIAL_AMOUNT_TAG, 0);
                gt.initialPrice = attr.getIntAttribute(INITIAL_PRICE_TAG, 1);
                gt.priceDiff = attr.getIntAttribute(PRICE_DIFFERENCE_TAG, 1);
        	}
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
