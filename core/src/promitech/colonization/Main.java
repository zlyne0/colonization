package promitech.colonization;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.math.Directions;
import promitech.colonization.math.Point;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends ApplicationAdapter {
	
	SpriteBatch batch;
	OrthographicCamera camera;
	OrthographicCamera cameraYdown;
	
	private InputKeyboardDevice inputKeyboardDevice = new InputKeyboardDevice();
	private MapRenderer mapRenderer; 
	private final GameResources gameResources = new GameResources();
	private Game game;
	
	@Override
	public void create () {
		//pointOperations.getCameraPoint().set(10, 10);
		mapRenderer = new MapRenderer(gameResources);
	
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

		cameraYdown = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraYdown.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraYdown.update();
        
		batch = new SpriteBatch();

		
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
		
		try {
			SaveGameParser saveGameParser = new SaveGameParser();
			saveGameParser.parse();
			game = saveGameParser.game;
			
			gameResources.load();
			
			System.out.println("game = " + game);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mapRenderer.initMapTiles(game.map);
	}

	
	
	@Override
	public void render () {
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
		batch.begin();
		mapRenderer.render(batch, game.map);
		batch.end();
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
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
		batch.end();
	}
	
	
}
