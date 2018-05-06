package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsContainer {

    private final ProductionSummary goods = new ProductionSummary();
    private int cargoSpaceTaken = 0;
    
    public GoodsContainer() {
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
    
    public boolean hasGoodsQuantity(ProductionSummary g) {
    	return goods.hasMoreOrEquals(g);
    }

    public boolean hasGoodsQuantity(MapIdEntities<RequiredGoods> requiredGoods) {
    	return goods.hasMoreOrEquals(requiredGoods);
    }
    
	public void increaseGoodsQuantity(ProductionSummary ps) {
		goods.addGoods(ps);
	}
    
    public void increaseGoodsQuantity(GoodsType goodsType, int quantity) {
    	increaseGoodsQuantity(goodsType.getId(), quantity);
    }
    
    public void increaseGoodsQuantity(String goodsId, int quantity) {
    	goods.addGoods(goodsId, quantity);
    	updateTakenCargoSlots();
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
    
    public List<AbstractGoods> carrierGoods() {
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
