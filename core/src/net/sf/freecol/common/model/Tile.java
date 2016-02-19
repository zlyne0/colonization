package net.sf.freecol.common.model;

import java.util.Collection;
import java.util.Collections;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
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
	
	public Tile(String id, int x, int y, TileType type, int style) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.type = type;
		this.style = style;
	}
	
	@Override
	public String getId() {
	    return id;
	}

    public boolean equalsCoordinates(int x, int y) {
        return this.x == x && this.y == y;
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
	
	
    public boolean isDirectlyHighSeasConnected() {
    	if (moveToEurope) {
    		return true;
    	} else {
    		return type.isDirectlyHighSeasConnected();
    	}
    }
	
    public int getMoveCost(Direction moveDirection, int basicMoveCost) {
    	if (tileItemContainer == null) {
    		return basicMoveCost;
    	}
    	return tileItemContainer.getMoveCost(moveDirection, basicMoveCost);
    }
    
    public int applyTileProductionModifier(final String goodsId, int quantity) {
        for (TileImprovement ti : getTileImprovements()) {
            quantity = (int)ti.type.applyModifier(goodsId, quantity);
        }
        for (TileResource tileResource : getTileResources()) {
            quantity = (int)tileResource.getResourceType().applyModifier(goodsId, quantity);
        }
        return quantity;
    }

    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }    
    
	public static class Xml extends XmlNodeParser {
	    
		public Xml() {
			addNode(CachedTile.class, new ObjectFromNodeSetter<Tile, CachedTile>() {
				@Override
				public void set(Tile target, CachedTile entity) {
					entity.getPlayer().setTileAsExplored(target, game.map);
				}
			});
		    addNode(TileItemContainer.class, new ObjectFromNodeSetter<Tile,TileItemContainer>() {
                @Override
                public void set(Tile target, TileItemContainer entity) {
                    target.tileItemContainer = entity;
                }
            });
			addNode(Unit.class, new ObjectFromNodeSetter<Tile,Unit>() {
	            @Override
	            public void set(Tile tile, Unit unit) {
	                tile.units.add(unit);
	                unit.setLocation(tile);
	                unit.getOwner().units.add(unit);
	            }
	        });
			addNode(Colony.class, new ObjectFromNodeSetter<Tile,Colony>() {
                @Override
                public void set(Tile target, Colony entity) {
                    target.settlement = entity;
                    entity.tile = target;
                }
            });
            addNode(IndianSettlement.class, new ObjectFromNodeSetter<Tile,IndianSettlement>() {
                @Override
                public void set(Tile target, IndianSettlement entity) {
                    target.settlement = entity;
                    entity.tile = target;
                }
            });
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			int x = attr.getIntAttribute("x");
			int y = attr.getIntAttribute("y");
			
			String tileTypeStr = attr.getStrAttribute("type");
			int tileStyle = attr.getIntAttribute("style");
			String idStr = attr.getStrAttribute("id");
			
			TileType tileType = Specification.instance.tileTypes.getById(tileTypeStr);
			Tile tile = new Tile(idStr, x, y, tileType, tileStyle);
			tile.connected = attr.getIntAttribute("connected", 0);
			tile.moveToEurope = attr.getBooleanAttribute("moveToEurope", false);
			
			nodeObject = tile;
		}

		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
		    return "tile";
		}
	}
}
