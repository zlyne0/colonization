package net.sf.freecol.common.model;

import net.sf.freecol.common.model.map.LostCityRumour;
import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileItemContainer implements Identifiable {
    
    public final MapIdEntities<TileImprovement> improvements = new MapIdEntities<TileImprovement>();
    public final MapIdEntities<TileResource> resources = new MapIdEntities<TileResource>();
    public final MapIdEntities<LostCityRumour> lostCityRumours = new MapIdEntities<LostCityRumour>();
    
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
		return improvements.isEmpty() && resources.isEmpty() && lostCityRumours.isEmpty();
	}
    
    public static class Xml extends XmlNodeParser {
        
        public Xml() {
            addNodeForMapIdEntities("improvements", TileImprovement.class);
            addNodeForMapIdEntities("resources", TileResource.class);
            addNodeForMapIdEntities("lostCityRumours", LostCityRumour.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TileItemContainer tic = new TileItemContainer();
            nodeObject = tic;
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
