package net.sf.freecol.common.model;

import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileItemContainer implements Identifiable {
    
    public final MapIdEntities<TileImprovement> improvements = new MapIdEntities<TileImprovement>();
    public final MapIdEntities<TileResource> resources = new MapIdEntities<TileResource>();
    
    private boolean lostCityRumours = false;
    
    @Override
    public String getId() {
    	throw new IllegalStateException("object without id");
    }
    
    public int getMoveCost(Direction moveDirection, int basicMoveCost) {
        int moveCost = basicMoveCost;
        Direction reverseMoveDirection = moveDirection.getReverseDirection();
        for (TileImprovement item : improvements.entities()) {
            if (item.isComplete()) {
                moveCost = Math.min(moveCost, item.getMoveCost(reverseMoveDirection, moveCost));
            }
        }
        return moveCost;
    }
    
	public boolean hasImprovementType(String improvementTypeId) {
		for (TileImprovement tileImprovement : improvements.entities()) {
			if (tileImprovement.type.equalsId(improvementTypeId)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		return improvements.isEmpty() && resources.isEmpty() && lostCityRumours == false;
	}

	public boolean isLostCityRumours() {
		return lostCityRumours;
	}

	public void setLostCityRumours(boolean lostCityRumours) {
		this.lostCityRumours = lostCityRumours;
	}

    public static class Xml extends XmlNodeParser {
        
        public Xml() {
            addNodeForMapIdEntities("improvements", TileImprovement.class);
            addNodeForMapIdEntities("resources", TileResource.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TileItemContainer tic = new TileItemContainer();
            nodeObject = tic;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
        	if (attr.isQNameEquals("lostCityRumour")) {
        		((TileItemContainer)nodeObject).lostCityRumours = true;
        	}
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "tileitemcontainer";
        }
        
    }
}
