package net.sf.freecol.common.model;

import java.util.Collections;
import java.util.LinkedList;

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
	
	private LinkedList<SortableTexture> fieldTextures = new LinkedList<SortableTexture>();
	private LinkedList<Frame> overlayTexture = new LinkedList<Frame>();
	
	public final LinkedList<TileResource> tileResources = new LinkedList<TileResource>(); 
	public final LinkedList<TileImprovement> tileImprovements = new LinkedList<TileImprovement>();
	
	public Tile(int id, TileType type, int style) {
		this.id = id;
		this.type = type;
		this.style = style;
	}
	
	public String toString() {
		return "id: " + id + ", type: " + type.toString() + ", style: " + style; 
	}
	
	public void addTileResources(TileResource tileResource) {
		this.tileResources.add(tileResource);
	}

	public void addTexture(SortableTexture texture) {
		fieldTextures.add(texture);
		Collections.sort(fieldTextures);
	}

	public void addOverlayTexture(Frame frame) {
		if (frame == null) {
			throw new NullPointerException();
		}
		overlayTexture.add(frame);
	}
	
	public void draw(SpriteBatch batch, int rx, int ry) {
		for (SortableTexture texture : fieldTextures) {
			batch.draw(texture.texture, rx, ry);
		}
	}
	
	public void drawOverlay(SpriteBatch batch, int rx, int ry) {
		for (Frame frame : overlayTexture) {
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
	
	public static class Xml extends XmlNodeParser {
		protected Tile tile;
		
		public Xml(XmlNodeParser parent) {
			super(parent);
			
			addNode(new TileResource.Xml(this));
			addNode(new TileImprovement.Xml(this));
			addNode(new Unit.Xml(this));
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
		}
		
		@Override
		public String getTagName() {
			return "tile";
		}
	}
	
	
	
}
