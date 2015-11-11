package net.sf.freecol.common.model;

public class ProductionConsumption {
    public final ProductionSummary baseProduction = new ProductionSummary();
    public final ProductionSummary baseConsumption = new ProductionSummary();
    
    public final ProductionSummary realProduction = new ProductionSummary();
    //public final ProductionSummary realConsumption = new ProductionSummary();
    
    public String toString() {
        return "\nbaseProduction: " + baseProduction + 
                "\nbaseConsumption: " + baseConsumption + 
                "\nrealProduction: " + realProduction;// +
                //"\nrealConsumption: " + realConsumption;
    }

	public void add(ProductionConsumption ps) {
		baseProduction.addGoods(ps.baseProduction);
		baseConsumption.addGoods(ps.baseConsumption);
		realProduction.addGoods(ps.realProduction);
		//realConsumption.addGoods(ps.realConsumption);
	}
	
	public void clean() {
		baseProduction.makeEmpty();
		baseConsumption.makeEmpty();
		realProduction.makeEmpty();
	}
}
