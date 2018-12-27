package net.sf.freecol.common.model;

import java.io.IOException;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyTile extends ObjectWithId implements ProductionLocation, UnitLocation {

	private Unit worker;
	public final ProductionInfo productionInfo = new ProductionInfo();
    public Tile tile;
	
	public ColonyTile(String id) {
		// colonyTile has the same id like tile which it concern 
		super(id);
	}
	
	public ColonyTile(Tile tile) {
		super(tile.getId());
		this.tile = tile;
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
	    return "ColonyTile workTileId[" + getId() + "]";
	}
	
    @Override
    public MapIdEntitiesReadOnly<Unit> getUnits() {
        throw new IllegalStateException("there are no units in colony tile " + getId());
    }

    @Override
    public void addUnit(Unit unit) {
        worker = unit;
    }

    @Override
    public void removeUnit(Unit unit) {
    	unit.reduceMovesLeftToZero();
        worker = null;
        productionInfo.clear();
    }
    
    @Override
    public boolean canAutoLoadUnit() {
        return false;
    }

    @Override
    public boolean canAutoUnloadUnits() {
        return false;
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
					target.productionInfo.addProduction(entity);
				}
				@Override
				public void generateXml(ColonyTile source, ChildObject2XmlCustomeHandler<Production> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.productionInfo.productions);
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
