package net.sf.freecol.common.model;

public class ProductionConsumption {
    public final ProductionSummary baseProduction = new ProductionSummary();
    public final ProductionSummary realProduction = new ProductionSummary();
    
    public String toString() {
        return "\nbaseProduction: " + baseProduction + 
                "\nrealProduction: " + realProduction;
    }

	public void add(ProductionConsumption ps) {
		baseProduction.addGoods(ps.baseProduction);
		realProduction.addGoods(ps.realProduction);
	}
	
	public void clean() {
		baseProduction.makeEmpty();
		realProduction.makeEmpty();
	}
}
