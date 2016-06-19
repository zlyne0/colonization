package promitech.colonization.actors.europe;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.actors.LabelGoodActor;
import promitech.colonization.infrastructure.FontResource;

public class MarketGoodsActor extends LabelGoodActor {
	private int sellPrice = 0;
	private int buyPrice = 0;
	
	public MarketGoodsActor(GoodsType goodsType) {
		super(goodsType);
	}
	
    @Override
    public float getPrefWidth() {
        return Math.max(textureRegion.getRegionWidth(), FontResource.strWidth(font, "15/15"));
    }
	
    public void initPrice(int sellPrice, int buyPrice) {
		this.sellPrice = sellPrice;
		this.buyPrice = buyPrice;
		setLabel("" + sellPrice + "/" + buyPrice);
    }
    
    public String toString() {
        return super.toString() + ", price[" + sellPrice + "/" + buyPrice + "]";
    }

}
