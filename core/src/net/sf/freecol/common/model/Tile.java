package net.sf.freecol.common.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.xml.sax.Attributes;

import promitech.colonization.SortableTexture;
import promitech.colonization.gdx.Frame;
import promitech.colonization.savegame.XmlNodeParser;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Tile {
	
	public final TileType type;
	public final int style;
	public final int id;
	public boolean lostCityRumour = false;
	private int connected = 0;
	
	public Colony colony;
	public IndianSettlement indianSettlement;
	private LinkedList<Unit> units = new LinkedList<Unit>();
	
	private LinkedList<SortableTexture> backgroundTerainTextures = new LinkedList<SortableTexture>();
	private LinkedList<Frame> foregroundTerainTextures = new LinkedList<Frame>();
	private LinkedList<Frame> objectTextures = new LinkedList<Frame>();
	
	public final LinkedList<TileResource> tileResources = new LinkedList<TileResource>(); 
	public final LinkedList<TileImprovement> tileImprovements = new LinkedList<TileImprovement>();
	
	private final Set<String> exploredByPlayers = new HashSet<String>();
	
	public Tile(int id, TileType type, int style) {
		this.id = id;
		this.type = type;
		this.style = style;
	}
	
	public String toString() {
		return "id: " + id + ", type: " + type.toString() + ", style: " + style + ", unitCount: " + unitsCount(); 
	}
	
	public void addTileResources(TileResource tileResource) {
		this.tileResources.add(tileResource);
	}

	public void addBackgroundTerainTexture(SortableTexture texture) {
		backgroundTerainTextures.add(texture);
		Collections.sort(backgroundTerainTextures);
	}

	public void addForegroundTerainTexture(Frame frame) {
		if (frame == null) {
			throw new NullPointerException();
		}
		foregroundTerainTextures.add(frame);
	}
	
	public void addObjectTexture(Frame frame) {
		objectTextures.add(frame);
	}

	public int unitsCount() {
		return units.size();
	}
	
	public Unit firstUnit() {
		return units.getFirst();
	}

	public void addUnit(Unit unit) {
		units.add(unit);
	}
	
	public boolean hasRoad() {
		if (colony != null || indianSettlement != null) {
			return true;
		}
		for (TileImprovement imprv : tileImprovements) {
			if (imprv.type.isRoad()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPlowed() {
		for (TileImprovement imprv : tileImprovements) {
			if (imprv.type.isPlowed()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasSettlement() {
		return colony != null || indianSettlement != null;
	}
	
	public void draw(SpriteBatch batch, int rx, int ry) {
		for (SortableTexture texture : backgroundTerainTextures) {
			batch.draw(texture.texture, rx, ry);
		}
	}
	
	public void drawOverlay(SpriteBatch batch, int rx, int ry) {
		for (Frame frame : foregroundTerainTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}

	public void drawObjects(SpriteBatch batch, int rx, int ry) {
		for (Frame frame : objectTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}
	
	public TileImprovement getTileImprovementByType(String typeStr) {
		for (TileImprovement ti : tileImprovements) {
			if (ti.type.id.equals(typeStr)) {
				return ti;
			}
		}
		return null;
	}
	
	public boolean isUnexplored(Player player) {
		return !exploredByPlayers.contains(player.getId());
	}
	
	public static class Xml extends XmlNodeParser {
		protected Tile tile;
		
		public Xml(XmlNodeParser parent) {
			super(parent);
			
			addNode(new TileResource.Xml(this));
			addNode(new TileImprovement.Xml(this));
			addNode(new Unit.Xml(this));
			addNode(new Colony.Xml(this));
            addNode(new IndianSettlement.Xml(this));
		}

		@Override
		public void startElement(String qName, Attributes attributes) {
			int x = getIntAttribute(attributes, "x");
			int y = getIntAttribute(attributes, "y");
			
			String tileTypeStr = getStrAttribute(attributes, "type");
			int tileStyle = getIntAttribute(attributes, "style");
			String idStr = getStrAttribute(attributes, "id").replaceAll("tile:", "");
			
			TileType tileType = specification.getTileTypeByTypeStr(tileTypeStr);
			tile = new Tile(Integer.parseInt(idStr), tileType, tileStyle);
			tile.connected = getIntAttribute(attributes, "connected", 0);
			
			Map.Xml xmlMap = getParentXmlParser();
			xmlMap.map.createTile(x, y, tile);
		}

		@Override
		public void startReadChildren(String qName, Attributes attributes) {
			if (qName.equals("lostCityRumour")) {
				tile.lostCityRumour = true;
			}
			if (qName.equals("cachedTile")) {
				String playerId = getStrAttribute(attributes, "player");
				tile.exploredByPlayers.add(playerId);
			}
		}
		
		@Override
		public String getTagName() {
			return "tile";
		}
	}
}
