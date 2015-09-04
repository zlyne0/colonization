package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Building extends ObjectWithId {

    public BuildingType buildingType;
    public final MapIdEntities<Unit> workers = new MapIdEntities<Unit>();
    
    public Building(String id) {
        super(id);
    }

    public static class Xml extends XmlNodeParser {

        public Xml() {
            addNodeForMapIdEntities("workers", Unit.class);
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            String id = attr.getStrAttribute("id");
            Building b = new Building(id);
            
            String buildingTypeId = attr.getStrAttribute("buildingType");
            if (buildingTypeId == null) {
                throw new IllegalStateException("can not find buildingType for building " + id);
            }
            BuildingType buildingType = game.specification.buildingTypes.getById(buildingTypeId);
            b.buildingType = buildingType;
            
            nodeObject = b;
        }

        @Override
        public String getTagName() {
            return tagName();
        }
        
        public static String tagName() {
            return "building";
        }
    }
}
