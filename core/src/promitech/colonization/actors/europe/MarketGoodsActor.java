package promitech.colonization.actors.europe;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;

import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.actors.LabelGoodActor;
import promitech.colonization.actors.ShiftPressed;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.infrastructure.FontResource;

public class MarketGoodsActor extends LabelGoodActor {
	
	public static class GoodsDragAndDropSource extends com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source {

		public GoodsDragAndDropSource(MarketGoodsActor actor) {
			super(actor);
		}

		@Override
		public Payload dragStart(InputEvent event, float x, float y, int pointer) {
			MarketGoodsActor goodActor = (MarketGoodsActor)getActor();
            System.out.println("MarketGoodsActor.dragStart");
            // TODO: jesli nie moze handlowac towarem to return null, przekazanie obiektu market
//            if (goodActor.isEmpty()) {
//                return null;
//            }
            Image dragImg = new Image(goodActor.getTextureRegion());
            
            AbstractGoods goods = new AbstractGoods(goodActor.goodsType.getId(), ProductionSummary.CARRIER_SLOT_MAX_QUANTITY);
            
            Payload payload = new Payload();
            payload.setObject(new LabelGoodActor.DragAndDropPayload(goods, ShiftPressed.isShiftPressed()));
            payload.setDragActor(dragImg);
            payload.setInvalidDragActor(dragImg);
            payload.setValidDragActor(dragImg);
            return payload;
		}
	}
	
	private int sellPrice = 0;
	private int buyPrice = 0;
	
	public MarketGoodsActor(GoodsType goodsType, DragAndDropSourceContainer<AbstractGoods> dragAndDropSourceContainer) {
		super(goodsType);
		this.dragAndDropSourceContainer = dragAndDropSourceContainer;
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
