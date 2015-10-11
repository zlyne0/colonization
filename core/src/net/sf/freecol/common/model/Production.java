package net.sf.freecol.common.model;

import java.util.Collection;
import java.util.HashMap;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Production implements Identifiable {
    private boolean unattended = false;
    private java.util.Map<String,Integer> input = new HashMap<String, Integer>(2); 
    private java.util.Map<String,Integer> output = new HashMap<String, Integer>(2); 
    
    public Production(boolean unattended) {
        this.unattended = unattended;
    }

	public Production(Production p) {
		this.unattended = p.unattended;
		this.input.putAll(p.input);
		this.output.putAll(p.output);
	}

	@Override
	public String getId() {
		throw new IllegalStateException("production has no id");
	}
    
    private void addOutput(String goodsType, int amount) {
        this.output.put(goodsType, amount);
    }

    private void addInput(String goodsType, int amount) {
        this.input.put(goodsType, amount);
    }

	public void sumProductionType(ProductionSummary summary, Collection<Unit> workers) {
		for (java.util.Map.Entry<String, Integer> outputEntry : output.entrySet()) {
			String goodsId = outputEntry.getKey();
			
			Integer goodProductionInitValue = outputEntry.getValue();
			if (0 == goodProductionInitValue) {
				continue;
			}
			int goodQuantity = 0;
			
			if (unattended && workers.isEmpty()) {
				goodQuantity += goodProductionInitValue;
			} 
			if (!unattended && !workers.isEmpty()) {
				for (Unit worker : workers) {
					goodQuantity += (int)worker.unitType.applyModifier(goodsId, goodProductionInitValue);
				}
			}
			if (goodQuantity != 0) {
				summary.addGoods(goodsId, goodQuantity);
			}
		}
	}

	public Production sumProductionForWorker(Unit worker) {
		Production prod = new Production(this.unattended);
		for (java.util.Map.Entry<String, Integer> outputEntry : output.entrySet()) {
			String goodsId = outputEntry.getKey();
			int goodQuantity = outputEntry.getValue();
			
			if (unattended) {
			} else {
				goodQuantity = (int)worker.unitType.applyModifier(goodsId, goodQuantity);
			}
			prod.addOutput(goodsId, goodQuantity);
		}
		return prod;
	}
	
	public void applyTileImprovementsModifiers(Tile aTile) {
		for (java.util.Map.Entry<String, Integer> outputEntry : output.entrySet()) {
			int quantity = outputEntry.getValue();
			String goodId = outputEntry.getKey();

			for (TileImprovement ti : aTile.getTileImprovements()) {
				quantity = (int)ti.type.applyModifier(goodId, quantity);
			}
			for (TileResource tileResource : aTile.getTileResources()) {
				quantity = (int)tileResource.getResourceType().applyModifier(goodId, quantity);
			}
			outputEntry.setValue(quantity);
		}
	}
	
	public boolean isProductMoreThen(Production maxProduction) {
		int sumThis = sumProduction(output);
		int sumArg = sumProduction(maxProduction.output);
		
		if (sumThis >= sumArg) {
			return true;
		} else {
			return false;
		}
	}
	
	private int sumProduction(java.util.Map<String,Integer> m) {
		int sum = 0;
		for (java.util.Map.Entry<String, Integer> entry : m.entrySet()) {
			sum += entry.getValue();
		}
		return sum;
	}
	
	public String toString() {
		String st = "";
		if (unattended) {
			st += "unattended ";
		}
		st += "input:[" + mapToString(input) + "]";
		st += ", output:[" + mapToString(output) + "]";
		return st;
	}
	
	private String mapToString(java.util.Map<String,Integer> mm) {
		String st = "";
		for (java.util.Map.Entry<String, Integer> entry : mm.entrySet()) {
			if (st.length() > 0) {
				st += ", ";
			}
			String goodsId = entry.getKey();
			st += goodsId + ": " + entry.getValue();
		}
		return st;
	}

	public boolean isUnattended() {
		return unattended;
	}

	public boolean outputEquals(Production prod) {
		if (this.output.size() != prod.output.size()) {
			return false;
		}
		for (java.util.Map.Entry<String, Integer> entry : output.entrySet()) {
			if (!prod.output.containsKey(entry.getKey())) {
				return false;
			}
		}
		return true;
	}
	
	public static class Xml extends XmlNodeParser {

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Production prod = new Production(attr.getBooleanAttribute("unattended", false));
			nodeObject = prod;
		}

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals("output")) {
                String goodsType = attr.getStrAttribute("goods-type");
                int amount = attr.getIntAttribute("value");
                ((Production)nodeObject).addOutput(goodsType, amount);
            }
            if (attr.isQNameEquals("input")) {
                String goodsType = attr.getStrAttribute("goods-type");
                int amount = attr.getIntAttribute("value");
                ((Production)nodeObject).addInput(goodsType, amount);
            }
        }
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "production";
		}
		
	}
}
