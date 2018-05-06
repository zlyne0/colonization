package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class SeekAndDestroyMission extends AbstractMission {

    public final Unit unit;
    
    private SeekAndDestroyMission(String id, Unit unit) {
        super(id);
        this.unit = unit;
    }
    
    public SeekAndDestroyMission(Unit unit) {
        super(Game.idGenerator.nextId(SeekAndDestroyMission.class));
        this.unit = unit;
    }

    @Override
    public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
        unitMissionsMapping.blockUnit(unit, this);
    }

    @Override
    public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unit, this);
    }
    
    @Override
    public String toString() {
        return "SeekAndDestroyMission unit: " + unit;
    }

    public static class Xml extends AbstractMission.Xml<SeekAndDestroyMission> {

        private static final String ATTR_UNIT = "unit";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String unitId = attr.getStrAttributeNotNull(ATTR_UNIT);
            Unit unit = PlayerMissionsContainer.Xml.getPlayerUnit(unitId);
            
            SeekAndDestroyMission m = new SeekAndDestroyMission(attr.getId(), unit);
            nodeObject = m;
        }

        @Override
        public void startWriteAttr(SeekAndDestroyMission m, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(m);
            attr.set(ATTR_UNIT, m.unit);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "seekAndDestroyMission";
        }
        
    }
    
    
}
