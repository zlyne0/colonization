package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLocation;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class HighSeas implements Identifiable, UnitLocation {
    private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    
    @Override
    public String getId() {
        throw new IllegalStateException("object without id");
    }

	@Override
	public MapIdEntities<Unit> getUnits() {
		return units;
	}
    
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<HighSeas,Unit>() {
                @Override
                public void set(HighSeas highSeas, Unit unit) {
                    unit.changeUnitLocation(highSeas);
                }
            });
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            HighSeas h = new HighSeas();
            
            nodeObject = h;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "highSeas";
        }
        
    }

}