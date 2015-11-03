package net.sf.freecol.common.model;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ColonyTile extends ObjectWithId {

	private Unit worker;
	private String workTileId;
	public final ProductionInfo productionInfo = new ProductionInfo();
    Tile tile;
	
	public ColonyTile(String id) {
		super(id);
	}

	public String getWorkTileId() {
		return workTileId;
	}

	public void moveWorkerTo(ColonyTile destColonyTile) {
		Unit takenWorker = takeWorker();
		destColonyTile.worker = takenWorker;
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
	    return "ColonyTile id[" + id +"], worktileId[" + workTileId + "]";
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
			String id = attr.getStrAttribute("id");
			
			ColonyTile colonyTile = new ColonyTile(id);
			colonyTile.workTileId = attr.getStrAttribute("workTile");
			
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
