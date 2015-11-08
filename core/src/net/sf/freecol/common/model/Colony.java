package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.specification.GameOptions;
import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Colony extends Settlement {
    public static final int LIBERTY_PER_REBEL = 200;

    private GoodsContainer goodsContainer;
    public final MapIdEntities<Building> buildings = new MapIdEntities<Building>();
    public final MapIdEntities<ColonyTile> colonyTiles = new MapIdEntities<ColonyTile>();
    
    private final List<Unit> colonyWorkers = new ArrayList<Unit>();
    private int colonyUnitsCount = -1;
    private int sonsOfLiberty = 0;
    private int tories = 0;
    private int productionBonus = 0;
    
    /**
     * The number of liberty points.  Liberty points are an
     * abstract game concept.  They are generated by but are not
     * identical to bells, and subject to further modification.
     */
    private int liberty = 0;
    
    private boolean isUndead() {
        return false;
    }

    public int getColonyUnitsCount() {
		return colonyUnitsCount;
	}
    
    public void updateColonyUnitsCount() {
    	colonyUnitsCount = 0;
    	colonyWorkers.clear();
    	for (Building building : buildings.entities()) {
    		colonyWorkers.addAll(building.workers.entities());
    		colonyUnitsCount += building.workers.size();
    	}
    	for (ColonyTile colonyTile : colonyTiles.entities()) {
    		if (colonyTile.getWorker() != null) {
    			colonyWorkers.add(colonyTile.getWorker());
    			colonyUnitsCount++;
    		}
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
        if (isUndead()) {
            return "undead";
        }
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

    public GoodsContainer getGoodsContainer() {
        return goodsContainer;
    }
    
    public int getWarehouseCapacity() {
        ObjectWithFeatures features = new ObjectWithFeatures("zaza");
        for (Building building : buildings.entities()) {
        	features.addFeatures(building.buildingType);
        }
    	return (int)features.applyModifier(Modifier.WAREHOUSE_STORAGE, 0);
    }
    
    public void production() {
    	ProductionSummary abstractWarehouse = goodsContainer.cloneGoods();
    	
    	ProductionConsumption prodCons = new ProductionConsumption();

        ObjectWithFeatures features = new ObjectWithFeatures("zaza");
        for (Building building : buildings.entities()) {
        	features.addFeatures(building.buildingType);
        }
    	
        int unitsThatUseNoBells = Specification.options.getIntValue(GameOptions.UNITS_THAT_USE_NO_BELLS);
        int amount = Math.min(unitsThatUseNoBells, getColonyUnitsCount());
    	prodCons.realProduction.addGoods("model.goods.bells", amount);
    	
        for (ColonyTile ct : colonyTiles.entities()) {
        	ProductionConsumption ps = productionSummaryForTerrain(ct.tile, ct, abstractWarehouse);
            prodCons.add(ps);
            abstractWarehouse.addGoods(ps.realProduction);
        }
        
        for (Unit worker : colonyWorkers) {
        	for (UnitConsumption uc : worker.unitType.unitConsumption.entities()) {
        		prodCons.baseConsumption.addGoods(uc.getId(), uc.getQuantity());
        		prodCons.realProduction.addGoods(uc.getId(), -uc.getQuantity());
        	}
        }

        // TODO: przemienienie wszystkich goods na ich odpowiedniki storedAs 
        
        for (Building building : buildings.entities()) {
        	ProductionConsumption ps = productionSummaryForBuilding(building, abstractWarehouse, prodCons);

            prodCons.add(ps);
            abstractWarehouse.addGoods(ps.realProduction);
        }
        
        System.out.println("warehouse = " + goodsContainer.cloneGoods());
        System.out.println("warehouse = " + abstractWarehouse);
        System.out.println("productionConsumption ##################");
        System.out.println("productionConsumption = " + prodCons);
        System.out.println("productionConsumption ##################");
    }
    
    public ProductionConsumption productionSummaryForBuilding(Building building) {
    	ProductionSummary warehouse = goodsContainer.cloneGoods();
    	return productionSummaryForBuilding(building, warehouse, new ProductionConsumption());
    }
    
    public ProductionConsumption productionSummaryForBuilding(Building building, ProductionSummary warehouse, ProductionConsumption globalProdCons) {
    	// to sa cechy koloni, docelowo cechy koloni uzupelniane przy zmianie lub cech budynkow 
    	ObjectWithFeatures colonyFeatures = new ObjectWithFeatures("zaza");
    	for (Building b : buildings.entities()) {
    		colonyFeatures.addFeatures(b.buildingType);
    	}
    	int warehouseCapacity = (int)colonyFeatures.applyModifier(Modifier.WAREHOUSE_STORAGE, 0);
    	System.out.println("warehouseCapacity = " + warehouseCapacity);
    	
    	ProductionConsumption prodCons = building.determineProductionConsumption(warehouse, warehouseCapacity, globalProdCons);

        prodCons.realProduction.applyModifiers(colonyFeatures);
        prodCons.baseProduction.applyModifiers(colonyFeatures);
    	return prodCons;
    }

	public ProductionConsumption productionSummaryForTerrain(Tile tile, ColonyTile colonyTile) {
		ProductionSummary abstractWarehouse = goodsContainer.cloneGoods();
		return productionSummaryForTerrain(tile, colonyTile, abstractWarehouse);
	}
    
	public ProductionConsumption productionSummaryForTerrain(Tile tile, ColonyTile colonyTile, ProductionSummary abstractWarehouse) {
		ProductionConsumption prodCons = new ProductionConsumption();
		
		List<Unit> workers = null;
		if (colonyTile.getWorker() != null) {
			workers = Arrays.asList(colonyTile.getWorker());
		} else {
			workers = Collections.emptyList();
		}
		colonyTile.productionInfo.determineProductionConsumption(prodCons, workers, abstractWarehouse);
		
		if (prodCons.baseProduction.isNotEmpty()) {
			prodCons.baseProduction.applyTileImprovementsModifiers(tile);
			prodCons.baseProduction.applyModifier(productionBonus());
		}
		if (prodCons.realProduction.isNotEmpty()) {
			prodCons.realProduction.applyTileImprovementsModifiers(tile);
			prodCons.realProduction.applyModifier(productionBonus());
		}
		return prodCons; 
	}
	
	public ProductionInfo maxPossibleProductionOnTile(Unit aUnit, Tile aTile) {
		ProductionInfo productionInfo = aTile.type.productionInfo;
		ProductionInfo productionSummaryForWorker = productionInfo.productionSummaryForWorker(aUnit);
		productionSummaryForWorker.applyTileImprovementsModifiers(aTile);
		return productionSummaryForWorker;
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
        }
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml() {
            addNode(GoodsContainer.class, "goodsContainer");
            addNodeForMapIdEntities("buildings", Building.class);
            addNodeForMapIdEntities("colonyTiles", ColonyTile.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String strAttribute = attr.getStrAttribute("settlementType");
            Player owner = game.players.getById(attr.getStrAttribute("owner"));
            
            Colony colony = new Colony();
            colony.id = attr.getStrAttribute("id");
            colony.name = attr.getStrAttribute("name");
            colony.sonsOfLiberty = attr.getIntAttribute("sonsOfLiberty", 0);
            colony.tories = attr.getIntAttribute("tories", 0);
            colony.productionBonus = attr.getIntAttribute("productionBonus", 0);
            colony.liberty = attr.getIntAttribute("liberty", 0);
            colony.owner = owner;
            colony.settlementType = owner.nationType.settlementTypes.getById(strAttribute);
            owner.settlements.add(colony);
            
            nodeObject = colony;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        	if (qName.equals(tagName())) {
        		((Colony)nodeObject).updateColonyUnitsCount();
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
