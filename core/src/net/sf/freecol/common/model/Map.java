package net.sf.freecol.common.model;

import promitech.colonization.Direction;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Map {

	public final int width;
	public final int height;
	
	private final Tile[][] tiles;
	
	public Map(int width, int height) {
		this.width = width;
		this.height = height;
		
		tiles = new Tile[height][width];
	}
	
	public Tile getTile(int x, int y, Direction direction) {
		return getTile(direction.stepX(x, y), direction.stepY(x, y));
	}
	
	public Tile getTile(int x, int y) {
		if (!isCoordinateValid(x, y)) {
			return null;
		}
		Tile tile = tiles[y][x];
		if (tile == null) {
			throw new RuntimeException("not implementd, shoud return empty tile or default");
		}
		return tile;
	}

	public boolean isCoordinateValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public void createTile(int x, int y, Tile tile) {
		tiles[y][x] = tile;
	}
	
	public String toString() {
		return "width = " + width + ", height = " + height;
	}

	public static class Xml extends XmlNodeParser {
		Map map;
		
		public Xml(XmlNodeParser parent) {
			super(parent);
			
			addNode(new Tile.Xml(this).addSetter(new ObjectFromNodeSetter() {
                @Override
                public void set(Identifiable entity) {
                    Tile tile = (Tile)entity;
                    map.createTile(tile.x, tile.y, tile);
                }
            }));
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			int width = attr.getIntAttribute("width");
			int height = attr.getIntAttribute("height");
			map = new Map(width, height);
			
			((Game.Xml)parentXmlNodeParser).game.map = map;
		}

		@Override
		public String getTagName() {
			return "map";
		}
		
	}

	
}
