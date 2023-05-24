package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.colonyproduction.GoodsCollection;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.Goods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsContainer {

    private final ProductionSummary goods = new ProductionSummary();
    private int cargoSpaceTaken = 0;
    
    public GoodsContainer() {
    }

    public void transferGoods(AbstractGoods goods, GoodsContainer toContainer) {
    	decreaseGoodsQuantity(goods);
    	toContainer.increaseGoodsQuantity(goods);
    }
    
    public void transferGoods(Goods goods, GoodsContainer toContainer) {
    	decreaseGoodsQuantity(goods.getType(), goods.getAmount());
    	toContainer.increaseGoodsQuantity(goods.getType(), goods.getAmount());
    }
    
    public void transferGoods(GoodsType goodsType, int quantity, GoodsContainer toContainer) {
    	decreaseGoodsQuantity(goodsType, quantity);
    	toContainer.increaseGoodsQuantity(goodsType, quantity);
    }

    public void transferGoods(String goodsTypeId, int amount, GoodsContainer toContainer) {
    	decreaseGoodsQuantity(goodsTypeId, amount);
    	toContainer.increaseGoodsQuantity(goodsTypeId, amount);
    }
    
    public void transferGoods(String goodsTypeId, GoodsContainer toContainer) {
    	int amount = goodsAmount(goodsTypeId);
    	decreaseGoodsQuantity(goodsTypeId, amount);
    	toContainer.increaseGoodsQuantity(goodsTypeId, amount);
    }
    
    public Entries<String> entries() {
    	return goods.entries();
    }
    
	public int goodsAmount(String id) {
        return goods.getQuantity(id);
	}
    
    public int goodsAmount(GoodsType type) {
        return goods.getQuantity(type.getId());
    }

    public boolean hasGoodsQuantity(String goodsId, int amount) {
    	return goods.getQuantity(goodsId) >= amount;
    }

    public boolean hasGoodsQuantity(GoodsType type, int amount) {
    	return goods.getQuantity(type.getId()) >= amount;
    }
    
    public boolean hasGoodsQuantity(ProductionSummary g) {
    	return goods.hasMoreOrEquals(g);
    }

    public boolean hasMoreOrEquals(GoodsCollection goodsCollection) {
        return goods.hasMoreOrEquals(goodsCollection);
    }

    public boolean hasPart(String goodsTypeId, int amount, float ratio) {
        return goods.hasPart(goodsTypeId, amount, ratio);
    }
    
    public boolean hasPart(ProductionSummary ps, float ratio) {
    	return goods.hasPart(ps, ratio);
    }
    
	public void increaseGoodsQuantity(ProductionSummary ps) {
		goods.addGoods(ps);
	}
    
    public void increaseGoodsQuantity(GoodsType goodsType, int quantity) {
    	increaseGoodsQuantity(goodsType.getId(), quantity);
    }
    
    public GoodsContainer increaseGoodsQuantity(String goodsId, int quantity) {
    	goods.addGoods(goodsId, quantity);
    	updateTakenCargoSlots();
    	return this;
    }
    
    public void increaseGoodsQuantity(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() == 0) {
            return;
        }
        goods.addGoods(anAbstractGoods);
        updateTakenCargoSlots();
    }

	public void decreaseGoodsQuantity(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() == 0) {
            return;
        }
        goods.decreaseToZero(anAbstractGoods);
        updateTakenCargoSlots();
    }

	public void decreaseGoodsQuantity(GoodsType gt, int quantity) {
	    if (quantity == 0) {
	        return;
	    }
	    goods.decrease(gt.getId(), quantity);
        updateTakenCargoSlots();
	}

	public void decreaseGoodsQuantity(String goodsTypeId, int quantity) {
        if (quantity == 0) {
            return;
        }
        goods.decrease(goodsTypeId, quantity);
        updateTakenCargoSlots();
    }
	
	public void decreaseGoodsQuantity(ProductionSummary required) {
		goods.decreaseGoods(required);
        updateTakenCargoSlots();
	}

	public void decreaseGoodsQuantity(GoodsCollection goodsCollection) {
		goods.decreaseGoods(goodsCollection);
        updateTakenCargoSlots();
	}

    public void decreaseGoodsToMinZero(ProductionSummary goods) {
        goods.decreaseToMinZero(goods);
        updateTakenCargoSlots();
    }

    public void decreaseGoodsToMinZero(String goodsTypeId, int quantity) {
        goods.decreaseToMinZero(goodsTypeId, quantity);
        updateTakenCargoSlots();
    }

	public void decreaseToZero(String goodsTypeId) {
		goods.setZero(goodsTypeId);
		updateTakenCargoSlots();
	}
	
	public void decreaseAllToZero() {
		goods.decreaseAllToZero();
		updateTakenCargoSlots();
	}
    
    public int size() {
        return goods.size();
    }

	public int getCargoSpaceTaken() {
		return cargoSpaceTaken;
	}
    
    public List<AbstractGoods> slotedGoods() {
        return goods.slotedGoods();
    }

    private void updateTakenCargoSlots() {
    	cargoSpaceTaken = goods.allCargoSlots();
    }
    
    public int takenCargoSlotsWithAdditionalCargo(AbstractGoods additionalCargo) {
        return goods.allCargoSlotsWithAdditionalCargo(additionalCargo);
    }

    public int maxGoodsAmountToFillFreeSlots(String goodsId, int freeSlots) {
    	return goods.maxGoodsAmountToFillFreeSlots(goodsId, freeSlots);
    }
    
    public ProductionSummary cloneGoods() {
        return goods.cloneGoods();
    }

	public void cloneTo(GoodsContainer gc) {
		gc.decreaseAllToZero();
		goods.cloneTo(gc.goods);
		gc.updateTakenCargoSlots();
	}
	
	@Override
	public String toString() {
		return goods.toString();
	}

	public void clear() {
        goods.clear();
        updateTakenCargoSlots();
    }

    public boolean isEmpty() {
        return goods.isEmpty();
    }

    public void removeAbove(int capacity) {
        goods.removeAbove(capacity);
        updateTakenCargoSlots();
    }

    public static class Xml extends XmlNodeParser<GoodsContainer> {
        private static final String ATTR_AMOUNT = "amount";
		private static final String ATTR_TYPE = "type";
		private static final String ELEMENT_GOODS = "goods";

		@Override
        public void startElement(XmlNodeAttributes attr) {
            GoodsContainer goodsContainer = new GoodsContainer();
            nodeObject = goodsContainer;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(ELEMENT_GOODS)) {
                String typeStr = attr.getStrAttribute(ATTR_TYPE);
                int amount = attr.getIntAttribute(ATTR_AMOUNT, 0);
                
                nodeObject.goods.addGoods(typeStr, amount);
                nodeObject.updateTakenCargoSlots();
            }
        }
        
        @Override
        public void startWriteAttr(GoodsContainer gc, XmlNodeAttributesWriter attr) throws IOException {
        	for (Entry<String> goodEntry : gc.goods.entries()) {
				attr.xml.element(ELEMENT_GOODS);
				attr.set(ATTR_TYPE, goodEntry.key);
				attr.set(ATTR_AMOUNT, goodEntry.value);
				attr.xml.pop();
			}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "goodsContainer";
        }
    }
}
