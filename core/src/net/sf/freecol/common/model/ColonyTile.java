package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.GoodsType;

import java.io.IOException;
import java.util.List;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyTile extends ObjectWithId implements ProductionLocation, UnitLocation {

	private Unit worker;
	public Production production = Production.EMPTY_READONLY;

    public Tile tile;
	
	private ColonyTile(String id) {
		// colonyTile has the same id like tile which it concern 
		super(id);
	}
	
	public ColonyTile(Tile tile) {
		super(tile.getId());
		this.tile = tile;
	}

	@Override
	public String productionLocationId() {
		return tile.getId();
	}

	public Unit getWorker() {
		return worker;
	}
	
	public boolean hasWorker(Unit w) {
	    return worker != null && worker.equalsId(w);
	}
	
	public boolean hasWorker() {
	    return worker != null;
	}
	
    public boolean hasNotWorker() {
        return worker == null;
    }
	
    @Override
	public String toString() {
	    return "ColonyTile workTileId[" + getId() + ", " + tile.getType().getId() + "]";
	}
	
    @Override
    public MapIdEntitiesReadOnly<Unit> getUnits() {
        return new OneMapIdEntitiesReadOnly<Unit>(worker);
    }

    @Override
    public void addUnit(Unit unit) {
        worker = unit;
    }

    @Override
    public void removeUnit(Unit unit) {
    	unit.reduceMovesLeftToZero();
        worker = null;
        production = Production.EMPTY_READONLY;
    }

    public void initMaxPossibleProductionOnTile() {
		List<Production> tileProductions;
		if (worker != null) {
			tileProductions = tile.getType().productionInfo.getAttendedProductions();
		} else {
			tileProductions = tile.getType().productionInfo.getUnattendedProductions();
		}

		Production tmpProd = new Production(worker == null);

		Production maxProd = null;
		for (Production p : tileProductions) {
			tmpProd.init(p);
			if (worker != null) {
				tmpProd.applyModifiers(worker.unitType);
			}
			if (tile.getOwner() != null) {
				tmpProd.applyModifiers(tile.getOwner().foundingFathers.entities());
			}
			tmpProd.applyTileImprovementsModifiers(tile);
			if (maxProd == null || tmpProd.isProductMoreThen(maxProd)) {
				maxProd = p;
			}
		}
		if (maxProd == null) {
			throw new IllegalStateException("can not find max production for tile type " + tile.getType());
		}
		this.production = maxProd;
	}

	public Production tileProduction() {
		return this.production;
	}

	public void initProduction(Production production) {
		this.production = production;
	}

	public void initProducitonType(GoodsType goodsType) {
		for (Production p : tile.getType().productionInfo.productions) {
			if (p.outputTypesEquals(goodsType.getId())) {
				this.production = p;
			}
		}
	}

	public boolean hasFoodGrainProduction() {
		return this.production.containsOutputGoods(GoodsType.GRAIN);
	}

	public static class Xml extends XmlNodeParser<ColonyTile> {

    	private static final String ATTR_WORK_TILE = "workTile";

		public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<ColonyTile, Unit>() {
                @Override
                public void set(ColonyTile target, Unit entity) {
                    entity.changeUnitLocation(target);
                }

                @Override
                public void generateXml(ColonyTile source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
                    if (source.worker != null) {
                        xmlGenerator.generateXml(source.worker);
                    }
                }
            });
    		
    		addNode(Production.class, new ObjectFromNodeSetter<ColonyTile, Production>() {
				@Override
				public void set(ColonyTile target, Production entity) {
					target.production = entity;
				}
				@Override
				public void generateXml(ColonyTile source, ChildObject2XmlCustomeHandler<Production> xmlGenerator) throws IOException {
					if (source.production.isNotEmpty()) {
						xmlGenerator.generateXml(source.production);
					}
				}
			});
		}
    	
		@Override
		public void startElement(XmlNodeAttributes attr) {
			ColonyTile colonyTile = new ColonyTile(attr.getStrAttribute(ATTR_WORK_TILE));
			nodeObject = colonyTile;
		}

		@Override
		public void startWriteAttr(ColonyTile node, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_WORK_TILE, node);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}
    	
        public static String tagName() {
            return "colonyTile";
        }
		
    }
}
