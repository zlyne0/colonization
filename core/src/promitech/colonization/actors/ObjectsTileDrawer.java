package promitech.colonization.actors;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.MapRenderer.TileDrawer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

class ObjectsTileDrawer extends TileDrawer {
	private static final int UNIT_IMAGE_OFFSET = 20;
	
	protected Player player;
	
	private int w = MapRenderer.TILE_WIDTH;
	private int h = MapRenderer.TILE_HEIGHT;

	private final GameResources gameResources;
	
	public ObjectsTileDrawer(GameResources gameResources) {
		this.gameResources = gameResources;
	}
	
	@Override
	public void draw() {
		if (player.isTileUnExplored(tile)) {
			return;
		}
		
		drawSettlement();
		drawTerrainFocus();
		if (player.hasFogOfWar(tile)) {
			return;
		}
		
		boolean drawUnits = true;
		if (mapDrawModel.selectedUnit != null) {
			Tile selectedUnitTile = mapDrawModel.selectedUnit.getTile();
			if (selectedUnitTile != null && mapx == selectedUnitTile.x && mapy == selectedUnitTile.y) {
				
				if (mapDrawModel.unitDislocationAnimation.isUnitAnimated(mapDrawModel.selectedUnit)) {
					if (mapDrawModel.unitDislocationAnimation.nextStep()) {
						screenPoint.set(mapDrawModel.unitDislocationAnimation.v);
					}
				}
				
				drawFocus();
				Frame frame = gameResources.getCenterAdjustFrameTexture(mapDrawModel.selectedUnit.resourceImageKey());
				drawUnit(mapDrawModel.selectedUnit, frame);
				drawUnits = false;
			}
		}
		if (drawUnits && tile.units.size() > 0 && !tile.hasSettlement()) {
			Unit firstUnit = tile.units.first();
			Frame frame = gameResources.getCenterAdjustFrameTexture(firstUnit.resourceImageKey());
			drawUnit(firstUnit, frame);
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
		font.setColor(tile.getSettlement().getOwner().getNation().getColor());
		font.draw(batch, tile.getSettlement().getName(), screenPoint.x + w/2 - strWidth/2, screenPoint.y);
		
		if (tile.hasSettlementOwnedBy(player) && tile.getSettlement().isColony()) {
			Colony colony = tile.getSettlement().getColony();
			font.setColor(Color.WHITE);
			font.draw(batch, "" + colony.getDisplayUnitCount(), 
					screenPoint.x + w/2, screenPoint.y + h/2 + 5
			);
		}
	}
	
	private void drawUnit(Unit unit, Frame frame) {
		batch.draw(frame.texture, 
				screenPoint.x + frame.offsetX, screenPoint.y + frame.offsetY + UNIT_IMAGE_OFFSET
		);
		
		drawUnitChip(unit);
	}
	
	private static final int BOX_WIDTH = 17;
	
	private void drawUnitChip(Unit unit) {
		float cx = screenPoint.x + 35;
		float cy = screenPoint.y + h - 5;
		
		batch.end();
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(unit.getOwner().getNation().getColor());
		shapeRenderer.rect(cx, cy, BOX_WIDTH, 20);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, 20);
		
		if (tile.units.size() > 1) {
			shapeRenderer.setColor(Color.WHITE);
			int max = tile.units.size() > 10 ? 10 : tile.units.size(); 
			for (int i=0; i<max; i++) {
				shapeRenderer.line(cx - 7, cy - (i*2) + 19, cx - 1, cy - (i*2) + 19);
			}
		}
		
		shapeRenderer.end();
		
		batch.begin();
		
		String occupationKey = unit.getOccupationKey(player);
		String text = Messages.msg(occupationKey);
		
		BitmapFont unitBoxFont = FontResource.getUnitBoxFont();
		unitBoxFont.setColor(Color.BLACK);
		float textWidth = FontResource.strWidth(unitBoxFont, text);
		unitBoxFont.draw(batch, text, cx + BOX_WIDTH/2 - textWidth/2, cy + 20 - 2);
	}
}
