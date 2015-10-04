package net.sf.freecol.common.model;

import java.util.Collection;
import java.util.HashMap;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Production implements Identifiable {
	
	public static enum AttendedType {
		UNATTENDED, ATTENDED, BOTH;
	};
	
    private boolean unattended = false;
    private java.util.Map<String,Integer> input = new HashMap<String, Integer>(2); 
    private java.util.Map<String,Integer> output = new HashMap<String, Integer>(2); 
    
    public Production(boolean unattended) {
        this.unattended = unattended;
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

	public void sumProductionType(ProductionSummary summary, Collection<Unit> workers, AttendedType attendedType) {
		for (java.util.Map.Entry<String, Integer> outputEntry : output.entrySet()) {
			String goodsId = outputEntry.getKey();
			
			Integer goodProductionInitValue = outputEntry.getValue();
			if (0 == goodProductionInitValue) {
				continue;
			}
			int goodQuantity = 0;
			
			if (AttendedType.ATTENDED.equals(attendedType)) {
				if (this.unattended == false) {
					goodQuantity += goodProductionInitValue;
				}
			}
			if (AttendedType.UNATTENDED.equals(attendedType)) {
				if (this.unattended == true) {
					goodQuantity += goodProductionInitValue;
				}
			}
			if (AttendedType.BOTH.equals(attendedType)) {
				if (unattended && workers.isEmpty()) {
					goodQuantity += goodProductionInitValue;
				} 
				if (!unattended && !workers.isEmpty()) {
					for (Unit worker : workers) {
						goodQuantity += (int)worker.unitType.applyModifier(goodsId, goodProductionInitValue);
					}
				}
			}
			if (goodQuantity != 0) {
				summary.addGoods(goodsId, goodQuantity);
			}
		}
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
