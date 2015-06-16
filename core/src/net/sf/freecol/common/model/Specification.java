package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Specification {
	private List<TileType> tileTypes = new ArrayList<TileType>();
	private Map<String,TileType> tileTypeByTileTypeStr = new HashMap<String, TileType>();
	
	private Map<String,ResourceType> resourceTypes = new HashMap<String,ResourceType>();
	private Map<String,TileImprovementType> tileImprovementTypeByStr = new HashMap<String, TileImprovementType>();
	
	public void addTileType(TileType tileType) {
		tileType.setOrder(tileTypes.size());
		this.tileTypes.add(tileType);
		this.tileTypeByTileTypeStr.put(tileType.getTypeStr(), tileType);
	}

	public TileType getTileTypeByTypeStr(String tileTypeStr) {
		TileType tileType = tileTypeByTileTypeStr.get(tileTypeStr);
		if (tileType == null) {
			throw new IllegalArgumentException("can not find tile type by str: " + tileTypeStr);
		}
		return tileType;
	}
	
	public void addResourceType(ResourceType resourceType) {
		resourceTypes.put(resourceType.getResourceTypeId(), resourceType);
	}
	
	public void addTileimprovementType(TileImprovementType type) {
		tileImprovementTypeByStr.put(type.id, type);
	}
	
	public TileImprovementType getTileImprovementTypeBy(String typeStr) {
		TileImprovementType type = tileImprovementTypeByStr.get(typeStr);
		if (type == null) {
			throw new IllegalArgumentException("can not find tile improvment type by str: " + typeStr);
		}
		return type;
	}
	
	public ResourceType getResourceTypeByStr(String resourceTypeStr) {
		ResourceType resourceType = resourceTypes.get(resourceTypeStr);
		if (resourceType == null) {
			throw new IllegalArgumentException("can not find resource type by str: " + resourceTypeStr);
		}
		return resourceType;
	}
}

