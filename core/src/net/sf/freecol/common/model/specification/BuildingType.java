package net.sf.freecol.common.model.specification;

import java.io.IOException;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionInfo;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitContainer;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.UnitContainer.NoAddReason;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class BuildingType extends BuildableType {

	public static final String TOWN_HALL = "model.building.townHall";
	public static final String DOCKS = "model.building.docks";
	public static final String WAREHOUSE = "model.building.warehouse";
	public static final String WAREHOUSE_EXPANSION = "model.building.warehouseExpansion";

    int level = 1;
    int workplaces = 3;
    private int goodsOutputChainLevel = 0;
    private String upgradesFromId;
    private String upgradesToId;
    private BuildingType upgradesFrom;
    
    int minSkill = UNDEFINED;
    int maxSkill = INFINITY;
    private int upkeep = 0;
    private int priority = Consumer.BUILDING_PRIORITY;
    
    public final ProductionInfo productionInfo = new ProductionInfo();
    
    public BuildingType(String id) {
        super(id);
    }

	public boolean isBuildingType() {
		return true;
	}

	public int getLevel() {
		return level;
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

    public NoAddReason addWorkerToBuildingReason(UnitType unitType, int workersSpaceTaken) {
		if (!unitType.isPerson()) {
			return UnitContainer.NoAddReason.WRONG_TYPE;
		}
		UnitContainer.NoAddReason reason = getNoAddReason(unitType);
		if (reason == NoAddReason.NONE 
			&& unitType.getSpaceTaken() + workersSpaceTaken > getWorkplaces()) {
			return UnitContainer.NoAddReason.CAPACITY_EXCEEDED;
		}
		return reason;
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

	public BuildingType getUpgradesFrom() {
		return upgradesFrom;
	}

    public boolean isAutomaticBuild() {
        return doesNotNeedGoodsToBuild() && isRoot();
    }

    public void calculateGoodsOutputChainLevel() {
    	goodsOutputChainLevel = 0;
    	for (Production production : productionInfo.getAttendedProductions()) {
    		for (Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
				int l = outputEntry.getKey().calculateMadeFromLevel();
				if (l > goodsOutputChainLevel) {
					goodsOutputChainLevel = l;
				}
			}
		}
    }
    
	public Production productionForInput(GoodsType inputGoodsType) {
		for (Production production : productionInfo.getAttendedProductions()) {
			if (production.inputTypesEquals(inputGoodsType)) {
				return production;
			}
		}
		return null;
	}
    
	public int getGoodsOutputChainLevel() {
		return goodsOutputChainLevel;
	}

	public boolean hasAttendedOutputGoods(GoodsType goodsType) {
		for (Production production : productionInfo.getAttendedProductions()) {
			if (production.containsOutputGoods(goodsType)) {
				return true;
			}
		}
		return false;
	}

    public static class Xml extends XmlNodeParser<BuildingType> {
        private static final String ATTR_PRIORITY = "priority";
		private static final String ATTR_UPKEEP = "upkeep";
		private static final String ATTR_MAX_SKILL = "maxSkill";
		private static final String ATTR_MIN_SKILL = "minSkill";
		private static final String ATTR_WORKPLACES = "workplaces";
		private static final String ATTR_UPGRADES_FROM = "upgradesFrom";
		private static final String ATTR_EXTENDS = "extends";

		public Xml() {
        	BuildableType.Xml.abstractAddNodes(this);
        	
        	addNode(Production.class, new ObjectFromNodeSetter<BuildingType, Production>() {
				@Override
				public void set(BuildingType target, Production entity) {
					target.productionInfo.addProduction(entity);
				}
				@Override
				public void generateXml(BuildingType source, ChildObject2XmlCustomeHandler<Production> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.productionInfo.productions);
				}
			});
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            BuildingType bt = new BuildingType(attr.getStrAttribute(ATTR_ID));
            BuildableType.Xml.abstractStartElement(attr, bt);
            
            String parentIdStr = attr.getStrAttribute(ATTR_EXTENDS);
            BuildingType parent = null;
            if (parentIdStr != null) {
                parent = Specification.instance.buildingTypes.getByIdOrNull(parentIdStr);
            }
            if (parent == null) {
                parent = bt;
            }
            
            String upgradesFromStr = attr.getStrAttribute(ATTR_UPGRADES_FROM);
            BuildingType upgradesFrom = Specification.instance.buildingTypes.getByIdOrNull(upgradesFromStr);
            if (upgradesFrom == null) {
                bt.level = 1;
            } else {
                upgradesFrom.upgradesToId = bt.id;
                bt.upgradesFrom = upgradesFrom;
                bt.level = upgradesFrom.level + 1;
            }
            
            bt.workplaces = attr.getIntAttribute(ATTR_WORKPLACES, parent.workplaces);
            bt.minSkill = attr.getIntAttribute(ATTR_MIN_SKILL, parent.minSkill);
            bt.maxSkill = attr.getIntAttribute(ATTR_MAX_SKILL, parent.maxSkill);
            bt.upkeep = attr.getIntAttribute(ATTR_UPKEEP, parent.upkeep);
            bt.priority = attr.getIntAttribute(ATTR_PRIORITY, parent.priority);
            
            if (parent != null) {
                if (attr.getStrAttribute(BuildableType.Xml.TAG_REQUIRED_POPULATION) == null) {
                    bt.requiredPopulation = parent.requiredPopulation;
                }
            }
            
            bt.addFeaturesAndOverwriteExisted(parent);
            nodeObject = bt;
        }
        
        @Override
        public void startWriteAttr(BuildingType bt, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(bt);
        	
        	if (bt.upgradesFrom != null) {
        		attr.set(ATTR_UPGRADES_FROM, bt.upgradesFrom.getId());
        	}
        	attr.set(ATTR_WORKPLACES, bt.workplaces);
        	attr.set(ATTR_MIN_SKILL, bt.minSkill, UNDEFINED);
        	attr.set(ATTR_MAX_SKILL, bt.maxSkill, INFINITY);
        	attr.set(ATTR_UPKEEP, bt.upkeep);
        	attr.set(ATTR_PRIORITY, bt.priority);
        	
            BuildableType.Xml.abstractStartWriteAttr(bt, attr);
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
