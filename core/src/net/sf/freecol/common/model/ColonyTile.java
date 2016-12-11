package net.sf.freecol.common.model;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyTile extends ObjectWithId implements ProductionLocation {

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

	public Unit takeWorker() {
		Unit takenWorker = worker;
		worker = null;
		productionInfo.clear();
		return takenWorker;
	}

	public Unit getWorker() {
		return worker;
	}
	
	public void setWorker(Unit worker) {
		this.worker = worker;
	}

	public String toString() {
	    return "ColonyTile workTileId[" + id + "]";
	}
	
    public static class Xml extends XmlNodeParser {

    	public Xml() {
    		addNode(Unit.class, "worker");
    		addNode(Production.class, new ObjectFromNodeSetter<ColonyTile, Production>() {
				@Override
				public void set(ColonyTile target, Production entity) {
					target.productionInfo.addProduction(entity);
				}
			});
		}
    	
		@Override
		public void startElement(XmlNodeAttributes attr) {
			ColonyTile colonyTile = new ColonyTile(attr.getStrAttribute("workTile"));
			nodeObject = colonyTile;
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
