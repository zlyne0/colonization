package net.sf.freecol.common.model.x;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IdGenerator;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class XGame implements Identifiable {

	private XMap map;
	private XSpecification specification;
	private final MapIdEntities<XPlayer> players = MapIdEntities.linkedMapIdEntities();
	
	@Override
	public String getId() {
		throw new IllegalStateException("no id for object");
	}

	public void setMap(XMap xMap) {
		this.map = xMap;
	}

	public void addPlayer(XPlayer player) {
		this.players.add(player);
	}
	
	public void setSpecification(XSpecification specification) {
		this.specification = specification;
	}
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
			//addNode(new XSpecification.Xml());
			addNode(XSpecification.class, "specification");
			addNodeForMapIdEntities("players", XPlayer.class);
			addNode(XMap.class, "map");
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			XGame game = new XGame();
			
			//XmlNodeParser.game = game;
			
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
