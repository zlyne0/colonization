package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.IndianNationType;
import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Specification implements Identifiable {
	
	public static class Options {
		
		public boolean getBoolean(String code) {
			return false;
		}
		
		public void reset() {
		}
	}
	
	public static final Options options = new Options();
	
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
			
			addNodeForMapIdEntities("tile-types", "tileTypes", TileType.class);
            addNodeForMapIdEntities("resource-types", "resourceTypes", ResourceType.class);
            addNodeForMapIdEntities("tileimprovement-types", "tileImprovementTypes", TileImprovementType.class);
            addNodeForMapIdEntities("unit-types", "unitTypes", UnitType.class);
            addNodeForMapIdEntities("roles", "unitRoles", UnitRole.class);
            addNodeForMapIdEntities("european-nation-types", "nationTypes", EuropeanNationType.class);
            addNodeForMapIdEntities("indian-nation-types", "nationTypes", IndianNationType.class);
            addNodeForMapIdEntities("nations", "nations", Nation.class);
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
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

