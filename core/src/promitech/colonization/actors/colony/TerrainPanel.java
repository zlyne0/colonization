package promitech.colonization.actors.colony;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapDrawModel;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.gdx.Frame;

public class TerrainPanel extends Actor {
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private Colony colony;
	private Tile colonyTile;
	private MapRenderer mapRenderer;
	
	private ColonyTile[] colonyTiles = new ColonyTile[9];
	private Tile[] colonyTerrains = new Tile[9];
	private Frame[] colonyTerrainsWorkerImages = new Frame[9];
	private ProductionQuantityDrawModel[] productionQuantityDrawModels = new ProductionQuantityDrawModel[9];
	
	private final ProductionQuantityDrawer productionQuantityDrawer;
	
	public TerrainPanel() {
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
	
	public void initTerrains(MapDrawModel mapDrawModel, Tile colonyTile) {
		int w = MapRenderer.TILE_WIDTH * 3 + MapRenderer.TILE_WIDTH/2;
		int h = MapRenderer.TILE_HEIGHT * 3 + MapRenderer.TILE_HEIGHT/2;
		setWidth(w);
		setHeight(h);
		
		mapRenderer = new MapRenderer(
				mapDrawModel, 
				GameResources.instance, 
				shapeRenderer
		);
		mapRenderer.setMapRendererSize(w, h);
		mapRenderer.centerCameraOnTileCords(colonyTile.x, colonyTile.y);
		
		this.colonyTile = colonyTile;
		this.colony = (Colony)colonyTile.getSettlement();

		initTerrainsDrawModel();
	}

	private void initTerrainsDrawModel() {
		for (int i=0; i<colonyTiles.length; i++) {
			colonyTiles[i] = null;
			colonyTerrains[i] = null;
			colonyTerrainsWorkerImages[i] = null;
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
			if (ct.getWorker() != null) {
				colonyTerrainsWorkerImages[i] = GameResources.instance.getFrame(ct.getWorker().resourceImageKey());
			}
			i++;
		}
		
		ProductionSummary productionSummary;
		for (i=0; i<colonyTiles.length; i++) {
			ColonyTile ct = colonyTiles[i];
			
			productionSummary = colony.productionSummaryForTerrain(
					colonyTerrains[i], 
					colonyTiles[i] 
					);
			System.out.println("tile " + ct.getWorkTileId() + ", " + productionSummary);
			
			productionQuantityDrawModels[i].init(productionSummary);
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		mapRenderer.drawColonyTiles(colonyTile, batch, shapeRenderer, getX(), getY());
		
		for (int i=0; i<colonyTiles.length; i++) {
			Tile t = colonyTerrains[i];
			Vector2 tileScreenCords = mapRenderer.mapToScreenCords(t.x, t.y);
			
			Frame workerImg = colonyTerrainsWorkerImages[i];
			if (workerImg != null) {
				batch.draw(
					workerImg.texture, 
					getX() + tileScreenCords.x + MapRenderer.TILE_WIDTH/2 - workerImg.texture.getRegionWidth()/2, 
					getY() + tileScreenCords.y + MapRenderer.TILE_HEIGHT/2 - workerImg.texture.getRegionHeight()/2
				);
			}
			
			productionQuantityDrawer.draw(batch, productionQuantityDrawModels[i], 
				getX() + tileScreenCords.x, 
				getY() + tileScreenCords.y
			);
		}
	}
	
}
