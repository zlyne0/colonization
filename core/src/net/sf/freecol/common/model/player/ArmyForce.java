package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.MapIdEntities;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class ArmyForce implements Identifiable {

    public final MapIdEntities<ArmyForceAbstractUnit> navalUnits = new MapIdEntities<ArmyForceAbstractUnit>();
    public final MapIdEntities<ArmyForceAbstractUnit> landUnits = new MapIdEntities<ArmyForceAbstractUnit>();

    @Override
    public String getId() {
        throw new IllegalStateException("object do not has id");
    }
    
    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNodeForMapIdEntities("navalUnits", "navalUnits", ArmyForceAbstractUnit.class);
            addNodeForMapIdEntities("landUnits", "landUnits", ArmyForceAbstractUnit.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            nodeObject = new ArmyForce();
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "expeditionaryForce";
        }
        
    }
    
}
