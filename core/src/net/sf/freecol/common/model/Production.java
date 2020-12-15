package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Production {
	
	public static final Production EMPTY_READONLY = new Production(false) {
		@Override
		public void applyModifiers(Collection<? extends ObjectWithFeatures> features) {
			throw new IllegalStateException("can not modify readonly object");
		}
		
		@Override
		public void applyTileImprovementsModifiers(Tile aTile) {
			throw new IllegalStateException("can not modify readonly object");
		}
	};
	
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

	public Production sumProductionForWorker() {
		return sumProductionForWorker(null);
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
	
	public java.util.Map.Entry<GoodsType, Integer> singleOutput() {
		if (output.isEmpty()) {
			return null;
		}
		// first
		return output.entrySet().iterator().next();
	}
	
	public void applyTileImprovementsModifiers(Tile aTile) {
		for (java.util.Map.Entry<GoodsType, Integer> outputEntry : output.entrySet()) {
			int quantity = aTile.applyTileProductionModifier(outputEntry.getKey().getId(), outputEntry.getValue());
			outputEntry.setValue(quantity);
		}
	}
	
	public void applyModifiers(Collection<? extends ObjectWithFeatures> features) {
		for (java.util.Map.Entry<GoodsType, Integer> outputEntry : output.entrySet()) {
			int quantity = outputEntry.getValue();
			String goodId = outputEntry.getKey().getId();
			
			for (ObjectWithFeatures ff : features) {
				quantity = (int)ff.applyModifier(goodId, quantity);
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

	public boolean outputTypesEquals(Production prod) {
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
	
	public Entry<GoodsType, Integer> singleFilteredOutputTypeEquals(MapIdEntities<GoodsType> goodsType) {
		for (java.util.Map.Entry<GoodsType, Integer> entry : output.entrySet()) {
			if (goodsType.containsId(entry.getKey())) {
				return entry;
			}
		}
		return null;
	}
	
	public boolean outputTypesEquals(String goodsTypeId) {
		if (this.output.size() != 1) {
			return false;
		}
		for (java.util.Map.Entry<GoodsType, Integer> entry : output.entrySet()) {
			if (!entry.getKey().equalsId(goodsTypeId)) {
				return false;
			}
		}
		return true;
	}

	public boolean outputTypesEquals(String goodsTypeId, int amount) {
		if (this.output.size() == 0) {
			return false;
		}
		boolean found = false;
		for (java.util.Map.Entry<GoodsType, Integer> entry : output.entrySet()) {
			if (entry.getKey().equalsId(goodsTypeId) && entry.getValue() == amount) {
				found = true;
			}
		}
		return found;
	}
	
	public boolean inputTypesEquals(GoodsType goodsType) {
		if (this.input.size() != 1) {
			return false;
		}
		for (Entry<GoodsType, Integer> entry : input.entrySet()) {
			if (entry.getKey().equalsId(goodsType)) {
				return true;
			}
		}
		return false;
	}
	
	public static class Xml extends XmlNodeParser<Production> {

		private static final String ATTR_VALUE = "value";
		private static final String ATTR_GOODS_TYPE = "goods-type";
		private static final String INPUT_ELEMENT = "input";
		private static final String OUTPUT_ELEMENT = "output";
		private static final String ATTR_UNATTENDED = "unattended";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			Production prod = new Production(attr.getBooleanAttribute(ATTR_UNATTENDED, false));
			nodeObject = prod;
		}

		@Override
		public void startWriteAttr(Production prod, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_UNATTENDED, prod.unattended);
			
			for (Entry<GoodsType, Integer> inputEntry : prod.input.entrySet()) {
				attr.xml.element(INPUT_ELEMENT);
				attr.set(ATTR_GOODS_TYPE, inputEntry.getKey().getId());
				attr.set(ATTR_VALUE, inputEntry.getValue().intValue());
				attr.xml.pop();
			}
			for (Entry<GoodsType, Integer> outputEntry : prod.output.entrySet()) {
				attr.xml.element(OUTPUT_ELEMENT);
				attr.set(ATTR_GOODS_TYPE, outputEntry.getKey().getId());
				attr.set(ATTR_VALUE, outputEntry.getValue().intValue());
				attr.xml.pop();
			}
		}
		
        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(OUTPUT_ELEMENT)) {
                String goodsTypeId = attr.getStrAttribute(ATTR_GOODS_TYPE);
                GoodsType goodType = Specification.instance.goodsTypes.getById(goodsTypeId);
                
                int amount = attr.getIntAttribute(ATTR_VALUE);
                nodeObject.addOutput(goodType, amount);
            }
            if (attr.isQNameEquals(INPUT_ELEMENT)) {
                String goodsTypeId = attr.getStrAttribute(ATTR_GOODS_TYPE);
                GoodsType goodType = Specification.instance.goodsTypes.getById(goodsTypeId);
                
                int amount = attr.getIntAttribute(ATTR_VALUE);
                nodeObject.addInput(goodType, amount);
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
