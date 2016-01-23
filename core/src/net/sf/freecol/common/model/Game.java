package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;


public class Game implements Identifiable {

    private String id;
    
	public Map map;
	public Player playingPlayer;
	public final MapIdEntities<Player> players = new MapIdEntities<Player>();
	
	public String toString() {
		return "map[" + map + "]";
	}

    @Override
    public String getId() {
        return id;
    }
	
	public static class Xml extends XmlNodeParser {
		public Game game = new Game();
		
		public Xml() {
			addNode(new Specification.Xml());
			addNode(new Map.Xml());
			addNodeForMapIdEntities("players", Player.class);
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
