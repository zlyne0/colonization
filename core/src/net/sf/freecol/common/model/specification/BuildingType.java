package net.sf.freecol.common.model.specification;

import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Modifier;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionInfo;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitContainer;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class BuildingType extends ObjectWithFeatures {

    /** The required population for an ordinary buildable. */
    private static final int DEFAULT_REQUIRED_POPULATION = 1;

    /**
     * The minimum population that a Colony needs in order to build
     * this type.
     */
    private int requiredPopulation = DEFAULT_REQUIRED_POPULATION;
    
    int level = 1;
    int workplaces = 3;
    private String upgradesFromId;
    private String upgradesToId;
    private BuildingType upgradesFrom;
    
    int minSkill = UNDEFINED;
    int maxSkill = INFINITY;
    private int upkeep = 0;
    private int priority = Consumer.BUILDING_PRIORITY;
    
    public final MapIdEntities<RequiredGoods> requiredGoods = new MapIdEntities<RequiredGoods>();
    public final ProductionInfo productionInfo = new ProductionInfo();
    
    public BuildingType(String id) {
        super(id);
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

    public boolean isTheSameRoot(BuildingType bt) {
    	BuildingType thisRoot = this.rootType();
    	BuildingType btRoot = bt.rootType();
    	return thisRoot.equalsId(btRoot);
    }
    
    public BuildingType rootType() {
    	BuildingType root = this;
    	while (root.upgradesFrom != null) {
    		root = root.upgradesFrom;
    	}
    	return root;
    }

	public boolean isRoot() {
		return upgradesFrom == null;
	}
    
	public boolean canUpgradeTo(BuildingType buildingType) {
		return this.equalsId(buildingType.upgradesFrom); 
	}
    
	public int getWorkplaces() {
		return workplaces;
	}

	public int getRequiredPopulation() {
		return requiredPopulation;
	}

    public static class Xml extends XmlNodeParser {
        public Xml() {
        	addNode(Production.class, new ObjectFromNodeSetter<BuildingType, Production>() {
				@Override
				public void set(BuildingType target, Production entity) {
					target.productionInfo.addProduction(entity);
				}
			});
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNode("required-ability", Ability.class, "requiredAbilities");
            addNodeForMapIdEntities("requiredGoods", RequiredGoods.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            BuildingType bt = new BuildingType(attr.getStrAttribute("id"));
            
            String parentIdStr = attr.getStrAttribute("extends");
            BuildingType parent = null;
            if (parentIdStr != null) {
                parent = Specification.instance.buildingTypes.getByIdOrNull(parentIdStr);
            }
            if (parent == null) {
                parent = bt;
            }
            
            String upgradesFromStr = attr.getStrAttribute("upgradesFrom");
            BuildingType upgradesFrom = Specification.instance.buildingTypes.getByIdOrNull(upgradesFromStr);
            if (upgradesFrom == null) {
                bt.level = 1;
            } else {
                upgradesFrom.upgradesToId = bt.id;
                bt.upgradesFrom = upgradesFrom;
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
            
            bt.addFeaturesAndOverwriteExisted(parent);
            nodeObject = bt;
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
