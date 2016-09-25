package promitech.colonization.actors.europe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;

import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.player.MarketData;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.actors.LabelGoodActor;
import promitech.colonization.actors.QuantityGoodActor;
import promitech.colonization.actors.ShiftPressed;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.infrastructure.FontResource;

class MarketGoodsActor extends LabelGoodActor {
	
	private static final Color GOODS_ARREARS_COLOR = new Color(0.75f, 0.0f, 0.0f, 0.30f);    	
	
	static class MarketDragAndDropPayload extends DragAndDropPayload {
		MarketDragAndDropPayload(AbstractGoods abstractGoods, boolean changeTransferGoodsQuantity) {
			super(abstractGoods, changeTransferGoodsQuantity);
		}
	}
	
	static class GoodsDragAndDropSource extends com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source {
		
		public GoodsDragAndDropSource(MarketGoodsActor actor) {
			super(actor);
		}

		@Override
		public Payload dragStart(InputEvent event, float x, float y, int pointer) {
			MarketGoodsActor goodActor = (MarketGoodsActor)getActor();
            System.out.println("MarketGoodsActor.dragStart " + goodActor);
            if (goodActor.hasArrears) {
            	return null;
            }
            Image dragImg = new Image(goodActor.getTextureRegion());
            
            AbstractGoods goods = new AbstractGoods(goodActor.goodsType.getId(), ProductionSummary.CARRIER_SLOT_MAX_QUANTITY);
            
            Payload payload = new Payload();
            payload.setObject(new MarketDragAndDropPayload(goods, ShiftPressed.isShiftPressed()));
            payload.setDragActor(dragImg);
            payload.setInvalidDragActor(dragImg);
            payload.setValidDragActor(dragImg);
            return payload;
		}
	}
	
	static class GoodsDragAndDropTarget extends QuantityGoodActor.GoodsDragAndDropTarget {
		
		public GoodsDragAndDropTarget(Actor actor, DragAndDropTargetContainer<AbstractGoods> targetContainer) {
			super(actor, targetContainer);
		}
		
		@Override
		public void drop(Source source, Payload payload, float x, float y, int pointer) {
            if (payload.getObject() instanceof MarketDragAndDropPayload) {
            	return;
            }
			super.drop(source, payload, x, y, pointer);
		}
	}
	
	private final ShapeRenderer shapeRenderer;
	private int sellPrice = 0;
	private int buyPrice = 0;
	private boolean hasArrears = false;
	
	public MarketGoodsActor(GoodsType goodsType, ShapeRenderer shapeRenderer, DragAndDropSourceContainer<AbstractGoods> dragAndDropSourceContainer) {
		super(goodsType);
		this.shapeRenderer = shapeRenderer;
		this.dragAndDropSourceContainer = dragAndDropSourceContainer;
	}
	
    @Override
    public float getPrefWidth() {
        return Math.max(textureRegion.getRegionWidth(), FontResource.strWidth(font, "15/15"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
    	super.draw(batch, parentAlpha);
        
        if (hasArrears) {
        	batch.end();
        	
        	Gdx.gl.glEnable(GL20.GL_BLEND);
        	Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        	
	        shapeRenderer.begin(ShapeType.Filled);
	        shapeRenderer.setColor(GOODS_ARREARS_COLOR);
	        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
	        shapeRenderer.end();
	        
	        batch.begin();
        }
    }
    
    public void initData(MarketData marketData) {
    	this.sellPrice = marketData.getSalePrice();
    	this.buyPrice = marketData.getBuyPrice();
    	this.hasArrears = marketData.hasArrears();
    	setLabel("" + sellPrice + "/" + buyPrice);
    }
    
    public String toString() {
        return super.toString() + ", price[" + sellPrice + "/" + buyPrice + "]";
    }
}
