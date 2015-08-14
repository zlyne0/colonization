package net.sf.freecol.common.model;

import net.sf.freecol.common.model.map.LostCityRumour;

import org.xml.sax.Attributes;

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
        
        public Xml(XmlNodeParser parent) {
            super(parent);
            
            addNode(new MapIdEntities.Xml(this, "improvements", TileImprovement.class));
            addNode(new MapIdEntities.Xml(this, "resources", TileResource.class));
            addNode(new MapIdEntities.Xml(this, "lostCityRumours", LostCityRumour.class));
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            TileItemContainer tic = new TileItemContainer();
            tic.id = getStrAttribute(attributes, "id");
            nodeObject = tic;
        }
        
        @Override
        public String getTagName() {
            return "tileitemcontainer";
        }
        
    }
}
