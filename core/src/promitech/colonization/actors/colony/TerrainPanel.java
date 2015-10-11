package promitech.colonization.actors.colony;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.actors.map.MapRenderer;

public class TerrainPanel extends Table {
	private static final int PREF_WIDTH = MapRenderer.TILE_WIDTH * 3 + MapRenderer.TILE_WIDTH/2;
	private static final int PREF_HEIGHT = MapRenderer.TILE_HEIGHT * 3 + MapRenderer.TILE_HEIGHT/2;
	
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private Colony colony;
	private Tile colonyTile;
	private MapRenderer mapRenderer;
	
	private ColonyTile[] colonyTiles = new ColonyTile[9];
	private Tile[] colonyTerrains = new Tile[9];
	private UnitActor[] colonyTerrainsWorkers = new UnitActor[9];
	private ProductionQuantityDrawModel[] productionQuantityDrawModels = new ProductionQuantityDrawModel[9];
	
	private final ProductionQuantityDrawer productionQuantityDrawer;
	
	@Override
	public float getPrefWidth() {
		return PREF_WIDTH;
	}
	
	@Override
	public float getPrefHeight() {
		return PREF_HEIGHT;
	}
	
	public TerrainPanel() {
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
		
		for (int i=0; i<9; i++) {
			productionQuantityDrawModels[i] = new ProductionQuantityDrawModel();
		}
		productionQuantityDrawer = new ProductionQuantityDrawer(MapRenderer.TILE_WIDTH/2, MapRenderer.TILE_HEIGHT/2);
		productionQuantityDrawer.centerToPoint(MapRenderer.TILE_WIDTH/2, MapRenderer.TILE_HEIGHT/2);
		
		addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				System.out.println("terrain panel touch down xy [" + x + "," + y + "]");
				
				Tile tile = mapRenderer.getColonyTileByScreenCords(colonyTile, (int)x, (int)y);
				System.out.println("tile = " + tile);
				return super.touchDown(event, x, y, pointer, button);
			}
		});
	}
	
	public boolean canPutWorkerOnTerrain(float x, float y) {
		ColonyTile ct = getColonyTileByScreenCords(x, y);
		if (ct == null) {
			return false;
		}
		if (ct.getWorkTileId().equals(colonyTile.getId())) {
			return false;
		}
		return ct.getWorker() == null;
	}
	
	public ColonyTile getColonyTileByScreenCords(float x, float y) {
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
	
	public void moveWorkerToTile(UnitActor worker, ColonyTile destColonyTile) {
		for (int i=0; i<colonyTerrainsWorkers.length; i++) {
			UnitActor tWorker = colonyTerrainsWorkers[i];
			if (tWorker != null && tWorker.unit.equalsId(worker.unit)) {
				ColonyTile sourceColonyTile = colonyTiles[i];
				
				System.out.println("move worker "
						+ "[" + worker.unit + "] from "
						+ "[" + sourceColonyTile.getId() + "] to "
						+ "[" + destColonyTile.getId() + "]");
				
				sourceColonyTile.moveWorkerTo(destColonyTile);
				colonyTerrainsWorkers[i] = null;
				putWorkerOnTile(worker, destColonyTile);
				return;
			}
		}
		throw new IllegalStateException("can not find source colony tile by worker: " + worker.unit.getId());
	}
	
	public void putWorkerOnTile(UnitActor worker, ColonyTile destColonyTile) {
		for (int i=0; i<colonyTiles.length; i++) {
			if (colonyTiles[i].equalsId(destColonyTile)) {
				colonyTerrainsWorkers[i] = worker;
				destColonyTile.setWorker(worker.unit);
				addActor(worker);
				updateWorkerScreenPosition(worker, colonyTerrains[i]);
				initProduction();
				return;
			}
		}
		throw new IllegalStateException("can not find colony tile by id: " + destColonyTile.getId());
	}
	
	public void takeWorker(UnitActor unitActor) {
		for (int i=0; i<colonyTerrainsWorkers.length; i++) {
			UnitActor tWorker = colonyTerrainsWorkers[i];
			if (tWorker != null && tWorker.unit.equalsId(unitActor.unit)) {
				colonyTiles[i].takeWorker();
				colonyTerrainsWorkers[i] = null;
				removeActor(unitActor);
				initProduction();
				return;
			}
		}
		throw new IllegalStateException("can not find colony tile by workerId: " + unitActor.unit.getId());
	}
	
	public void initTerrains(MapDrawModel mapDrawModel, Tile colonyTile, DragAndDrop dragAndDrop) {
		dragAndDrop.addTarget(new UnitTerrainDragAndDropTarget(this));
		
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
		initProduction();
	}

	private void initWorkersActors(DragAndDrop dragAndDrop) {
		for (int i=0; i<colonyTiles.length; i++) {
			colonyTerrainsWorkers[i] = null;
			ColonyTile ct = colonyTiles[i];
			Tile t = colonyTerrains[i];
			
			
			if (ct.getWorker() != null) {
				UnitActor ua = new UnitActor(ct.getWorker());
				addActor(ua);
				updateWorkerScreenPosition(ua, t);
				
				dragAndDrop.addSource(new UnitDragAndDropSource(ua));
				colonyTerrainsWorkers[i] = ua;
			}
		}
	}

	private void updateWorkerScreenPosition(UnitActor ua, Tile t) {
		Vector2 tileScreenCords = mapRenderer.mapToScreenCords(t.x, t.y);
		ua.setPosition(0, 0);
		ua.moveBy(
			tileScreenCords.x + MapRenderer.TILE_WIDTH/2 - ua.getWidth()/2, 
			tileScreenCords.y + MapRenderer.TILE_HEIGHT/2 - ua.getHeight()/2
		);
	}
	
	private void initTerrainsDrawModel() {
		for (int i=0; i<colonyTiles.length; i++) {
			colonyTiles[i] = null;
			colonyTerrains[i] = null;
		}
		java.util.Map<String,Tile> colonyTileById = new HashMap<String, Tile>(); 
		mapRenderer.populateColonyTiles(colonyTile, colonyTileById);
		
		int i = 0;
		for (ColonyTile ct : colony.colonyTiles.entities()) {
			colonyTiles[i] = ct;
			colonyTerrains[i] = colonyTileById.get(ct.getWorkTileId());
			if (colonyTerrains[i] == null) {
				throw new IllegalStateException("can not find colony terrain by colony tile work id: " + ct.getWorkTileId());
			}
			i++;
		}
	}

	private void initProduction() {
		for (int i=0; i<colonyTiles.length; i++) {
			ProductionSummary productionSummary = colony.productionSummaryForTerrain(
				colonyTerrains[i], 
				colonyTiles[i] 
			);
			productionQuantityDrawModels[i].init(productionSummary);
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		mapRenderer.drawColonyTiles(colonyTile, batch, shapeRenderer, getX(), getY());
		
		super.draw(batch, parentAlpha);
		
		for (int i=0; i<colonyTiles.length; i++) {
			Tile t = colonyTerrains[i];
			Vector2 tileScreenCords = mapRenderer.mapToScreenCords(t.x, t.y);
			
			productionQuantityDrawer.draw(batch, productionQuantityDrawModels[i], 
				getX() + tileScreenCords.x, 
				getY() + tileScreenCords.y
			);
		}
	}

}
