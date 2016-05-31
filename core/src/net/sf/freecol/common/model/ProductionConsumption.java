package net.sf.freecol.common.model;

 
//TODO: XXXX tak wyglada gdy w magazynie jest 5 furs,
// trzeba zobaczyc jak jest wypelniany baseProduction bo 18 to jakies z czapy
// trzeba pewnie bedzie dodac baseConsumption
//baseProduction: [[model.goods.furs, 18], [model.goods.coats, 9], ]
//realConsumption: [[model.goods.furs, -5], ]
//realProduction: [[model.goods.coats, 5], ]

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
