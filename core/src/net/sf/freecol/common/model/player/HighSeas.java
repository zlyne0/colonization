package net.sf.freecol.common.model.player;

import java.io.IOException;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLocation;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class HighSeas implements UnitLocation {
    private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    
	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		return units;
	}

    @Override
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
        units.removeId(unit);
    }
	
	@Override
	public boolean canAutoLoadUnit() {
		return false;
	}
    
	@Override
	public boolean canAutoUnloadUnits() {
		return false;
	}
	
    public static class Xml extends XmlNodeParser<HighSeas> {

        public Xml() {
            addNode(Unit.class, new ObjectFromNodeSetter<HighSeas,Unit>() {
                @Override
                public void set(HighSeas highSeas, Unit unit) {
                    unit.changeUnitLocation(highSeas);
                }
				@Override
				public void generateXml(HighSeas source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.units.entities());
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
