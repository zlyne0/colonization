package net.sf.freecol.common.model;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.specification.BuildingType.Production;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Colony extends Settlement {

    private GoodsContainer goodsContainer;
    public final MapIdEntities<Building> buildings = new MapIdEntities<Building>();
    public final MapIdEntities<ColonyTile> colonyTiles = new MapIdEntities<ColonyTile>();
    
    private int colonyUnitsCount = -1;
    
    private boolean isUndead() {
        return false;
    }

    public int getColonyUnitsCount() {
		return colonyUnitsCount;
	}
    
    private void updateColonyUnitsCount() {
    	colonyUnitsCount = 0;
    	for (Building building : buildings.entities()) {
    		colonyUnitsCount += building.workers.size();
    	}
    	for (ColonyTile colonyTile : colonyTiles.entities()) {
    		if (colonyTile.getWorker() != null) {
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
    
    public BuildingProductionInfo productionInfo(Building building) {
        BuildingProductionInfo info = new BuildingProductionInfo();
        for (Production production : building.buildingType.productions) {
    		production.sumProductionType(info.goods, building.workers.entities());
        }
        return info;
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
