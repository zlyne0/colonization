package net.sf.freecol.common.model;

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
            String nationTypeStr = getStrAttribute(attributes, "nationType");
            if (nationTypeStr == null) {
                return;
            }
            String nationIdStr = getStrAttribute(attributes, "nationId");
            
            Player player = new Player();
            player.id = getStrAttribute(attributes, "id");
            player.nation = specification.nations.getById(nationIdStr);
            player.nationType = specification.nationTypes.getById(nationTypeStr);
            
            Game.Xml gameXml = getParentXmlParser();
            gameXml.game.players.add(player);
        }

        @Override
        public String getTagName() {
            return "player";
        }
    }

}
