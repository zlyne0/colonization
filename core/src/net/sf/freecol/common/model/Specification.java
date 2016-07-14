package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.WithProbability;
import net.sf.freecol.common.model.specification.options.BooleanOption;
import net.sf.freecol.common.model.specification.options.IntegerOption;
import net.sf.freecol.common.model.specification.options.OptionGroup;
import net.sf.freecol.common.model.specification.options.StringOption;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Specification implements Identifiable {
	
    public static class Options {
        public final MapIdEntities<OptionGroup> allOptionGroup = new MapIdEntities<OptionGroup>();
        public final MapIdEntities<ObjectWithId> optionValues = new MapIdEntities<ObjectWithId>();
        
        private void clear() {
            optionGroupEntities.clear();
            allOptionGroup.clear();
            optionValues.clear();
        }
        
        public boolean getBoolean(String code) {
            BooleanOption option = (BooleanOption)optionValues.getById(code);
            return option.getValue();
        }

        public int getIntValue(String code) {
            IntegerOption option = (IntegerOption)optionValues.getById(code);
            return option.getValue();
        }

        public String getStringValue(String code) {
            StringOption option = (StringOption)optionValues.getById(code);
            return option.getValue();
        }
        
        public void flattenOptionsEntriesTree(String difficultyLevel) {
            for (OptionGroup og : optionGroupEntities.entities()) {
                addEntriesInOptionGroup(og);
            }
            if (difficultyLevel == null) {
                return;
            }
            System.out.println("setting specification options for " + difficultyLevel + " game difficulty level");
            
            OptionGroup diffLevelsOptions = optionGroupEntities.getById("difficultyLevels");
            OptionGroup choosenDifficultyLevelOptionGroup = diffLevelsOptions.optionsGroup.getById(difficultyLevel);
            addEntriesInOptionGroup(choosenDifficultyLevelOptionGroup);
        }
        
        private void addEntriesInOptionGroup(OptionGroup optionGroup) {
            allOptionGroup.add(optionGroup);
            
            for (ObjectWithId owi : optionGroup.abstractOptions.entities()) {
                optionValues.add(owi);
            }
            for (OptionGroup og : optionGroup.optionsGroup.entities()) {
                addEntriesInOptionGroup(og);
            }
        }
    }
    
	public static final Options options = new Options();
	public static final MapIdEntities<OptionGroup> optionGroupEntities = new MapIdEntities<OptionGroup>();
	
	public final MapIdEntities<TileType> tileTypes = new MapIdEntities<TileType>();
	public final MapIdEntities<TileImprovementType> tileImprovementTypes = new MapIdEntities<TileImprovementType>();
	public final MapIdEntities<UnitType> unitTypes = new MapIdEntities<UnitType>();
	public final MapIdEntities<UnitRole> unitRoles = new MapIdEntities<UnitRole>();
	public final MapIdEntities<ResourceType> resourceTypes = new MapIdEntities<ResourceType>();
    public final MapIdEntities<NationType> nationTypes = new MapIdEntities<NationType>();
    public final MapIdEntities<Nation> nations = new MapIdEntities<Nation>();
    public final MapIdEntities<GoodsType> goodsTypes = new MapIdEntities<GoodsType>();
    public final MapIdEntities<BuildingType> buildingTypes = new MapIdEntities<BuildingType>();
    public final MapIdEntities<FoundingFather> foundingFathers = new MapIdEntities<FoundingFather>();
    public final List<WithProbability<UnitType>> unitTypeRecruitProbabilities = new ArrayList<WithProbability<UnitType>>();

    public final MapIdEntities<Nation> europeanNations = new MapIdEntities<Nation>();
    public final java.util.Map<String,UnitType> expertUnitTypeByGoodType = new HashMap<String, UnitType>();
    
    public final MapIdEntities<UnitType> unitTypesTrainedInEurope = new SortedMapIdEntities<UnitType>(UnitType.UNIT_TYPE_PRICE_COMPARATOR);
    public final MapIdEntities<UnitType> unitTypesPurchasedInEurope = new SortedMapIdEntities<UnitType>(UnitType.UNIT_TYPE_PRICE_COMPARATOR);
    public final MapIdEntities<GoodsType> immigrationGoodsTypeList = new MapIdEntities<GoodsType>();
    
    private String difficultyLevel;
    
    public static final Specification instance = new Specification();
    
    private Specification() {
        options.clear();
    }
    
    @Override
    public String getId() {
        return "freecol";
    }
    
    public void updateReferences() {
        updateEuropeanNations();
        
        for (Nation nation : nations.entities()) {
            nation.updateReferences();
        }
        
        expertUnitTypeByGoodType.clear();
        for (UnitType unitType : unitTypes.entities()) {
        	unitType.updateDefaultRoleReference();
        	
            if (unitType.getExpertProductionForGoodsId() != null) {
                expertUnitTypeByGoodType.put(unitType.getExpertProductionForGoodsId(), unitType);
            }
            if (unitType.hasPrice()) {
            	if (unitType.getSkill() > 0) {
            		unitTypesTrainedInEurope.add(unitType);
            	} else if (!unitType.hasSkill()) {
            		unitTypesPurchasedInEurope.add(unitType);
            	}
            }
            if (unitType.isRecruitable()) {
            	unitTypeRecruitProbabilities.add(unitType.createRecruitProbability());
            }
        }
        
        immigrationGoodsTypeList.clear();
        for (GoodsType gt : goodsTypes.entities()) {
            if (gt.isImmigrationType()) {
                immigrationGoodsTypeList.add(gt);
            }
        }
    }

    private void updateEuropeanNations() {
        europeanNations.clear();
        for (Nation nation : nations.entities()) {
            if (nation.nationType.isEuropean()) {
                if (nation.isUnknownEnemy()) {
                    continue;
                }
                if (nation.nationType.isREF()) {
                    continue;
                }
                europeanNations.add(nation);
            }
        }
    }
    
    private void clear() {
    	tileTypes.clear();
    	tileImprovementTypes.clear();
    	unitTypes.clear();
    	unitRoles.clear();
    	resourceTypes.clear();
        nationTypes.clear();
        nations.clear();
        goodsTypes.clear();
        buildingTypes.clear();
        europeanNations.clear();
    }
    
	public static class Xml extends XmlNodeParser {
		public Xml() {
			addNodeForMapIdEntities("tile-types", "tileTypes", TileType.class);
            addNodeForMapIdEntities("resource-types", "resourceTypes", ResourceType.class);
            addNodeForMapIdEntities("tileimprovement-types", "tileImprovementTypes", TileImprovementType.class);
            addNodeForMapIdEntities("unit-types", "unitTypes", UnitType.class);
            addNodeForMapIdEntities("roles", "unitRoles", UnitRole.class);
            addNodeForMapIdEntities("european-nation-types", "nationTypes", EuropeanNationType.class);
            addNodeForMapIdEntities("indian-nation-types", "nationTypes", IndianNationType.class);
            addNodeForMapIdEntities("nations", "nations", Nation.class);
            addNodeForMapIdEntities("goods-types", "goodsTypes", GoodsType.class);
            addNodeForMapIdEntities("building-types", "buildingTypes", BuildingType.class);
            addNodeForMapIdEntities("founding-fathers", "foundingFathers", FoundingFather.class);
            addNodeForMapIdEntities("options", "optionGroupEntities", OptionGroup.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
		    Specification specification = Specification.instance;
		    specification.difficultyLevel = attr.getStrAttribute("difficultyLevel");
		    specification.clear();
		    nodeObject = specification;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		    if (qName.equals(getTagName())) {
		        options.flattenOptionsEntriesTree(Specification.instance.difficultyLevel);
		        Specification.instance.updateReferences();
		    }
		}
		
		@Override
		public String getTagName() {
			return "freecol-specification";
		}
	}
}

