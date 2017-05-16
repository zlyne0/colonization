package promitech.colonization.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.Validation;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;

public class QuantityGoodActor extends LabelGoodActor {
	
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
            Validation.instanceOf(payload.getObject(), DragAndDropPayload.class);
            
            DragAndDropPayload dadPayload = (DragAndDropPayload)payload.getObject();
            if (dadPayload.changeTransferGoodsQuantity) {
            	return true;
            }
            return targetContainer.canPutPayload(dadPayload.abstractGoods, x, y);
        }

        @Override
        public void drop(Source source, Payload payload, float x, float y, int pointer) {
            Validation.instanceOf(source.getActor(), LabelGoodActor.class);
            Validation.instanceOf(payload.getObject(), DragAndDropPayload.class);

            LabelGoodActor actor = (LabelGoodActor)source.getActor();
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
    
    protected int quantity = 0;
    
    public QuantityGoodActor(GoodsType goodsType, int quantity) {
    	super(goodsType);
    	this.setQuantity(quantity);
    }

    @Override
    public Color getQuantityColor() {
        if (quantity ==  0) {
            return Color.GRAY;
        }
    	return super.getQuantityColor();
    }
    
    public String toString() {
        return super.toString() + ", quantity[" + quantity + "]";
    }
    
    public void setQuantity(int quantity) {
    	this.quantity = quantity;
    	this.setLabel(Integer.toString(quantity));
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    AbstractGoods newAbstractGoods() {
    	return new AbstractGoods(goodsType.getId(), quantity);
    }
    
    public void increaseQuantity(AbstractGoods anAbstractGood) {
        quantity += anAbstractGood.getQuantity();
    	setLabel(Integer.toString(quantity));
    }

	public void decreaseQuantity(AbstractGoods anAbstractGood) {
        quantity -= anAbstractGood.getQuantity();
    	setLabel(Integer.toString(quantity));
	}

	public boolean equalsOrLess(AbstractGoods anAbstractGood) {
		return anAbstractGood.getQuantity() <= quantity;
	}

	public int getQuantity() {
		return quantity;
	}

}
