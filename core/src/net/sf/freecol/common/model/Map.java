package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.LinkedList;

import org.xml.sax.SAXException;

import com.badlogic.gdx.math.GridPoint2;

import net.sf.freecol.common.model.map.LandSeaAreaMap;
import net.sf.freecol.common.model.map.Region;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.util.Consumer;
import net.sf.freecol.common.util.Predicate;
import promitech.colonization.Direction;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;
import promitech.map.isometric.IsometricMap;
import promitech.map.isometric.IterableSpiral;
import promitech.map.isometric.NeighbourIterableTile;

public class Map extends ObjectWithId {

    public static final Predicate<Tile> TILE_HAS_COLONY = new Predicate<Tile>() {
        @Override
        public boolean test(Tile t) {
            return t.hasSettlement() && t.getSettlement().isColony();
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
    public static final int POLAR_HEIGHT = 2;
	
	public final int width;
	public final int height;
	
	private final IsometricMap<Tile> tiles;
	public final MapIdEntities<Region> regions = new MapIdEntities<Region>();
	private LandSeaAreaMap areas;
	
	public Map(String id, int width, int height) {
	    super(id);
	    
		this.width = width;
		this.height = height;
		
		tiles = new IsometricMap<Tile>(Tile.class, width, height);
	}
	
	public boolean isPolar(Tile tile) {
		return tile.y <= POLAR_HEIGHT || tile.y >= height - POLAR_HEIGHT - 1 || tile.getType().equalsId(TileType.ARCTIC);
	}

	public boolean isOnMapEdge(Tile tile) {
		return tile.x <= 2 || tile.x >= width-2 || tile.y <= 2 || tile.y >= height-2; 
	}
	
	public Tile getTile(int x, int y, Direction direction) {
	    return tiles.getTile(x, y, direction);
	}

	public Tile getTile(Tile source, Direction direction) {
	    return tiles.getTile(source.x, source.y, direction);
	}
	
	public Tile getTile(int x, int y) {
	    return tiles.getTile(x, y);
	}

	public Tile getSafeTile(int x, int y) {
	    return tiles.getSafeTile(x, y);
	}

	public Tile getSafeTile(int index) {
		return tiles.getSafeTile(index);
	}

	public Tile getSafeTile(GridPoint2 p) {
	    return tiles.getSafeTile(p.x, p.y);
	}
	
	public void createTile(int x, int y, Tile tile) {
	    tiles.createTile(x, y, tile);
	}
	
    public void updateReferences() {
        tiles.runOnAllTiles(new Consumer<Tile>() {
            @Override
            public void consume(Tile t) {
                tileLandConnection(t);
                
                if (t.hasSettlement() && t.getSettlement().isColony()) {
                    Colony colony = t.getSettlement().asColony();
                    colony.initColonyTilesTile(t, Map.this);
                    colony.updateColonyPopulation();
                    colony.updateColonyFeatures();
                }
            }
        });
    }

    private void tileLandConnection(Tile tile) {
        tile.tileConnected = Tile.ALL_NEIGHBOUR_WATER_BITS_VALUE;
        
        for (Direction direction : Direction.allDirections) {
            Tile neighbourTile = tiles.getTile(tile.x, tile.y, direction);
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
	
	public String toString() {
		return "width = " + width + ", height = " + height;
	}

	public LinkedList<Settlement> findSettlements(Tile sourceTile, int radius) {
		LinkedList<Settlement> ll = new LinkedList<Settlement>();
		if (sourceTile.hasSettlement()) {
			ll.add(sourceTile.getSettlement());
		}
		for (Tile tile : tiles.neighbourTiles(sourceTile.x, sourceTile.y, radius)) {
            if (tile.hasSettlement()) {
                ll.add(tile.settlement);
            }
        }
		return ll;
	}
	
	public Colony findColonyInRange(final Tile tile, final int radius, final Player colonyOwner) {
		Tile colonyTile = tiles.findFirst(tile.x, tile.y, radius, new Predicate<Tile>() {
			@Override
			public boolean test(Tile t) {
				return t.hasSettlement() 
					&& t.getSettlement().isColony() 
					&& t.getSettlement().getOwner().equalsId(colonyOwner);
			}
		});
		if (colonyTile == null) {
			return null;
		}
		return colonyTile.getSettlement().asColony();
	}
	
	public boolean hasColonyInRange(Tile tile, int radius) {
		if (tile.hasSettlement() && tile.getSettlement().isColony()) {
			return true;
		}
        return tiles.isTileExists(tile.x, tile.y, radius, TILE_HAS_COLONY);
	}
	
	public Iterable<NeighbourIterableTile<Tile>> neighbourTiles(int x, int y) {
	    return tiles.neighbourTiles(x, y);
	}
	
    public Iterable<NeighbourIterableTile<Tile>> neighbourTiles(final Tile sourceTile) {
        return tiles.neighbourTiles(sourceTile.x, sourceTile.y);
    }

    public Iterable<NeighbourIterableTile<Tile>> neighbourLandTiles(final Tile sourceTile) {
        return tiles.neighbourTiles(sourceTile.x, sourceTile.y, LAND_TILES);
    }

    public Iterable<NeighbourIterableTile<Tile>> neighbourWaterTiles(final Tile sourceTile) {
        return tiles.neighbourTiles(sourceTile.x, sourceTile.y, WATER_TILES);
    }
    
    /**
     * Important: Do not break loop. Iterable is put back to pool when Iterator.hasNext return false 
     * @param sourceTile
     * @param radius
     * @return
     */
    public Iterable<Tile> neighbourTiles(final Tile sourceTile, int radius) {
        return tiles.neighbourTiles(sourceTile.x, sourceTile.y, radius);
    }

    /**
     * Important: Do not break loop. Iterable is put back to pool when Iterator.hasNext return false 
     * @param x
     * @param y
     * @param radius
     * @return
     */
    public Iterable<Tile> neighbourTiles(int x, int y, int radius) {
        return tiles.neighbourTiles(x, y, radius);
    }
    
    public Iterable<Tile> neighbourTiles(IterableSpiral<Tile> is, Tile tile, int radius) {
    	is.reset(tiles, tile.x, tile.y, radius);
    	return is;
    }

	public boolean isTheSameArea(Tile tile1, Tile tile2) {
    	if (areas == null) {
    		areas = new LandSeaAreaMap(this.width, this.height);
    		areas.generate(this);
		}
    	return areas.isTheSameArea(tile1, tile2);
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
				public void generateXml(Map source, final ChildObject2XmlCustomeHandler<Tile> xmlGenerator) throws IOException {
				    source.tiles.runOnAllTiles(new Consumer<Tile>() {
                        @Override
                        public void consume(Tile obj) {
                            xmlGenerator.generateXml(obj);
                        }
                    });
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
