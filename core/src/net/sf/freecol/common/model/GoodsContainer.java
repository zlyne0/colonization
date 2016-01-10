package net.sf.freecol.common.model;

import java.util.List;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsContainer extends ObjectWithId {

    private final ProductionSummary goods = new ProductionSummary();
    private int cargoSpaceTaken = 0;
    
    public GoodsContainer(String id) {
        super(id);
    }

	public int goodsAmount(String id) {
        return goods.getQuantity(id);
	}
    
    public int goodsAmount(GoodsType type) {
        return goods.getQuantity(type.id);
    }

    public boolean hasGoodsQuantity(ProductionSummary g) {
    	return goods.hasMoreOrEquals(g);
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

	public void decreaseGoodsQuantity(ProductionSummary required) {
		goods.decreaseGoods(required);
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

    public ProductionSummary cloneGoods() {
        return goods.cloneGoods();
    }
    
    public static class Xml extends XmlNodeParser {
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            GoodsContainer goodsContainer = new GoodsContainer(id);
            
            nodeObject = goodsContainer;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals("goods")) {
                String typeStr = attr.getStrAttribute("type");
                int amount = attr.getIntAttribute("amount", 0);
                
                ((GoodsContainer)nodeObject).goods.addGoods(typeStr, amount);
                ((GoodsContainer)nodeObject).updateTakenCargoSlots();
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
