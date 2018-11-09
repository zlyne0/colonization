package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.xml.sax.SAXException;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.map.Region;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Predicate;
import promitech.colonization.Direction;
import promitech.colonization.SpiralIterator;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

class NeighbourTilesIterable implements Iterable<Tile>, Iterator<Tile> {
    
    public static final Predicate<Tile> ALL = new Predicate<Tile>() {
        @Override
        public boolean test(Tile tile) {
            return true;
        }
    };

    public static final Predicate<Tile> LAND_TILES = new Predicate<Tile>() {
        @Override
        public boolean test(Tile tile) {
            return tile.getType().isLand();
        }
    };

    public static final Predicate<Tile> WATER_TILES = new Predicate<Tile>() {
        @Override
        public boolean test(Tile tile) {
            return tile.getType().isWater();
        }
    };
    
    private final Map map;
    private final Tile sourceTile;
    private final Predicate<Tile> tileFilter;

    private int cursor = 0;
    private int foundFirstIndex = 0;
    private Tile cursorObject = null;
    
    NeighbourTilesIterable(Map map, Tile sourceTile, Predicate<Tile> tileFilter) {
        this.map = map;
        this.sourceTile = sourceTile;
        this.tileFilter = tileFilter;
    }
    
    @Override
    public Iterator<Tile> iterator() {
        return this;
            
    }
        
    @Override
    public boolean hasNext() {
        Tile t = found(cursor);
        cursor = foundFirstIndex;
        if (t == null) {
            return false;
        }
        cursorObject = t;
        return cursor < Direction.allDirections.size();
    }

    private Tile found(int ic) {
        Direction direction = null;
        Tile tile = null;
        for (foundFirstIndex = ic; foundFirstIndex < Direction.allDirections.size(); foundFirstIndex++) {
            direction = Direction.allDirections.get(foundFirstIndex);
            tile = map.getTile(sourceTile, direction);
            if (tile == null || !tileFilter.test(tile)) {
                continue;
            }
            return tile;
        }
        return null;
    }
    
    @Override
    public Tile next() {
        cursor++;
        return cursorObject;
    }
}

public class Map extends ObjectWithId {

	public static final String STANDARD_REGION_NAMES[][] = new String[][] {
		{ "model.region.northWest", "model.region.north",  "model.region.northEast" },
		{ "model.region.west",      "model.region.center", "model.region.east" },
		{ "model.region.southWest", "model.region.south",  "model.region.southEast" }		
	};
	
	
    public static int distance(Tile a, Tile b) {
    	return distance(a.x, a.y, b.x, b.y);
    }
	
    /**
     * Gets the distance in tiles between two map positions.
     * With an isometric map this is a non-trivial task.
     * The formula below has been developed largely through trial and
     * error.  It should cover all cases, but I wouldn't bet my
     * life on it.
     *
     * @param ax The x-coordinate of the first position.
     * @param ay The y-coordinate of the first position.
     * @param bx The x-coordinate of the second position.
     * @param by The y-coordinate of the second position.
     * @return The distance in tiles between the positions.
     */
    public static int distance(int ax, int ay, int bx, int by) {
        int r = (bx - ax) - (ay - by) / 2;

        if (by > ay && ay % 2 == 0 && by % 2 != 0) {
            r++;
        } else if (by < ay && ay % 2 != 0 && by % 2 == 0) {
            r--;
        }
        return Math.max(Math.abs(ay - by + r), Math.abs(r));
    }
	
	
    /**
     * The number of tiles from the upper edge that are considered
     * polar by default.
     */
    public final static int POLAR_HEIGHT = 2;
	
	public final int width;
	public final int height;
	
	private final Tile[][] tiles;
	private final SpiralIterator spiralIterator;
	
	public final MapIdEntities<Region> regions = new MapIdEntities<Region>();
	
	public Map(String id, int width, int height) {
	    super(id);
	    
		this.width = width;
		this.height = height;
		
		tiles = new Tile[height][width];
		spiralIterator = new SpiralIterator(width, height);
	}
	
	public boolean isPolar(Tile tile) {
		return tile.y <= POLAR_HEIGHT || tile.y >= height - POLAR_HEIGHT - 1 || tile.getType().equalsId(TileType.ARCTIC);
	}

	public boolean isOnMapEdge(Tile tile) {
		return tile.x <= 2 || tile.x >= width-2 || tile.y <= 2 || tile.y >= height-2; 
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

	public Tile getSafeTile(int x, int y) {
		return tiles[y][x];
	}
	
	public Tile getSafeTile(GridPoint2 p) {
		return tiles[p.y][p.x];
	}
	
	public boolean isCoordinateValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public void createTile(int x, int y, Tile tile) {
		tiles[y][x] = tile;
	}
	
    public void updateReferences() {
        for (int y=0; y<height; y++) {
            Tile row[] = tiles[y];
            for (int x=0; x<width; x++) {
                Tile t = row[x];
                tileLandConnection(t);
                
                if (t.hasSettlement() && t.getSettlement().isColony()) {
                    Colony colony = t.getSettlement().getColony();
                    colony.initColonyTilesTile(t, this);
            		colony.updateColonyPopulation();
            		colony.updateColonyFeatures();
                }
            }
        }
    }

    private void tileLandConnection(Tile tile) {
        tile.tileConnected = Tile.ALL_NEIGHBOUR_WATER_BITS_VALUE;
        
        for (Direction direction : Direction.allDirections) {
            Tile neighbourTile = getTile(tile, direction);
            if (neighbourTile != null) {
                if (neighbourTile.getType().isLand()) {
                    tile.tileConnected |= 1 << direction.ordinal();
                }
            }
        }
    }
    
	public void initPlayersMap(MapIdEntities<Player> players) {
        for (Player player : players.entities()) {
            player.fogOfWar.initFromMap(this, player);
            player.initExploredMap(this);
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
		Tile tile = null;
		spiralIterator.reset(sourceTile.x, sourceTile.y, true, radius);
		while (spiralIterator.hasNext()) {
			tile = getTile(spiralIterator.getX(), spiralIterator.getY());
			if (tile.hasSettlement() && tile.getSettlement().owner.equalsId(player)) {
				ll.add(tile.settlement);
			}
			spiralIterator.next();
		}
		return ll;
	}
	
	public boolean hasColonyInRange(Tile tile, int radius) {
		if (tile.hasSettlement() && tile.getSettlement().isColony()) {
			return true;
		}
		spiralIterator.reset(tile.x, tile.y, true, radius);
		Tile t;
		while (spiralIterator.hasNext()) {
			t = getTile(spiralIterator.getX(), spiralIterator.getY());
			if (t.hasSettlement() && t.getSettlement().isColony()) {
				return true;
			}
			spiralIterator.next();
		}
		return false;
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

    public Iterable<Tile> neighbourTiles(final Tile sourceTile) {
        return new NeighbourTilesIterable(this, sourceTile, NeighbourTilesIterable.ALL);
    }

    public Iterable<Tile> neighbourLandTiles(final Tile sourceTile) {
        return new NeighbourTilesIterable(this, sourceTile, NeighbourTilesIterable.LAND_TILES);
    }

    public Iterable<Tile> neighbourWaterTiles(final Tile sourceTile) {
        return new NeighbourTilesIterable(this, sourceTile, NeighbourTilesIterable.WATER_TILES);
    }
    
	public static class Xml extends XmlNodeParser<Map> {
		
		private static final String ATTR_HEIGHT = "height";
		private static final String ATTR_WIDTH = "width";

		public Xml() {
			addNodeForMapIdEntities("regions", Region.class);
			addNode(Tile.class, new ObjectFromNodeSetter<Map,Tile>() {
                @Override
                public void set(Map target, Tile entity) {
                    Tile tile = (Tile)entity;
                    target.createTile(tile.x, tile.y, tile);
                }
				@Override
				public void generateXml(Map source, ChildObject2XmlCustomeHandler<Tile> xmlGenerator) throws IOException {
					for (int y=0; y<source.height; y++) {
						for (int x=0; x<source.width; x++) {
							xmlGenerator.generateXml(source.tiles[y][x]);
						}
					}
				}
            });
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
		    String idStr = attr.getStrAttribute(ATTR_ID);
			int width = attr.getIntAttribute(ATTR_WIDTH);
			int height = attr.getIntAttribute(ATTR_HEIGHT);
			Map map = new Map(idStr, width, height);
			
			map.initPlayersMap(game.players);
			game.map = map;
			nodeObject = map;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		    if (qName.equals(getTagName())) {
		        ((Map)nodeObject).updateReferences();
		    }
		}
		
		@Override
		public void startWriteAttr(Map map, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(map);
			attr.set(ATTR_WIDTH, map.width);
			attr.set(ATTR_HEIGHT, map.height);
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
