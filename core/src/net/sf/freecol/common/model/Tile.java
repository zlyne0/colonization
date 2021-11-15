package net.sf.freecol.common.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Direction;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Tile implements UnitLocation, Identifiable {

    public static final int ALL_NEIGHBOUR_WATER_BITS_VALUE = 0;
    public static final int ALL_NEIGHBOUR_LAND_BITS_VALUE = allNeighbourLandBits();
    private static int allNeighbourLandBits() {
        int v = ALL_NEIGHBOUR_WATER_BITS_VALUE;
        for (Direction d : Direction.allDirections) {
            v |= 1 << d.ordinal();
        }
        return v;
    }
    
    
	public final int x;
	public final int y;
	private TileType type;
	private int style;
	public final String id;
	private boolean moveToEurope = false;
	private Player owner;
	private String owningSettlement;
	
	protected Settlement settlement;
	private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
	private TileItemContainer tileItemContainer;
	protected int tileConnected = 0;
	
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

	@Override
	public MapIdEntitiesReadOnly<Unit> getUnits() {
		return units;
	}
	
    @Override
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    @Override
    public void removeUnit(Unit unit) {
        units.removeId(unit);
    }
	
    public boolean equalsCoordinates(int x, int y) {
        return this.x == x && this.y == y;
    }
	
    public boolean equalsCoordinates(Tile t) {
    	return this.x == t.x && this.y == t.y;
    }
    
    public String toStringCords() {
    	return Integer.toString(x) + ", " + Integer.toString(y);
    }
    
	public String toString() {
		return "id: " + id + ", [" + x + "," + y + "], type: " + type.toString() + ", style: " + style + ", unit.size: " + units.size(); 
	}
	
	public Collection<TileImprovement> getTileImprovements() {
	    if (tileItemContainer == null) {
	        return Collections.emptyList();
	    }
	    return tileItemContainer.improvements.entities();
	}
	
	public void removeTileImprovement(String typeStr) {
		if (tileItemContainer == null) {
			return;
		}
		for (TileImprovement ti : tileItemContainer.improvements.entities()) {
			if (ti.type.id.equals(typeStr)) {
				tileItemContainer.improvements.removeId(ti);
				break;
			}
		}
		if (tileItemContainer.isEmpty()) {
			tileItemContainer = null;
		}
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
	
	public TileImprovement getRoadImprovement() {
		for (TileImprovement imprv : getTileImprovements()) {
			if (imprv.type.isRoad()) {
				return imprv;
			}
		}
		return null;
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
        return tileItemContainer.isLostCityRumours();
    }

    public void removeLostCityRumour() {
        if (tileItemContainer == null) {
        	return;
        }
        tileItemContainer.setLostCityRumours(false);
		if (tileItemContainer.isEmpty()) {
			tileItemContainer = null;
		}
    }
    
    public boolean hasTileResource() {
    	if (tileItemContainer == null) {
    		return false;
    	}
    	return tileItemContainer.resources.size() > 0;
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
	
	public boolean hasImprovementType(String improvementTypeId) {
		if (tileItemContainer == null) {
			return false;
		}
		return tileItemContainer.hasImprovementType(improvementTypeId);
	}
	
	public void addImprovement(TileImprovement tileImprovement) {
		if (tileItemContainer == null) {
			tileItemContainer = new TileItemContainer();
		}
		tileItemContainer.improvements.add(tileImprovement);
	}
	
	public void addResource(TileResource resource) {
		if (tileItemContainer == null) {
			tileItemContainer = new TileItemContainer();
		}
		tileItemContainer.resources.add(resource);
	}
	
	public void addLostCityRumors() {
		if (tileItemContainer == null) {
			tileItemContainer = new TileItemContainer();
		}
		tileItemContainer.setLostCityRumours(true);
	}
	
	public ResourceType reduceTileResourceQuantity(String resourceTypeId, int quantity) {
		if (tileItemContainer == null) {
			return null;
		}
		TileResource tileResource = tileItemContainer.resources.first();
		if (tileResource != null && tileResource.reduceQuantityResource(quantity)) {
			tileItemContainer.resources.removeId(tileResource);
			if (tileItemContainer.isEmpty()) {
				tileItemContainer = null;
			}
			return tileResource.getResourceType();
		}
		return null;
	}
	
	public boolean canBeImprovedByUnit(TileImprovementType improvementType, Unit unit) {
		if (!improvementType.isSatisfyUnitRole(unit.unitRole)) {
			return false;
		}
		if (unit.roleCount < improvementType.getExpendedAmount()) {
			return false;
		}
		if (hasImprovementType(improvementType.getId())) {
			return false;
		}
		if (!type.isTileImprovementAllowed(improvementType)) {
			return false;
		}
		return true;
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

    public boolean isColonyOnTileThatCanBombardNavyUnit(Player navyUnitOwner, boolean isNavyPiracy) {
        if (!hasSettlement()) {
            return false;
        }
        Settlement settlement = getSettlement();
        if (settlement.getOwner().equalsId(navyUnitOwner)) {
            return false;
        }
        if (settlement.canBombardEnemyShip() && (settlement.getOwner().atWarWith(navyUnitOwner) || isNavyPiracy)) {
            return true;
        }
        return false;
    }
    
    public boolean doesTileHaveNavyUnit() {
        for (Unit unit : units.entities()) {
            if (unit.unitType.isNaval()) {
                return true;
            }
        }
        return false;
    }
    
    public List<Unit> createNavyUnitsList() {
        List<Unit> navyList = new ArrayList<Unit>();
        for (Unit unit : units.entities()) {
            if (unit.unitType.isNaval()) {
                navyList.add(unit);
            }
        }
        return navyList;
    }
    
    public boolean isTileHasNavyUnitThatCanBombardUnit(Player navyUnitOwner, boolean isNavyPiracy) {
        if (type.isLand()) {
            // land units can not bombard ship
            return false;
        }
        if (units.isEmpty()) {
            return false;
        }
        for (Unit unit : units.entities()) {
            if (unit.getOwner().equalsId(navyUnitOwner)) {
                break;
            }
            if (isNavyPiracy) {
                if (Unit.isOffensiveUnit(unit)) {
                    return true;
                }
            } else {
                if ((Unit.isOffensiveUnit(unit) && navyUnitOwner.atWarWith(unit.getOwner())) || unit.unitType.hasAbility(Ability.PIRACY)) {
                    return true;
                }
            }
        }
        return false;
    }

	public void changeTileType(TileType newType) {
		this.type = newType;
		if (tileItemContainer == null) {
			return;
		}
		Iterator<TileResource> iterator = tileItemContainer.resources.entities().iterator();
		while (iterator.hasNext()) {
			TileResource next = iterator.next();
			if (!newType.canHaveResourceType(next.getResourceType())) {
				iterator.remove();
			}
		}
	}

	public TileType getType() {
		return type;
	}
	
	public void updateRoadConnections(Map map) {
		TileImprovement roadImprovement = this.getRoadImprovement();
		
		for (Direction direction : Direction.allDirections) {
			Tile neighbourTile = map.getTile(this.x, this.y, direction);
			
			TileImprovement neighbourRoadImprovement = neighbourTile.getRoadImprovement();
			if (roadImprovement != null && neighbourRoadImprovement != null) {
				roadImprovement.addConnection(direction);
				neighbourRoadImprovement.addConnection(direction.getReverseDirection())
					.updateStyle();
			} else {
				if (neighbourRoadImprovement != null && roadImprovement == null) {
					neighbourRoadImprovement.removeConnection(direction.getReverseDirection())
						.updateStyle();
				}
			}
		}
		if (roadImprovement != null) {
			roadImprovement.updateStyle();
		}
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public void removeOwner() {
		this.owner = null;
		this.owningSettlement = null;
	}
	
	public void changeOwner(Player player) {
		changeOwner(player, null);
	}
	
	public void changeOwner(Player player, Settlement settlement) {
		this.owner = player;
		if (settlement != null) {
			this.owningSettlement = settlement.getId();
		}
	}
	
	public void resetOwningSettlement() {
	    owningSettlement = null;
	}
	
	public boolean hasOwnerOrOwningSettlement() {
		return owner != null || owningSettlement != null;
	}

	public int getLandPriceForPlayer(Player player) {
		if (owner == null || player.equalsId(owner)) {
			return 0;
		}
		if (hasSettlement()) {
			throw new IllegalStateException("no price for land with settlement");
		}
		
		if (owner.isEuropean()) {
			if (player.equalsId(owningSettlement)) {
				return 0;
			} else {
				throw new IllegalStateException("no price for european settlement tile");
			}
		}
		
		if (player.getStance(owner) == Stance.UNCONTACTED) {
			return 0;
		}
		
		int price = 0;
        List<Production> productions = this.getType().productionInfo.getAttendedProductions();
        for (Production production : productions) {
            for (java.util.Map.Entry<GoodsType, Integer> outputEntry : production.outputEntries()) {
            	price += this.applyTileProductionModifier(outputEntry.getKey().getId(), outputEntry.getValue());
            }
        }
        price *= Specification.options.getIntValue(GameOptions.LAND_PRICE_FACTOR);
        price += 100;
        price = (int)player.getFeatures().applyModifier(Modifier.LAND_PAYMENT_MODIFIER, price);
		return price;
	}
	
	public void demandTileByPlayer(Player player) {
		if (getOwningSettlementId() != null) {
			if (owner.isIndian()) {
				Settlement tileSettlement = owner.settlements.getByIdOrNull(getOwningSettlementId());
				if (tileSettlement != null && tileSettlement.isIndianSettlement()) {
					IndianSettlement is = tileSettlement.asIndianSettlement();
					if (is.settlementType.isCapital()) {
						owner.modifyTensionAndPropagateToAllSettlements(player, Tension.TENSION_ADD_LAND_TAKEN);
					} else {
						owner.modifyTensionAndPropagateToAllSettlements(player, Tension.TENSION_ADD_LAND_TAKEN/2);
					}
				}
			} else {
				owner.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN);
			}
		} else {
			if (owner.isIndian()) {
				owner.modifyTensionAndPropagateToAllSettlements(player, Tension.TENSION_ADD_LAND_TAKEN);
			} else {
				owner.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN);
			}
		}
	}
	
	public boolean buyTileByPlayer(Player player) {
		int landPrice = getLandPriceForPlayer(player);
		if (player.hasNotGold(landPrice)) {
			System.out.println("player " + player + " has not gold to buy land for price " + landPrice);
			return false;
		}
		player.subtractGold(landPrice);
		owner.addGold(landPrice);
		changeOwner(player);
		return true;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public String getOwningSettlementId() {
		return owningSettlement;
	}
	
	public boolean isOwnBySettlement(Settlement settlement) {
		return settlement.equalsId(owningSettlement);
	}
	
    boolean isOccupiedForPlayer(Player player) {
        Unit tileUnit = units.first();
        if (tileUnit != null && tileUnit.getOwner().notEqualsId(player) && tileUnit.getOwner().atWarWith(player)) {
            for (Unit unit : tileUnit.getUnits().entities()) {
                if (Unit.isOffensiveUnit(unit)) {
                    return true;
                }
            }
        }
        return owningSettlement != null && owner != null && owner.notEqualsId(player);
    }
	
	public boolean hasWorkerOnTile() {
		if (owner == null || owningSettlement == null) {
			return false;
		}
		
		Settlement oSettlement = owner.settlements.getById(owningSettlement);
		if (!oSettlement.isColony()) {
			return false;
		}
		
		ColonyTile ct = oSettlement.asColony().colonyTiles.getById(this.getId());
		return ct.hasWorker();
	}
	
	public boolean isOnSeaSide() {
	    return getType().isLand() && tileConnected != ALL_NEIGHBOUR_LAND_BITS_VALUE; 
	}
	
	public boolean isNextToLand() {
		return getType().isWater() && tileConnected != ALL_NEIGHBOUR_WATER_BITS_VALUE;
	}

	public boolean isStepNextTo(Tile tile) {
		return Direction.fromCoordinates(this.x, this.y, tile.x, tile.y) != null;
	}

    public static class Xml extends XmlNodeParser<Tile> {
	    
		private static final String ATTR_PLAYER = "player";
		private static final String ELEMENT_CACHED_TILE = "cachedTile";
		private static final String ATTR_OWNING_SETTLEMENT = "owningSettlement";
		private static final String ATTR_OWNER = "owner";
		private static final String ATTR_MOVE_TO_EUROPE = "moveToEurope";
		private static final String ATTR_Y = "y";
		private static final String ATTR_X = "x";
		private static final String ATTR_TYPE = "type";
		private static final String ATTR_STYLE = "style";

		public Xml() {
			addNode(CachedTile.class, new ObjectFromNodeSetter<Tile, CachedTile>() {
				@Override
				public void set(Tile target, CachedTile entity) {
					entity.getPlayer().setTileAsExplored(target);
				}
				@Override
				public void generateXml(Tile tile, ChildObject2XmlCustomeHandler<CachedTile> xmlGenerator) throws IOException {
					// it's written in startWriteAttr, because easier
				}
			});
			
			addNode(TileItemContainer.class, "tileItemContainer");
			addNode(Unit.class, new ObjectFromNodeSetter<Tile,Unit>() {
	            @Override
	            public void set(Tile tile, Unit unit) {
	                unit.changeUnitLocation(tile);
	            }
				@Override
				public void generateXml(Tile source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
					xmlGenerator.generateXmlFromCollection(source.units.entities());
				}
	        });
			
			addNode(Colony.class, new ObjectFromNodeSetter<Tile,Colony>() {
                @Override
                public void set(Tile target, Colony entity) {
                    target.settlement = entity;
                    entity.tile = target;
                }
				@Override
				public void generateXml(Tile source, ChildObject2XmlCustomeHandler<Colony> xmlGenerator) throws IOException {
					if (source.hasSettlement() && source.settlement.isColony()) {
						xmlGenerator.generateXml((Colony)source.settlement);
					}
				}
            });
            addNode(IndianSettlement.class, new ObjectFromNodeSetter<Tile,IndianSettlement>() {
                @Override
                public void set(Tile target, IndianSettlement entity) {
                    target.settlement = entity;
                    entity.tile = target;
                }
				@Override
				public void generateXml(Tile source, ChildObject2XmlCustomeHandler<IndianSettlement> xmlGenerator) throws IOException {
					if (source.hasSettlement() && !source.settlement.isColony()) {
						xmlGenerator.generateXml((IndianSettlement)source.settlement);
					}
				}
            });
		}

		@Override
        public void startElement(XmlNodeAttributes attr) {
			int x = attr.getIntAttribute(ATTR_X);
			int y = attr.getIntAttribute(ATTR_Y);
			
			String tileTypeStr = attr.getStrAttribute(ATTR_TYPE);
			int tileStyle = attr.getIntAttribute(ATTR_STYLE);
			String idStr = attr.getStrAttribute(ATTR_ID);
			
			TileType tileType = Specification.instance.tileTypes.getById(tileTypeStr);
			Tile tile = new Tile(idStr, x, y, tileType, tileStyle);
			tile.moveToEurope = attr.getBooleanAttribute(ATTR_MOVE_TO_EUROPE, false);
			
			String ownerId = attr.getStrAttribute(ATTR_OWNER);
			if (ownerId != null) {
				tile.owner = game.players.getById(ownerId);
			}
			tile.owningSettlement = attr.getStrAttribute(ATTR_OWNING_SETTLEMENT);
			
			nodeObject = tile;
		}

		@Override
		public void startWriteAttr(Tile tile, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(tile);

			attr.set(ATTR_X, tile.x);
			attr.set(ATTR_Y, tile.y);
			attr.set(ATTR_TYPE, tile.type);
			attr.set(ATTR_STYLE, tile.style);
			
			attr.set(ATTR_MOVE_TO_EUROPE, tile.moveToEurope);
			attr.set(ATTR_OWNER, tile.owner);
			attr.set(ATTR_OWNING_SETTLEMENT, tile.owningSettlement);
			
			for (Player player : game.players.entities()) {
				if (player.isTileExplored(tile.x, tile.y)) {
					attr.xml.element(ELEMENT_CACHED_TILE);
					attr.set(ATTR_PLAYER, player);
					attr.xml.pop();
				}
			}
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
