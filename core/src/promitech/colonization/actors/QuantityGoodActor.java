package promitech.colonization.actors;

import com.badlogic.gdx.graphics.Color;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;

public class QuantityGoodActor extends LabelGoodActor {
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

}
