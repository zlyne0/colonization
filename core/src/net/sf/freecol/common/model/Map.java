package net.sf.freecol.common.model;

import java.util.LinkedList;

import org.xml.sax.SAXException;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.SpiralIterator;
import promitech.colonization.gamelogic.MoveType;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Map extends ObjectWithId {

	public final int width;
	public final int height;
	
	private final Tile[][] tiles;
	private final SpiralIterator spiralIterator;
	
	public Map(String id, int width, int height) {
	    super(id);
	    
		this.width = width;
		this.height = height;
		
		tiles = new Tile[height][width];
		spiralIterator = new SpiralIterator(width, height);
	}
	
	public Tile getTile(int x, int y, Direction direction) {
		return getTile(direction.stepX(x, y), direction.stepY(x, y));
	}

	public Tile getTile(Tile source, Direction direction) {
		return getTile(direction.stepX(source.x, source.y), direction.stepY(source.x, source.y));
	}
	
	public Tile getTile(int x, int y) {
		if (!isCoordinateValid(x, y)) {
			return null;
		}
		Tile tile = tiles[y][x];
		if (tile == null) {
			throw new RuntimeException("not implementd, should return empty tile or default");
		}
		return tile;
	}

	public boolean isCoordinateValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public void createTile(int x, int y, Tile tile) {
		tiles[y][x] = tile;
	}
	
    public void afterReadMap() {
        for (int y=0; y<height; y++) {
            Tile row[] = tiles[y];
            for (int x=0; x<width; x++) {
                Tile t = row[x];
                if (t.hasSettlement() && t.getSettlement().isColony()) {
                    Colony colony = t.getSettlement().getColony();
                    colony.initColonyTilesTile(t, this);
                }
            }
        }
    }

	public boolean isUnitSeeHostileUnit(Unit unit) {
		Tile unitTile = unit.getTile();
		int radius = unit.lineOfSight();
		SpiralIterator spiralIterator = new SpiralIterator(width, height);
		spiralIterator.reset(unitTile.x, unitTile.y, true, radius);
		
		while (spiralIterator.hasNext()) {
			Tile tile = getTile(spiralIterator.getX(), spiralIterator.getY());
			spiralIterator.next();
			if (tile == null) {
				continue;
			}
			Player tileOwner = null;
			if (tile.hasSettlement()) {
				tileOwner = tile.getSettlement().getOwner();
			} else {
				if (tile.getUnits().isNotEmpty()) {
					tileOwner = tile.getUnits().first().getOwner();
				}
			}
			if (tileOwner != null) {
				if (unit.getOwner().atWarWith(tileOwner)) {
					return true;
				}
			}
		}
		return false;
	}
    
	public String toString() {
		return "width = " + width + ", height = " + height;
	}

	public LinkedList<Settlement> findSettlements(Tile sourceTile, Player player, int radius) {
		LinkedList<Settlement> ll = new LinkedList<Settlement>();
		if (sourceTile.hasSettlement()) {
			ll.add(sourceTile.getSettlement());
		}
		spiralIterator.reset(sourceTile.x, sourceTile.y, true, radius);
		while (spiralIterator.hasNext()) {
			Tile tile = getTile(spiralIterator.getX(), spiralIterator.getY());
			if (tile.hasSettlement() && tile.getSettlement().owner.equalsId(player)) {
				ll.add(tile.settlement);
			}
			spiralIterator.next();
		}
		return ll;
	}
	
	public Tile findFirstMovableHighSeasTile(Unit unit, int x, int y) {
	    Tile tile = getTile(x, y);
	    MoveType navalMoveType = unit.getNavalMoveType(tile);
	    if (MoveType.MOVE_HIGH_SEAS.equals(navalMoveType)) {
	        return tile;
	    }
	    
	    int radius = 1;
        while (true) {
            spiralIterator.reset(x, y, false, radius);
            while (spiralIterator.hasNext()) {
                tile = getTile(spiralIterator.getX(), spiralIterator.getY());
                navalMoveType = unit.getNavalMoveType(tile);
                
                if (MoveType.MOVE_HIGH_SEAS.equals(navalMoveType)) {
                    return tile;
                }
                spiralIterator.next();
            }
            radius++;
	    }
	}
	
	public static class Xml extends XmlNodeParser {
		
		public Xml() {
			addNode(Tile.class, new ObjectFromNodeSetter<Map,Tile>() {
                @Override
                public void set(Map target, Tile entity) {
                    Tile tile = (Tile)entity;
                    target.createTile(tile.x, tile.y, tile);
                }
            });
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
		    String idStr = attr.getStrAttribute("id");
			int width = attr.getIntAttribute("width");
			int height = attr.getIntAttribute("height");
			Map map = new Map(idStr, width, height);
			game.map = map;
			nodeObject = map;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		    if (qName.equals(getTagName())) {
		        ((Map)nodeObject).afterReadMap();
		    }
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "map";
		}
		
	}
}
