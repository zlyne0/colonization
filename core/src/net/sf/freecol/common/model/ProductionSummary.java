package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.AbstractGoods;

public class ProductionSummary {
    public static final int CARRIER_SLOT_MAX_QUANTITY = 100;
    
	private ObjectIntMap<String> goods = new ObjectIntMap<String>();

    public boolean hasNotGood(String goodsId, int goodQuantity) {
        return goods.get(goodsId, 0) < goodQuantity;
    }

    public int getQuantity(String goodsId) {
        return goods.get(goodsId, 0);
    }
    
	public void makeEmpty() {
	    goods.clear();
	}
	
	public boolean isEmpty() {
		return goods.size == 0;
	}
	
    public boolean isNotEmpty() {
        return goods.size != 0;
    }

	public boolean isSingleGoods() {
		return goods.size == 1;
	}

	public int size() {
		return goods.size;
	}
	
	public Entry<String> singleEntry() {
		return goods.entries().next();
	}

    public ProductionSummary cloneGoods() {
        ProductionSummary ps = new ProductionSummary();
        ps.goods.putAll(goods);
        return ps;
    }
	
	public void addGoods(String goodsId, int goodQuantity) {
	    if (goodQuantity != 0) {
	        goods.getAndIncrement(goodsId, 0, goodQuantity);
	    }
	}

    public void addGoods(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() != 0) {
            goods.getAndIncrement(anAbstractGoods.getTypeId(), 0, anAbstractGoods.getQuantity());
        }
    }
	
    public void addGoods(ProductionSummary ps) {
        for (Entry<String> psEntry : goods.entries()) {
            goods.getAndIncrement(psEntry.key, 0, psEntry.value);
        }
    }

    public void decreaseToZero(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() != 0) {
            int i = goods.get(anAbstractGoods.getTypeId(), 0) - anAbstractGoods.getQuantity();
            if (i < 0) {
                i = 0;
            }
            goods.put(anAbstractGoods.getTypeId(), i);
        }
    }
    
    public List<AbstractGoods> slotedGoods() {
        List<AbstractGoods> goodsList = new ArrayList<AbstractGoods>();
        for (Entry<String> gsEntry : goods.entries()) {
            int quantity = gsEntry.value;
            while (quantity > 0) {
                if (quantity > CARRIER_SLOT_MAX_QUANTITY) {
                    goodsList.add(new AbstractGoods(gsEntry.key, CARRIER_SLOT_MAX_QUANTITY));
                    quantity -= CARRIER_SLOT_MAX_QUANTITY;
                } else {
                    goodsList.add(new AbstractGoods(gsEntry.key, quantity));
                    quantity -= quantity;
                }
            }
        }
        return goodsList;
    }

    public int allCargoSlots() {
        int cargoSpaceTaken = 0; 
        for (Entry<String> gsEntry : goods.entries()) {
            cargoSpaceTaken += slotsForQuantity(gsEntry.value);
        }
        return cargoSpaceTaken;
    }
    
    public int allCargoSlotsWithAdditionalCargo(AbstractGoods additionalCargo) {
        int slots = 0;
        int goodQuantity;
        boolean foundAdditionalCargoInContainer = false;
        for (Entry<String> gsEntry : goods.entries()) {
            goodQuantity = gsEntry.value;
            if (additionalCargo.getTypeId().equals(gsEntry.key)) {
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
    
    public boolean hasMoreOrEquals(ProductionSummary base) {
        for (Entry<String> baseEntry : base.entries()) {
            if (goods.get(baseEntry.key, 0) < baseEntry.value) {
                return false;
            }
        }
        return true;
    }
    
	public Entries<String> entries() {
		return goods.entries();
	}
	
	public void applyTileImprovementsModifiers(Tile tile) {
		for (Entry<String> entry : goods.entries()) {
			int quantity = entry.value;
			for (TileImprovement ti : tile.getTileImprovements()) {
				quantity = (int)ti.type.applyModifier(entry.key, quantity);
			}
			for (TileResource tileResource : tile.getTileResources()) {
				quantity = (int)tileResource.getResourceType().applyModifier(entry.key, quantity);
			}
			goods.put(entry.key, quantity);
		}
	}

	public void addProductionFromColonyTile(ColonyTile ct) {
	    ct.productionInfo.addProductionToSummary(this, ct.getWorker());
	}
	
	public void applyModifier(int productionBonus) {
	    for (Entry<String> entry : goods.entries()) {
            goods.getAndIncrement(entry.key, 0, productionBonus);
	    }
	}
	
	public String toString() {
		String st = "[";
		for (Entry<String> entry : goods.entries()) {
			st += "[" + entry.key + ", " + entry.value + "], ";
		}
		st += "]";
		return st;
	}
}
