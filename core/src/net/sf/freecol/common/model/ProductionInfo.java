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

    public List<Production> getUnattendedProductions() {
        return unattendedProductions;
    }

    public List<Production> getAttendedProductions() {
        return attendedProductions;
    }
}
