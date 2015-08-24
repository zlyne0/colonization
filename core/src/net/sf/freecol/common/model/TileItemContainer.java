package net.sf.freecol.common.model;

import net.sf.freecol.common.model.map.LostCityRumour;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class TileItemContainer implements Identifiable {
    private String id;
    
    public final MapIdEntities<TileImprovement> improvements = new MapIdEntities<TileImprovement>();
    public final MapIdEntities<TileResource> resources = new MapIdEntities<TileResource>();
    public final MapIdEntities<LostCityRumour> lostCityRumours = new MapIdEntities<LostCityRumour>();
    
    @Override
    public String getId() {
        return id;
    }
    
    public static class Xml extends XmlNodeParser {
        
        public Xml() {
            addNodeForMapIdEntities("improvements", TileImprovement.class);
            addNodeForMapIdEntities("resources", TileResource.class);
            addNodeForMapIdEntities("lostCityRumours", LostCityRumour.class);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TileItemContainer tic = new TileItemContainer();
            tic.id = attr.getStrAttribute("id");
            nodeObject = tic;
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "tileitemcontainer";
        }
        
    }
}