package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class Specification {	
	public final MapIdEntities<TileType> tileTypes = new MapIdEntities<TileType>();
	public final MapIdEntities<TileImprovementType> tileImprovementTypes = new MapIdEntities<TileImprovementType>();
	public final MapIdEntities<UnitType> unitTypes = new MapIdEntities<UnitType>();
	public final MapIdEntities<UnitRole> unitRoles = new MapIdEntities<UnitRole>();
	public final MapIdEntities<ResourceType> resourceTypes = new MapIdEntities<ResourceType>();
    public final MapIdEntities<NationType> nationTypes = new MapIdEntities<NationType>();
    public final MapIdEntities<Nation> nations = new MapIdEntities<Nation>();
	
	public static class Xml extends XmlNodeParser {
		public Xml(Game.Xml parent) {
			super(parent);
			addNode(new TileType.Xml(this));
			addNode(new ResourceType.Xml(this));
			addNode(new TileImprovementType.Xml(this));
            addNode(new UnitType.Xml(this));
            addNode(new UnitRole.Xml(this));
            addNode(new NationType.IndianXml(this));
            addNode(new NationType.EuropeanXml(this));
            addNode(new Nation.Xml(this));
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

