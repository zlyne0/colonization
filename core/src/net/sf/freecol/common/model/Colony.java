package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.colonyproduction.DefaultColonySettingProvider;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;
import net.sf.freecol.common.model.colonyproduction.ProductionSimulation;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.TransactionEffectOnMarket;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.Modifier.ModifierType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import net.sf.freecol.common.model.colonyproduction.ColonyProduction;
import net.sf.freecol.common.util.StringUtils;
import promitech.colonization.Direction;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class Colony extends Settlement {
	public static final int NEVER_COMPLETE_BUILD = -1;
    public static final int LIBERTY_PER_REBEL = 200;

	private static final Comparator<Building> BUILDING_GOODS_OUTPUT_CHAIN_LEVEL = new Comparator<Building>() {
		@Override
		public int compare(Building o1, Building o2) {
			return o1.buildingType.getGoodsOutputChainLevel() - o2.buildingType.getGoodsOutputChainLevel();
		}
	};

    /** Reasons for not building a buildable. */
    public static enum NoBuildReason {
        NONE,
        NOT_BUILDING,
        NOT_BUILDABLE,
        POPULATION_TOO_SMALL,
        MISSING_BUILD_ABILITY,
        MISSING_ABILITY,
        WRONG_UPGRADE,
        COASTAL,
        LIMIT_EXCEEDED
    }
    
    public final MapIdEntities<Building> buildings = new SortedMapIdEntities<Building>(BUILDING_GOODS_OUTPUT_CHAIN_LEVEL);
    public final MapIdEntities<ColonyTile> colonyTiles = new MapIdEntities<ColonyTile>();
    public final List<ColonyBuildingQueueItem> buildingQueue = new ArrayList<ColonyBuildingQueueItem>();
	private MapIdEntities<ExportInfo> exportInfos = new MapIdEntities<ExportInfo>();

    public final ObjectWithFeatures colonyUpdatableFeatures;
    
    private final ColonyProduction colonyProduction;

    private final MapIdEntities<Unit> colonyWorkers = new MapIdEntities<Unit>();
    private Modifier productionBonus = new Modifier(Modifier.COLONY_PRODUCTION_BONUS, ModifierType.ADDITIVE, 0);
    public final ColonyLiberty colonyLiberty = new ColonyLiberty();
    
    protected boolean seaConnectionToEurope = false;
    
    private Colony(String id, SettlementType settlementType) {
    	super(id, settlementType);
    	colonyUpdatableFeatures = new ObjectWithFeatures("tmp" + id);
    	colonyProduction = new ColonyProduction(new DefaultColonySettingProvider(this));
    	// constructor used only by xml parser which create goodsContainer
    }

    public Colony(IdGenerator idGenerator, SettlementType settlementType) {
		this(idGenerator.nextId(Colony.class), settlementType);
		goodsContainer = new GoodsContainer();
	}

    @Override
	public String toString() {
    	return "id=" + getId() + ", name=" + getName();
    }
    
    public int getColonyUnitsCount() {
		return colonyWorkers.size();
	}
    
    public boolean isColonyEmpty() {
    	return colonyWorkers.isEmpty();
    }

    public boolean canReducePopulation() {
    	return getColonyUnitsCount() > colonyUpdatableFeatures.applyModifier(Modifier.MINIMUM_COLONY_SIZE, 0); 
    }
    
    public void updateModelOnWorkerAllocationOrGoodsTransfer() {
		colonyProduction.updateRequest();
    }

    public boolean isUnitInColony(Unit unit) {
        for (Building building : buildings.entities()) {
            if (building.getUnits().containsId(unit)) {
                return true;
            }
        }
        if (isUnitOnTerrain(unit)) {
        	return true;
        }
        return false;
    }
    
	public boolean isUnitOnTerrain(Unit unit) {
	    return unit.isAtLocation(ColonyTile.class);
	}

    public void updateColonyPopulation() {
    	colonyWorkers.clear();
    	for (Building building : buildings.entities()) {
    		colonyWorkers.addAll(building.getUnits());
    	}
    	for (ColonyTile colonyTile : colonyTiles.entities()) {
			if (colonyTile.hasWorker()) {
				colonyWorkers.add(colonyTile.getWorker());
			}
		}
		colonyProduction.updateRequest();
    	colonyLiberty.updateSonOfLiberty(owner, getColonyUnitsCount());
    	updateProductionBonus();
    }
    
    public void updateColonyFeatures() {
    	colonyUpdatableFeatures.clear();
    	if (isCoastland()) {
    		colonyUpdatableFeatures.addAbility(Ability.HAS_PORT_ABILITY);
    	}
    	for (Building b : buildings.entities()) {
    		colonyUpdatableFeatures.addFeatures(b.buildingType);
    	}
    	for (FoundingFather ff : owner.foundingFathers.entities()) {
    	    colonyUpdatableFeatures.addFeatures(ff);
    	}
    	colonyUpdatableFeatures.addFeatures(settlementType);
    	colonyUpdatableFeatures.addModifier(productionBonus);
    }
    
    public void addModifiersTo(ObjectWithFeatures mods, String modifierCode) {
    	mods.addModifierFrom(colonyUpdatableFeatures, modifierCode);
    }
    
    @Override
    public boolean hasAbility(String abilityCode) {
        return colonyUpdatableFeatures.hasAbility(abilityCode);
    }
    
	@Override
	public int applyModifiers(String modifierCode, int val) {
		return (int)colonyUpdatableFeatures.applyModifier(modifierCode, (float)val);
	}

	public boolean hasBurnableBuildings() {
	    for (Building building : buildings.entities()) {
	    	if (isBuildingBurnable(building)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public List<Building> createBurnableBuildingsList() {
	    List<Building> burnable = new ArrayList<Building>();
	    for (Building building : buildings.entities()) {
	        if (isBuildingBurnable(building)) {
	            burnable.add(building);
	        }
	    }
	    return burnable;
	}
	
	private boolean isBuildingBurnable(Building building) {
		return !isAutoBuildable(building.buildingType);
	}
	
	public boolean hasStockade() {
		Building stockade = getStockade();
		return stockade != null;
	}
	
	public int getStockadeLevel() {
		Building stockade = getStockade();
		if (stockade != null) {
			return stockade.buildingType.getLevel();
		}
		return 1;
	}
	
    private String getStockadeKey() {
		Building stockade = getStockade();
		if (stockade == null) {
			return null;
		}
		return StringUtils.lastPart(stockade.buildingType.getId(), ".");
    }
    
    private Building getStockade() {
	    for (Building building : buildings.entities()) {
	        if (building.buildingType.hasModifier(Modifier.DEFENCE)) {
	            return building;
	        }
	    }
	    return null;
    }

	public boolean hasSeaConnectionToEurope() {
		return seaConnectionToEurope;
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
        addWorkerToColony(unit, building);
    }

    public void addWorkerToBuilding(BuildingType buildingType, Unit unit) {
		Building building = findBuildingByType(buildingType.getId());
		addWorkerToColony(unit, building);
	}

	public void addWorkerToTerrain(Tile aTile, Unit unit) {
		ColonyTile aColonyTile = colonyTiles.getById(aTile.getId());
		addWorkerToTerrain(aColonyTile, unit);
	}

    private void addWorkerToTerrain(ColonyTile aColonyTile, Unit unit) {
    	addWorkerToColony(unit, aColonyTile);
        aColonyTile.tile.changeOwner(owner, this);
        aColonyTile.initMaxPossibleProductionOnTile();
    }

    public void addWorkerToTerrain(Tile aTile, Unit unit, Production production) {
		ColonyTile aColonyTile = colonyTiles.getById(aTile.getId());
		addWorkerToColony(unit, aColonyTile);
		aColonyTile.tile.changeOwner(owner, this);
		aColonyTile.initProduction(production);
	}

    public void addWorkerToTerrain(ColonyTile aColonyTile, Unit unit, GoodsType goodsType) {
        addWorkerToColony(unit, aColonyTile);
        aColonyTile.tile.changeOwner(owner, this);
		aColonyTile.initProducitonType(goodsType);
    }
    
    private void addWorkerToColony(Unit worker, UnitLocation unitLocation) {
        worker.setState(UnitState.IN_COLONY);
        UnitRole defaultUnitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
        changeUnitRole(worker, defaultUnitRole);
        worker.changeUnitLocation(unitLocation);
        
        updateModelOnWorkerAllocationOrGoodsTransfer();
    }

	public boolean canSustainNewWorker(UnitType unitType, GoodsType goodsTypeToProduce, int produceAmount) {
		ProductionSummary productionSummary = productionSummary();
		for (UnitConsumption unitConsumption : unitType.unitConsumption.entities()) {
			if (unitConsumption.getTypeId().equals(GoodsType.BELLS)) {
				// do not care
				continue;
			}
			int prod = productionSummary.getQuantity(unitConsumption.getTypeId());
			// when unit produce what consume, unit can sustain himself
			if (GoodsType.isFoodGoodsType(unitConsumption.getTypeId()) && goodsTypeToProduce != null && goodsTypeToProduce.isFood()) {
		        prod += produceAmount;
			}
			
			// when consume food and is lack of food then it is possible to stop breeding horses and sustain colonist
			if (GoodsType.isFoodGoodsType(unitConsumption.getTypeId())) {
			    prod += productionSummary.getQuantity(GoodsType.HORSES);
			}
			if (unitConsumption.getQuantity() > prod) {
				return false;
			}
		}
		return true;
	}

    public boolean canSustainNewWorker(UnitType workerType) {
    	return canSustainWorkers(1, 0);
    }
	
    public boolean canSustainWorkers(int workersCount, int additionalFoodProduction) {
    	ProductionSummary productionSummary = productionSummary();    	
    	int prod = productionSummary.getQuantity(GoodsType.FOOD);
    	prod += productionSummary.getQuantity(GoodsType.HORSES);
    	prod += additionalFoodProduction;
    	return workersCount*2 <= prod;
    }
    
    public void changeUnitRole(Unit unit, UnitRole newUnitRole) {
    	super.changeUnitRole(unit, newUnitRole, colonyUpdatableFeatures);
    }
    
    public void increaseWorkersExperience() {
        for (ColonyTile colonyTile : colonyTiles.entities()) {
            if (colonyTile.hasWorker() && !colonyTile.getWorker().isExpert()) {
                Unit worker = colonyTile.getWorker();
                increaseExperienceForWorker(colonyTile, worker, 1);
            }
        }
        for (Building building : buildings.entities()) {
            for (Unit worker : building.getUnits().entities()) {
                if (worker.isExpert()) {
                    continue;
                }
                increaseExperienceForWorker(building, worker, building.getUnits().size());
            }
        }
    }
    
    private void increaseExperienceForWorker(ProductionLocation productionLocation, Unit worker, int prodLocWorkersAmount) {
        ProductionSummary realProduction = productionSummary(productionLocation).realProduction;
        System.out.println("production location [" + productionLocation + "] produce [" + realProduction + "]");
        for (Entry<String> entry : realProduction.entries()) {
            UnitType goodsExpertUnitType = Specification.instance.expertUnitTypeByGoodType.get(entry.key);
            if (goodsExpertUnitType != null && worker.unitType.canBeUpgraded(goodsExpertUnitType, ChangeType.EXPERIENCE)) {
                int experience = entry.value / prodLocWorkersAmount;
                System.out.println("worker [" + worker + "] gain " + experience + " experience to be " + goodsExpertUnitType );
                worker.gainExperience(experience);
                if (worker.isPromotedToExpert()) {
                	updateModelOnWorkerAllocationOrGoodsTransfer();
                	
                	StringTemplate oldName = UnitLabel.getPlainUnitLabel(worker);
                	worker.changeUnitType(goodsExpertUnitType);
                	StringTemplate newName = UnitLabel.getPlainUnitLabel(worker);
                	
                	StringTemplate st = StringTemplate.template("model.unit.experience")
                		.addStringTemplate("%oldName%", oldName)
                		.addStringTemplate("%unit%", newName)
                		.add("%colony%", getName());
                	owner.eventsNotifications.addMessageNotification(st);
                	
                }
            }
        }
    }
    
    public void handleLackOfResources(Game game) {
        int foodProdCons = colonyProduction.globalProductionConsumption().getQuantity(GoodsType.FOOD);
        if (foodProdCons < 0) {
            // food consumption is greater then production
            int quantityToConsume = -foodProdCons;
            int storedAfterConsume = goodsContainer.goodsAmount(GoodsType.FOOD) - quantityToConsume;
            
            if (storedAfterConsume >= 0) {
                int turns = goodsContainer.goodsAmount(GoodsType.FOOD) / quantityToConsume;
                if (turns < 3) {
                	StringTemplate st = StringTemplate.template("model.colony.famineFeared")
        				.add("%colony%", getName())
        				.addAmount("%number%", turns);
                	owner.eventsNotifications.addMessageNotification(st);
                }
            } else {
            	// equalize to zero
            	goodsContainer.increaseGoodsQuantity(GoodsType.FOOD, quantityToConsume);
            	
            	Unit unit = colonyWorkers.first();
            	unit.removeFromLocation();
            	owner.removeUnit(unit);

            	System.out.println("unit[" + unit + "] was removed from colony[" + getName() + "]");
            	
    			updateColonyPopulation();
    			updateModelOnWorkerAllocationOrGoodsTransfer();
            	
    			if (getColonyUnitsCount() > 0) {
    				StringTemplate st = StringTemplate.template("model.colony.colonistStarved").add("%colony%", getName());
    				owner.eventsNotifications.addMessageNotification(st);
    			} else {
    				StringTemplate st = StringTemplate.template("model.colony.colonyStarved").add("%colony%", getName());
    				owner.eventsNotifications.addMessageNotification(st);
    				removeFromMap(game);
    				removeFromPlayer();
    			}
            }
        }
        
        for (Building building : buildings.entities()) {
            ProductionConsumption productionSummary = productionSummary(building);

            for (Entry<String> entry : productionSummary.baseProduction.entries()) {
                if (GoodsType.isFoodGoodsType(entry.key)) {
                    continue;
                }
                GoodsType goodsType = Specification.instance.goodsTypes.getById(entry.key);
                if (!goodsType.isStorable()) {
                	continue;
                }
                int quantityToConsume = -entry.value;
                int afterConsume = goodsContainer.goodsAmount(entry.key) - quantityToConsume;
                if (afterConsume == 0) {
					StringTemplate st = StringTemplate.template("model.building.notEnoughInput")
                		.add("%colony%", getName())
                		.addName("%inputGoods%", goodsType);
					owner.eventsNotifications.addMessageNotification(st);
                } else {
                    if (afterConsume < quantityToConsume) {
                    	StringTemplate st = StringTemplate.template("model.building.warehouseEmpty")
                    		.add("%colony%", getName())
                    		.addName("%goods%", goodsType)
                    		.addAmount("%level%", quantityToConsume);
                    	owner.eventsNotifications.addMessageNotification(st);
                    }
                }
            }
        }
    }
    
    public void resetLiberty() {
    	colonyLiberty.reset();
    	this.productionBonus.setValue(0);
    }
    
	public void calculateSonsOfLiberty() {
		int oldSonsOfLiberty = colonyLiberty.sonsOfLiberty();
		ProductionSummary gpc = colonyProduction.globalProductionConsumption();
		int bells = gpc.getQuantity(GoodsType.BELLS);

		owner.modifyLiberty(bells);
		colonyLiberty.calculateSonsOfLiberty(owner, bells, getColonyUnitsCount());

		updateProductionBonus();

		colonyLiberty.sonsOfLibertyChangeNotification(this, oldSonsOfLiberty);
	}
    
    public ProductionConsumption productionSummary(ProductionLocation productionLocation) {
        return colonyProduction.productionConsumptionForLocation(productionLocation);
    }

    public ProductionSummary productionSummary() {
    	return colonyProduction.globalProductionConsumption();
    }
    
	public void increaseWarehouseByProduction() {
		goodsContainer.increaseGoodsQuantity(productionSummary());
		colonyProduction.updateRequest();
	}
    
	public void reduceTileResourceQuantity() {
		for (ColonyTile ct : colonyTiles.entities()) {
			if (ct.hasNotWorker()) {
				continue;
			}
			ProductionConsumption ps = productionSummary(ct);
			for (Entry<String> entry : ps.realProduction.entries()) {
				ResourceType reducedResourceType = ct.tile.reduceTileResourceQuantity(entry.key, entry.value);
				if (reducedResourceType != null) {
					updateModelOnWorkerAllocationOrGoodsTransfer();

					StringTemplate st = StringTemplate.template("model.tile.resourceExhausted")
						.addName("%resource%", reducedResourceType)
						.add("%colony%", getName());
					owner.eventsNotifications.addMessageNotification(st);
				}
			}
		}
	}
	
	@Override
	public void updateProductionToMaxPossible(Tile tile) {
		for (ColonyTile colonyTile : colonyTiles.entities()) {
			if (colonyTile.equalsId(tile)) {
				if (Colony.this.tile.equalsCoordinates(tile)) {
					// init for center colony tile
	    		    colonyTile.initMaxPossibleProductionOnTile();
	    			updateModelOnWorkerAllocationOrGoodsTransfer();
				} else {
					if (colonyTile.hasWorker()) {
						// init for tile with worker
		    		    colonyTile.initMaxPossibleProductionOnTile();
		    			updateModelOnWorkerAllocationOrGoodsTransfer();
					}
					// no init for others tiles
				}
				return;
			}
		}
	}
	
	@Override
    public int warehouseCapacity() {
    	return (int)colonyUpdatableFeatures.applyModifier(Modifier.WAREHOUSE_STORAGE, 0);
    }

    public int sonsOfLiberty() {
        return colonyLiberty.sonsOfLiberty();
    }

    public int rebels() {
    	return colonyLiberty.rebels(getColonyUnitsCount());
    }

    public int tories() {
    	return colonyLiberty.tories();
    }

    public Modifier productionBonus() {
        return productionBonus;
    }
    
    /**
     * Update the colony's production bonus.
     *
     * @return True if the bonus changed.
     */
    protected boolean updateProductionBonus() {
    	int newBonus = colonyLiberty.productionBonus();

        if (productionBonus.asInt() != newBonus) {
            productionBonus.setValue(newBonus);
			colonyProduction.updateRequest();
            return true;
        }
        return false;
    }

    public int getPreferredSizeChange() {
    	return colonyLiberty.getPreferredSizeChange(owner, productionBonus, getColonyUnitsCount());
    }

    public void createColonyTiles(Map map, Tile centerColonyTile) {
    	this.tile = centerColonyTile;
    	this.coastland = this.tile.isOnSeaSide();
    	
		ColonyTile centerTile = new ColonyTile(tile);
		colonyTiles.add(centerTile);
		centerTile.initMaxPossibleProductionOnTile();
		
    	for (Direction d : Direction.allDirections) {
    		Tile neighbourTile = map.getTile(tile, d);
    		if (neighbourTile == null) {
    			continue;
    		}
    		colonyTiles.add(new ColonyTile(neighbourTile));
    	}
    }
    
    public void initColonyTilesTile(Tile colonyTile, Map map) {
    	this.coastland = this.tile.isOnSeaSide();
    	
    	for (ColonyTile ct : colonyTiles.entities()) {
            boolean foundTileForColonyTile = false; 
            for (Direction direction : Direction.allDirections) {
                if (ct.equalsId(colonyTile)) {
                    ct.tile = colonyTile;
                    foundTileForColonyTile = true;
                    break;
                }
                Tile borderTile = map.getTile(colonyTile.x, colonyTile.y, direction);
                if (ct.equalsId(borderTile)) {
                    ct.tile = borderTile;
                    foundTileForColonyTile = true;
                    break;
                }
            }
            if (foundTileForColonyTile == false) {
                throw new IllegalStateException("can not find Tile for ColonyTile: " + ct);
            }
        }
    }
    
    public void addItemToBuildingQueue(ColonyBuildingQueueItem item) {
    	buildingQueue.add(item);
    }
    
    public void removeItemFromBuildingQueue(ColonyBuildingQueueItem item) {
        buildingQueue.remove(item);
    }
    
	public BuildableType getFirstItemInBuildingQueue() {
        if (buildingQueue.isEmpty()) {
            return null;
        }
        return buildingQueue.get(0).getType();
	}
	
	public void ifPossibleAddFreeBuildings() {
		for (BuildingType buildingType : Specification.instance.buildingTypes.entities()) {
			if (isAutoBuildableInColony(buildingType)) {
				ifPossibleAddFreeBuilding(buildingType);
			}
		}
	}
	
	public void ifPossibleAddFreeBuilding(BuildingType buildingType) {
		if (isBuildingAlreadyBuilt(buildingType)) {
			return;
		}
		NoBuildReason noBuildReason = getNoBuildReason(buildingType);
		if (noBuildReason != NoBuildReason.NONE) {
			System.out.println("addFreeBuilding[" + owner.getId() + "] reason " + noBuildReason);
			return;
		}
    	for (ColonyBuildingQueueItem item : buildingQueue) {
    		if (buildingType.equalsId(item.getId())) {
    			buildingQueue.remove(item);
    			break;
    		}
    	}		
		finishBuilding(buildingType);
	}
	
    public void buildableBuildings(List<ColonyBuildingQueueItem> items) {
    	for (BuildingType bt : Specification.instance.buildingTypes.entities()) {
    	    NoBuildReason noBuildReason = getNoBuildReason(bt);
    	    if (noBuildReason != NoBuildReason.NONE) {
    	        System.out.println("" + bt + ": " + noBuildReason);
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
    		items.add(new ColonyBuildingQueueItem(bt));
    	}
    }
    
    public void buildableUnits(List<ColonyBuildingQueueItem> items) {
    	for (UnitType unitType : Specification.instance.unitTypes.entities()) {
    	    NoBuildReason noBuildReason = getNoBuildReason(unitType);
            if (noBuildReason != NoBuildReason.NONE) {
                System.out.println("can not build " + unitType + " because of " + noBuildReason);
                continue;
            }
    		items.add(new ColonyBuildingQueueItem(unitType));
    	}
    }
    
    private boolean isBuildingCanBeBuiltBecauseOfLevel(BuildingType buildingType) {
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
    
	public int getPriceForBuilding(BuildableType buildableType) {
		Market market = owner.market();
		
		int sum = 0;
		for (RequiredGoods rg : buildableType.requiredGoods()) {
			if (market.hasArrears(rg.goodsType)) {
				return Integer.MAX_VALUE;
			}
			int warehouseGoodsAmount = goodsContainer.goodsAmount(rg.getId());
			if (rg.amount > warehouseGoodsAmount) {
				int requireGoods = rg.amount - warehouseGoodsAmount;
				sum += market.buildingGoodsPrice(rg.goodsType, requireGoods);
			}
		}
		return sum;
	}

	public void payForBuilding(BuildableType buildableType, Game game) {
		if (!Specification.options.getBoolean(GameOptions.PAY_FOR_BUILDING)) {
			throw new IllegalStateException("Pay for building is disabled");
		}
		
		Market ownerMarket = owner.market();
		for (RequiredGoods requiredGood : buildableType.requiredGoods()) {
			int reqDiffAmount = requiredGood.amount - goodsContainer.goodsAmount(requiredGood.goodsType);
			if (reqDiffAmount <= 0) {
				continue;
			}
			TransactionEffectOnMarket effectOnMarket = ownerMarket.buyGoodsForBuilding(game, owner, requiredGood.goodsType, reqDiffAmount);
			goodsContainer.increaseGoodsQuantity(requiredGood.goodsType, reqDiffAmount);
			
			if (effectOnMarket.isMarketPriceChanged()) {
				owner.eventsNotifications.addMessageNotification(MessageNotification.createGoodsPriceChangeNotification(owner, effectOnMarket));
			}
		}
		updateModelOnWorkerAllocationOrGoodsTransfer();
	}
	
	public void increaseColonySize() {
	    int foodGoodsAmount = goodsContainer.goodsAmount(GoodsType.FOOD);
	    if (foodGoodsAmount >= FOOD_PER_COLONIST) {
	        goodsContainer.decreaseGoodsQuantity(GoodsType.FOOD, FOOD_PER_COLONIST);
	        
	        UnitType freeColonistUnitType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
	        UnitFactory.create(freeColonistUnitType, owner, tile);
	        
            StringTemplate st = StringTemplate.template("model.colony.newColonist")
                        .add("%colony%", getName());
            owner.eventsNotifications.addMessageNotification(st);
	    }
	}
	
	public void buildBuildings() {
	    BuildableType buildableType = getFirstItemInBuildingQueue();
		if (buildableType == null) {
			return;
		}
		ObjectIntMap<String> requiredTurnsForGoods = new ObjectIntMap<String>(2);
		int turnsToGatherResourcesForBuild = getTurnsToComplete(buildableType, requiredTurnsForGoods);
		if (turnsToGatherResourcesForBuild == NEVER_COMPLETE_BUILD) {
			neverFinishBuildingNotification(buildableType, requiredTurnsForGoods);
			return;
		} 
		if (turnsToGatherResourcesForBuild == 0) {
			NoBuildReason noBuildReason = getNoBuildReason(buildableType);
			if (NoBuildReason.NONE != noBuildReason) {
				finishBuildingProblemNotification(buildableType, noBuildReason);
			} else {
				finishBuilding(buildableType);
			}
		}
	}

    private void finishBuildingProblemNotification(BuildableType buildableType, NoBuildReason noBuildReason) {
        StringTemplate st;
        switch (noBuildReason) {
        case LIMIT_EXCEEDED:
        	st = StringTemplate.template("model.limit.wagonTrains.description");
        	break;
        case POPULATION_TOO_SMALL:
        	st = StringTemplate.template("model.colony.buildNeedPop")
                .add("%colony%", getName())
                .addName("%building%", buildableType);
        	break;
        default:
        	st = StringTemplate.template("colonyPanel.unbuildable")
        		.add("%colony%", getName())
        		.addName("%object%", buildableType);
        	break;
        }
        System.out.println("" + buildableType + " no build reason '" + noBuildReason + "'");
        owner.eventsNotifications.addMessageNotification(st);
    }

    private void neverFinishBuildingNotification(BuildableType buildableType, ObjectIntMap<String> requiredTurnsForGoods) {
        for (RequiredGoods requiredGood : buildableType.requiredGoods()) {
        	int turnsForGoodsType = requiredTurnsForGoods.get(requiredGood.getId(), -1);
        	if (turnsForGoodsType == NEVER_COMPLETE_BUILD) {
        		int amount = requiredGood.amount - goodsContainer.goodsAmount(requiredGood.getId());
        		
        		StringTemplate st = StringTemplate.template("model.colony.buildableNeedsGoods")
        			.addName("%goodsType%", requiredGood.getId())
        			.addAmount("%amount%", amount)
        			.add("%colony%", getName())
        			.addName("%buildable%", buildableType.getId());
        		owner.eventsNotifications.addMessageNotification(st);
        		break;
        	}
        }
    }

	private void finishBuilding(BuildableType buildableType) {
		if (buildableType.isUnitType()) {
			Unit unit = UnitFactory.create((UnitType)buildableType, owner, tile);
			
			StringTemplate unitNameSt = UnitLabel.getPlainUnitLabel(unit);
			StringTemplate st = StringTemplate.template("model.colony.unitReady")
				.add("%colony%", getName())
				.addStringTemplate("%unit%", unitNameSt);
			owner.eventsNotifications.addMessageNotification(st);
		}
		if (buildableType.isBuildingType()) {
			BuildingType buildingType = (BuildingType)buildableType;
			finishBuilding(buildingType);
			
			StringTemplate st = StringTemplate.template("model.colony.buildingReady")
		        .add("%colony%", getName())
		        .addName("%building%", buildableType);
			System.out.println("new building " + buildingType + ", msg: " + Messages.message(st));
			owner.eventsNotifications.addMessageNotification(st);
		}
		
		removeResourcesAfterCompleteBuilding(buildableType);
		buildingQueue.remove(0);
	}
	
	private Building finishBuilding(BuildingType buildingType) {
		BuildingType from = buildingType.getUpgradesFrom();
		Building building;
		if (from != null) {
			building = findBuildingByType(from.getId());
			building.upgrade(buildingType);
		} else {
			building = new Building(Game.idGenerator, buildingType);
			buildings.add(building);
		}
		updateColonyFeatures();
		updateColonyPopulation();
		updateModelOnWorkerAllocationOrGoodsTransfer();
		return building;
	}
	
	public Building addBuilding(final BuildingType buildingType) {
		Building building = findBuildingByBuildingTypeHierarchy(buildingType);
		if (building != null) {
			building.upgrade(buildingType);
		} else {
			building = new Building(Game.idGenerator, buildingType);
			buildings.add(building);
		}
		return building;
	}
	
	public void removeBuilding(final String buildingTypeId) {
		Building building = findBuildingByTypeOrNull(buildingTypeId);
		if (building != null) {
			buildings.removeId(building);
		}
	}
	
	protected Building findBuildingByBuildingTypeHierarchy(final BuildingType buildingType) {
		Building foundBuilding = null;
		BuildingType bt = buildingType;
		
		while (bt != null) {
			foundBuilding = findBuildingByTypeOrNull(bt.getId());
			if (foundBuilding != null) {
				break;
			}
			bt = bt.getUpgradesFrom();
		}
		return foundBuilding;
	}
	
	public UnitLocation findUnitLocationById(String unitLocationId) {
		ColonyTile colonyTile = colonyTiles.getByIdOrNull(unitLocationId);
		if (colonyTile != null) {
			return colonyTile;
		}
		return buildings.getByIdOrNull(unitLocationId);
	}
	
	public Building findBuildingByType(String buildingTypeId) {
		Building building = findBuildingByTypeOrNull(buildingTypeId);
		if (building == null) {
			throw new IllegalStateException("can not find building '" + buildingTypeId + "' in colony " + this);
		}
		return building;
	}
	
	protected Building findBuildingByTypeOrNull(String buildingTypeId) {
		for (Building building : buildings.entities()) {
			if (building.buildingType.equalsId(buildingTypeId)) {
				return building;
			}
		}
		return null;
	}
	
    private void removeResourcesAfterCompleteBuilding(BuildableType type) {
    	for (RequiredGoods requiredGoods : type.requiredGoods.entities()) {
    		goodsContainer.decreaseGoodsQuantity(requiredGoods.goodsType, requiredGoods.amount);
    	}
	}

    public void damageBuilding(Building building) {
    	if (building.buildingType.isRoot()) {
    		MapIdEntities<Unit> ejectWorkers = new MapIdEntities<Unit>(building.getUnits());
    		buildings.removeId(building);
    		
    		updateColonyFeatures();
    		
    		for (ColonyTile ct : colonyTiles.entities()) {
    			if (ct.hasWorker() && isTileLocked(ct.tile, false)) {
    				ejectWorkers.add(ct.getWorker());
    			}
    		}
    		for (Building b : buildings.entities()) {
    			if (b.getUnits().isNotEmpty()) {
    				b.getWorkersToEject(ejectWorkers);
    			}
    		}
    		
    		ejectWorkers(ejectWorkers);
    	} else if (isBuildingBurnable(building)) {
    		MapIdEntities<Unit> ejectWorkers = building.damageBuilding();
    		ejectWorkers(ejectWorkers);
    	} else {
    		return;
    	}
    	
		updateColonyFeatures();
		updateColonyPopulation();
		updateModelOnWorkerAllocationOrGoodsTransfer();
    }
    
    private void ejectWorkers(MapIdEntitiesReadOnly<Unit> ejectWorkers) {
    	if (ejectWorkers.isNotEmpty()) {
    		for (Unit ejectedWorker : ejectWorkers.entities()) {
    			boolean foundBuilding = false;
    			for (Building b : buildings.entities()) {
    				if (b.canAddWorker(ejectedWorker.unitType)) {
    					foundBuilding = true;
    					ejectedWorker.changeUnitLocation(b);
    				}
    			}
    			if (!foundBuilding) {
    				ejectedWorker.changeUnitLocation(tile);
    			}
    		}
    	}
    }
    
	/**
     * Return the reason why the give <code>BuildableType</code> can
     * not be built.
     *
     * @param item A <code>BuildableType</code> to build.
     * @return A <code>NoBuildReason</code> value decribing the failure,
     *     including <code>NoBuildReason.NONE</code> on success.
     */
	public NoBuildReason getNoBuildReason(BuildableType item) {
		if (item == null) {
			return NoBuildReason.NOT_BUILDING;
		} else if (item.doesNotNeedGoodsToBuild()) {
			return NoBuildReason.NOT_BUILDABLE;
		} else if (item.getRequiredPopulation() > getColonyUnitsCount()) {
			return NoBuildReason.POPULATION_TOO_SMALL;
		} else if (item.hasAbility(Ability.HAS_PORT) && !isCoastland()) {
			return NoBuildReason.COASTAL;
		} else {
			if (!colonyUpdatableFeatures.hasAbilitiesRequiredFrom(item)) {
				return NoBuildReason.MISSING_ABILITY;
			}
		}
		if (item.isBuildingType()) {
			if (!isBuildingCanBeBuiltBecauseOfLevel((BuildingType) item)) {
				return NoBuildReason.WRONG_UPGRADE;
			}
		}
		if (item.isUnitType()) {
			if (UnitType.WAGON_TRAIN.equals(item.getId())) {
				if (owner.unitTypeCount((UnitType) item) >= owner.settlements.size()) {
					return NoBuildReason.LIMIT_EXCEEDED;
				}
			}
			if (!colonyUpdatableFeatures.canApplyAbilityToObject(Ability.BUILD, item)) {
				return NoBuildReason.MISSING_BUILD_ABILITY;
			}
		}
		return NoBuildReason.NONE;
	}
	
	public int getTurnsToComplete(BuildableType buildableType, ObjectIntMap<String> requiredTurnsForGood) {
		ProductionSummary production = productionSummary();
		GoodsContainer warehouse = goodsContainer;
		
		int requiredTurn = -1;
		for (RequiredGoods requiredGood : buildableType.requiredGoods()) {
			int warehouseAmount = warehouse.goodsAmount(requiredGood.getId());
			int productionAmount = production.getQuantity(requiredGood.getId());
			int goodRequiredTurn = NEVER_COMPLETE_BUILD;
			
			if (warehouseAmount < requiredGood.amount) {
				if (productionAmount > 0) {
					int reqToProduce = requiredGood.amount - warehouseAmount;
					goodRequiredTurn = reqToProduce / productionAmount;
					if (reqToProduce % productionAmount != 0) {
						goodRequiredTurn++;
					}
				} else {
					goodRequiredTurn = NEVER_COMPLETE_BUILD;
				}
			} else {
				goodRequiredTurn = 0;
			}
			requiredTurnsForGood.put(requiredGood.getId(), goodRequiredTurn);
			
			if (goodRequiredTurn > requiredTurn || goodRequiredTurn == NEVER_COMPLETE_BUILD) {
				requiredTurn = goodRequiredTurn;
			}
		}
		return requiredTurn;
	}
	
	protected void initDefaultBuildings() {
    	for (BuildingType buildingType : Specification.instance.buildingTypes.entities()) {
    		if (isAutoBuildable(buildingType)) {
    			buildings.add(new Building(Game.idGenerator, buildingType));
				colonyProduction.updateRequest();
    		}
    	}
	}
	
	private boolean isAutoBuildable(BuildingType buildingType) {
	    return buildingType.isAutomaticBuild() || isAutoBuildableInColony(buildingType);
	}
	
    private boolean isAutoBuildableInColony(BuildingType buildingType) {
    	float modified = owner.getFeatures().applyModifier(Modifier.BUILDING_PRICE_BONUS, 100, buildingType);
    	NoBuildReason noBuildReason = getNoBuildReason(buildingType);
    	return modified == 0f && noBuildReason == NoBuildReason.NONE;
    }
	
	public boolean isTileLockedBecauseNoDock(Tile tile) {
		if (tile.getType().isWater() && !colonyUpdatableFeatures.hasAbility(Ability.PRODUCE_IN_WATER)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Is tile locked for colony worker. 
	 */
	public boolean isTileLocked(Tile tile, boolean ignoreIndianOwner) {
		if (isTileLockedBecauseNoDock(tile)) {
			return true;
		}
		if (tile.hasLostCityRumour()) {
			return true;
		}
		if (tile.getOwner() != null) {
			if (tile.getOwner().isIndian()) {
				if (ignoreIndianOwner) {
					return false;
				}
				return !owner.foundingFathers.containsId(FoundingFather.PETER_MINUIT);
			} else {
				if (tile.getOwningSettlementId() != null) {
					if (tile.isOwnBySettlement(this)) {
						return false;
					}
					if (tile.hasWorkerOnTile()) {
						return true;
					}
				} 
			}
		}
		return false;
	}

	public boolean hasLootableGoods() {
	    Specification spec = Specification.instance;
	    for (Entry<String> goods : goodsContainer.entries()) {
	        if (goods.value > 0 && spec.goodsTypes.getById(goods.key).isStorable()) {
	            return true;
	        }
        }
	    return false;
	}
	
	public List<GoodsType> createLootableGoodsList() {
	    List<GoodsType> lootable = new ArrayList<GoodsType>();
	    Specification spec = Specification.instance;
        for (Entry<String> goods : goodsContainer.entries()) {
            GoodsType gt = spec.goodsTypes.getById(goods.key);
            if (goods.value > 0 && gt.isStorable()) {
                lootable.add(gt);
            }
        }
	    return lootable;
	}
	
	public Collection<Unit> settlementWorkers() {
		return colonyWorkers.entities();
	}
	
	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		return colonyWorkers;
	}

    @Override
    public void addUnit(Unit unit) {
        throw new IllegalStateException("should add unit directly to building or colony tile");
    }

    @Override
    public void removeUnit(Unit unit) {
        throw new IllegalStateException("should remove unit directly from building or colony tile");
    }
	
	public void changeOwner(Player newOwner) {
		Player oldOwner = owner;
		super.changeOwner(newOwner);
		
		if (oldOwner != null) {
			for (ColonyTile colonyTile : colonyTiles.entities()) {
				if (oldOwner.equalsId(colonyTile.tile.getOwner()) && colonyTile.tile.isOwnBySettlement(this)) {
					colonyTile.tile.changeOwner(newOwner);
				}
			}
		}
		
		buildingQueue.clear();
		ifPossibleAddFreeBuildings();
	}
	
	public ExportInfo exportInfo(GoodsType goodsType) {
		ExportInfo info = exportInfos.getByIdOrNull(goodsType.getId());
		if (info == null) {
			info = new ExportInfo(goodsType.getId());
			exportInfos.add(info);
		}
		return info;
	}

	public void exportGoods(Game game) {
		if (!hasAbility(Ability.EXPORT)) {
			return;
		}
		
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (!goodsType.isStorable()) {
				continue;
			}
			ExportInfo exportInfo = exportInfo(goodsType);
			if (!exportInfo.isExport() || !owner.market().canTradeInCustomHouse(game, owner, goodsType.getId())) {
				continue;
			}
			int exportAmount = goodsContainer.goodsAmount(goodsType) - exportInfo.getExportLevel();
			if (exportAmount <= 0) {
				continue;
			}
			TransactionEffectOnMarket transaction = owner.market().sellGoods(game, owner, goodsType, exportAmount);
			goodsContainer.decreaseGoodsQuantity(goodsType, exportAmount);
			
			System.out.println("exportGoods[" + owner.getId() + "].export " 
				+ goodsType.getId() + " " + transaction.quantity 
				+ " for price: " + transaction.netPrice
			);
		}
	}

	public ProductionSimulation productionSimulation() {
		return colonyProduction.simulation();
	}

    public static class Xml extends XmlNodeParser<Colony> {
		private static final String ATTR_PRODUCTION_BONUS = "productionBonus";
		private static final String ATTR_NAME = "name";
		private static final String ATTR_OWNER = "owner";
		private static final String ATTR_SETTLEMENT_TYPE = "settlementType";
		private static final String ATTR_SEA_CONNECTION_TO_EUROPE = "seaConnectionToEurope";

		public Xml() {
        	addNode(ColonyBuildingQueueItem.class, new ObjectFromNodeSetter<Colony, ColonyBuildingQueueItem>() {
				@Override
				public void set(Colony target, ColonyBuildingQueueItem entity) {
					target.buildingQueue.add(entity);
				}
				@Override
				public void generateXml(Colony source, ChildObject2XmlCustomeHandler<ColonyBuildingQueueItem> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.buildingQueue);
				}
			});
        	
        	addNode(ExportInfo.class, new ObjectFromNodeSetter<Colony, ExportInfo>() {
				@Override
				public void set(Colony target, ExportInfo entity) {
					target.exportInfos.add(entity);
				}

				@Override
				public void generateXml(Colony source, ChildObject2XmlCustomeHandler<ExportInfo> xmlGenerator) throws IOException {
					for (ExportInfo exportInfo : source.exportInfos.entities()) {
						if (exportInfo.isNotDefaultSettings()) {
							xmlGenerator.generateXml(exportInfo);
						}
					}
				}
        	});
        	
            addNode(GoodsContainer.class, new ObjectFromNodeSetter<Colony, GoodsContainer>() {
				@Override
				public void set(Colony target, GoodsContainer entity) {
					target.goodsContainer = entity;
				}

				@Override
				public void generateXml(Colony source, ChildObject2XmlCustomeHandler<GoodsContainer> xmlGenerator)
						throws IOException {
					xmlGenerator.generateXml(source.goodsContainer);
				}
            });
            addNodeForMapIdEntities("buildings", Building.class);
            addNodeForMapIdEntities("colonyTiles", ColonyTile.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String settlementTypeStr = attr.getStrAttribute(ATTR_SETTLEMENT_TYPE);
            Player owner = game.players.getById(attr.getStrAttribute(ATTR_OWNER));
            
            Colony colony = new Colony(
        		attr.getStrAttribute(ATTR_ID),
        		owner.nationType().settlementTypes.getById(settlementTypeStr)
    		);
            colony.name = attr.getStrAttribute(ATTR_NAME);
            colony.productionBonus.setValue(attr.getIntAttribute(ATTR_PRODUCTION_BONUS, 0));
            colony.owner = owner;
            colony.seaConnectionToEurope = attr.getBooleanAttribute(ATTR_SEA_CONNECTION_TO_EUROPE, false);

			ColonyLiberty.Xml.read(colony.colonyLiberty, attr);

            owner.settlements.add(colony);
            
            nodeObject = colony;
        }

        @Override
        public void startWriteAttr(Colony colony, XmlNodeAttributesWriter attr) throws IOException {
        	attr.setId(colony);
        	
        	attr.set(ATTR_SETTLEMENT_TYPE, colony.settlementType);
        	attr.set(ATTR_OWNER, colony.owner);
            
        	attr.set(ATTR_NAME, colony.name);
			attr.set(ATTR_PRODUCTION_BONUS, colony.productionBonus.asInt());
        	attr.set(ATTR_SEA_CONNECTION_TO_EUROPE, colony.seaConnectionToEurope, false);

        	ColonyLiberty.Xml.write(colony.colonyLiberty, attr);
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
