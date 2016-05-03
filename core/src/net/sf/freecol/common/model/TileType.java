package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public final class TileType extends ObjectWithFeatures {

	private static final String MODEL_TILE_LAKE = "model.tile.lake";
	private static final String GREAT_RIVER = "model.tile.greatRiver";
    private static final String OCEAN = "model.tile.ocean";
	private static final String HIGH_SEAS = "model.tile.highSeas";
	
	public final MapIdEntities<TileTypeAllowedResource> allowedResourceTypes = new MapIdEntities<TileTypeAllowedResource>();
	boolean isForest;
	private int basicMoveCost;
	private int basicWorkTurns;
	public ProductionInfo productionInfo = new ProductionInfo();
	
	public TileType(String id, boolean isForest) {
		super(id);
		this.isForest = isForest;
	}
    
	public String toString() {
		return id + ":" + getInsertOrder();
	}
	
	public boolean isWater() {
		return id.equals(OCEAN) || id.equals(HIGH_SEAS) || id.equals(GREAT_RIVER) || id.equals(MODEL_TILE_LAKE);
	}

	public boolean isHighSea() {
		return id.equals(HIGH_SEAS);
	}
	
	public boolean isLand() {
		return !isWater();
	}

	public boolean hasTheSameTerain(TileType tType) {
	    return !hasDifferentTerain(tType);
	}
	
	public boolean hasDifferentTerain(TileType tType) {
		return !this.id.equals(tType.id);
	}

	public boolean isForested() {
		return isForest;
	}

    public boolean isDirectlyHighSeasConnected() {
        return hasAbility(Ability.MOVE_TO_EUROPE);
    }
	
    public int getBasicMoveCost() {
    	return basicMoveCost;
    }
    
	public int getBasicWorkTurns() {
		return basicWorkTurns;
	}
    
	public boolean canHaveResourceType(ResourceType resourceType) {
		return allowedResourceTypes.containsId(resourceType);
	}
	
	public ResourceType exposeResource() {
		TileTypeAllowedResource allowedResource = Randomizer.getInstance().randomOne(allowedResourceTypes.entities());
		return allowedResource.resourceType;
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNode(Production.class, new ObjectFromNodeSetter<TileType, Production>() {
				@Override
				public void set(TileType target, Production entity) {
					target.productionInfo.addProduction(entity);
				}
			});
            addNodeForMapIdEntities("allowedResourceTypes", TileTypeAllowedResource.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			boolean isForest = attr.getBooleanAttribute("is-forest");
			
			TileType tileType = new TileType(id, isForest);
			tileType.basicMoveCost = attr.getIntAttribute("basic-move-cost");
			tileType.basicWorkTurns = attr.getIntAttribute("basic-work-turns");
			
			nodeObject = tileType; 
		}

		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
		    return "tile-type";
		}
	}
}
