package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class Specification {
	private List<TileType> tileTypes = new ArrayList<TileType>();
	private Map<String,TileType> tileTypeByTileTypeStr = new HashMap<String, TileType>();
	
	public final MapIdEntities<TileImprovementType> tileImprovementTypes = new MapIdEntities<TileImprovementType>();
	public final MapIdEntities<UnitType> unitTypes = new MapIdEntities<UnitType>();
	public final MapIdEntities<ResourceType> resourceTypes = new MapIdEntities<ResourceType>();
	
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
	
	public static class Xml extends XmlNodeParser {
		public Xml(Game.Xml parent) {
			super(parent);
			addNode(new TileType.Xml(this));
			addNode(new ResourceType.Xml(this));
			addNode(new TileImprovementType.Xml(this));
            addNode(new UnitType.Xml(this));
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			specification = new Specification();
			Game.Xml xmlGame = getParentXmlParser();
			xmlGame.game.specification = specification;
		}

		@Override
		public String getTagName() {
			return "freecol-specification";
		}
	}
	
	
}

