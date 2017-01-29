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
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.UnitActor;
import promitech.colonization.actors.UnitDragAndDropSource;
import promitech.colonization.actors.UnitDragAndDropTarget;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.QuestionDialog;

public class TerrainPanel extends Table implements 
	DragAndDropSourceContainer<UnitActor>, 
	DragAndDropTargetContainer<UnitActor>,
	DragAndDropPreHandlerTargetContainer<UnitActor>
{
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
	private final GUIGameController gameController;
	
	@Override
	public float getPrefWidth() {
		return PREF_WIDTH;
	}
	
	@Override
	public float getPrefHeight() {
		return PREF_HEIGHT;
	}
	
	public TerrainPanel(ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener, GUIGameController gameController) {
	    this.changeColonyStateListener = changeColonyStateListener;
	    this.unitActorDoubleClickListener = unitActorDoubleClickListener;
	    this.gameController = gameController;
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
		changeColonyStateListener.changeUnitAllocation();
	}
	
	void putWorkerOnTerrain(UnitActor worker, ColonyTile aColonyTile) {
	    worker.disableFocus();
	    worker.disableUnitChip();	    
		worker.dragAndDropSourceContainer = this;
		
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		colony.addWorkerToTerrain(aColonyTile, worker.unit);
		worker.updateTexture();

		addActor(worker);
		updateWorkerScreenPosition(worker, aColonyTile);

		colony.initMaxPossibleProductionOnTile(aColonyTile);
		
		changeColonyStateListener.changeUnitAllocation();
	}
	
	@Override
	public void putPayload(UnitActor worker, float x, float y) {
		ColonyTile destColonyTile = getColonyTile(x, y);
		if (destColonyTile == null) {
			throw new IllegalStateException("can not find dest colony tile by screen cords. Should invoke canPutpayload before");
		}
		putWorkerOnTerrain(worker, destColonyTile);
	}

	@Override
	public boolean canPutPayload(UnitActor unitActor, float x, float y) {
		ColonyTile ct = getColonyTile(x, y);
		if (ct == null) {
			return false;
		}
		if (ct.equalsId(colonyTile)) {
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
				
				changeColonyStateListener.changeUnitAllocation();
				return;
			}
		}
		throw new IllegalStateException("can not find colony tile by workerId: " + unitActor.unit.getId());
	}

	@Override
	public boolean isPrePutPayload(UnitActor worker, float x, float y) {
		ColonyTile ct = getColonyTileNotNull(x, y);
		return colony.isTileLocked(ct.tile);
	}

	@Override
	public void prePutPayload(final UnitActor worker, final float x, final float y, final DragAndDropSourceContainer<UnitActor> sourceContainer) {
		final ColonyTile ct = getColonyTileNotNull(x, y);
		
		if (ct.tile.getType().isWater()) {
			return;
		}
		
		int landPrice = -1;
		if (worker.unit.getOwner().hasContacted(ct.tile.getOwner())) {
			landPrice = ct.tile.getLandPriceForPlayer(worker.unit.getOwner());
		}
		
		if (landPrice != 0) {			
			final QuestionDialog.OptionAction<Unit> moveWorkerAction = new QuestionDialog.OptionAction<Unit>() {
				@Override
				public void executeAction(Unit claimedUnit) {
					sourceContainer.takePayload(worker, x, y);
					putWorkerOnTerrain(worker, ct);
				}
			};
			
			QuestionDialog questionDialog = gameController.createIndianLandDemandQuestions(landPrice, worker.unit, ct.tile, moveWorkerAction);
			questionDialog.show(getStage());
		}
	}

	private ColonyTile getColonyTileNotNull(float x, float y) {
		ColonyTile ct = getColonyTile(x, y);
		if (ct == null) {
			throw new IllegalStateException("can not find tile by cords [" + x + ", " + y + "]");
		}
		return ct;
	}
	
	private ColonyTile getColonyTile(float x, float y) {
		Tile tile = mapRenderer.getColonyTileByScreenCords(colonyTile, (int)x, (int)y);
		if (tile == null) {
			return null;
		}
		for (int i=0; i<colonyTiles.length; i++) {
			if (colonyTiles[i].equalsId(tile)) {
				return colonyTiles[i];
			}
		}
		return null;
	}
	
	public void initTerrains(MapDrawModel mapDrawModel, Tile colonyTile, DragAndDrop dragAndDrop) {
		dragAndDrop.addTarget(new UnitDragAndDropTarget(this, this));
		clear();
		
		this.colonyTile = colonyTile;
		this.colony = (Colony)colonyTile.getSettlement();
		
		mapRenderer = new MapRenderer(
				mapDrawModel, 
				GameResources.instance, 
				shapeRenderer
		);
		mapRenderer.setMapRendererSize(PREF_WIDTH, PREF_HEIGHT);
		mapRenderer.centerCameraOnTileCords(colonyTile.x, colonyTile.y);
		mapRenderer.showColonyLockedTiles(colony);

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
			ProductionConsumption productionConsumption = colony.productionSummary(colonyTiles[i]);
			productionQuantityDrawModels[i].init(productionConsumption.realProduction);
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
		
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
