package promitech.colonization.actors.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

public class UnitDrawer {

	private static final float NO_ALPHA = 1f;
    public static final int BOX_HEIGHT = 20;
	public static final int BOX_WIDTH = 17;
	
	public static void drawMapUnitChip(
			Batch batch, ShapeRenderer shapeRenderer, 
			Unit unit, Tile tile, Player currentPlayer,
			float screenX, float screenY,
			float alpha
	) {
		drawChip(batch, shapeRenderer, 
			unit, tile, currentPlayer, 
			screenX + 35, screenY + MapRenderer.TILE_HEIGHT - 5,
			alpha
		);
	}

	public static void drawColonyUnitChip(
			Batch batch, ShapeRenderer shapeRenderer, 
			Unit unit,
			float screenX, float screenY, float unitActorHeight
	) {
		drawChip(batch, shapeRenderer, 
			unit, null, unit.getOwner(), 
			screenX+1, screenY + unitActorHeight - BOX_HEIGHT
		);
	}

    public static void drawChip(
        Batch batch, ShapeRenderer shapeRenderer, 
        Unit unit, Tile tile, Player currentPlayer,
        float cx, float cy
    ) {
        drawChip(batch, shapeRenderer, 
            unit, tile, currentPlayer, 
            cx, cy, 
            NO_ALPHA
        );
    }
	
	public static void drawChip(
			Batch batch, ShapeRenderer shapeRenderer, 
			Unit unit, Tile tile, Player currentPlayer,
			float cx, float cy,
			float alpha
	) {
		batch.end();
		if (alpha != NO_ALPHA) {
		    Gdx.gl.glEnable(GL20.GL_BLEND);
		}
		shapeRenderer.begin(ShapeType.Filled);
		Color nationColor = unit.getOwner().nation().getColor();
        shapeRenderer.setColor(nationColor.r, nationColor.g, nationColor.b, alpha);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, alpha);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		
		if (tile != null && tile.getUnits().size() > 1) {
			shapeRenderer.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
			int max = tile.getUnits().size() > 10 ? 10 : tile.getUnits().size(); 
			for (int i=0; i<max; i++) {
				shapeRenderer.line(cx - 7, cy - (i*2) + 19, cx - 1, cy - (i*2) + 19);
			}
		}
		
		shapeRenderer.end();
		
		batch.begin();
		String occupationKey = unit.getOccupationKey(currentPlayer);
		String text;
		if (occupationKey.length() == 1) {
			text = occupationKey;
		} else {
			text = Messages.msg(occupationKey);
		}
		
		BitmapFont unitBoxFont = FontResource.getUnitBoxFont();
		unitBoxFont.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, alpha);
		float textWidth = FontResource.strWidth(unitBoxFont, text);
		unitBoxFont.draw(batch, text, cx + BOX_WIDTH/2f - textWidth/2, cy + BOX_HEIGHT - 4);
	}
	
    public static void drawMapUnitFocus(Batch batch, ShapeRenderer shapeRenderer, float x, float y) {
        drawFocus(batch, shapeRenderer, 
            x + MapRenderer.TILE_WIDTH/4f, 
            y + MapRenderer.TILE_HEIGHT/4f
        );
    }
	
    public static void drawColonyUnitFocus(
        Batch batch, ShapeRenderer shapeRenderer, 
        float x, float y, 
        float unitImageWidth, boolean drawUnitChip
    ) {
        if (drawUnitChip) {
            x += BOX_WIDTH;
        }
        x += unitImageWidth / 2 - MapRenderer.TILE_WIDTH/4; 
        drawFocus(batch, shapeRenderer, x, y);
    }
    
    private static void drawFocus(Batch batch, ShapeRenderer shapeRenderer, float x, float y) {
        batch.end();
        
        Gdx.gl20.glLineWidth(3);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.ellipse(
                x, 
                y, 
                MapRenderer.TILE_WIDTH/2,
                MapRenderer.TILE_HEIGHT/2
        );
        shapeRenderer.end();
        Gdx.gl20.glLineWidth(1);
        
        batch.begin();
    }
	
}
