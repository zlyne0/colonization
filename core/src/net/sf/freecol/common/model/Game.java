package net.sf.freecol.common.model;

import java.util.HashSet;
import java.util.Set;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class Game implements Identifiable {

	public Map map;
	public Player playingPlayer;
	public final MapIdEntities<Player> players = new MapIdEntities<Player>();

	public static IdGenerator idGenerator;
	public String activeUnitId;
	private Turn turn;
	
	public Game() {
		turn = new Turn(0);
	}
	
	@Override
	public String getId() {
		throw new IllegalStateException("no id for object");
	}
	
	public String toString() {
		return "map[" + map + "]";
	}

	public Turn getTurn() {
		return turn;
	}
	
	public Set<String> getEuropeanNationIds() {
		Set<String> nationsIds = new HashSet<String>(players.size());
		for (Player player : players.entities()) {
			if (player.isEuropean()) {
				nationsIds.add(player.nation().getId());
			}
		}
		return nationsIds;
	}
    
	public static class Xml extends XmlNodeParser {
		public Xml() {
			addNode(new Specification.Xml());
			addNode(Map.class, "map");
			addNodeForMapIdEntities("players", Player.class);
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			System.out.println("startElement game");
			Game.idGenerator = new IdGenerator(attr.getIntAttribute("nextId", 1));
			
			Game game = new Game();
			game.activeUnitId = attr.getStrAttribute("activeUnit");
			game.turn = new Turn(attr.getIntAttribute("turn"));
			
			XmlNodeParser.game = game;
			
			nodeObject = game;
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "game";
		}
	}
}
