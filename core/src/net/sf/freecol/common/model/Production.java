package net.sf.freecol.common.model;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Production implements Identifiable {
    private boolean unattended = false;
    private final java.util.Map<GoodsType,Integer> input = new HashMap<GoodsType, Integer>(2); 
    private final java.util.Map<GoodsType,Integer> output = new HashMap<GoodsType, Integer>(2); 
    
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
    
	public Set<Entry<GoodsType, Integer>> inputEntries() {
		return input.entrySet();
	}

	public Set<Entry<GoodsType, Integer>> outputEntries() {
		return output.entrySet();
	}
	
    private void addOutput(GoodsType goodsType, int amount) {
        this.output.put(goodsType, amount);
    }

    private void addInput(GoodsType goodsType, int amount) {
        this.input.put(goodsType, amount);
    }

	public Production sumProductionForWorker(Unit worker) {
		Production prod = new Production(this.unattended);
		for (java.util.Map.Entry<GoodsType, Integer> outputEntry : output.entrySet()) {
			String goodsId = outputEntry.getKey().getId();
			int goodQuantity = outputEntry.getValue();
			
			if (unattended) {
			} else {
				goodQuantity = (int)worker.unitType.applyModifier(goodsId, goodQuantity);
			}
			prod.addOutput(outputEntry.getKey(), goodQuantity);
		}
		return prod;
	}
	
	public void applyTileImprovementsModifiers(Tile aTile) {
		for (java.util.Map.Entry<GoodsType, Integer> outputEntry : output.entrySet()) {
			int quantity = outputEntry.getValue();
			String goodId = outputEntry.getKey().getId();

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
	
	private int sumProduction(java.util.Map<GoodsType,Integer> m) {
		int sum = 0;
		for (java.util.Map.Entry<GoodsType, Integer> entry : m.entrySet()) {
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
	
	private String mapToString(java.util.Map<GoodsType,Integer> mm) {
		String st = "";
		for (java.util.Map.Entry<GoodsType, Integer> entry : mm.entrySet()) {
			if (st.length() > 0) {
				st += ", ";
			}
			String goodsId = entry.getKey().getId();
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
		for (java.util.Map.Entry<GoodsType, Integer> entry : output.entrySet()) {
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
                String goodsTypeId = attr.getStrAttribute("goods-type");
                GoodsType goodType = Specification.instance.goodsTypes.getById(goodsTypeId);
                
                int amount = attr.getIntAttribute("value");
                ((Production)nodeObject).addOutput(goodType, amount);
            }
            if (attr.isQNameEquals("input")) {
                String goodsTypeId = attr.getStrAttribute("goods-type");
                GoodsType goodType = Specification.instance.goodsTypes.getById(goodsTypeId);
                
                int amount = attr.getIntAttribute("value");
                ((Production)nodeObject).addInput(goodType, amount);
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
