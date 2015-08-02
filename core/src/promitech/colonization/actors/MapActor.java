package promitech.colonization.actors;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.GameResources;
import promitech.colonization.InputKeyboardDevice;
import promitech.colonization.math.Directions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MapActor extends Actor {

	private final MapDrawModel mapDrawModel = new MapDrawModel();
	private final MapRenderer mapRenderer;
	
	private InputKeyboardDevice inputKeyboardDevice = new InputKeyboardDevice();
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	public MapActor(final Game game, GameResources gameResources) {
		addListener(new InputListener() {
			private boolean pressed = false;
			private final Vector2 v = new Vector2();
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				pressed = true;
				v.x = x;
				v.y = y;
				return true;
			}
			
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				pressed = false;
			}
			
			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				if (pressed) {
					v.sub(x, y);
					v.x = -v.x;
					mapRenderer.cameraPosition.add(v);
					v.set(x, y);
				}
			}
		});
		
		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				mapRenderer.screenToMapCords((int)x, (int)y, mapDrawModel.unitFocus);
				mapDrawModel.selectedTile = game.map.getTile(mapDrawModel.unitFocus.x, mapDrawModel.unitFocus.y);

				
    			Tile tile = mapDrawModel.selectedTile;
    			System.out.println("screenPoint: " + x + ", " + y);
    			System.out.println("p = " + mapDrawModel.unitFocus);
    			if (tile != null) {
    				System.out.println("tile: " + tile);
    			} else {
    				System.out.println("tile is null");
    			}
    			
    			Object tileDrawModel = mapDrawModel.getTileDrawModel(mapDrawModel.unitFocus.x, mapDrawModel.unitFocus.y);
    			if (tileDrawModel != null) {
    				System.out.println("drawmodel = " + tileDrawModel.toString());
    			}
			}
		});
		
		mapRenderer = new MapRenderer(game.playingPlayer, game.map, mapDrawModel, gameResources, shapeRenderer);
		mapDrawModel.initialize(game.map, game.playingPlayer, gameResources);
	}
	
	@Override
	public void act(float delta) {
		inputKeyboardDevice.recognizeInput();
		Directions moveDirection = inputKeyboardDevice.getMoveDirection();
		if (moveDirection != null) {
			keyDirection(moveDirection);
		}
		super.act(delta);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        shapeRenderer.translate(getX(), getY(), 0);

        mapRenderer.render(batch);
	}
	
	@Override
	protected void sizeChanged() {
		mapRenderer.setMapRendererSize((int)getWidth(), (int)getHeight());
		mapRenderer.centerCameraOnTileCords(24, 78);
	}

	public void keyDirection(Directions moveDirection) {
	    mapRenderer.cameraPosition.add(moveDirection.vx * 10, moveDirection.vy * 10);
	    System.out.println("moveDirection " + moveDirection + " camera.position " + mapRenderer.cameraPosition);
	}

	public void drawSelectedTile(Batch batch, ShapeRenderer shapeRenderer, float px, float py) {
		mapRenderer.drawSelectedTileOnInfoPanel(batch, shapeRenderer, px, py);
	}

	public MapDrawModel mapDrawModel() {
		return mapDrawModel;
	}
}
