package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entries;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.colonyproduction.GoodsCollection;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;

public class ProductionSummary {
    public static final int CARRIER_SLOT_MAX_QUANTITY = 100;

	public static int slotsForQuantity(int quantity) {
		if (quantity <= 0) {
			return 0;
		}
		if (quantity % CARRIER_SLOT_MAX_QUANTITY > 0) {
			return (quantity / CARRIER_SLOT_MAX_QUANTITY) + 1;
		} else {
			return (quantity / CARRIER_SLOT_MAX_QUANTITY);
		}
	}

	private ObjectIntMap<String> goods = new ObjectIntMap<String>();

	public static final ProductionSummary EMPTY = new ProductionSummary();
	
	public int getQuantity(GoodsType goodsType) {
		return goods.get(goodsType.getId(), 0);
	}
	
    public int getQuantity(String goodsId) {
        return goods.get(goodsId, 0);
    }
    
    public void clear() {
    	goods.clear();
    }
    
	public void makeEmpty() {
	    goods.clear();
	}
	
	public boolean isEmpty() {
		if (goods.size == 0) {
			return true;
		}
		for (Entry<String> goodsEntry : goods) {
			if (goodsEntry.value > 0) {
				return false;
			}
		}
		return true;
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
	
	public void cloneTo(ProductionSummary ps) {
		ps.goods.putAll(goods);
	}
    
	public void addGoods(String goodsId, int goodQuantity) {
	    if (goodQuantity != 0) {
	        goods.getAndIncrement(goodsId, 0, goodQuantity);
	    }
	}

	public void addGoods(Collection<? extends AbstractGoods> anAbstractGoods) {
		for (AbstractGoods abstractGoods : anAbstractGoods) {
			addGoods(abstractGoods);
		}
	}
	
    public void addGoods(AbstractGoods anAbstractGoods) {
        if (anAbstractGoods.getQuantity() != 0) {
            goods.getAndIncrement(anAbstractGoods.getTypeId(), 0, anAbstractGoods.getQuantity());
        }
    }
    
    public void addGoods(ProductionSummary ps) {
        for (Entry<String> psEntry : ps.goods.entries()) {
            goods.getAndIncrement(psEntry.key, 0, psEntry.value);
        }
    }

	public void addGoods(GoodsCollection goodsCollection) {
		for (Entry<GoodsType> goodsTypeEntry : goodsCollection) {
			goods.getAndIncrement(goodsTypeEntry.key.getId(), 0, goodsTypeEntry.value);
		}
	}

	public void decreaseGoods(ProductionSummary goodsCollection) {
		for (Entry<String> g : goodsCollection.goods.entries()) {
			goods.getAndIncrement(g.key, 0, -g.value);
		}
	}

	public void decreaseGoods(GoodsCollection goodsCollection) {
		for (Entry<GoodsType> goodsTypeEntry : goodsCollection) {
			goods.getAndIncrement(goodsTypeEntry.key.getId(), 0, -goodsTypeEntry.value);
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
    
	public void decreaseAllToZero() {
		for (Entry<String> gsEntry : goods.entries()) {
			goods.put(gsEntry.key, 0);
		}
	}
    
    public void setZero(String goodsId) {
    	goods.put(goodsId, 0);
    }
    
    public void decreaseToMinZero(ProductionSummary goodsCollection) {
        for (Entry<String> gEntry : goodsCollection.goods.entries()) {
            int q = goods.get(gEntry.key, 0);
            if (q > 0) {
                int quantity = Math.min(gEntry.value, q);
                goods.getAndIncrement(gEntry.key, 0, -quantity);
            }
        }
    }
    
    public boolean decreaseIfHas(String goodsId, int quantity) {
    	int q = goods.get(goodsId, 0);
    	if (q < quantity) {
    		return false;
    	}
    	goods.getAndIncrement(goodsId, 0, -quantity);
    	return true;
    }

	public void decrease(String goodsId, int quantity) {
	    if (quantity != 0) {
	        goods.getAndIncrement(goodsId, 0, -quantity);
	    }
	}
    
    public void decreaseToRatio(String goodsTypeId, double ratio) {
        int amount = goods.get(goodsTypeId, 0);
        if (amount != 0) {
            goods.put(goodsTypeId, (int)Math.round(amount * ratio));
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
    
    public int maxGoodsAmountToFillFreeSlots(String goodsId, final int cargoSlots) {
		int usedCargoSlots = 0;
    	for (Entry<String> gsEntry : goods.entries()) {
			usedCargoSlots += slotsForQuantity(gsEntry.value);
    	}
		int goodsIdAmount = goods.get(goodsId, 0);
		if (goodsIdAmount == 0 || goodsIdAmount % CARRIER_SLOT_MAX_QUANTITY == 0) {
			return (cargoSlots - usedCargoSlots) * CARRIER_SLOT_MAX_QUANTITY;
		}
		return (cargoSlots - usedCargoSlots) * CARRIER_SLOT_MAX_QUANTITY + (CARRIER_SLOT_MAX_QUANTITY - (goodsIdAmount % CARRIER_SLOT_MAX_QUANTITY));
    }
    
    public boolean hasMoreOrEquals(ProductionSummary base) {
        for (Entry<String> baseEntry : base.entries()) {
            if (goods.get(baseEntry.key, 0) < baseEntry.value) {
                return false;
            }
        }
        return true;
    }

	public boolean hasMoreOrEquals(GoodsCollection base) {
		for (Entry<GoodsType> baseEntry : base) {
			if (goods.get(baseEntry.key.getId(), 0) < baseEntry.value) {
				return false;
			}
		}
		return true;
	}

    /**
     * Check that has goods in base amount or in base amount ratio/
     * Ratio from 0 to 1
     * Example: base 12, goods 6, ratio 1,  return false
     *          base 12, goods 6, ratio 0.5 return true
     */
    public boolean hasPart(ProductionSummary base, float ratio) {
    	for (Entry<String> baseEntry : base.entries()) {
    		int baseAmount = (int)(baseEntry.value * ratio);
    		if (goods.get(baseEntry.key, 0) < baseAmount) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean hasPart(String goodsTypeId, int amount, float ratio) {
        int baseAmount = (int)(amount * ratio);
        return goods.get(goodsTypeId, 0) >= baseAmount;
    }
    
	public void put(String goodsId, int quantity) {
		goods.put(goodsId, quantity);
	}
	
	public Entries<String> entries() {
		return goods.entries();
	}
	
	public void applyModifiers(ObjectWithFeatures modifiers) {
		for (Entry<String> entry : goods.entries()) {
			int quantity = entry.value;
			quantity = (int)modifiers.applyModifier(entry.key, quantity);
			goods.put(entry.key, quantity);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goods == null) ? 0 : goods.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductionSummary other = (ProductionSummary) obj;
		if (goods == null) {
			if (other.goods != null)
				return false;
		} else if (!goods.equals(other.goods))
			return false;
		return true;
	}
}
