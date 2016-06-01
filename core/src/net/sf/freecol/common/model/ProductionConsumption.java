package net.sf.freecol.common.model;

public class ProductionConsumption {
    public final ProductionSummary baseConsumption = new ProductionSummary();
    public final ProductionSummary baseProduction = new ProductionSummary();
    public final ProductionSummary realConsumption = new ProductionSummary();
    public final ProductionSummary realProduction = new ProductionSummary();
    
    public String toString() {
        return "\nbaseConsumption: " + baseConsumption +
                "\nbaseProduction: " + baseProduction +
        		"\nrealConsumption: " + realConsumption +
                "\nrealProduction: " + realProduction;
    }

	public void add(ProductionConsumption ps) {
	    baseConsumption.addGoods(ps.baseConsumption);
		baseProduction.addGoods(ps.baseProduction);
		realConsumption.addGoods(ps.realConsumption);
		realProduction.addGoods(ps.realProduction);
	}
	
	public void clean() {
		baseProduction.makeEmpty();
		realConsumption.makeEmpty();
		realProduction.makeEmpty();
	}
}
