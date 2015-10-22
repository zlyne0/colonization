package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class GoodsContainer extends ObjectWithId {

    public static final int CARRIER_SLOT_MAX_QUANTITY = 100;
    
    private final java.util.Map<String,Integer> goods = new HashMap<String, Integer>();
    private int cargoSpaceTaken = 0;
    
    public GoodsContainer(String id) {
        super(id);
    }

    public int goodsAmount(GoodsType type) {
        Integer amount = goods.get(type.id);
        if (amount == null) {
            return 0;
        }
        return amount;
    }

    public void increaseGoodsQuantity(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() == 0) {
            return;
        }
        Integer actualQuantity = goods.get(anAbstractGoods.getTypeId());
        if (actualQuantity == null) {
            goods.put(anAbstractGoods.getTypeId(), anAbstractGoods.getQuantity());
        } else {
            goods.put(anAbstractGoods.getTypeId(), actualQuantity + anAbstractGoods.getQuantity());
        }
        updateTakenCargoSlots();
    }

	public void decreaseGoodsQuantity(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() == 0) {
            return;
        }
        Integer actualQuantity = goods.get(anAbstractGoods.getTypeId());
        if (actualQuantity != null) {
            if (actualQuantity > anAbstractGoods.getQuantity()) {
                goods.put(anAbstractGoods.getTypeId(), actualQuantity - anAbstractGoods.getQuantity());
            } else {
                goods.put(anAbstractGoods.getTypeId(), 0);
            }
        }
        updateTakenCargoSlots();
    }
    
    public int size() {
        return goods.size();
    }

	public int getCargoSpaceTaken() {
		return cargoSpaceTaken;
	}
    
    public List<AbstractGoods> carrierGoods() {
        List<AbstractGoods> goodsList = new ArrayList<AbstractGoods>();
        
        for (Entry<String, Integer> entrySet : goods.entrySet()) {
            int quantity = entrySet.getValue();
            while (quantity > 0) {
                if (quantity > CARRIER_SLOT_MAX_QUANTITY) {
                    goodsList.add(new AbstractGoods(entrySet.getKey(), CARRIER_SLOT_MAX_QUANTITY));
                    quantity -= CARRIER_SLOT_MAX_QUANTITY;
                } else {
                    goodsList.add(new AbstractGoods(entrySet.getKey(), quantity));
                    quantity -= quantity;
                }
            }
        }
        return goodsList;
    }

    private void updateTakenCargoSlots() {
    	cargoSpaceTaken = 0;
        for (Entry<String, Integer> entrySet : goods.entrySet()) {
            cargoSpaceTaken += slotsForQuantity(entrySet.getValue());
        }
    }

    private int slotsForQuantity(int quantity) {
        if (quantity <= 0) {
            return 0;
        }
        if (quantity % CARRIER_SLOT_MAX_QUANTITY > 0) {
            return (quantity / CARRIER_SLOT_MAX_QUANTITY) + 1; 
        } else {
            return (quantity / CARRIER_SLOT_MAX_QUANTITY); 
        }
    }
    
    public int takenCargoSlotsWithAdditionalCargo(AbstractGoods additionalCargo) {
        int slots = 0;
        int goodQuantity;
        boolean foundAdditionalCargoInContainer = false;
        for (Entry<String, Integer> entrySet : goods.entrySet()) {
            goodQuantity = entrySet.getValue();
            if (additionalCargo.getTypeId().equals(entrySet.getKey())) {
                foundAdditionalCargoInContainer = true;
                goodQuantity += additionalCargo.getQuantity();
            }
            slots += slotsForQuantity(goodQuantity);
        }
        if (!foundAdditionalCargoInContainer) {
            slots += additionalCargo.takenCargoSlot();
        }
        return slots;
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
                
                ((GoodsContainer)nodeObject).goods.put(typeStr, amount);
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
