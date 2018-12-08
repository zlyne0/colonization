package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class IndianSettlementMissionary implements UnitLocation {

    public Unit unit;

    public boolean isMissionaryExpert() {
    	return unit.isExpert();
    }
    
    public int lineOfSight(SettlementType type) {
    	if (Specification.options.getBoolean(GameOptions.ENHANCED_MISSIONARIES)) {
    		return (int)unit.getOwner().getFeatures().applyModifier(Modifier.LINE_OF_SIGHT_BONUS, type.getVisibleRadius());
    	}
    	return 1;
    }
    
    public static class Xml extends XmlNodeParser<IndianSettlementMissionary> {

        public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<IndianSettlementMissionary, Unit>() {
                @Override
                public void set(IndianSettlementMissionary target, Unit entity) {
                    target.unit = entity;
                }

                @Override
                public void generateXml(IndianSettlementMissionary source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
                    xmlGenerator.generateXml(source.unit);
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            nodeObject = new IndianSettlementMissionary();
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "missionary";
        }
    }

	@Override
	public void addUnit(Unit unit) {
		this.unit = unit;
	}

	@Override
	public void removeUnit(Unit unit) {
		this.unit = null;
	}

	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		throw new IllegalStateException("Not implemented. No use case.");
	}

	@Override
	public boolean canAutoLoadUnit() {
		return false;
	}

	@Override
	public boolean canAutoUnloadUnits() {
		return false;
	}
}
