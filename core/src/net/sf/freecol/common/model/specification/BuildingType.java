package net.sf.freecol.common.model.specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Modifier;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.UnitContainer;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class BuildingType extends ObjectWithFeatures {

    public static class Production {
        private boolean unattended = false;
        private Map<String,Integer> input = new HashMap<String, Integer>(2); 
        public Map<String,Integer> output = new HashMap<String, Integer>(2); 
        
        public Production(boolean unattended) {
            this.unattended = unattended;
        }

        public void addOutput(String goodsType, int amount) {
            this.output.put(goodsType, amount);
        }

        public void addInput(String goodsType, int amount) {
            this.input.put(goodsType, amount);
        }
    }
    
    /** The required population for an ordinary buildable. */
    private static final int DEFAULT_REQUIRED_POPULATION = 1;

    /**
     * The minimum population that a Colony needs in order to build
     * this type.
     */
    private int requiredPopulation = DEFAULT_REQUIRED_POPULATION;
    
    int level = 1;
    int workplaces = 3;
    String upgradesFromId;
    String upgradesToId;
    int minSkill = UNDEFINED;
    int maxSkill = INFINITY;
    private int upkeep = 0;
    private int priority = Consumer.BUILDING_PRIORITY;
    
    public final MapIdEntities<RequiredGoods> requiredGoods = new MapIdEntities<RequiredGoods>();
    public final List<Production> productions = new ArrayList<Production>();
    
    public BuildingType(String id) {
        super(id);
    }

    public void addProduction(Production production) {
        this.productions.add(production);
    }
    
    /**
     * Gets the reason why a given unit type can not be added to a
     * building of this type.
     *
     * @param unitType The <code>UnitType</code> to test.
     * @return The reason why adding would fail.
     */
    public UnitContainer.NoAddReason getNoAddReason(UnitType unitType) {
        return (workplaces == 0) ? UnitContainer.NoAddReason.CAPACITY_EXCEEDED
            : (!unitType.hasSkill()) ? UnitContainer.NoAddReason.MISSING_SKILL
            : (unitType.getSkill() < minSkill) ? UnitContainer.NoAddReason.MINIMUM_SKILL
            : (unitType.getSkill() > maxSkill) ? UnitContainer.NoAddReason.MAXIMUM_SKILL
            : UnitContainer.NoAddReason.NONE;
    }

	public int getWorkplaces() {
		return workplaces;
	}
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNodeForMapIdEntities("modifiers", Modifier.class);
            addNodeForMapIdEntities("abilities", Ability.class);
            addNodeForMapIdEntities("requiredGoods", RequiredGoods.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            BuildingType bt = new BuildingType(attr.getStrAttribute("id"));
            
            String parentIdStr = attr.getStrAttribute("extends");
            BuildingType parent = null;
            if (parentIdStr != null) {
                parent = game.specification.buildingTypes.getByIdOrNull(parentIdStr);
            }
            if (parent == null) {
                parent = bt;
            }
            
            String upgradesFromStr = attr.getStrAttribute("upgradesFrom");
            BuildingType upgradesFrom = game.specification.buildingTypes.getByIdOrNull(upgradesFromStr);
            if (upgradesFrom == null) {
                bt.level = 1;
            } else {
                upgradesFrom.upgradesToId = bt.id;
                bt.level = upgradesFrom.level + 1;
            }
            
            bt.workplaces = attr.getIntAttribute("workplaces", parent.workplaces);
            bt.minSkill = attr.getIntAttribute("minSkill", parent.minSkill);
            bt.maxSkill = attr.getIntAttribute("maxSkill", parent.maxSkill);
            bt.upkeep = attr.getIntAttribute("upkeep", parent.upkeep);
            bt.priority = attr.getIntAttribute("priority", parent.priority);
            
            if (parent != null) {
                if (attr.getStrAttribute("required-population") == null) {
                    bt.requiredPopulation = parent.requiredPopulation;
                }
            }
            
            bt.addFeatures(parent);
            nodeObject = bt;
        }

        Production production = null;
        
        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals("production")) {
                production = new Production(attr.getBooleanAttribute("unattended", false));
            }
            if (attr.isQNameEquals("output")) {
                String goodsType = attr.getStrAttribute("goods-type");
                int amount = attr.getIntAttribute("value");
                production.addOutput(goodsType, amount);
            }
            if (attr.isQNameEquals("input")) {
                String goodsType = attr.getStrAttribute("goods-type");
                int amount = attr.getIntAttribute("value");
                production.addInput(goodsType, amount);
            }
        }
        
        @Override
        public void endReadChildren(String qName) {
            if ("production".equals(qName)) {
                if (production != null) {
                    ((BuildingType)nodeObject).addProduction(production);
                }
                production = null;
            }
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "building-type";
        }
    }

}
