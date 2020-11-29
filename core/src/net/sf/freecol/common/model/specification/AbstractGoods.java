package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.ProductionSummary;

public class AbstractGoods implements Identifiable {
	private int quantity;
	private String typeId;

	public AbstractGoods(String typeId, int quantity) {
	    this.quantity = quantity;
	    this.typeId = typeId;
	}

	public boolean isEquals(String typeId, int quantity) {
		return this.typeId.equals(typeId) && this.quantity == quantity;
	}

	@Override
	public String getId() {
		return typeId;
	}
	
	public int takenCargoSlot() {
		if (quantity <= 0) {
			throw new IllegalStateException("should not move zero or less quantity");
		}
		if (quantity % ProductionSummary.CARRIER_SLOT_MAX_QUANTITY > 0) {
			return quantity / ProductionSummary.CARRIER_SLOT_MAX_QUANTITY + 1;
		} else {
			return quantity / ProductionSummary.CARRIER_SLOT_MAX_QUANTITY;
		}
	}
	
	public int amountToFillSlot() {
		return ProductionSummary.CARRIER_SLOT_MAX_QUANTITY - quantity;
	}
	
	public void makeEmpty() {
		quantity = 0;
	}
	
	public boolean isNotEmpty() {
		return quantity > 0;
	}
	
	public int getQuantity() {
		return quantity;
	}

    public String getTypeId() {
        return typeId;
    }
    
    public String toString() {
    	return "type[" + typeId + "], quantity[" + quantity + "]";
    }

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
