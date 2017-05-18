package promitech.colonization.actors.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapRenderer.TileDrawer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

class ObjectsTileDrawer extends TileDrawer {
	private static final int UNIT_IMAGE_OFFSET = 20;
	
	private int w = MapRenderer.TILE_WIDTH;
	private int h = MapRenderer.TILE_HEIGHT;

	private final GameResources gameResources;
	
	public ObjectsTileDrawer(MapDrawModel mapDrawModel, GameResources gameResources) {
		super(mapDrawModel);
		this.gameResources = gameResources;
	}
	
	@Override
	public void draw() {
		if (tileDrawModel.isNotExplored()) {
			return;
		}
		
		drawSettlement();
		drawTerrainFocus();
		if (tileDrawModel.hasFogOfWar()) {
			return;
		}
		
		// draw selected unit always on top
		boolean drawRestOfUnits = true;
		Unit selectedUnit = mapDrawModel.getSelectedUnit();
		if (selectedUnit != null && !mapDrawModel.unitDislocationAnimation.isUnitAnimated(selectedUnit)) {
			Tile selectedUnitTile = selectedUnit.getTileLocationOrNull();
			if (selectedUnitTile != null && mapx == selectedUnitTile.x && mapy == selectedUnitTile.y) {
				drawUnit(selectedUnit);
				drawRestOfUnits = false;
			}
		}
		if (drawRestOfUnits && tile.getUnits().size() > 0 && !tile.hasSettlement()) {
			Unit firstUnit = tile.getUnits().first();
			
			if (mapDrawModel.unitDislocationAnimation.isUnitAnimated(firstUnit)) {
				mapDrawModel.unitDislocationAnimation.drawUnit(this);
			} else {
				drawUnit(firstUnit);
			}
		}
		
		if (mapDrawModel.unitDislocationAnimation.isTileAnimated(mapx, mapy)) {
			mapDrawModel.unitDislocationAnimation.drawUnit(this);
		}
	}

	private void drawTerrainFocus() {
		if (mapDrawModel.selectedTile == null 
				|| mapDrawModel.selectedTile.x != mapx 
				|| mapDrawModel.selectedTile.y != mapy) {
			return;
		}
		drawFocus();
	}
	
	private void drawFocus() {
		batch.end();
		
		Gdx.gl20.glLineWidth(3);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.ellipse(
				screenPoint.x + MapRenderer.TILE_WIDTH/4, 
				screenPoint.y + MapRenderer.TILE_HEIGHT/4, 
				MapRenderer.TILE_WIDTH/2,
				MapRenderer.TILE_HEIGHT/2
		);
		shapeRenderer.end();
		Gdx.gl20.glLineWidth(1);
		
		batch.begin();
	}
	
	private void drawSettlement() {
		if (!tile.hasSettlement()) {
			return;
		}
		tileDrawModel.drawSettlementImage(batch, screenPoint.x, screenPoint.y);
		
		BitmapFont font = FontResource.getCityNamesFont();
		
		float strWidth = FontResource.strWidth(font, tile.getSettlement().getName());
		font.setColor(tile.getSettlement().getOwner().nation().getColor());
		font.draw(batch, tile.getSettlement().getName(), screenPoint.x + w/2 - strWidth/2, screenPoint.y);
		
		if (tile.hasSettlementOwnedBy(mapDrawModel.playingPlayer) && tile.getSettlement().isColony()) {
			Colony colony = tile.getSettlement().getColony();
			font.setColor(Color.WHITE);
			font.draw(batch, "" + colony.getColonyUnitsCount(), 
					screenPoint.x + w/2, 
					screenPoint.y + h/2 + 5
			);
		}
	}

	protected void drawUnit(Unit unit, Vector2 aScreenPoint) {
		this.screenPoint.set(aScreenPoint);
		this.drawUnit(unit);
	}
	
	protected void drawUnit(Unit unit) {
		Unit selectedUnit = mapDrawModel.getSelectedUnit();
		if (selectedUnit != null && selectedUnit.equalsId(unit)) {
			drawFocus();
		}
		
		Frame frame = gameResources.getCenterAdjustFrameTexture(unit.resourceImageKey());
		batch.draw(frame.texture, 
				screenPoint.x + frame.offsetX, 
				screenPoint.y + frame.offsetY + UNIT_IMAGE_OFFSET
		);
		
		UnitDrawer.drawMapUnitChip(batch, shapeRenderer, 
			unit, tile, mapDrawModel.playingPlayer, 
			screenPoint.x, screenPoint.y
		);
	}
	
}
