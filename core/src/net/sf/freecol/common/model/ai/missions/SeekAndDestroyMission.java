package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class SeekAndDestroyMission extends AbstractMission {

    private final String unitId;
    
    private SeekAndDestroyMission(String id, String unitId) {
        super(id);
        this.unitId = unitId;
    }
    
    public SeekAndDestroyMission(Unit unit) {
        super(Game.idGenerator.nextId(SeekAndDestroyMission.class));
        this.unitId = unit.getId();
    }

    @Override
    public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
        unitMissionsMapping.blockUnit(unitId, this);
    }

    @Override
    public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
        unitMissionsMapping.unblockUnitFromMission(unitId, this);
    }

    public String getUnitId() {
        return unitId;
    }

    @Override
    public String toString() {
        return "SeekAndDestroyMission unit: " + unitId;
    }

    public static class Xml extends AbstractMission.Xml<SeekAndDestroyMission> {

        private static final String ATTR_UNIT = "unit";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            SeekAndDestroyMission m = new SeekAndDestroyMission(
                    attr.getId(),
                    attr.getStrAttributeNotNull(ATTR_UNIT)
            );
            nodeObject = m;
        }

        @Override
        public void startWriteAttr(SeekAndDestroyMission m, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(m);
            attr.set(ATTR_UNIT, m.unitId);
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
