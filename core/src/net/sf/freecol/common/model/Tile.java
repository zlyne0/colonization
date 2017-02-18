package net.sf.freecol.common.model;

import java.io.IOException;
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
import promitech.colonization.savegame.XmlNodeParser;

public class Tile implements UnitLocation, Identifiable {
	
	public final int x;
	public final int y;
	private TileType type;
	private int style;
	public final String id;
	private int connected = 0;
	private boolean moveToEurope = false;
	private Player owner;
	private String owningSettlement;
	
	protected Settlement settlement;
	private final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
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

	@Override
	public MapIdEntities<Unit> getUnits() {
		return units;
	}
	
	@Override
	public boolean canAutoLoadUnit() {
		return hasSettlement() && units.isNotEmpty();
	}
	
	@Override
	public boolean canAutoUnloadUnits() {
		return hasSettlement();
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
                if (unit.isOffensiveUnit()) {
                    return true;
                }
            } else {
                if ((unit.isOffensiveUnit() && navyUnitOwner.atWarWith(unit.getOwner())) || unit.unitType.hasAbility(Ability.PIRACY)) {
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

	public void changeOwner(Player player) {
		changeOwner(player, null);
	}
	
	public void changeOwner(Player player, Settlement settlement) {
		this.owner = player;
		if (settlement != null) {
			this.owningSettlement = settlement.getId();
		}
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
			owner.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN);
			
			if (owner.isIndian()) {
				for (Settlement settlement : owner.settlements.entities()) {
					IndianSettlement indianSett = (IndianSettlement)settlement;
					if (indianSett.settlementType.isCapital() || indianSett.equalsId(getOwningSettlementId())) {
						indianSett.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN);
					} else {
						indianSett.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN/2);
					}
				}
			}
		} else {
			owner.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN);
			
			if (owner.isIndian()) {
				for (Settlement settlement : owner.settlements.entities()) {
					IndianSettlement indianSett = (IndianSettlement)settlement;
					if (indianSett.hasContact(player)) {
						indianSett.modifyTension(player, Tension.TENSION_ADD_LAND_TAKEN);
					}
				}
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
	
	public boolean hasWorkerOnTile() {
		if (owner == null || owningSettlement == null) {
			return false;
		}
		Colony tileOwnerColony = (Colony)owner.settlements.getById(owningSettlement);
		ColonyTile ct = tileOwnerColony.colonyTiles.getById(this.getId());
		return ct.getWorker() != null;
	}
	
	public static class Xml extends XmlNodeParser<Tile> {
	    
		public Xml() {
			addNode(CachedTile.class, new ObjectFromNodeSetter<Tile, CachedTile>() {
				@Override
				public void set(Tile target, CachedTile entity) {
					entity.getPlayer().setTileAsExplored(target);
				}
				@Override
				public void generateXml(Tile source, ChildObject2XmlCustomeHandler<CachedTile> xmlGenerator) throws IOException {
					throw new RuntimeException("not implemented");
				}
			});
		    addNode(TileItemContainer.class, new ObjectFromNodeSetter<Tile,TileItemContainer>() {
                @Override
                public void set(Tile target, TileItemContainer entity) {
                    target.tileItemContainer = entity;
                }
				@Override
				public void generateXml(Tile source, ChildObject2XmlCustomeHandler<TileItemContainer> xmlGenerator) throws IOException {
					throw new RuntimeException("not implemented");
				}
            });
			addNode(Unit.class, new ObjectFromNodeSetter<Tile,Unit>() {
	            @Override
	            public void set(Tile tile, Unit unit) {
	                unit.changeUnitLocation(tile);
	            }
				@Override
				public void generateXml(Tile source, ChildObject2XmlCustomeHandler<Unit> xmlGenerator) throws IOException {
					throw new RuntimeException("not implemented");
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
					throw new RuntimeException("not implemented");
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
					throw new RuntimeException("not implemented");
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
			
			String ownerId = attr.getStrAttribute("owner");
			if (ownerId != null) {
				tile.owner = game.players.getById(ownerId);
			}
			tile.owningSettlement = attr.getStrAttribute("owningSettlement");
			
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
