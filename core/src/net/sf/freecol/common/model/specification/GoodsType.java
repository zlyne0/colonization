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
    public static final String MUSKETS = "model.goods.muskets";
    public static final String HORSES = "model.goods.horses";
    
    private static final float DEFAULT_PRODUCTION_WEIGHT = 1.0f;
    private static final float DEFAULT_LOW_PRODUCTION_THRESHOLD = 0.0f;
    private static final float DEFAULT_ZERO_PRODUCTION_FACTOR = 1.0f;
    
    public static boolean isFoodGoodsType(String goodsTypeId) {
        return GRAIN.equals(goodsTypeId) || FISH.equals(goodsTypeId);
    }
    
    boolean farmed;
    private boolean food;
    boolean military;
    boolean ignoreLimit;
    boolean newWorldGoods;
    /** Whether these are trade goods that can only be obtained in Europe. */
    boolean tradeGoods;
    boolean storable;
    /**
     * Whether this type of goods is required for building. (Derived
     * attribute)
     */
    private boolean buildingMaterial = false;
    private String storedAs;
    private int breedingNumber;
    private int price;
    /** What this goods type is made from. */
    private GoodsType madeFrom;
    /** What this goods type can make.  (Derived attribute) */
    private GoodsType makes;

    /** The initial market price difference for this type of goods. */
    private int priceDiff = 1;
    /** The initial amount of this goods type in a market. */
    private int initialAmount = 0;
    /** The initial <em>minimum</em> sales price for this type of goods. */
    private int initialPrice = 1;
    
    /**
     * A weight for the potential production of this goods type at a colony site.
     */
    private float productionWeight = DEFAULT_PRODUCTION_WEIGHT;
    /**
     * A threshold amount of potential production of this goods type
     * at a colony site, below which the score for the site is reduced.
     */
    private float lowProductionThreshold = DEFAULT_LOW_PRODUCTION_THRESHOLD;
    /**
     * The multiplicative factor with which to penalize a colony site
     * with zero production of this goods type, scaling linearly to
     * unity when the amount reaches lowResourceThreshold.
     */
    private float zeroProductionFactor = DEFAULT_ZERO_PRODUCTION_FACTOR;
    
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
		if (obj == null) {
			return false;
		}
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
    	return isNewWorldGoodsType() || madeFrom != null && madeFrom.isNewWorldGoodsType();
    }
    
    public boolean isImmigrationType() {
        return hasModifier(Modifier.IMMIGRATION);
    }
    
    /**
     * Is this goods type made from somthing?
     *
     * @return True if this {@link GoodsType} is made from something.
     */
    public boolean isRefined() {
        return madeFrom != null;
    }
    
    public boolean isFood() {
        return food;
    }
    
	public float getProductionWeight() {
		return productionWeight;
	}
    
	public float getLowProductionThreshold() {
		return lowProductionThreshold;
	}

	public float getZeroProductionFactor() {
		return zeroProductionFactor;
	}

	public boolean isMilitary() {
		return military;
	}

	public boolean isTradeGoods() {
		return tradeGoods;
	}
	
    /**
     * Is this type of goods required somewhere in the chain for
     * producing a BuildableType, and is not itself buildable.
     *
     * @return True if a raw building type.
     * @see BuildableType
     */
	public boolean isRawBuildingMaterial() {
		if (madeFrom != null) {
			return false;
		}
        GoodsType refinedType = makes;
        while (refinedType != null) {
            if (refinedType.buildingMaterial) {
            	return true;
            }
            refinedType = refinedType.makes;
        }
        return false;
	}
	
	public void setBuildingMaterial(boolean buildingMaterial) {
		this.buildingMaterial = buildingMaterial;
	}

	public GoodsType getMakes() {
		return makes;
	}
	
	public static class Xml extends XmlNodeParser<GoodsType> {
		private static final String ATTR_ZERO_PRODUCTION_FACTOR = "zero-production-factor";
		private static final String ATTR_LOW_PRODUCTION_THRESHOLD = "low-production-threshold";
		private static final String ATTR_PRODUCTION_WEIGHT = "production-weight";
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
            String id = attr.getStrAttribute(ATTR_ID);
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
        		gt.madeFrom.makes = gt;
            }
            gt.productionWeight = attr.getFloatAttribute(ATTR_PRODUCTION_WEIGHT, DEFAULT_PRODUCTION_WEIGHT);
            gt.lowProductionThreshold = attr.getFloatAttribute(ATTR_LOW_PRODUCTION_THRESHOLD, DEFAULT_LOW_PRODUCTION_THRESHOLD);
            gt.zeroProductionFactor = attr.getFloatAttribute(ATTR_ZERO_PRODUCTION_FACTOR, DEFAULT_ZERO_PRODUCTION_FACTOR);
            
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
        	attr.set(ATTR_PRODUCTION_WEIGHT, gt.productionWeight, DEFAULT_PRODUCTION_WEIGHT);
            attr.set(ATTR_LOW_PRODUCTION_THRESHOLD, gt.lowProductionThreshold, DEFAULT_LOW_PRODUCTION_THRESHOLD);
            attr.set(ATTR_ZERO_PRODUCTION_FACTOR, gt.zeroProductionFactor, DEFAULT_ZERO_PRODUCTION_FACTOR);
        	
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
