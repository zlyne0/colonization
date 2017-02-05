package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
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
    
	public static class Xml extends XmlNodeParser<Game> {
		private static final String NEXT_ID_ATTR = "nextId";
		private static final String TURN_ATTR = "turn";
		private static final String ACTIVE_UNIT_ATTR = "activeUnit";

		public Xml() {
			addNode(new Specification.Xml());
			addNode(Map.class, "map");
			addNodeForMapIdEntities("players", Player.class);
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			Game.idGenerator = new IdGenerator(attr.getIntAttribute(NEXT_ID_ATTR, 1));
			
			Game game = new Game();
			game.activeUnitId = attr.getStrAttribute(ACTIVE_UNIT_ATTR);
			game.turn = new Turn(attr.getIntAttribute(TURN_ATTR));
			
			XmlNodeParser.game = game;
			
			nodeObject = game;
		}

		@Override
		public void startWriteAttr(Game game, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ACTIVE_UNIT_ATTR, game.activeUnitId);
			attr.set(TURN_ATTR, game.turn.getNumber());
			attr.set(NEXT_ID_ATTR, Game.idGenerator.idSequence);
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
