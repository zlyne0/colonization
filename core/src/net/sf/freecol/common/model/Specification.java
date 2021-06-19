package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.WithProbability;
import net.sf.freecol.common.model.specification.options.BooleanOption;
import net.sf.freecol.common.model.specification.options.IntegerOption;
import net.sf.freecol.common.model.specification.options.OptionGroup;
import net.sf.freecol.common.model.specification.options.PercentageOption;
import net.sf.freecol.common.model.specification.options.RangeOption;
import net.sf.freecol.common.model.specification.options.StringOption;
import net.sf.freecol.common.model.specification.options.UnitListOption;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Specification {
	
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

        public int getIntValue(String code, int defaultValue) {
        	ObjectWithId option = optionValues.getByIdOrNull(code);
        	if (option == null) {
        		return defaultValue;
        	}
        	return getIntValue(option);
        }

        public int getIntValue(String code) {
        	ObjectWithId option = optionValues.getById(code);
        	return getIntValue(option);
        }
        
        private int getIntValue(ObjectWithId option) {
        	if (option instanceof RangeOption) {
        		return ((RangeOption)option).getValueAsInt();
        	}
        	if (option instanceof PercentageOption) {
        		return ((PercentageOption)option).getValue();
        	}
        	return ((IntegerOption)option).getValue();
        }

        public String getStringValue(String code) {
            StringOption option = (StringOption)optionValues.getById(code);
            return option.getValue();
        }
        
        public int getStringValueAsInt(String code) {
            StringOption option = (StringOption)optionValues.getById(code);
            return Integer.parseInt(option.getValue());
        }
        
        public UnitListOption getUnitListOption(String code) {
        	return (UnitListOption)optionValues.getById(code);
        }
        
        public void flattenOptionsEntriesTree(String difficultyLevel) {
            if (difficultyLevel == null) {
                return;
            }
            for (OptionGroup og : optionGroupEntities.entities()) {
            	addEntriesInOptionGroup(og);
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
	public static final MapIdEntities<OptionGroup> optionGroupEntities = MapIdEntities.linkedMapIdEntities();
	
	public final MapIdEntities<Modifier> modifiers = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<ResourceType> resourceTypes = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<GoodsType> goodsTypes = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<TileType> tileTypes = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<TileImprovementType> tileImprovementTypes = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<UnitType> unitTypes = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<UnitRole> unitRoles = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<BuildingType> buildingTypes = MapIdEntities.linkedMapIdEntities();
    public final MapIdEntities<NationType> nationTypes = MapIdEntities.linkedMapIdEntities();
    
    public final MapIdEntities<Nation> nations = new MapIdEntities<Nation>();
    public final MapIdEntities<FoundingFather> foundingFathers = new MapIdEntities<FoundingFather>();
    public final List<WithProbability<UnitType>> unitTypeRecruitProbabilities = new ArrayList<WithProbability<UnitType>>();

    public final MapIdEntities<Nation> europeanNations = new MapIdEntities<Nation>();
    public final java.util.Map<String,UnitType> expertUnitTypeByGoodType = new HashMap<String, UnitType>();
    public UnitType freeColonistUnitType;
    
    public final MapIdEntities<UnitType> unitTypesTrainedInEurope = new SortedMapIdEntities<UnitType>(UnitType.UNIT_TYPE_PRICE_COMPARATOR);
    public final MapIdEntities<UnitType> unitTypesPurchasedInEurope = new SortedMapIdEntities<UnitType>(UnitType.UNIT_TYPE_PRICE_COMPARATOR);
    public final MapIdEntities<GoodsType> immigrationGoodsTypeList = new MapIdEntities<GoodsType>();
    
    public final List<UnitType> navalTypes = new ArrayList<UnitType>();
    public final List<UnitType> bombardTypes = new ArrayList<UnitType>();
    public final List<UnitType> landTypes = new ArrayList<UnitType>();
    public final List<UnitType> mercenaryTypes = new ArrayList<UnitType>();
    public final List<UnitRole> militaryRoles = new ArrayList<UnitRole>();
    public final List<UnitRole> nativeMilitaryRoles = new ArrayList<UnitRole>();
    public final MapIdEntities<GoodsType> goodsTypeToScoreByPrice = new MapIdEntities<GoodsType>();
    public final List<GoodsType> foodsGoodsTypes = new ArrayList<GoodsType>();
    
    private String difficultyLevel;
    
    public static final Specification instance = new Specification();
    
    private Specification() {
        options.clear();
    }
    
    private void updateReferences() {
        updateEuropeanNations();
        
        for (Nation nation : nations.entities()) {
            nation.updateReferences();
        }

        freeColonistUnitType = unitTypes.getById(UnitType.FREE_COLONIST);
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
			updateGoodsTypeAsBuildingMaterials(unitType.requiredGoods);
        }
        
        immigrationGoodsTypeList.clear();
        for (GoodsType gt : goodsTypes.entities()) {
            if (gt.isImmigrationType()) {
                immigrationGoodsTypeList.add(gt);
            }
        }
        
        for (UnitRole ur : unitRoles.entities()) {
			if (ur.isOffensive()) {
				militaryRoles.add(ur);
			}
			updateGoodsTypeAsBuildingMaterials(ur.requiredGoods);
		}
        for (BuildingType buildingType : buildingTypes.entities()) {
        	buildingType.calculateGoodsOutputChainLevel();
			updateGoodsTypeAsBuildingMaterials(buildingType.requiredGoods);
		}
        createSupportUnitLists();
        updateModifiersFromDifficultyLevel();
        determineNativeMilitaryRoles();
        createGoodsTypeToScoreByPrice();
        
        foodsGoodsTypes.clear();
        foodsGoodsTypes.add(goodsTypes.getById(GoodsType.FISH));
        foodsGoodsTypes.add(goodsTypes.getById(GoodsType.GRAIN));
    }

    private void createGoodsTypeToScoreByPrice() {
    	goodsTypeToScoreByPrice.clear();
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.sugar"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.tobacco"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.cotton"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.furs"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.ore"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.silver"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.rum"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.cigars"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.cloth"));
    	goodsTypeToScoreByPrice.add(goodsTypes.getById("model.goods.coats"));
    }
    
    private void determineNativeMilitaryRoles() {
		UnitType brave = Specification.instance.unitTypes.getById(UnitType.BRAVE);
		NationType nativeType = nationTypes.getById("model.nationType.apache");

		nativeMilitaryRoles.clear();
		for (UnitRole unitRole : Specification.instance.militaryRoles) {
			if (unitRole.isAvailableTo(nativeType, brave)) {
				nativeMilitaryRoles.add(unitRole);
			}
		}
		Collections.sort(nativeMilitaryRoles, UnitRole.OFFENCE_POWER_COMPARATOR);
	}

	private void updateModifiersFromDifficultyLevel() {
    	Modifier shipTradePenalty = modifiers.getById(Modifier.SHIP_TRADE_PENALTY);
    	shipTradePenalty.setValue(options.getIntValue(GameOptions.SHIP_TRADE_PENALTY, -30));
	}

	private void updateGoodsTypeAsBuildingMaterials(MapIdEntities<RequiredGoods> requiredGoodsList) {
		for (RequiredGoods requiredGoods : requiredGoodsList.entities()) {
			requiredGoods.goodsType.setBuildingMaterial(true);
		}
    }
    
	private void updateEuropeanNations() {
        europeanNations.clear();
        for (Nation nation : nations.entities()) {
            if (nation.nationType.isEuropean()) {
                if (nation.nationType.isREF()) {
                    continue;
                }
                europeanNations.add(nation);
            }
        }
    }
    
    public List<UnitType> getRoyalLandUnitTypes() {
        List<UnitType> types = new ArrayList<UnitType>();
        for (UnitType ut : unitTypes.entities()) {
    		if (!ut.isNaval() && ut.hasAbility(Ability.REF_UNIT)) {
    			types.add(ut);
    		}
        }
        return types;
    }
    
    public List<UnitType> getRoyalNavyUnitTypes() {
    	List<UnitType> types = new ArrayList<UnitType>();
    	for (UnitType ut : unitTypes.entities()) {
    		if (ut.isNaval() && ut.hasAbility(Ability.REF_UNIT)) {
    			types.add(ut);
    		}
    	}
    	return types;
    }

    public List<UnitType> getUnitTypesWithAbility(String abilityCode) {
    	List<UnitType> types = new ArrayList<UnitType>();
    	for (UnitType ut : unitTypes.entities()) {
    		if (ut.hasAbility(abilityCode)) {
    			types.add(ut);
    		}
    	}
    	return types;
    }

    public UnitType expertUnitTypeForGoodsType(GoodsType goodsType) {
        return expertUnitTypeForGoodsType(goodsType, freeColonistUnitType);
    }

    public UnitType expertUnitTypeForGoodsType(GoodsType goodsType, UnitType defaultUnitType) {
    	UnitType unitType = expertUnitTypeByGoodType.get(goodsType.getId());
    	if (unitType == null) {
    		return defaultUnitType;
    	}
    	return unitType;
    }
    
    private void createSupportUnitLists() {
		for (UnitType unitType : unitTypes.entities()) {
			if (unitType.hasAbility(Ability.SUPPORT_UNIT)) {
				if (unitType.hasAbility(Ability.NAVAL_UNIT)) {
					navalTypes.add(unitType);
				} else if (unitType.hasAbility(Ability.BOMBARD)) {
					bombardTypes.add(unitType);
				} else if (unitType.hasAbility(Ability.CAN_BE_EQUIPPED)) {
					landTypes.add(unitType);
				}
			}
			if (unitType.hasAbility(Ability.MERCENARY_UNIT)) {
				mercenaryTypes.add(unitType);
			}
		}
	}

    private void clearReferences() {
    	modifiers.clear();
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
        
        unitTypesTrainedInEurope.clear();
        unitTypesPurchasedInEurope.clear();
        immigrationGoodsTypeList.clear();
        navalTypes.clear();
        bombardTypes.clear();
        landTypes.clear();
        mercenaryTypes.clear();
        militaryRoles.clear();
        goodsTypeToScoreByPrice.clear();
        foodsGoodsTypes.clear();
    }
    
	public static class Xml extends XmlNodeParser<Specification> {
		private static final String DIFFICULTY_LEVEL = "difficultyLevel";

		public Xml() {
			addNodeForMapIdEntities("modifiers", "modifiers", Modifier.class);
			
            addNodeForMapIdEntities("resource-types", "resourceTypes", ResourceType.class);
            addNodeForMapIdEntities("goods-types", "goodsTypes", GoodsType.class);
            addNodeForMapIdEntities("tile-types", "tileTypes", TileType.class);
            addNodeForMapIdEntities("tileimprovement-types", "tileImprovementTypes", TileImprovementType.class);
            addNodeForMapIdEntities("unit-types", "unitTypes", UnitType.class);
            addNodeForMapIdEntities("roles", "unitRoles", UnitRole.class);
            addNodeForMapIdEntities("building-types", "buildingTypes", BuildingType.class);
            addNodeForMapIdEntities("european-nation-types", "nationTypes", EuropeanNationType.class);            
            addNodeForMapIdEntities("indian-nation-types", "nationTypes", IndianNationType.class);
            addNodeForMapIdEntities("nations", "nations", Nation.class);
            addNodeForMapIdEntities("founding-fathers", "foundingFathers", FoundingFather.class);
            addNodeForMapIdEntities("options", "optionGroupEntities", OptionGroup.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
		    Specification specification = Specification.instance;
		    specification.difficultyLevel = attr.getStrAttribute(DIFFICULTY_LEVEL);
		    specification.clearReferences();
		    nodeObject = specification;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		    if (qName.equals(getTagName())) {
		    	Specification.instance.updateOptionsFromDifficultyLevel();
		    }
		}
		
		@Override
		public void startWriteAttr(Specification node, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(DIFFICULTY_LEVEL, node.difficultyLevel);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "freecol-specification";
		}
	}

	public void updateOptionsFromDifficultyLevel() {
		updateOptionsFromDifficultyLevel(Specification.instance.difficultyLevel);
	}
	
	public void updateOptionsFromDifficultyLevel(String difficultyLevel) {
		Specification.instance.difficultyLevel = difficultyLevel;
        options.flattenOptionsEntriesTree(difficultyLevel);
        Specification.instance.updateReferences();
	}

	
}

