package promitech.colonization.screen.map;

import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.IndianSettlementMissionary;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.resources.Messages;

class IndianSettlementChip {
	private static final String CROSS = "+";
	private static final String ALARM = "!";
	
    private static final float NO_ALPHA = 1f;
	private static final int BOX_HEIGHT = 20;
	private static final int BOX_WIDTH = 14;
	
	private final String msg;
	private final IndianSettlement indianSettlement;
	private final boolean missionaryExpert;
	private Player mostHatedPlayer;
	private float tenstionLevel; 
	
	public IndianSettlementChip(MapIdEntities<Player> players, Player playingPlayer, IndianSettlement indianSettlement) {
		this.indianSettlement = indianSettlement;
		String msgKey = "indianSettlement." + (indianSettlement.settlementType.isCapital() ? "capital" : "normal");
		this.msg = Messages.msg(msgKey);
		
		Entry<String, Tension> playerTension = indianSettlement.mostHatedPlayer(players);
		if (playerTension != null) {
			mostHatedPlayer = players.getById(playerTension.getKey());
			float levelVal = Math.min(playerTension.getValue().getValue(), Tension.Level.HATEFUL.getLimit());
			tenstionLevel = (levelVal / (float)Tension.Level.HATEFUL.getLimit()) * BOX_HEIGHT;
		}
		
		if (indianSettlement.hasMissionary()) {
			missionaryExpert = indianSettlement.getMissionary().isMissionaryExpert();
		} else {
			missionaryExpert = false;
		}
	}
	
	public void draw(
		Batch batch, ShapeRenderer shapeRenderer,
		float cx, float cy
	) {
		cx += 30;
		cy += MapRenderer.TILE_HEIGHT - 25;
		float xOffset = 0;
		
		drawSettlementOwnerChip(batch, shapeRenderer, cx, cy);
		
		IndianSettlementMissionary missionary = indianSettlement.getMissionary();
		if (missionary != null) {
			xOffset += BOX_WIDTH + 2;
			drawMissionary(batch, shapeRenderer, cx + xOffset, cy);
		}
		if (mostHatedPlayer != null) {
			xOffset += BOX_WIDTH + 2;
			drawAlarm(batch, shapeRenderer, cx + xOffset, cy);
		}
	}

	private void drawSettlementOwnerChip(Batch batch, ShapeRenderer shapeRenderer, float cx, float cy) {
		Color nationColor = indianSettlement.getOwner().nation().getColor();
		
		batch.end();
		drawBox(shapeRenderer, cx, cy, nationColor);
		
		batch.begin();
		BitmapFont unitBoxFont = FontResource.getUnitBoxFont();
		unitBoxFont.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, NO_ALPHA);
		float textWidth = FontResource.strWidth(unitBoxFont, msg);
		unitBoxFont.draw(batch, msg, cx + BOX_WIDTH/2f - textWidth/2, cy + BOX_HEIGHT - 4);
	}
	
	private void drawMissionary(
		Batch batch, ShapeRenderer shapeRenderer,
		float cx, float cy
	) {
		Color ownerColor = indianSettlement.missionaryOwner().nation().getColor();

		batch.end();
		drawBox(shapeRenderer, cx, cy, ownerColor);
		
		batch.begin();
		
		Color crossColor = Color.GRAY;
		if (missionaryExpert) {
			crossColor = Color.BLACK;
		}
		
		BitmapFont crossBoxFont = FontResource.getUnitBoxFont();
		crossBoxFont.setColor(crossColor.r, crossColor.g, crossColor.b, NO_ALPHA);
		float textWidth = FontResource.strWidth(crossBoxFont, CROSS);
		crossBoxFont.draw(batch, CROSS, cx + BOX_WIDTH/2f - textWidth/2, cy + BOX_HEIGHT - 4);
	}
	
	private void drawAlarm(Batch batch, ShapeRenderer shapeRenderer, float cx, float cy) {
		Color color = indianSettlement.getOwner().nation().getColor();
		Color tenstionColor = mostHatedPlayer.nation().getColor();

		batch.end();
		shapeRenderer.begin(ShapeType.Filled);		
		shapeRenderer.setColor(color.r, color.g, color.b, NO_ALPHA);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.setColor(tenstionColor.r, tenstionColor.g, tenstionColor.b, NO_ALPHA);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, tenstionLevel);
		shapeRenderer.end();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, NO_ALPHA);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.end();
		
		batch.begin();
		
		BitmapFont crossBoxFont = FontResource.getUnitBoxFont();
		crossBoxFont.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, NO_ALPHA);
		float textWidth = FontResource.strWidth(crossBoxFont, ALARM);
		crossBoxFont.draw(batch, ALARM, cx + BOX_WIDTH/2f - textWidth/2, cy + BOX_HEIGHT - 4);
	}
	
	private void drawBox(ShapeRenderer shapeRenderer, float cx, float cy, Color color) {
		shapeRenderer.begin(ShapeType.Filled);		
		shapeRenderer.setColor(color.r, color.g, color.b, NO_ALPHA);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.end();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, NO_ALPHA);
		shapeRenderer.rect(cx, cy, BOX_WIDTH, BOX_HEIGHT);
		shapeRenderer.end();
	}
}