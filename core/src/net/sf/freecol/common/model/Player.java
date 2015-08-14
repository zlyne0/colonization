package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.NationType;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;

public class Player implements Identifiable {

    private String id;
    Nation nation;
    NationType nationType;
    public MapIdEntities<Unit> units = new MapIdEntities<Unit>();
    public MapIdEntities<Settlement> settlements = new MapIdEntities<Settlement>();
    
    @Override
    public String getId() {
        return id;
    }
    
    public Nation getNation() {
    	return nation;
    }
    
    public static class Xml extends XmlNodeParser {

        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(String qName, Attributes attributes) {
            String idStr = getStrAttribute(attributes, "id");
            String nationIdStr = getStrAttribute(attributes, "nationId");
            String nationTypeStr = getStrAttribute(attributes, "nationType");
            
            Player player = new Player();
            player.id = idStr;
            player.nation = game.specification.nations.getById(nationIdStr);
            if (nationTypeStr != null) {
                player.nationType = game.specification.nationTypes.getById(nationTypeStr);
            }
            
            nodeObject = player;
        }

        @Override
        public String getTagName() {
            return "player";
        }
    }
}
