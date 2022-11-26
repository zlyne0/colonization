package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import net.sf.freecol.common.model.specification.GoodsType;

public class ProductionInfo {
	public final List<Production> productions = new ArrayList<Production>();
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
	
	public int unattendedProductions(String goodsTypeId) {
		int sum = 0;
		for (Production production : unattendedProductions) {
			for (Entry<GoodsType, Integer> entry : production.outputEntries()) {
				if (entry.getKey().equalsId(goodsTypeId)) {
					sum += entry.getValue();
				}
			}
		}
		return sum;
	}

	public int attendedProductions(String goodsTypeId) {
		int sum = 0;
		for (Production production : attendedProductions) {
			for (Entry<GoodsType, Integer> entry : production.outputEntries()) {
				if (entry.getKey().equalsId(goodsTypeId)) {
					sum += entry.getValue();
				}
			}
		}
		return sum;
	}

	public void addUnattendedProductionToSummary(ProductionSummary ps) {
		for (Production production : unattendedProductions) {
			for (Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
				ps.addGoods(outputEntry.getKey().getId(), outputEntry.getValue());
			}
		}
	}
	
	public String toString() {
		StringBuilder st = new StringBuilder();
		for (Production p : productions) {
			if (productions.size() > 1) {
				st.append("\n");
			}
			st.append(p);
		}
		return st.toString();
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
			if (p.isOutputTypesEquals(productionTypes)) {
				addProduction(new Production(p)); 
			}
		}
	}
	
	public void initProductionType(ProductionInfo sourceProduction, GoodsType goodsType) {
		clear();
		for (Production p : sourceProduction.productions) {
			if (p.isOutputTypesEquals(goodsType.getId())) {
				addProduction(new Production(p));
			}
		}
	}
	
	public void initProduction(Production sourceProduction) {
		clear();
		addProduction(sourceProduction);
	}
	
	private Production maxProduction() {
		Production maxProduction = null;
		for (Production p : productions) {
			if (maxProduction == null || p.isProductMoreThen(maxProduction)) {
				maxProduction = p;
			}
		}
		if (maxProduction == null) {
			return Production.EMPTY_READONLY;
		}
		return maxProduction;
	}

	public Entry<GoodsType, Integer> singleFilteredUnattendedProduction(MapIdEntities<GoodsType> goodsType) {
		if (unattendedProductions.isEmpty()) { 
			return null;
		}
		Production production = unattendedProductions.get(0);
		return production.singleFilteredOutputTypeEquals(goodsType);
	}

	public Production firstAttendentProduction(GoodsType goodsType) {
		for (Production prod : attendedProductions) {
			if (prod.isOutputTypesEquals(goodsType.getId())) {
				return prod;
			}
		}
		return Production.EMPTY_READONLY;
	}

    public List<Production> getUnattendedProductions() {
        return unattendedProductions;
    }

    public List<Production> getAttendedProductions() {
        return attendedProductions;
    }

	public Production findAttendedProductionContainsGoodsTypes(Production production) {
		for (Production attendedProduction : attendedProductions) {
			if (attendedProduction.isOutputTypesEquals(production)) {
				return attendedProduction;
			}
		}
		return null;
	}
}
