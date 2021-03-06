package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.ProductionInfo;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.ui.DoubleClickedListener;

public class TerrainPanel extends Table implements DragAndDropSourceContainer<UnitActor>, DragAndDropTargetContainer<UnitActor> {
	private static final int PREF_WIDTH = MapRenderer.TILE_WIDTH * 3 + MapRenderer.TILE_WIDTH/2;
	private static final int PREF_HEIGHT = MapRenderer.TILE_HEIGHT * 3 + MapRenderer.TILE_HEIGHT/2;
	
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private Colony colony;
	private Tile colonyTile;
	private MapRenderer mapRenderer;
	
	private ColonyTile[] colonyTiles = new ColonyTile[9];
	private ProductionQuantityDrawModel[] productionQuantityDrawModels = new ProductionQuantityDrawModel[9];
	
	private final ProductionQuantityDrawer productionQuantityDrawer;
	private final ChangeColonyStateListener changeColonyStateListener;
	private final DoubleClickedListener unitActorDoubleClickListener;
	
	@Override
	public float getPrefWidth() {
		return PREF_WIDTH;
	}
	
	@Override
	public float getPrefHeight() {
		return PREF_HEIGHT;
	}
	
	public TerrainPanel(ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
	    this.changeColonyStateListener = changeColonyStateListener;
	    this.unitActorDoubleClickListener = unitActorDoubleClickListener;
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
		
		for (int i=0; i<9; i++) {
			productionQuantityDrawModels[i] = new ProductionQuantityDrawModel();
		}
		productionQuantityDrawer = new ProductionQuantityDrawer(MapRenderer.TILE_WIDTH/2, MapRenderer.TILE_HEIGHT/2);
		productionQuantityDrawer.centerToPoint(MapRenderer.TILE_WIDTH/2, MapRenderer.TILE_HEIGHT/2);
	}

	void changeWorkerProduction(UnitActor unitActor, GoodMaxProductionLocation prodLocation) {
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		ColonyTile aColonyTile = prodLocation.getColonyTile();
		aColonyTile.productionInfo.clear();
		aColonyTile.productionInfo.addProduction(prodLocation.tileTypeInitProduction);
		changeColonyStateListener.changeUnitAllocation(colony);
	}
	
	void putWorkerOnTerrain(UnitActor worker, ColonyTile aColonyTile) {
		worker.dragAndDropSourceContainer = this;
		
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		colony.addWorkerToTerrain(aColonyTile, worker.unit);
		worker.updateTexture();

		addActor(worker);
		updateWorkerScreenPosition(worker, aColonyTile);

		initMaxPossibleProdctionOnTile(worker.unit, aColonyTile.tile, aColonyTile);
		
		changeColonyStateListener.changeUnitAllocation(colony);
	}
	
	@Override
	public void putPayload(UnitActor worker, float x, float y) {
		ColonyTile destColonyTile = getColonyTileByScreenCords(x, y);
		if (destColonyTile == null) {
			throw new IllegalStateException("can not find dest colony tile by screen cords. Should invoke canPutpayload before");
		}
		putWorkerOnTerrain(worker, destColonyTile);
	}

	@Override
	public boolean canPutPayload(UnitActor unitActor, float x, float y) {
		ColonyTile ct = getColonyTileByScreenCords(x, y);
		if (ct == null) {
			return false;
		}
		if (ct.getWorkTileId().equals(colonyTile.getId())) {
			return false;
		}
		return ct.getWorker() == null;
	}

	@Override
	public void takePayload(UnitActor unitActor, float x, float y) {
		unitActor.dragAndDropSourceContainer = null;
		
		for (ColonyTile colonyTile : colony.colonyTiles.entities()) {
			Unit tWorker = colonyTile.getWorker();
			if (tWorker != null && tWorker.equalsId(unitActor.unit)) {
				System.out.println("take worker "
						+ "[" + unitActor.unit + "] from "
						+ "[" + colonyTile.getId() + "] "
					);
				colony.updateModelOnWorkerAllocationOrGoodsTransfer();
				colonyTile.takeWorker();
				removeActor(unitActor);
				
				changeColonyStateListener.changeUnitAllocation(colony);
				return;
			}
		}
		throw new IllegalStateException("can not find colony tile by workerId: " + unitActor.unit.getId());
	}
	
	private ColonyTile getColonyTileByScreenCords(float x, float y) {
		Tile tile = mapRenderer.getColonyTileByScreenCords(colonyTile, (int)x, (int)y);
		if (tile == null) {
			return null;
		}
		for (int i=0; i<colonyTiles.length; i++) {
			if (tile.getId().equals(colonyTiles[i].getWorkTileId())) {
				return colonyTiles[i];
			}
		}
		return null;
	}
	
	private void initMaxPossibleProdctionOnTile(Unit aUnit, Tile aTile, ColonyTile aColonyTile) {
		System.out.println("maxPossibleProductionOnTile: forTile: " + aTile.type.productionInfo);
		ProductionInfo maxPossibleProductionOnTile = colony.maxPossibleProductionOnTile(aUnit, aTile);

		System.out.println("maxPossibleProductionOnTile: maxProductions: " + maxPossibleProductionOnTile);
		
		maxPossibleProductionOnTile.determineMaxProductionType(aTile.type.productionInfo, aColonyTile.productionInfo);
		
		System.out.println("maxPossibleProductionOnTile: maxProductionType: " + aColonyTile.productionInfo);
	}
	
	public void initTerrains(MapDrawModel mapDrawModel, Tile colonyTile, DragAndDrop dragAndDrop) {
		dragAndDrop.addTarget(new UnitDragAndDropTarget(this, this));
		clear();
		
		mapRenderer = new MapRenderer(
				mapDrawModel, 
				GameResources.instance, 
				shapeRenderer
		);
		mapRenderer.setMapRendererSize(PREF_WIDTH, PREF_HEIGHT);
		mapRenderer.centerCameraOnTileCords(colonyTile.x, colonyTile.y);
		
		this.colonyTile = colonyTile;
		this.colony = (Colony)colonyTile.getSettlement();

		initTerrainsDrawModel();
		initWorkersActors(dragAndDrop);
		updateProduction();
	}

	private void initWorkersActors(DragAndDrop dragAndDrop) {
		for (ColonyTile ct : colony.colonyTiles.entities()) {
			if (ct.getWorker() != null) {
				
				UnitActor ua = new UnitActor(ct.getWorker(), unitActorDoubleClickListener);
				ua.dragAndDropSourceContainer = this;
				addActor(ua);
				updateWorkerScreenPosition(ua, ct);
				
				dragAndDrop.addSource(new UnitDragAndDropSource(ua));
			}
		}
	}

	private void updateWorkerScreenPosition(UnitActor ua, ColonyTile ct) {
		Vector2 tileScreenCords = mapRenderer.mapToScreenCords(ct.tile.x, ct.tile.y);
		ua.setPosition(0, 0);
		ua.moveBy(
			tileScreenCords.x + MapRenderer.TILE_WIDTH/2 - ua.getWidth()/2, 
			tileScreenCords.y + MapRenderer.TILE_HEIGHT/2 - ua.getHeight()/2
		);
	}
	
	private void initTerrainsDrawModel() {
		for (int i=0; i<colonyTiles.length; i++) {
			colonyTiles[i] = null;
		}
		int i = 0;
		for (ColonyTile ct : colony.colonyTiles.entities()) {
			colonyTiles[i] = ct;
			i++;
		}
	}

	void updateProduction() {
		for (int i=0; i<colonyTiles.length; i++) {
			ProductionConsumption productionConsumption = colony.productionSummaryForTerrain(colonyTiles[i]);
			productionQuantityDrawModels[i].init(productionConsumption.realProduction);
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		mapRenderer.drawColonyTiles(colonyTile, batch, shapeRenderer, getX(), getY());
		
		super.draw(batch, parentAlpha);
		
		for (int i=0; i<colonyTiles.length; i++) {
			ColonyTile ct = colonyTiles[i];
			Vector2 tileScreenCords = mapRenderer.mapToScreenCords(ct.tile.x, ct.tile.y);
			
			productionQuantityDrawer.draw(batch, productionQuantityDrawModels[i], 
				getX() + tileScreenCords.x, 
				getY() + tileScreenCords.y
			);
		}
	}
}
