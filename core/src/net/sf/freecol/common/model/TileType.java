package net.sf.freecol.common.model;

import java.io.IOException;

import net.sf.freecol.common.model.map.GenerationValues;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public final class TileType extends ObjectWithFeatures {

	public static final String MODEL_TILE_LAKE = "model.tile.lake";
	public static final String GREAT_RIVER = "model.tile.greatRiver";
    public static final String OCEAN = "model.tile.ocean";
	public static final String HIGH_SEAS = "model.tile.highSeas";
	public static final String LAKE = "model.tile.lake";
	public static final String ARCTIC = "model.tile.arctic";
	public static final String HILLS = "model.tile.hills";
    public static final String MOUNTAINS = "model.tile.mountains";
	
	public final MapIdEntities<TileTypeAllowedResource> allowedResourceTypes = new MapIdEntities<TileTypeAllowedResource>();
	boolean isForest;
	private boolean canSettle;
	private boolean elevation;
	private int basicMoveCost;
	private int basicWorkTurns;
	public final ProductionInfo productionInfo = new ProductionInfo();
	private GenerationValues generationValues;
	
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

	public boolean canSettle() {
		return canSettle;
	}
	
    public boolean isDirectlyHighSeasConnected() {
        return hasAbility(Ability.MOVE_TO_EUROPE);
    }
	
    public boolean isTileImprovementAllowed(TileImprovementType impType) {
    	return impType.canApplyAllScopes(this);
    }
    
    public int getBasicMoveCost() {
    	return basicMoveCost;
    }
    
	public int getBasicWorkTurns() {
		return basicWorkTurns;
	}

	public boolean isElevation() {
		return elevation;
	}
	
	public GenerationValues getGenerationValues() {
		return generationValues;
	}
	
	public boolean canHaveResourceType(ResourceType resourceType) {
		return allowedResourceTypes.containsId(resourceType);
	}
	
	public ResourceType exposeResource() {
		TileTypeAllowedResource allowedResource = Randomizer.instance().randomOne(allowedResourceTypes.entities());
		return allowedResource.resourceType;
	}
	
	public static class Xml extends XmlNodeParser<TileType> {
		private static final String IS_FOREST = "is-forest";
		private static final String CAN_SETTLE = "can-settle";
		private static final String IS_ELEVATION = "is-elevation";
		private static final String BASIC_WORK_TURNS = "basic-work-turns";
		private static final String BASIC_MOVE_COST = "basic-move-cost";

		public Xml() {
            addNode(Modifier.class, ObjectWithFeatures.OBJECT_MODIFIER_NODE_SETTER);
            addNode(Ability.class, ObjectWithFeatures.OBJECT_ABILITY_NODE_SETTER);
            addNodeForMapIdEntities("allowedResourceTypes", TileTypeAllowedResource.class);
            
            addNode(Production.class, new ObjectFromNodeSetter<TileType, Production>() {
				@Override
				public void set(TileType target, Production entity) {
					target.productionInfo.addProduction(entity);
				}
				@Override
				public void generateXml(TileType source, ChildObject2XmlCustomeHandler<Production> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.productionInfo.productions);
				}
			});
            addNode(GenerationValues.class, "generationValues");
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute(ATTR_ID);
			boolean isForest = attr.getBooleanAttribute(IS_FOREST);
			
			TileType tileType = new TileType(id, isForest);
			tileType.basicMoveCost = attr.getIntAttribute(BASIC_MOVE_COST);
			tileType.basicWorkTurns = attr.getIntAttribute(BASIC_WORK_TURNS);
			tileType.elevation = attr.getBooleanAttribute(IS_ELEVATION, false);
			tileType.canSettle = attr.getBooleanAttribute(CAN_SETTLE, !tileType.isWater());
			nodeObject = tileType; 
		}

		@Override
		public void startWriteAttr(TileType node, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(node);
			attr.set(IS_FOREST, node.isForest);
			attr.set(BASIC_MOVE_COST, node.basicMoveCost);
			attr.set(BASIC_WORK_TURNS, node.basicWorkTurns);
			attr.set(IS_ELEVATION, node.elevation);
			attr.set(CAN_SETTLE, node.canSettle);
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
