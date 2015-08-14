package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.NationType;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class Specification implements Identifiable {	
	public final MapIdEntities<TileType> tileTypes = new MapIdEntities<TileType>();
	public final MapIdEntities<TileImprovementType> tileImprovementTypes = new MapIdEntities<TileImprovementType>();
	public final MapIdEntities<UnitType> unitTypes = new MapIdEntities<UnitType>();
	public final MapIdEntities<UnitRole> unitRoles = new MapIdEntities<UnitRole>();
	public final MapIdEntities<ResourceType> resourceTypes = new MapIdEntities<ResourceType>();
    public final MapIdEntities<NationType> nationTypes = new MapIdEntities<NationType>();
    public final MapIdEntities<Nation> nations = new MapIdEntities<Nation>();

    @Override
    public String getId() {
        return "freecol";
    }
    
	public static class Xml extends XmlNodeParser {
		public Xml(Game.Xml parent) {
			super(parent);
			
			addNode(new MapIdEntities.Xml(this, "tile-types", "tileTypes", TileType.class));
			addNode(new MapIdEntities.Xml(this, "resource-types", "resourceTypes", ResourceType.class));
            addNode(new MapIdEntities.Xml(this, "tileimprovement-types", "tileImprovementTypes", TileImprovementType.class));
            addNode(new MapIdEntities.Xml(this, "unit-types", "unitTypes", UnitType.class));
            addNode(new MapIdEntities.Xml(this, "roles", "unitRoles", UnitRole.class));
            
            addNode(new MapIdEntities.Xml(this, "european-nation-types", "nationTypes", EuropeanNationType.class));
            addNode(new MapIdEntities.Xml(this, "indian-nation-types", "nationTypes", IndianNationType.class));
            
            addNode(new MapIdEntities.Xml(this, "nations", "nations", Nation.class));
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
		    Specification specification = new Specification();
		    nodeObject = specification;
		    
		    game.specification = specification;
		}

		@Override
		public String getTagName() {
			return "freecol-specification";
		}
	}
}

