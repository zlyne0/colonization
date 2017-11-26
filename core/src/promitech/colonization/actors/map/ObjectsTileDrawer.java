package promitech.colonization.actors.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameResources;
import promitech.colonization.actors.map.MapRenderer.TileDrawer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

class ObjectsTileDrawer extends TileDrawer {
	private static final float NO_ALPHA = 1f;

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
		

		
		if (mapDrawModel.unitTileAnimation.isTileAnimated(mapx, mapy)) {
			// tile is animated so tak first unit as background
			if (!tile.hasSettlement()) {
				Unit unitToDraw = firstButNot(tile.getUnits(), mapDrawModel.unitTileAnimation.getUnit());
				if (unitToDraw != null) {
					drawUnit(unitToDraw);
				}
			}
			mapDrawModel.unitTileAnimation.drawUnit(this);
		} else {
			// tile is not animated so take first unit or selected
			Unit unitToDraw = null;
			Unit selectedUnit = mapDrawModel.getSelectedUnit();
			if (selectedUnit != null) {
				Tile selectedUnitTile = selectedUnit.getTileLocationOrNull();
				if (selectedUnitTile != null && mapx == selectedUnitTile.x && mapy == selectedUnitTile.y) {
					unitToDraw = selectedUnit;
				}
			}
			if (unitToDraw == null) {
				// no selected unit on actual tile
				if (!tile.hasSettlement()) {
					unitToDraw = tile.getUnits().first();
				}
			}
			if (unitToDraw != null) {
				drawUnit(unitToDraw);
			}
		}
	}

	// TODO: refactoring move to MapIdEntities
	private Unit firstButNot(MapIdEntities<Unit> units, Unit excludeUnit) {
		if (excludeUnit == null) {
			return units.first();
		}
		for (Unit u : units.entities()) {
			if (!u.equalsId(excludeUnit)) {
				return u;
			}
		}
		return null;
	}
	
	private void drawTerrainFocus() {
		if (mapDrawModel.selectedTile == null 
				|| mapDrawModel.selectedTile.x != mapx 
				|| mapDrawModel.selectedTile.y != mapy) {
			return;
		}
		drawFocus(NO_ALPHA);
	}
	
	private void drawFocus(float alpha) {
		batch.end();
		
		Gdx.gl20.glLineWidth(3);
        if (alpha != NO_ALPHA) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
        }
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
		shapeRenderer.ellipse(
				screenPoint.x + MapRenderer.TILE_WIDTH/4f, 
				screenPoint.y + MapRenderer.TILE_HEIGHT/4f, 
				MapRenderer.TILE_WIDTH/2f,
				MapRenderer.TILE_HEIGHT/2f
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
		font.draw(batch, tile.getSettlement().getName(), screenPoint.x + w/2f - strWidth/2, screenPoint.y);
		
		if (tile.hasSettlementOwnedBy(mapDrawModel.playingPlayer) && tile.getSettlement().isColony()) {
			Colony colony = tile.getSettlement().getColony();
			font.setColor(Color.WHITE);
			font.draw(batch, Integer.toString(colony.getColonyUnitsCount()), 
					screenPoint.x + w/2f, 
					screenPoint.y + h/2f + 5
			);
		}
	}

	protected void drawUnit(Unit unit, Vector2 aScreenPoint) {
	    this.screenPoint.set(aScreenPoint);
	    this.drawUnit(unit);
	}
	
    protected void drawUnit(Unit unit, Vector2 pos, float alpha) {
        this.screenPoint.set(pos);
        
        Unit selectedUnit = mapDrawModel.getSelectedUnit();
        if (selectedUnit != null && selectedUnit.equalsId(unit)) {
            drawFocus(alpha);
        }
        
        Frame frame = gameResources.getCenterAdjustFrameTexture(unit.resourceImageKey());
        
        Color tmpColor = batch.getColor();
        batch.setColor(tmpColor.r, tmpColor.g, tmpColor.b, alpha);
        batch.draw(frame.texture, 
                screenPoint.x + frame.offsetX, 
                screenPoint.y + frame.offsetY + UNIT_IMAGE_OFFSET
        );
        batch.setColor(tmpColor.r, tmpColor.g, tmpColor.b, 1f);
        
        UnitDrawer.drawMapUnitChip(batch, shapeRenderer, 
            unit, tile, mapDrawModel.playingPlayer, 
            screenPoint.x, screenPoint.y,
            alpha
        );
    }
	
	protected void drawUnit(Unit unit) {
		Unit selectedUnit = mapDrawModel.getSelectedUnit();
		if (selectedUnit != null && selectedUnit.equalsId(unit)) {
			drawFocus(NO_ALPHA);
		}
		
		Frame frame = gameResources.getCenterAdjustFrameTexture(unit.resourceImageKey());
		batch.draw(frame.texture, 
				screenPoint.x + frame.offsetX, 
				screenPoint.y + frame.offsetY + UNIT_IMAGE_OFFSET
		);
		
		UnitDrawer.drawMapUnitChip(batch, shapeRenderer, 
			unit, tile, mapDrawModel.playingPlayer, 
			screenPoint.x, screenPoint.y,
			NO_ALPHA
		);
	}
	
}
