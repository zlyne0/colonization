package promitech.colonization.screen.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.screen.map.MapDrawModel;
import promitech.colonization.screen.map.MapRenderer;
import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.screen.ui.IndianLandDemandQuestionsDialog;
import promitech.colonization.screen.ui.UnitActor;
import promitech.colonization.screen.ui.UnitDragAndDropSource;
import promitech.colonization.screen.ui.UnitDragAndDropTarget;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.QuestionDialog;

public class TerrainPanel extends Group implements 
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
	private MapDrawModel mapDrawModel;
	
	private ColonyTile[] colonyTiles = new ColonyTile[9];
	private ProductionQuantityDrawModel[] productionQuantityDrawModels = new ProductionQuantityDrawModel[9];
	
	private final ProductionQuantityDrawer productionQuantityDrawer;
	private final ChangeColonyStateListener changeColonyStateListener;
	private final DoubleClickedListener unitActorDoubleClickListener;
	
	public TerrainPanel(ChangeColonyStateListener changeColonyStateListener, DoubleClickedListener unitActorDoubleClickListener) {
	    this.changeColonyStateListener = changeColonyStateListener;
	    this.unitActorDoubleClickListener = unitActorDoubleClickListener;
		setWidth(PREF_WIDTH);
		setHeight(PREF_HEIGHT);
		
		for (int i=0; i<productionQuantityDrawModels.length; i++) {
			productionQuantityDrawModels[i] = new ProductionQuantityDrawModel();
		}
		productionQuantityDrawer = new ProductionQuantityDrawer(MapRenderer.TILE_WIDTH/2, MapRenderer.TILE_HEIGHT/2);
		productionQuantityDrawer.centerToPoint(MapRenderer.TILE_WIDTH/2, MapRenderer.TILE_HEIGHT/2);
	}

	void changeWorkerProduction(UnitActor unitActor, GoodMaxProductionLocation prodLocation) {
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		ColonyTile aColonyTile = prodLocation.getColonyTile();
		aColonyTile.productionInfo.initProduction(prodLocation.tileTypeInitProduction);
		changeColonyStateListener.changeUnitAllocation();
	}
	
	void putWorkerOnTerrain(UnitActor worker, ColonyTile aColonyTile) {
	    worker.disableFocus();
	    worker.disableUnitChip();	    
		worker.dragAndDropSourceContainer = this;
		
		colony.addWorkerToTerrain(aColonyTile, worker.unit);
		
		worker.updateTexture();

		addActor(worker);
		updateWorkerScreenPosition(worker, aColonyTile);
		
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
		if (ct.tile.hasSettlement()) {
			return false;
		}
		if (ct.tile.hasLostCityRumour()) {
			return false;
		}
		if (colony.isTileLockedBecauseNoDock(ct.tile)) {
			return false;
		}
		return ct.hasNotWorker();
	}

	@Override
	public void takePayload(UnitActor unitActor, float x, float y) {
		unitActor.dragAndDropSourceContainer = null;
		
		if (unitActor.unit.isAtLocation(ColonyTile.class)) {
		    System.out.println("take worker "
		            + "[" + unitActor.unit + "] from "
		            + "[" + unitActor.unit.getLocationOrNull(ColonyTile.class) + "] "
		            );
		    unitActor.unit.removeFromLocation();
            removeActor(unitActor);
            changeColonyStateListener.changeUnitAllocation();
		}
	}

	@Override
	public boolean isPrePutPayload(UnitActor worker, float x, float y) {
		ColonyTile ct = getColonyTileNotNull(x, y);
		return colony.isTileLocked(ct.tile, false);
	}

	@Override
	public void onDragPayload(float screenX, float screenY) {
		Tile tile = mapRenderer.getColonyTileByScreenCords(colonyTile, (int)screenX, (int)screenY);
		if (tile != null) {
			mapDrawModel.selectedTile = tile;
		}
	}

	@Override
	public void onLeaveDragPayload() {
		mapDrawModel.selectedTile = null;
	}
	
	@Override
	public void prePutPayload(final UnitActor worker, final float x, final float y, final DragAndDropSourceContainer<UnitActor> sourceContainer) {
		final ColonyTile ct = getColonyTileNotNull(x, y);
		
		if (ct.tile.getType().isWater()) {
			return;
		}
		if (ct.tile.hasWorkerOnTile()) {
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
			
			QuestionDialog questionDialog = new IndianLandDemandQuestionsDialog(landPrice, worker.unit, ct.tile, moveWorkerAction);
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
	
	private ColonyTile getColonyTile(float screenX, float screenY) {
		Tile tile = mapRenderer.getColonyTileByScreenCords(colonyTile, (int)screenX, (int)screenY);
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
		this.mapDrawModel = mapDrawModel;
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
			if (ct.hasWorker()) {
				
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
			tileScreenCords.x + MapRenderer.TILE_WIDTH/2f - ua.getWidth()/2, 
			tileScreenCords.y + MapRenderer.TILE_HEIGHT/2f - ua.getHeight()/2
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
