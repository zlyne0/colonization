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

	public void determineProductionConsumption(ProductionConsumption prodCons, Collection<Unit> workers, ProductionSummary warehouse) {
	    // assumption that for unattended or attended production there is only one production object
        for (Production production : productions) {
            production.sumProductionType(prodCons, workers, warehouse);
        }
	}
	
    public void addProductionToSummary(ProductionSummary summary, Unit worker) {
        for (Production production : productions) {
            production.sumProductionType(summary, worker);
        }
    }
	
    public void addProductionToSummary(ProductionSummary summary, Collection<Unit> workers) {
        for (Production production : productions) {
            production.sumProductionType(summary, workers);
        }
    }
    
	public ProductionInfo productionSummaryForWorker(Unit worker) {
		ProductionInfo prodInfo = new ProductionInfo();
		for (Production production : productions) {
			prodInfo.addProduction(production.sumProductionForWorker(worker));
		}
		return prodInfo;
	}
	
	public String toString() {
		String st = "";
		for (Production p : productions) {
			st += p + System.lineSeparator();
		}
		return st;
	}

	public void applyTileImprovementsModifiers(Tile aTile) {
		for (Production p : productions) {
			p.applyTileImprovementsModifiers(aTile);
		}
	}

	public ProductionInfo determineMaxProductionType(ProductionInfo initTypes, ProductionInfo maxProdType) {
		Production maxProduction = null;
		for (Production p : productions) {
			if (p.isUnattended()) {
				continue;
			}
			if (maxProduction == null || p.isProductMoreThen(maxProduction)) {
				maxProduction = p;
			}
		}
		if (maxProduction == null) {
			throw new IllegalStateException("can not find max produtions in " + this.toString());
		}
		
		maxProdType.clear();
		for (Production p : initTypes.productions) {
			if (p.outputEquals(maxProduction)) {
				maxProdType.addProduction(new Production(p)); 
			}
		}
		return maxProdType;
	}

}
