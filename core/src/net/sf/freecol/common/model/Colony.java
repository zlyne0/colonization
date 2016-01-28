package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.TransactionEffectOnMarket;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.RequiredGoods;
import promitech.colonization.Direction;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Colony extends Settlement {
    public static final int LIBERTY_PER_REBEL = 200;

    GoodsContainer goodsContainer;
    public final MapIdEntities<Building> buildings = new MapIdEntities<Building>();
    public final MapIdEntities<ColonyTile> colonyTiles = new MapIdEntities<ColonyTile>();
    public final List<ColonyBuildingQueueItem> buildingQueue = new ArrayList<ColonyBuildingQueueItem>(); 
    
    final ObjectWithFeatures colonyUpdatableFeatures;
    
    private final ColonyProduction colonyProduction;
    final List<Unit> colonyWorkers = new ArrayList<Unit>();
    private int sonsOfLiberty = 0;
    private int tories = 0;
    private int productionBonus = 0;
    
    /**
     * The number of liberty points.  Liberty points are an
     * abstract game concept.  They are generated by but are not
     * identical to bells, and subject to further modification.
     */
    private int liberty = 0;
    String tileId;
    
    public Colony(String id) {
    	this.id = id;
    	colonyUpdatableFeatures = new ObjectWithFeatures("tmp" + id);
    	colonyProduction = new ColonyProduction(this);
    }
    
    public int getColonyUnitsCount() {
		return colonyWorkers.size();
	}

    public boolean canReducePopulation() {
    	return getColonyUnitsCount() > colonyUpdatableFeatures.applyModifier(Modifier.MINIMUM_COLONY_SIZE, 0); 
    }
    
    public void updateModelOnWorkerAllocationOrGoodsTransfer() {
    	colonyProduction.setAsNeedUpdate();
    }

    public boolean isUnitInColony(Unit unit) {
        for (Building building : buildings.entities()) {
            if (building.workers.containsId(unit)) {
                return true;
            }
        }
        if (isUnitOnTerrain(unit)) {
        	return true;
        }
        return false;
    }
    
	public boolean isUnitOnTerrain(Unit unit) {
		return unitWorkingTerrain(unit) != null;
	}

	public ColonyTile unitWorkingTerrain(Unit unit) {
        for (ColonyTile colonyTile : colonyTiles.entities()) {
            if (colonyTile.getWorker() != null && unit.equalsId(colonyTile.getWorker())) {
            	return colonyTile;
            }
        }
        return null;
	}
	
    public void updateColonyPopulation() {
    	colonyWorkers.clear();
    	for (Building building : buildings.entities()) {
    		colonyWorkers.addAll(building.workers.entities());
    	}
    	for (ColonyTile colonyTile : colonyTiles.entities()) {
    		if (colonyTile.getWorker() != null) {
    			colonyWorkers.add(colonyTile.getWorker());
    		}
    	}
    	updateSonOfLiberty();
    	updateProductionBonus();
    }
    
    public void updateColonyFeatures() {
    	colonyUpdatableFeatures.clear();
    	for (Building b : buildings.entities()) {
    		colonyUpdatableFeatures.addFeatures(b.buildingType);
    	}
    	for (FoundingFather ff : owner.foundingFathers.entities()) {
    	    colonyUpdatableFeatures.addFeatures(ff);
    	}
    }
    
    private String getStockadeKey() {
        return null;
    }

	@Override
	public boolean isColony() {
		return true;
	}
    
    public String getImageKey() {
        int count = getColonyUnitsCount();
        String key = (count <= 3) ? "small"
            : (count <= 7) ? "medium"
            : "large";
        String stockade = getStockadeKey();
        if (stockade != null) {
            key += "." + stockade;
        }
        return "model.settlement." + key + ".image";
    }

    public void addWorkerToBuilding(Building building, Unit unit) {
    	unit.setState(UnitState.IN_COLONY);
    	UnitRole defaultUnitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
    	changeUnitRole(unit, defaultUnitRole);
        building.workers.add(unit);
    }
    
    public void addWorkerToTerrain(ColonyTile destColonyTile, Unit unit) {
    	unit.setState(UnitState.IN_COLONY);
    	UnitRole defaultUnitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
    	changeUnitRole(unit, defaultUnitRole);
        destColonyTile.setWorker(unit);
    }
    
    public List<GoodMaxProductionLocation> determinePotentialTerrainProductions(Unit unit) {
        if (!unit.isPerson()) {
            return Collections.emptyList();
        }
        ColonyTile terrain = unitWorkingTerrain(unit);
        if (terrain == null) {
        	return Collections.emptyList();
        }
    	return colonyProduction.determinePotentialTerrainProductions(terrain, unit);
    }
    
    public List<GoodMaxProductionLocation> determinePotentialMaxGoodsProduction(Unit unit) {
        if (!unit.isPerson()) {
            return Collections.emptyList();
        }
        List<GoodMaxProductionLocation> determinePotentialMaxGoodsProduction = colonyProduction.determinePotentialMaxGoodsProduction(unit);
        return determinePotentialMaxGoodsProduction;
    }

    public void changeUnitRole(Unit unit, UnitRole newUnitRole) {
    	if (!newUnitRole.isAvailableTo(unit.unitType)) {
    		throw new IllegalStateException("can not change role for unit: " + unit + " from " + unit.unitRole + " to " + newUnitRole);
    	}
    	ProductionSummary required = unit.unitRole.requiredGoodsToChangeRoleTo(newUnitRole);
    	
    	if (!goodsContainer.hasGoodsQuantity(required)) {
    		throw new IllegalStateException("warehouse do not have enough goods " + required);
    	}
    	unit.changeRole(newUnitRole);
    	goodsContainer.decreaseGoodsQuantity(required);
    }
    
    public GoodsContainer getGoodsContainer() {
        return goodsContainer;
    }
    
    public ProductionConsumption productionSummary(Building building) {
    	return colonyProduction.productionConsumptionForObject(building.getId());
    }
    
    public ProductionSummary productionSummary() {
    	return colonyProduction.globalProductionConsumption();
    }
    
    public ProductionConsumption productionSummaryForTerrain(ColonyTile colonyTile) {
    	return colonyProduction.productionConsumptionForObject(colonyTile.getId());
    }
    
	public ProductionInfo maxPossibleProductionOnTile(Unit aUnit, Tile aTile) {
		ProductionInfo productionInfo = aTile.type.productionInfo;
		ProductionInfo productionSummaryForWorker = productionInfo.productionSummaryForWorker(aUnit);
		productionSummaryForWorker.applyTileImprovementsModifiers(aTile);
		return productionSummaryForWorker;
	}

    public int getWarehouseCapacity() {
    	return (int)colonyUpdatableFeatures.applyModifier(Modifier.WAREHOUSE_STORAGE, 0);
    }

    public int sonsOfLiberty() {
        return sonsOfLiberty;
    }

    public int rebels() {
        return (int)Math.floor(0.01 * sonsOfLiberty * getColonyUnitsCount());
    }

    public int tories() {
        return tories;
    }

    public int productionBonus() {
        return productionBonus;
    }
    
    /**
     * Update the colony's production bonus.
     *
     * @return True if the bonus changed.
     */
    protected boolean updateProductionBonus() {
        final int veryBadGovernment = Specification.options.getIntValue(GameOptions.VERY_BAD_GOVERNMENT_LIMIT);
        final int badGovernment = Specification.options.getIntValue(GameOptions.BAD_GOVERNMENT_LIMIT);
        final int veryGoodGovernment = Specification.options.getIntValue(GameOptions.VERY_GOOD_GOVERNMENT_LIMIT);
        final int goodGovernment = Specification.options.getIntValue(GameOptions.GOOD_GOVERNMENT_LIMIT);
        int newBonus = (sonsOfLiberty >= veryGoodGovernment) ? 2
            : (sonsOfLiberty >= goodGovernment) ? 1
            : (tories > veryBadGovernment) ? -2
            : (tories > badGovernment) ? -1
            : 0;
        if (productionBonus != newBonus) {
            productionBonus = newBonus;
            return true;
        }
        return false;
    }
    
    private void updateSonOfLiberty() {
        int uc = getColonyUnitsCount();
        sonsOfLiberty = calculateSoLPercentage(uc, liberty);
        tories = uc - rebels();
    }
    
    /**
     * Gets the number of units that would be good to add/remove from this
     * colony.  That is the number of extra units that can be added without
     * damaging the production bonus, or the number of units to remove to
     * improve it.
     *
     * @return The number of units to add to the colony, or if negative
     *      the negation of the number of units to remove.
     */
    public int getPreferredSizeChange() {
        int i, limit, pop = getColonyUnitsCount();
        if (productionBonus < 0) {
            limit = pop;
            for (i = 1; i < limit; i++) {
                if (governmentChange(pop - i) == 1) break;
            }
            return -i;
        } else {
            limit = Specification.options.getIntValue(GameOptions.BAD_GOVERNMENT_LIMIT);
            for (i = 1; i < limit; i++) {
                if (governmentChange(pop + i) == -1) break;
            }
            return i - 1;
        }
    }
    
    /**
     * Returns 1, 0, or -1 to indicate that government would improve,
     * remain the same, or deteriorate if the colony had the given
     * population.
     *
     * @param unitCount The proposed population for the colony.
     * @return 1, 0 or -1.
     */
    public int governmentChange(int unitCount) {
        final int veryBadGovernment = Specification.options.getIntValue(GameOptions.VERY_BAD_GOVERNMENT_LIMIT);
        final int badGovernment = Specification.options.getIntValue(GameOptions.BAD_GOVERNMENT_LIMIT);
        final int veryGoodGovernment = Specification.options.getIntValue(GameOptions.VERY_GOOD_GOVERNMENT_LIMIT);
        final int goodGovernment = Specification.options.getIntValue(GameOptions.GOOD_GOVERNMENT_LIMIT);

        int rebelPercent = calculateSoLPercentage(unitCount, liberty);
        int rebelCount = rebels();
        int loyalistCount = unitCount - rebelCount;

        int result = 0;
        if (rebelPercent >= veryGoodGovernment) { // There are no tories left.
            if (sonsOfLiberty < veryGoodGovernment) {
                result = 1;
            }
        } else if (rebelPercent >= goodGovernment) {
            if (sonsOfLiberty >= veryGoodGovernment) {
                result = -1;
            } else if (sonsOfLiberty < goodGovernment) {
                result = 1;
            }
        } else {
            if (sonsOfLiberty >= goodGovernment) {
                result = -1;
            } else { // Now that no bonus is applied, penalties may.
                if (loyalistCount > veryBadGovernment) {
                    if (tories <= veryBadGovernment) {
                        result = -1;
                    }
                } else if (loyalistCount > badGovernment) {
                    if (tories <= badGovernment) {
                        result = -1;
                    } else if (tories > veryBadGovernment) {
                        result = 1;
                    }
                } else {
                    if (tories > badGovernment) {
                        result = 1;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Calculate the SoL membership percentage of the colony based on the
     * number of colonists and liberty.
     *
     * @param uc The proposed number of units in the colony.
     * @param liberty The amount of liberty.
     * @return The percentage of SoLs, negative if not calculable.
     */
    private int calculateSoLPercentage(int uc, int liberty) {
        if (uc <= 0) {
            return -1;
        }
        float membership = (liberty * 100.0f) / (LIBERTY_PER_REBEL * uc);
        membership = owner.applyModifier(Modifier.SOL, membership);
        
        if (membership < 0.0f) {
            membership = 0.0f;
        } else if (membership > 100.0f) {
            membership = 100.0f;
        }
        return (int)membership;
    }
    

    public void initColonyTilesTile(Tile colonyTile, Map map) {
        for (ColonyTile ct : colonyTiles.entities()) {
            boolean foundTileForColonyTile = false; 
            for (Direction direction : Direction.allDirections) {
                if (ct.getWorkTileId().equals(colonyTile.getId())) {
                    ct.tile = colonyTile;
                    foundTileForColonyTile = true;
                    break;
                }
                Tile borderTile = map.getTile(colonyTile.x, colonyTile.y, direction);
                if (ct.getWorkTileId().equals(borderTile.getId())) {
                    ct.tile = borderTile;
                    foundTileForColonyTile = true;
                    break;
                }
            }
            if (foundTileForColonyTile == false) {
                throw new IllegalStateException("can not find Tile for ColonyTile: " + ct);
            }
            
        	if (ct.tile.type.isWater()) {
        		coastland = true;
        	}
        }
    }
    
    public void addItemToBuildingQueue(ColonyBuildingQueueItem item) {
    	buildingQueue.add(item);
    }
    
	public void initBuildingQueue(List<ColonyBuildingQueueItem> items) {
		buildingQueue.clear();
		buildingQueue.addAll(items);
	}

	public ColonyBuildingQueueItem getFirstBuildableItem() {
		if (buildingQueue.isEmpty()) {
			return null;
		}
		return buildingQueue.get(0);
	}

    public void buildableBuildings(List<ColonyBuildingQueueItem> items) {
    	Collection<BuildingType> buildingsTypes = Specification.instance.buildingTypes.sortedEntities();
    	for (BuildingType bt : buildingsTypes) {
    		if (!colonyUpdatableFeatures.hasAbilitiesRequiredFrom(bt)) {
    			System.out.println("" + bt + ": colony do not have required ability");
    			continue;
    		}
    		if (isOnBuildingQueue(bt)) {
    			System.out.println("" + bt + " already on building queue list");
    			continue;
    		}
    		if (isBuildingAlreadyBuilt(bt)) {
    			System.out.println("" + bt + " has already built");
    			continue;
    		}
    		if (!isBuildingCanBeBuiltBecauseOfLevel(bt)) {
    			System.out.println("" + bt + " build level not accessible");
    			continue;
    		}
    		if (bt.hasAbility(Ability.COASTAL_ONLY) && !isCoastland()) {
    			System.out.println("" + bt + " can be built only on coastland");
    			continue;
    		}
    		if (bt.getRequiredPopulation() > getColonyUnitsCount()) {
    			System.out.println("" + bt + " required more colony units");
    			continue;
    		}
    		items.add(new ColonyBuildingQueueItem(bt));
    	}
    }
    
    public void buildableUnits(List<ColonyBuildingQueueItem> items) {
    	Collection<UnitType> unitTypes = Specification.instance.unitTypes.sortedEntities();
    	for (UnitType unitType : unitTypes) {
    		if (unitType.getId().equals("model.unit.flyingDutchman")) {
    			System.getProperties();
    		}
    		if (!colonyUpdatableFeatures.canApplyAbilityToObject(Ability.BUILD, unitType)) {
    			System.out.println("" + unitType + " can not be built because of buildable ability");
    			continue;
    		}
    		if (!colonyUpdatableFeatures.hasAbilitiesRequiredFrom(unitType)) {
    			System.out.println("" + unitType + " can not be built because of required abilities");
    			continue;
    		}
    		if (UnitType.WAGON_TRAIN.equals(unitType.getId())) {
    			if (owner.unitTypeCount(unitType) >= owner.settlements.size()) {
    				continue;
    			}
    		}
    		items.add(new ColonyBuildingQueueItem(unitType));
    	}
    }
    
    public boolean isBuildingCanBeBuiltBecauseOfLevel(BuildingType buildingType) {
    	for (Building building : buildings.entities()) {
    		if (building.buildingType.isTheSameRoot(buildingType)) {
    			if (building.buildingType.canUpgradeTo(buildingType)) {
    				return true;
    			} else {
    				return false;
    			}
    		}
    	}
    	// does not find building
    	return buildingType.isRoot();
    }
    
    public boolean isBuildingAlreadyBuilt(BuildingType buildingType) {
    	for (Building building : buildings.entities()) {
    		if (building.buildingType.equalsId(buildingType)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isOnBuildingQueue(BuildingType buildingType) {
    	for (ColonyBuildingQueueItem item : buildingQueue) {
    		if (buildingType.equalsId(item.getId())) {
    			return true;
    		}
    	}
    	return false;
    }
    
	public int getPriceForBuilding(ColonyBuildingQueueItem currentBuildableItem) {
		List<RequiredGoods> requiredGoods = currentBuildableItem.requiredGoods();
		
		Market market = owner.market();
		
		int sum = 0;
		for (RequiredGoods rg : requiredGoods) {
			int warehouseGoodsAmount = goodsContainer.goodsAmount(rg.getId());
			if (rg.amount > warehouseGoodsAmount) {
				int requireGoods = rg.amount - warehouseGoodsAmount;
				sum += market.buildingGoodsPrice(rg.goodsType, requireGoods);
			}
		}
		return sum;
	}

	public void payForBuilding(ColonyBuildingQueueItem currentBuildableItem, Game game) {
		if (!Specification.options.getBoolean(GameOptions.PAY_FOR_BUILDING)) {
			throw new IllegalStateException("Pay for building is disabled");
		}
		
		Market ownerMarket = owner.market();
		for (RequiredGoods requiredGood : currentBuildableItem.requiredGoods()) {
			int reqDiffAmount = requiredGood.amount - goodsContainer.goodsAmount(requiredGood.goodsType);
			if (reqDiffAmount <= 0) {
				continue;
			}
			TransactionEffectOnMarket effectOnMarket = ownerMarket.buyGoods(owner, requiredGood.goodsType, reqDiffAmount, goodsContainer);
			
			game.propagateBuyToEuropeanMarkets(owner, requiredGood.goodsType, effectOnMarket.goodsModifiedMarket);
			if (effectOnMarket.priceChanged()) {
				owner.eventsNotifications.addPriceChangeNotification(requiredGood.goodsType, effectOnMarket.beforePrice, effectOnMarket.afterPrice);
			}
		}
		updateModelOnWorkerAllocationOrGoodsTransfer();
	}
	
    public static class Xml extends XmlNodeParser {
        public Xml() {
        	addNode(ColonyBuildingQueueItem.class, new ObjectFromNodeSetter<Colony, ColonyBuildingQueueItem>() {
				@Override
				public void set(Colony target, ColonyBuildingQueueItem entity) {
					target.buildingQueue.add(entity);
				}
			});
            addNode(GoodsContainer.class, "goodsContainer");
            addNodeForMapIdEntities("buildings", Building.class);
            addNodeForMapIdEntities("colonyTiles", ColonyTile.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String strAttribute = attr.getStrAttribute("settlementType");
            Player owner = game.players.getById(attr.getStrAttribute("owner"));
            
            Colony colony = new Colony(attr.getStrAttribute("id"));
            colony.name = attr.getStrAttribute("name");
            colony.sonsOfLiberty = attr.getIntAttribute("sonsOfLiberty", 0);
            colony.tories = attr.getIntAttribute("tories", 0);
            colony.productionBonus = attr.getIntAttribute("productionBonus", 0);
            colony.liberty = attr.getIntAttribute("liberty", 0);
            colony.tileId = attr.getStrAttribute("tile");
            colony.owner = owner;
            colony.settlementType = owner.nationType().settlementTypes.getById(strAttribute);
            owner.settlements.add(colony);
            
            nodeObject = colony;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        	if (qName.equals(tagName())) {
        		((Colony)nodeObject).updateColonyPopulation();
        		((Colony)nodeObject).updateColonyFeatures();
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "colony";
        }
    }

}
