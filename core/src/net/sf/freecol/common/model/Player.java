package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.NationType;
import promitech.colonization.savegame.XmlNodeAttributes;
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
    
    public String toString() {
        return "id = " + id + ", nation = " + nation;
    }
    
    public static class Xml extends XmlNodeParser {
        public Xml(XmlNodeParser parent) {
            super(parent);
        }

        @Override
        public void startElement(XmlNodeAttributes attr) {
            String idStr = attr.getStrAttribute("id");
            String nationIdStr = attr.getStrAttribute("nationId");
            String nationTypeStr = attr.getStrAttribute("nationType");
            
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
            return tagName();
        }
        
        public static String tagName() {
            return "player";
        }
    }
}
