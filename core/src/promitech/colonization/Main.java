package promitech.colonization;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import promitech.colonization.actors.MapActor;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.infrastructure.ManyStageInputProcessor;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.hud.HudStage;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
	
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;
	OrthographicCamera camera;
	OrthographicCamera cameraYdown;
	
	private GameResources gameResources;
	private Game game;
	private Player player;
	
	
	private Stage stage;
	private HudStage hudStage;
	
	@Override
	public void create () {
		
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

		cameraYdown = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraYdown.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraYdown.update();
        
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		
		gameResources = new GameResources();
        
		try {
			FontResource.instance().load();
			
			SaveGameParser saveGameParser = new SaveGameParser();
			game = saveGameParser.parse();
			
			gameResources.load();
			
			System.out.println("game = " + game);
		} catch (Exception e) {
			e.printStackTrace();
		}
		player = game.players.getById("player:1");
		
		MapActor mapActor = new MapActor(player, game, gameResources);
		
		hudStage = new HudStage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), gameResources);
		//stage = new Stage(new CenterSizableViewport(640, 480, 640, 480));
		stage = new Stage();
		Gdx.input.setInputProcessor(new ManyStageInputProcessor(hudStage, stage));
		
        Table w = new Table();
		w.setFillParent(true);
		w.defaults().fillX();
		w.defaults().fillY();
		w.defaults().expandX();
		w.defaults().expandY();
		w.add(mapActor);
		w.pack();
		
		stage.addActor(w);		
	}
	
	@Override
	public void render () {
		frameRenderMillis = System.currentTimeMillis();
		
//		Gdx.gl.glEnable(GL20.GL_BLEND);
//    	Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
        stage.getViewport().apply();
        stage.act();
        stage.draw();

        hudStage.getViewport().apply();
        hudStage.act();
        hudStage.draw();
		
		frameRenderMillis = System.currentTimeMillis() - frameRenderMillis;
		
		drawFPS();
	}
	
	private BitmapFont font;	
	private long frameRenderMillis;
	
	private void drawFPS() {
		if (font == null) {
			font = new BitmapFont(true);
		}
		batch.setProjectionMatrix(cameraYdown.combined);
		batch.begin();
		font.setColor(Color.GREEN);
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond() + ", time: " + frameRenderMillis, 20, 20);
		batch.end();
	}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        hudStage.getViewport().update(width, height, true);
    }
    
    @Override
    public void dispose() {
    	FontResource.instance().dispose();
    }
}
