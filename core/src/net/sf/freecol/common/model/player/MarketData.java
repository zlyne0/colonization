package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class MarketData extends ObjectWithId {
	
    /** Inclusive lower bound on goods price. */
    public static final int MINIMUM_PRICE = 1;

    /** Inclusive upper bound on goods price. */
    public static final int MAXIMUM_PRICE = 19;
	
    /**
     * European markets are bottomless.  Goods present never decrease
     * below this threshold.
     */
    public static final int MINIMUM_AMOUNT = 100;
	
	private GoodsType goodsType;
    /** Amount of this goods in the market. */
	private int amountInMarket;
	private int initialPrice;
	/** Arrears owed to the crown. */
	private int arrears;
    /** Total sales. */
    private int sales;
    /** Total income before taxes. */
    private int incomeBeforeTaxes;
    /** Total income after taxes. */
    private int incomeAfterTaxes;
    /** Has this good been traded? */
    private boolean traded;
    /** Current selling price. */
    private int paidForSale;
    
    /** Current purchase price. */
    private int costToBuy;
    
    /**
     * Place to save to old price so as to be able to tell when a price change
     * message should be generated.  Not necessary to serialize.
     */
    private int oldPrice;
    
	public MarketData(GoodsType goodsType) {
		super(goodsType.getId());
		this.goodsType = goodsType;
		
		paidForSale = goodsType.getInitialSellPrice();
		costToBuy = goodsType.getInitialBuyPrice();
		amountInMarket = goodsType.getInitialAmount();
		
        arrears = 0;
        sales = 0;
        incomeBeforeTaxes = 0;
        incomeAfterTaxes = 0;
        oldPrice = costToBuy;
        traded = false;
	}

    public void update() {
        costToBuy = -1; // Disable price change clamping
        price();
    }

    /**
     * Adjust the prices.
     *
     * Sets the costToBuy and paidForSale fields from the amount in
     * the market, initial price and goods-type specific information.
     * Ensures that prices change incrementally with a clamping
     * mechanism.
     *
     * @return True if the price changes.
     */
    public boolean price() {
        if (!goodsType.isStorable()) {
        	return false;
        }
        int diff = goodsType.getPriceDifference();
        float amountPrice = initialPrice * (goodsType.getInitialAmount() / (float) amountInMarket);
        int newSalePrice = Math.round(amountPrice);
        int newPrice = newSalePrice + diff;

        // Work-around to limit prices of new world goods
        // and related manufactured goods.
        if (goodsType.isNewWorldOrigin() && newSalePrice > initialPrice + 2) {
            newSalePrice = initialPrice + 2;
            newPrice = newSalePrice + diff;
        }

        // Another hack to prevent price changing too fast in one hit.
        // Push the amount in market back as well to keep this stable.
        //
        // Prices that change by more than the buy/sell difference
        // allow big traders to exploit the market and extract free
        // money... not sure I want to be fighting economic reality
        // but game balance demands it here.
        if (costToBuy > 0) {
            if (newPrice > costToBuy + diff) {
                amountPrice -= newPrice - (costToBuy + diff);
                amountInMarket = Math.round(goodsType.getInitialAmount() * (initialPrice / amountPrice));
                System.out.println("Clamped price rise for " + getId()
                    + " from " + newPrice
                    + " to " + (costToBuy + diff));
                newPrice = costToBuy + diff;
            } else if (newPrice < costToBuy - diff) {
                amountPrice += (costToBuy - diff) - newPrice;
                amountInMarket = Math.round(goodsType.getInitialAmount() * (initialPrice / amountPrice));
                System.out.println("Clamped price fall for " + getId()
                    + " from " + newPrice
                    + " to " + (costToBuy - diff));
                newPrice = costToBuy - diff;
            }
            newSalePrice = newPrice - diff;
        }

        // Clamp extremes.
        if (newPrice > MAXIMUM_PRICE) {
            newPrice = MAXIMUM_PRICE;
            newSalePrice = newPrice - diff;
        } else if (newSalePrice < MINIMUM_PRICE) {
            newSalePrice = MINIMUM_PRICE;
            newPrice = newSalePrice + diff;
        }

        int oldCostToBuy = costToBuy, oldPaidForSale = paidForSale;
        costToBuy = newPrice;
        paidForSale = newSalePrice;
        return costToBuy != oldCostToBuy || paidForSale != oldPaidForSale;
    }
    
    public final int getBuyPrice() {
        return costToBuy;
    }
	
    public final int getSalePrice() {
        return paidForSale;
    }
    
    public final int getCostToBuy(int amount) {
    	return costToBuy * amount;
    }
    
    public final int getCostToSell(int amount) {
    	return paidForSale * amount;
    }
    
	void modifySales(int goodsAmount) {
		if (goodsAmount != 0) {
			traded = true;
			sales += goodsAmount;
		}
	}

	void modifyIncomeBeforeTaxes(int price) {
		incomeBeforeTaxes += price;
	}

	void modifyIncomeAfterTaxes(int price) {
		incomeAfterTaxes += price;
	}

	void modifyAmountInMarket(int goodsAmount) {
		amountInMarket = Math.max(MINIMUM_AMOUNT, amountInMarket + goodsAmount);
		traded = true;
	}

	/**
	 * 
	 * @param goodsAmount
	 * @param price
	 * @param playerModifiedMarketAmount
	 * @return True if the price changes as a result of this addition.
	 */
	boolean modifyOnBuyGoods(int goodsAmount, int price, int playerModifiedMarketAmount) {
		if (goodsAmount == 0) {
			return false;
		}
		
		modifySales(-goodsAmount);
		modifyIncomeBeforeTaxes(-price);
		modifyIncomeAfterTaxes(-price);
		modifyAmountInMarket(-playerModifiedMarketAmount);
		return price();
	}

    boolean modifyOnSellGoods(int goodsAmount, int price, int priceAfterTax, int playerModifiedMarketAmount) {
        if (goodsAmount == 0) {
            return false;
        }
        modifySales(goodsAmount);
        modifyIncomeBeforeTaxes(price);
        modifyIncomeAfterTaxes(priceAfterTax);
        modifyAmountInMarket(playerModifiedMarketAmount);
        return price();
    }
	
    public boolean hasNotArrears() {
    	return arrears == 0;
    }
    
    public boolean hasArrears() {
    	return arrears > 0;
    }

	public void repayArrears() {
		arrears = 0;
	}

    public int getArrears() {
    	return arrears;
    }

	protected GoodsType getGoodsType() {
		return goodsType;
	}
    
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			String goodsTypeStr = attr.getStrAttribute("goods-type");
			GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsTypeStr);
		
			MarketData md = new MarketData(goodsType);
			md.amountInMarket = attr.getIntAttribute("amount", 0);
			md.initialPrice = attr.getIntAttribute("initialPrice", -1);
			md.arrears = attr.getIntAttribute("arrears", 0);
			md.sales = attr.getIntAttribute("sales", 0);
			md.incomeAfterTaxes = attr.getIntAttribute("incomeAfterTaxes", 0);
			md.incomeBeforeTaxes = attr.getIntAttribute("incomeBeforeTaxes", 0);
			md.traded = attr.getBooleanAttribute("traded", md.sales != 0);
			
	        md.update();
	        md.oldPrice = md.costToBuy;
			
			nodeObject = md;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "marketData";
		}
	}
}
