package promitech.colonization.actors;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.GameController;
import promitech.colonization.GameResources;
import promitech.colonization.actors.UnitDislocationAnimation.EndOfAnimation;
import promitech.colonization.math.Point;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MapActor extends Actor {

	private final GameResources gameResources;
	private final MapDrawModel mapDrawModel = new MapDrawModel();
	private final MapRenderer mapRenderer;
	private final Game game;
	
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private GameController gameController;
	
	public MapActor(final Game game, GameResources gameResources) {
		this.game = game;
		this.gameResources = gameResources;
		
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
			private final Point tmpPoint = new Point(); 
			@Override
			public void clicked(InputEvent event, float x, float y) {
				mapRenderer.screenToMapCords((int)x, (int)y, tmpPoint);

				gameController.clickOnTile(tmpPoint);
				
				{
	    			Tile tile = game.map.getTile(tmpPoint.x, tmpPoint.y);
	    			System.out.println("screenPoint: " + x + ", " + y);
	    			System.out.println("p = " + tmpPoint);
	    			if (tile != null) {
	    				System.out.println("tile: " + tile);
	    			} else {
	    				System.out.println("tile is null");
	    			}
	    			Object tileDrawModel = mapDrawModel.getTileDrawModel(tmpPoint.x, tmpPoint.y);
	    			if (tileDrawModel != null) {
	    				System.out.println("drawmodel = " + tileDrawModel.toString());
	    			}
				}
			}
		});
		
		mapRenderer = new MapRenderer(game.playingPlayer, game.map, mapDrawModel, gameResources, shapeRenderer);
		mapDrawModel.initialize(game.map, game.playingPlayer, gameResources);
	}
	
	public void resetUnexploredBorders() {
		mapDrawModel.resetUnexploredBorders(game.map, gameResources);
	}
	
	@Override
	public void act(float delta) {
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

	public void centerCameraOnTile(Tile tile) {
		mapRenderer.centerCameraOnTileCords(tile.x, tile.y);
	}

	public Point getCenterOfScreen() {
		return mapRenderer.getCenterOfScreen();
	}
	
	public boolean isTileOnScreenEdge(Tile tile) {
		return mapRenderer.isPointOnScreenEdge(tile.x, tile.y);
	}

	public void drawSelectedTile(Batch batch, ShapeRenderer shapeRenderer, float screenX, float screenY) {
		mapRenderer.drawSelectedTileOnInfoPanel(batch, shapeRenderer, screenX, screenY);
	}

	public MapDrawModel mapDrawModel() {
		return mapDrawModel;
	}

	public void setGameController(GameController gameController) {
		this.gameController = gameController;
	}

	public void startUnitDislocationAnimation(
			Unit unit, 
			Tile sourceTile, Tile descTile, 
			EndOfAnimation endOfUnitDislocationAnimation) 
	{
		mapDrawModel.unitDislocationAnimation.init(mapRenderer, unit, sourceTile, descTile);
		mapDrawModel.unitDislocationAnimation.addEndOfAnimation(endOfUnitDislocationAnimation);
	}
}
