package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductionInfo {
	private final List<Production> productions = new ArrayList<Production>();
    private final List<Production> unattendedProductions = new ArrayList<Production>();
    private final List<Production> attendedProductions = new ArrayList<Production>();
	
    public void addProduction(Production production) {
        this.productions.add(production);
        if (production.isUnattended()) {
            this.unattendedProductions.add(production);
        } else {
            this.attendedProductions.add(production);
        }
    }

	public int size() {
		return productions.size();
	}

	public void clear() {
		productions.clear();
		unattendedProductions.clear();
		attendedProductions.clear();
	}

	public ProductionInfo productionSummaryForWorker(Unit worker) {
		ProductionInfo prodInfo = new ProductionInfo();
		if (worker != null) {
			for (Production production : attendedProductions) {
				prodInfo.addProduction(production.sumProductionForWorker(worker));
			}
		} else {
			for (Production production : unattendedProductions) {
				prodInfo.addProduction(production.sumProductionForWorker());
			}
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

	public void applyModifiers(Collection<? extends ObjectWithFeatures> features) {
		for (Production p : productions) {
			p.applyModifiers(features);
		}
	}
	
	public void applyTileImprovementsModifiers(Tile aTile) {
		for (Production p : productions) {
			p.applyTileImprovementsModifiers(aTile);
		}
	}

	public void writeMaxProductionFromAllowed(ProductionInfo allowedProductions, ProductionInfo sourceProduction) {
		Production productionTypes = allowedProductions.maxProduction();
		clear();
		for (Production p : sourceProduction.productions) {
			if (p.outputTypesEquals(productionTypes)) {
				addProduction(new Production(p)); 
			}
		}
	}
	
	private Production maxProduction() {
		Production maxProduction = null;
		for (Production p : productions) {
			if (maxProduction == null || p.isProductMoreThen(maxProduction)) {
				maxProduction = p;
			}
		}
		if (maxProduction == null) {
			throw new IllegalStateException("can not find max produtions in " + this.toString());
		}
		return maxProduction;
	}

    public List<Production> getUnattendedProductions() {
        return unattendedProductions;
    }

    public List<Production> getAttendedProductions() {
        return attendedProductions;
    }
}
