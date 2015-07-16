package promitech.colonization;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.math.Directions;
import promitech.colonization.math.Point;
import promitech.colonization.savegame.SaveGameParser;

public class Main extends ApplicationAdapter {
	
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;
	OrthographicCamera camera;
	OrthographicCamera cameraYdown;
	
	private InputKeyboardDevice inputKeyboardDevice = new InputKeyboardDevice();
	private MapRenderer mapRenderer; 
	private GameResources gameResources;
	private Game game;
	private Player player;
	
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
		
        Gdx.input.setInputProcessor(new InputAdapter() {
        	@Override
        	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        		if (button == Input.Buttons.LEFT) {
        		    Point p = new Point();
        		    mapRenderer.screenToMapCords(screenX, screenY, p);
        		    
        		    System.out.println("p = " + p);
        			Tile tile = game.map.getTile(p.x, p.y);
        			if (tile != null) {
        				System.out.println("tile: " + tile);
        				
//        				Direction direction  = Direction.N;
//						Tile t1 = game.map.getTile(p.x, p.y, direction.getNextDirection());
//						Tile t2 = game.map.getTile(p.x, p.y, direction.getPreviousDirection());
//        				System.out.println("" + direction.getNextDirection() + " " + t1);
//        				System.out.println("" + direction.getPreviousDirection() + " " + t2);
        				
        			} else {
        				System.out.println("tile is null");
        			}
        		}
         		return false;
        	}
        });

		gameResources = new GameResources();
        
		try {
			SaveGameParser saveGameParser = new SaveGameParser();
			game = saveGameParser.parse();
			
			gameResources.load();
			
			System.out.println("game = " + game);
		} catch (Exception e) {
			e.printStackTrace();
		}
		player = game.players.getById("player:1");
		
		mapRenderer = new MapRenderer(
				game.map, gameResources, 
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
				batch, shapeRenderer
		);
		mapRenderer.centerCameraOnTileCords(24, 78);
		mapRenderer.initMapTiles(game.map, player);
	}
	
	@Override
	public void render () {
		frameRenderMillis = System.currentTimeMillis();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		inputKeyboardDevice.recognizeInput();
		Directions moveDirection = inputKeyboardDevice.getMoveDirection();
		if (moveDirection != null) {
		    mapRenderer.cameraPosition.add(moveDirection.vx * 10, moveDirection.vy * 10);
		    System.out.println("moveDirection " + moveDirection + " camera.position " + mapRenderer.cameraPosition);
			//pointOperations.getCameraPoint().add(moveDirection.vx, moveDirection.vy);
		}
		
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer.setProjectionMatrix(camera.combined);
		batch.begin();
		mapRenderer.render(player);
		batch.end();
		
		frameRenderMillis = System.currentTimeMillis() - frameRenderMillis;
		
		drawFPS();
	}
	
	private BitmapFont font;	
	
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
	
	private long frameRenderMillis;
	
}
