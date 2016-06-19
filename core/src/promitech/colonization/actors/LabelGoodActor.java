package promitech.colonization.actors;

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
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.Validation;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

public class LabelGoodActor extends Widget {

	private static class DragAndDropPayload {
		AbstractGoods abstractGoods;
		boolean changeTransferGoodsQuantity = false;
		
		DragAndDropPayload(AbstractGoods abstractGoods, boolean changeTransferGoodsQuantity) {
			this.abstractGoods = abstractGoods;
			this.changeTransferGoodsQuantity = changeTransferGoodsQuantity;
		}
	}
	
    public static class GoodsDragAndDropSource extends com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source {
        public GoodsDragAndDropSource(QuantityGoodActor actor) {
            super(actor);
        }

        @Override
        public Payload dragStart(InputEvent event, float x, float y, int pointer) {
            QuantityGoodActor goodActor = (QuantityGoodActor)getActor();
            System.out.println("GoodsDragAndDropSource.dragStart");
            if (goodActor.isEmpty()) {
                return null;
            }
            
            Image dragImg = new Image(goodActor.getTextureRegion());
            
            Payload payload = new Payload();
            payload.setObject(new DragAndDropPayload(goodActor.newAbstractGoods(), ShiftPressed.isShiftPressed()));
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
            Validation.instanceOf(source.getActor(), QuantityGoodActor.class);
            Validation.instanceOf(payload.getObject(), DragAndDropPayload.class);
            
            DragAndDropPayload dadPayload = (DragAndDropPayload)payload.getObject();
            if (dadPayload.changeTransferGoodsQuantity) {
            	return true;
            }
            return targetContainer.canPutPayload(dadPayload.abstractGoods, x, y);
        }

        @Override
        public void drop(Source source, Payload payload, float x, float y, int pointer) {
            Validation.instanceOf(source.getActor(), QuantityGoodActor.class);
            Validation.instanceOf(payload.getObject(), DragAndDropPayload.class);

            QuantityGoodActor actor = (QuantityGoodActor)source.getActor();
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
	
	
	protected final GoodsType goodsType;
	protected final TextureRegion textureRegion;
	protected BitmapFont font;
	protected String label = "";
    public DragAndDropSourceContainer<AbstractGoods> dragAndDropSourceContainer;
    
	public LabelGoodActor(GoodsType goodsType) {
		this.goodsType = goodsType;
		
		Frame img = GameResources.instance.goodsImage(goodsType);
		this.textureRegion = img.texture;
		this.font = FontResource.getGoodsQuantityFont();
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
    @Override
    public float getPrefHeight() {
    	return textureRegion.getRegionHeight() + font.getCapHeight();
    }
    
    @Override
    public float getPrefWidth() {
        return Math.max(textureRegion.getRegionWidth(), FontResource.strWidth(font, label));
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	font.setColor(getQuantityColor());
    	batch.draw(
    	    textureRegion, 
    	    getX() + getWidth() / 2 - textureRegion.getRegionWidth() / 2, 
    	    getY() + font.getCapHeight()
    	);
        float priceStrLength = FontResource.strWidth(font, label);
        font.draw(batch, label, 
        		getX() + getWidth()/2 - priceStrLength/2, 
        		getY() + font.getCapHeight()
        );
    }

    public Color getQuantityColor() {
        return Color.WHITE;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }
    
	public String toString() {
		return "type[" + goodsType + "], label[" + label + "]";
	}
}
