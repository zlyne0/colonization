package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.GameResources;
import promitech.colonization.Validation;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

class GoodActor extends Widget {
    
	static class DragAndDropPayload {
		AbstractGoods abstractGoods;
		boolean changeTransferGoodsQuantity = false;
		
		DragAndDropPayload(AbstractGoods abstractGoods, boolean changeTransferGoodsQuantity) {
			this.abstractGoods = abstractGoods;
			this.changeTransferGoodsQuantity = changeTransferGoodsQuantity;
		}
	}
	
    public static class GoodsDragAndDropSource extends com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source {
        public GoodsDragAndDropSource(GoodActor actor) {
            super(actor);
        }

        @Override
        public Payload dragStart(InputEvent event, float x, float y, int pointer) {
            GoodActor goodActor = (GoodActor)getActor();
            System.out.println("GoodsDragAndDropSource.dragStart");
            if (goodActor.isEmpty()) {
                return null;
            }
            
            Image dragImg = new Image(goodActor.getTextureRegion());
            
            Payload payload = new Payload();
            payload.setObject(new DragAndDropPayload(goodActor.newAbstractGoods(), ColonyApplicationScreen.isShiftPressed()));
            payload.setDragActor(dragImg);
            payload.setInvalidDragActor(dragImg);
            payload.setValidDragActor(dragImg);
            return payload;
        }
    }
    
    public static class GoodsDragAndDropTarget extends Target {
        private final DragAndDropTargetContainer<AbstractGoods> targetContainer;
        
        public GoodsDragAndDropTarget(Actor actor, DragAndDropTargetContainer<AbstractGoods> targetContainer) {
            super(actor);
            this.targetContainer = targetContainer;
        }

        @Override
        public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
            Validation.instanceOf(source.getActor(), GoodActor.class);
            Validation.instanceOf(payload.getObject(), DragAndDropPayload.class);
            
            DragAndDropPayload dadPayload = (DragAndDropPayload)payload.getObject();
            if (dadPayload.changeTransferGoodsQuantity) {
            	return true;
            }
            return targetContainer.canPutPayload(dadPayload.abstractGoods, x, y);
        }

        @Override
        public void drop(Source source, Payload payload, float x, float y, int pointer) {
            Validation.instanceOf(source.getActor(), GoodActor.class);
            Validation.instanceOf(payload.getObject(), DragAndDropPayload.class);

            GoodActor actor = (GoodActor)source.getActor();
            DragAndDropSourceContainer<AbstractGoods> sourceContainer = actor.dragAndDropSourceContainer;
            
            DragAndDropPayload dadPayload = (DragAndDropPayload)payload.getObject();
            if (dadPayload.changeTransferGoodsQuantity) {
            	GoodTransferQuantityWindow window = new GoodTransferQuantityWindow(
            		dadPayload.abstractGoods, sourceContainer, targetContainer
            	);
            	window.show(source.getActor().getStage());
            } else {
            	sourceContainer.takePayload(dadPayload.abstractGoods, x, y);
            	targetContainer.putPayload(dadPayload.abstractGoods, x, y);
            }
        }
    }
    
    private final String goodTypeId;
    private int quantity = 0;
    private final TextureRegion textureRegion;
    
    private final float fontHeight;
    DragAndDropSourceContainer<AbstractGoods> dragAndDropSourceContainer;
    
    GoodActor(String goodTypeId, int quantity) {
        Frame img = GameResources.instance.goodsImage(goodTypeId);
        textureRegion = img.texture;
        
        this.quantity = quantity;
        this.goodTypeId = goodTypeId;
        
        setSize(getPrefWidth(), getPrefHeight());
        fontHeight = FontResource.getGoodsQuantityFont().getCapHeight();
    }
    
    @Override
    public float getPrefHeight() {
    	return textureRegion.getRegionHeight() + fontHeight;
    }
    
    @Override
    public float getPrefWidth() {
    	return textureRegion.getRegionWidth();
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	batch.draw(textureRegion, getX(), getY() + fontHeight);
        
        BitmapFont font = FontResource.getGoodsQuantityFont();
        if (quantity == 0) {
            font.setColor(Color.GRAY);
        } else {
            font.setColor(Color.WHITE);
        }
        float quantityStrLength = FontResource.strIntWidth(font, quantity);
        font.draw(batch, Integer.toString(quantity), 
        		getX() + getWidth()/2 - quantityStrLength/2, 
        		getY() + fontHeight
        );
    }

    public String toString() {
        return "type[" + goodTypeId + "], quantity[" + quantity + "]";
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    public String getGoodTypeId() {
        return goodTypeId;
    }

    AbstractGoods newAbstractGoods() {
    	return new AbstractGoods(goodTypeId, quantity);
    }
    
    public void increaseQuantity(AbstractGoods anAbstractGood) {
        quantity += anAbstractGood.getQuantity();
    }

	public void decreaseQuantity(AbstractGoods anAbstractGood) {
        quantity -= anAbstractGood.getQuantity();
	}

	public boolean equalsOrLess(AbstractGoods anAbstractGood) {
		return anAbstractGood.getQuantity() <= quantity;
	}

}
