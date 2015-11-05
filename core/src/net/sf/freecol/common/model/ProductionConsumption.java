package net.sf.freecol.common.model;

public class ProductionConsumption {
    public final ProductionSummary baseProduction = new ProductionSummary();
    public final ProductionSummary baseConsumption = new ProductionSummary();
    
    public final ProductionSummary realProduction = new ProductionSummary();
    public final ProductionSummary realConsumption = new ProductionSummary();
    
    public String toString() {
        return "\nbaseProduction: " + baseProduction + 
                "\nbaseConsumption: " + baseConsumption + 
                "\nrealProduction: " + realProduction +
                "\nrealConsumption: " + realConsumption;
    }
}
