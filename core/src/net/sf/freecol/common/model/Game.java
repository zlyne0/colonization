package net.sf.freecol.common.model;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class Game implements Identifiable {

    private final String id;
    
	public Map map;
	public Player playingPlayer;
	public final MapIdEntities<Player> players = new MapIdEntities<Player>();

	public static IdGenerator idGenerator;
	public String activeUnitId;
	
	public Game(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public String toString() {
		return "map[" + map + "]";
	}
	
    public void afterLoadGame() {
        for (Player player : players.entities()) {
            player.fogOfWar.initFromMap(map, player);
        }
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
			
			Game game = new Game(attr.getStrAttribute("id"));
			game.activeUnitId = attr.getStrAttribute("activeUnit");
			
			XmlNodeParser.game = game;
			
			nodeObject = game;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(getTagName())) {
                ((Game)nodeObject).afterLoadGame();
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

	public void propagateBuyToEuropeanMarkets(Player owner, GoodsType goodsType, int marketBoughtGoodsAmount) {
		if (!goodsType.isStorable()) {
			return;
		}
		marketBoughtGoodsAmount = Market.modifyGoodsAmountPropagatetToMarkets(marketBoughtGoodsAmount);
        if (marketBoughtGoodsAmount == 0) {
        	return;
        }

        for (Player player : players.entities()) {
        	if (player.isNotLiveEuropeanPlayer()) {
        		continue;
        	}
        	if (player.equalsId(owner)) {
        		continue;
        	}
        	player.market().addGoodsToMarket(goodsType, marketBoughtGoodsAmount);
        }
	}
}
