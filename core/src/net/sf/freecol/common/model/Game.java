package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class Game implements Identifiable {

    private String id;
    
	public Map map;
	public Player playingPlayer;
	public Specification specification;
	public final MapIdEntities<Player> players = new MapIdEntities<Player>();
	
	public String toString() {
		return "map[" + map + "], specification = " + specification;
	}

    @Override
    public String getId() {
        return id;
    }
	
	public static class Xml extends XmlNodeParser {
		public Game game = new Game();
		
		public Xml(Specification defaultSpecification) {
			addNode(new Specification.Xml());
			addNode(new Map.Xml());
			addNodeForMapIdEntities("players", Player.class);
			
			game.specification = defaultSpecification;
			XmlNodeParser.game = game;
			this.nodeObject = game;
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
		    System.out.println("static game");
		}
		
		@Override
		public String getTagName() {
			return "game";
		}
	}
}
