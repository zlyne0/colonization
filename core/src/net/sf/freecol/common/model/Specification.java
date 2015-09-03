package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.GoodsType;
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
    public final MapIdEntities<GoodsType> goodsTypes = new MapIdEntities<GoodsType>();

    @Override
    public String getId() {
        return "freecol";
    }
    
    public boolean isUnitTypeExpert(UnitType unitType) {
		for (UnitRole ur : unitRoles.entities()) {
			if (ur.expertUnitTypeId != null && ur.expertUnitTypeId.equals(unitType.id)) {
				return true;
			}
		}
    	return false;
    }
    
	public static class Xml extends XmlNodeParser {
		public Xml() {
			addNodeForMapIdEntities("tile-types", "tileTypes", TileType.class);
            addNodeForMapIdEntities("resource-types", "resourceTypes", ResourceType.class);
            addNodeForMapIdEntities("tileimprovement-types", "tileImprovementTypes", TileImprovementType.class);
            addNodeForMapIdEntities("unit-types", "unitTypes", UnitType.class);
            addNodeForMapIdEntities("roles", "unitRoles", UnitRole.class);
            addNodeForMapIdEntities("european-nation-types", "nationTypes", EuropeanNationType.class);
            addNodeForMapIdEntities("indian-nation-types", "nationTypes", IndianNationType.class);
            addNodeForMapIdEntities("nations", "nations", Nation.class);
            addNodeForMapIdEntities("goods-types", "goodsTypes", GoodsType.class);
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

