package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductionInfo {
	private final List<Production> productions = new ArrayList<Production>();
	
    public void addProduction(Production production) {
        this.productions.add(production);
    }

	public int size() {
		return productions.size();
	}

	public void clear() {
		productions.clear();
	}
	
	public ProductionSummary productionSummaryForWorkers(Collection<Unit> workers) {
		ProductionSummary summary = new ProductionSummary();
        for (Production production : productions) {
    		production.sumProductionType(summary, workers);
        }
        return summary;
	}

	public ProductionSummary productionSummaryForWorkers(Collection<Unit> workers, Production.AttendedType attendedType) {
		ProductionSummary summary = new ProductionSummary();
        for (Production production : productions) {
    		production.sumProductionType(summary, workers, attendedType);
        }
        return summary;
	}
	
}
