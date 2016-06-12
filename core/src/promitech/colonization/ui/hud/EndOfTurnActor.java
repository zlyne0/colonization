package promitech.colonization.ui.hud;

import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.EndOfTurnPhaseListener;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.infrastructure.ThreadsResources;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class EndOfTurnActor extends Actor {

	private static final Color BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.6f);
	private final ShapeRenderer shapeRenderer;
	private final AtomicReference<Player> playerTurn = new AtomicReference<Player>();
	private final GUIGameController gameController;
	
	public EndOfTurnActor(ShapeRenderer shapeRenderer, final GUIGameController gameController) {
		this.shapeRenderer = shapeRenderer;
		this.gameController = gameController;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (playerTurn.get() != null) {
			batch.end();
			
	        Gdx.gl.glEnable(GL20.GL_BLEND);
	        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(BACKGROUND_COLOR);
			shapeRenderer.rect(0, 0, getWidth(), getHeight());
			shapeRenderer.end();
			
			batch.begin();
			
			Nation nation = playerTurn.get().nation();
			
            StringTemplate t = StringTemplate.template("waitingFor")
                .addStringTemplate("%nation%", playerTurn.get().getNationName());
            String st = Messages.message(t);
			
            BitmapFont cityNamesFont = FontResource.getCityNamesFont();
            cityNamesFont.setColor(nation.getColor());
            
			float strWidth = FontResource.strWidth(cityNamesFont, st);
			float x = getWidth() / 2 - strWidth / 2;
			float y = getHeight() / 2 - FontResource.fontHeight(cityNamesFont) / 2;
			cityNamesFont.draw(batch, st, x, y);
			
			
			Frame coatOfArms = GameResources.instance.coatOfArms(nation);
			batch.draw(
				coatOfArms.texture, 
				getWidth()/2 - coatOfArms.texture.getRegionWidth()/2, 
				y
			);
		}
	}

	private EndOfTurnPhaseListener endOfTurnPhaseListener = new EndOfTurnPhaseListener() {
		@Override
		public void nextAIturn(Player player) {
			playerTurn.set(player);
			Gdx.graphics.requestRendering();
		}

		@Override
		public void endOfAIturns() {
			EndOfTurnActor.this.remove();
			
			playerTurn.set(null);
			Gdx.graphics.requestRendering();
		}
	};
	
	private final Runnable endTurnCalculations = new Runnable() {
		@Override
		public void run() {
			gameController.endTurn(endOfTurnPhaseListener);
		}
	};
	
	public void start(final GUIGameController gameController) {
		ThreadsResources.instance.executeAImovement(endTurnCalculations);
	}
}
