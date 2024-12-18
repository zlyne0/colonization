package net.sf.freecol.common.model;

import com.badlogic.gdx.math.GridPoint2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.ai.AIContainer;
import net.sf.freecol.common.model.map.LostCityRumour;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.colonization.ui.resources.Messages;


public class Game {

	private Specification specification;
	public Map map;
	public Player playingPlayer;
	private final GridPoint2 playerCenterScreen = new GridPoint2(-1, -1);
	private String currentPlayerStr;
	public final MapIdEntities<Player> players = MapIdEntities.linkedMapIdEntities();

	public static IdGenerator idGenerator;
	private String uuid;
	public String activeUnitId;
	private Turn turn;
	private final List<String> citiesOfCibola = new ArrayList<String>(7);
	
	public AIContainer aiContainer = new AIContainer();
	
	public Game() {
		turn = new Turn(0);
		uuid = UUID.randomUUID().toString();
	}
	
	public String toString() {
		return "map[" + map + "]";
	}

	public Turn getTurn() {
		return turn;
	}

	public void increaseTurnNumber() {
		this.turn = turn.increaseTurnNumber();
	}

	public String uuid() {
		return uuid;
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
		if (playerCenterScreen.x == -1 && playerCenterScreen.y == -1) {
			playerCenterScreen.set(playingPlayer.getEntryLocation());
		}
	}

	public void setSpecification(Specification specification) {
		this.specification = specification;
	}
	
	public List<String> getCitiesOfCibola() {
		return citiesOfCibola;
	}
	
	public String removeNextCityOfCibola() {
		return citiesOfCibola.remove(0);
	}
	
	public void initCibolaCityNamesForNewGame() {
		int cibolaCitiesCount = Messages.keyMessagePrefixCount(LostCityRumour.CIBOLA_CITY_NAME_KEY_PREFIX);
		for (int i=0; i<cibolaCitiesCount; i++) {
			citiesOfCibola.add(LostCityRumour.CIBOLA_CITY_NAME_KEY_PREFIX + i);
		}
	}

	public GridPoint2 getPlayerCenterScreen() {
		return playerCenterScreen;
	}

	public static class Xml extends XmlNodeParser<Game> {
		private static final String UUID_ATTR = "uuid";
		private static final String ELEMENT_CIBOLA = "cibola";
		private static final String ATTR_CURRENT_PLAYER = "currentPlayer";
		private static final String NEXT_ID_ATTR = "nextId";
		private static final String TURN_ATTR = "turn";
		private static final String ACTIVE_UNIT_ATTR = "activeUnit";
		private static final String CURRENT_PLAYER_CENTER = "currentPlayerCenter";

		public Xml() {
			addNode(Specification.class, "specification");
			addNodeForMapIdEntities("players", Player.class);
			addNode(Map.class, "map");
			addNode(AIContainer.class, "aiContainer");
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			Game.idGenerator = new IdGenerator(attr.getIntAttribute(NEXT_ID_ATTR, 1));
			
			Game game = new Game();
			game.uuid = attr.getStrAttribute(UUID_ATTR, UUID.randomUUID().toString());
			game.activeUnitId = attr.getStrAttribute(ACTIVE_UNIT_ATTR);
			game.turn = new Turn(attr.getIntAttribute(TURN_ATTR));
			game.currentPlayerStr = attr.getStrAttribute(ATTR_CURRENT_PLAYER);
			game.playerCenterScreen.set(attr.getPoint(CURRENT_PLAYER_CENTER, -1, -1));

			XmlNodeParser.game = game;
			
			nodeObject = game;
		}

		@Override
		public void startWriteAttr(Game game, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(UUID_ATTR, game.uuid);
			attr.set(ACTIVE_UNIT_ATTR, game.activeUnitId);
			attr.set(TURN_ATTR, game.turn.getNumber());
			attr.set(NEXT_ID_ATTR, Game.idGenerator.idSequence);
			attr.set(ATTR_CURRENT_PLAYER, game.playingPlayer);
			attr.setPoint(CURRENT_PLAYER_CENTER, game.playerCenterScreen);
			
			for (String cityOfCibola : game.citiesOfCibola) {
				attr.xml.element(ELEMENT_CIBOLA);
				attr.set(ATTR_ID, cityOfCibola);
				attr.xml.pop();
			}
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals(ELEMENT_CIBOLA)) {
				nodeObject.citiesOfCibola .add(attr.getStrAttribute(ATTR_ID));
			}
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
