package promitech.colonization.actors.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

public class UnitDrawer {

	public static final int BOX_HEIGHT = 20;
	public static final int BOX_WIDTH = 17;
	
	public static void drawMapUnitChip(
			Batch batch, ShapeRenderer shapeRenderer, 
			Unit unit, Tile tile, Player currentPlayer,
			float screenX, float screenY
	) {
		drawChip(batch, shapeRenderer, 
			unit, tile, currentPlayer, 
			screenX + 35, screenY + MapRenderer.TILE_HEIGHT - 5
		);
	}

	public static void drawColonyUnitChip(
			Batch batch, ShapeRenderer shapeRenderer, 
			Unit unit,
			float screenX, float screenY, float unitImageHeight
	) {
		drawChip(batch, shapeRenderer, 
			unit, null, unit.getOwner(), 
			screenX+1, screenY + unitImageHeight - BOX_HEIGHT
		);
	}
	
	public static void drawChip(
			Batch batch, ShapeRenderer shapeRenderer, 
			Unit unit, Tile tile, Player currentPlayer,
			float cx, float cy
	) {
		batch.end();
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(unit.getOwner().getNation().getColor());
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		
		if (tile != null && tile.units.size() > 1) {
			shapeRenderer.setColor(Color.WHITE);
			int max = tile.units.size() > 10 ? 10 : tile.units.size(); 
			for (int i=0; i<max; i++) {
				shapeRenderer.line(cx - 7, cy - (i*2) + 19, cx - 1, cy - (i*2) + 19);
			}
		}
		
		shapeRenderer.end();
		
		batch.begin();
		
		String occupationKey = unit.getOccupationKey(currentPlayer);
		String text = Messages.msg(occupationKey);
		
		BitmapFont unitBoxFont = FontResource.getUnitBoxFont();
		unitBoxFont.setColor(Color.BLACK);
		float textWidth = FontResource.strWidth(unitBoxFont, text);
		unitBoxFont.draw(batch, text, cx + BOX_WIDTH/2 - textWidth/2, cy + BOX_HEIGHT - 4);
	}
	
	
}
