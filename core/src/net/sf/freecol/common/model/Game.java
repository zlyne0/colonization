package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;


public class Game {

	private Specification specification;
	public Map map;
	public Player playingPlayer;
	private String currentPlayerStr;
	public final MapIdEntities<Player> players = MapIdEntities.linkedMapIdEntities();

	public static IdGenerator idGenerator;
	public String activeUnitId;
	private Turn turn;
	
	public Game() {
		turn = new Turn(0);
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
    
	private void setPlayingPlayer() {
		this.playingPlayer = players.getById(currentPlayerStr);
	}

	public void setSpecification(Specification specification) {
		this.specification = specification;
	}
	
	public static class Xml extends XmlNodeParser<Game> {
		private static final String ATTR_CURRENT_PLAYER = "currentPlayer";
		private static final String NEXT_ID_ATTR = "nextId";
		private static final String TURN_ATTR = "turn";
		private static final String ACTIVE_UNIT_ATTR = "activeUnit";

		public Xml() {
			addNode(Specification.class, "specification");
			addNodeForMapIdEntities("players", Player.class);
			addNode(Map.class, "map");
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			Game.idGenerator = new IdGenerator(attr.getIntAttribute(NEXT_ID_ATTR, 1));
			
			Game game = new Game();
			game.activeUnitId = attr.getStrAttribute(ACTIVE_UNIT_ATTR);
			game.turn = new Turn(attr.getIntAttribute(TURN_ATTR));
			game.currentPlayerStr = attr.getStrAttribute(ATTR_CURRENT_PLAYER);
			
			XmlNodeParser.game = game;
			
			nodeObject = game;
		}

		@Override
		public void startWriteAttr(Game game, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ACTIVE_UNIT_ATTR, game.activeUnitId);
			attr.set(TURN_ATTR, game.turn.getNumber());
			attr.set(NEXT_ID_ATTR, Game.idGenerator.idSequence);
			attr.set(ATTR_CURRENT_PLAYER, game.playingPlayer);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (getTagName().equals(qName)) {
				nodeObject.setPlayingPlayer(); 
			}
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
