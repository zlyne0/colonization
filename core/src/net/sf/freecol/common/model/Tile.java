package net.sf.freecol.common.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Tile implements Location, Identifiable {
	
	public final int x;
	public final int y;
	public final TileType type;
	public final int style;
	public final String id;
	private int connected = 0;
	private boolean moveToEurope = false;
	
	protected Settlement settlement;
	public final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
	private TileItemContainer tileItemContainer;
	private final Set<String> exploredByPlayers = new HashSet<String>();
	
	public Tile(String id, int x, int y, TileType type, int style) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.type = type;
		this.style = style;
	}
	
	@Override
	public String getId() {
	    return "id";
	}
	
	public String toString() {
		return "id: " + id + ", type: " + type.toString() + ", style: " + style + ", unit.size: " + units.size(); 
	}
	
	public Collection<TileImprovement> getTileImprovements() {
	    if (tileItemContainer == null) {
	        return Collections.emptyList();
	    }
	    return tileItemContainer.improvements.entities();
	}
	
	public Collection<TileResource> getTileResources() {
	    if (tileItemContainer == null) {
	        return Collections.emptyList();
	    }
	    return tileItemContainer.resources.entities();
	}
	
	public boolean hasRoad() {
		if (settlement != null) {
			return true;
		}
		for (TileImprovement imprv : getTileImprovements()) {
			if (imprv.type.isRoad()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPlowed() {
		for (TileImprovement imprv : getTileImprovements()) {
			if (imprv.type.isPlowed()) {
				return true;
			}
		}
		return false;
	}
	
    public boolean hasLostCityRumour() {
        if (tileItemContainer == null) {
            return false;
        }
        return tileItemContainer.lostCityRumours.size() > 0;
    }

	public boolean hasSettlement() {
		return settlement != null;
	}
	
	public boolean hasSettlementOwnedBy(Player player) {
		return settlement != null && settlement.owner.equals(player);
	}

	public Settlement getSettlement() {
		return settlement;
	}

	public TileImprovement getTileImprovementByType(String typeStr) {
		for (TileImprovement ti : getTileImprovements()) {
			if (ti.type.id.equals(typeStr)) {
				return ti;
			}
		}
		return null;
	}
	
	public boolean isUnexplored(Player player) {
		return !exploredByPlayers.contains(player.getId());
	}
	
    public boolean isDirectlyHighSeasConnected() {
    	if (moveToEurope) {
    		return true;
    	} else {
    		return type.isDirectlyHighSeasConnected();
    	}
    }
	
	public static class Xml extends XmlNodeParser {
	    protected Tile tile;
	    
		public Xml(XmlNodeParser parent) {
			super(parent);
			
			addNode(new TileItemContainer.Xml(this).addSetter(new ObjectFromNodeSetter() {
                @Override
                public void set(Identifiable entity) {
                    ((Tile)nodeObject).tileItemContainer = (TileItemContainer)entity;
                }
            }));
			
			addNode(new Unit.Xml(this));
			addNode(new Colony.Xml(this).addSetter(new ObjectFromNodeSetter() {
                @Override
                public void set(Identifiable entity) {
                    Colony colony = (Colony)entity;
                    ((Tile)nodeObject).settlement = colony;
                    colony.tile = (Tile)nodeObject;
                }
            }));
            addNode(new IndianSettlement.Xml(this).addSetter(new ObjectFromNodeSetter() {
                @Override
                public void set(Identifiable entity) {
                    IndianSettlement is = (IndianSettlement)entity;
                    ((Tile)nodeObject).settlement = is;
                    is.tile = ((Tile)nodeObject);
                }
            }));
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			int x = attr.getIntAttribute("x");
			int y = attr.getIntAttribute("y");
			
			String tileTypeStr = attr.getStrAttribute("type");
			int tileStyle = attr.getIntAttribute("style");
			String idStr = attr.getStrAttribute("id");
			
			TileType tileType = game.specification.tileTypes.getById(tileTypeStr);
			tile = new Tile(idStr, x, y, tileType, tileStyle);
			tile.connected = attr.getIntAttribute("connected", 0);
			tile.moveToEurope = attr.getBooleanAttribute("moveToEurope", false);
			
			nodeObject = tile;
		}

		@Override
		public void startReadChildren(XmlNodeAttributes attr) {
			if (attr.isQNameEquals("cachedTile")) {
				String playerId = attr.getStrAttribute("player");
				tile.exploredByPlayers.add(playerId);
			}
		}
		
		@Override
		public String getTagName() {
			return "tile";
		}
	}
}
