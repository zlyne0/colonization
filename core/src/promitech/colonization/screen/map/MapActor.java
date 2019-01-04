package promitech.colonization.screen.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.player.MoveExploredTiles;
import promitech.colonization.GameResources;
import promitech.colonization.math.Point;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.screen.map.unitanimation.UnitTileAnimation;

public class MapActor extends Widget implements Map {

	private final GUIGameModel guiGameModel;
	private final MapDrawModel mapDrawModel;
	private final MapRenderer mapRenderer;
	private final GridPoint2 mapCenteredToCords = new GridPoint2();
	private boolean mapCentered = true;

    private final Array<UnitTileAnimation> unitAnimationsToStart = new Array<UnitTileAnimation>(2); 
	
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	public MapActor(final GUIGameController gameController, GameResources gameResources, GUIGameModel guiGameModel) {
		this.mapDrawModel = new MapDrawModel(gameResources);
		this.guiGameModel = guiGameModel;
		
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
				System.out.println("screenPoint: " + x + ", " + y);
				mapRenderer.screenToMapCords((int)x, (int)y, tmpPoint);
				
				if (getTapCount() > 1) {
					gameController.doubleClickOnTile(tmpPoint);
				} else {
					gameController.clickOnTile(tmpPoint);
				}
			}
		});
		addListener(new ClickListener(Buttons.RIGHT) {
			private final Point tmpPoint = new Point();
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("screenPoint: " + x + ", " + y);
				mapRenderer.screenToMapCords((int)x, (int)y, tmpPoint);
				
				gameController.rightClickOnTile(tmpPoint);
			}
		});
		
		mapRenderer = new MapRenderer(mapDrawModel, gameResources, shapeRenderer);
	}
	
	@Override
	public void resetUnexploredBorders() {
		mapDrawModel.resetUnexploredBorders();
	}
	
	public void resetUnexploredBorders(MoveExploredTiles exploredTiles) {
		mapDrawModel.resetUnexploredBorders(exploredTiles);
	}
	
	@Override
	public void resetMapModel() {
		mapDrawModel.initialize(guiGameModel.game);
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

        if (mapCentered == false) {
			mapCentered = true;
			mapRenderer.centerCameraOnTileCords(mapCenteredToCords.x, mapCenteredToCords.y);
		}
        if (unitAnimationsToStart.size > 0) {
            for (int i = 0; i < unitAnimationsToStart.size; i++) {
                unitAnimationsToStart.get(i).initMapPos(mapRenderer);
                getStage().addAction((Action)unitAnimationsToStart.get(i));
            }
            mapDrawModel.unitTileAnimation = unitAnimationsToStart.get(0);
            unitAnimationsToStart.clear();
        }
        mapRenderer.render(batch);
	}
	
	@Override
	public void layout() {
		mapRenderer.setMapRendererSize((int)getWidth(), (int)getHeight());
	}

	public void centerCameraOnTile(int x, int y) {
		mapCentered = false;
		mapCenteredToCords.set(x, y);
	}
	
	public void centerCameraOnTile(Tile tile) {
		centerCameraOnTile(tile.x, tile.y);
	}

	public void centerCameraOnScreenCords(float x, float y) {
		Point tile = mapRenderer.screenToMapCords((int)x, (int)y);
		centerCameraOnTile(tile.x, tile.y);
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

	public void startUnitTileAnimation(UnitTileAnimation animation) {
		unitAnimationsToStart.add(animation);
		Gdx.graphics.requestRendering();
	}
	
	public void showTileOwners() {
		mapRenderer.showTileOwners();
	}

	public void hideTileOwners() {
		mapRenderer.hideTileOwners();
	}
	
	public void showTileDebugStrings(String strings[][]) {
		mapRenderer.showTileDebugStrings(strings);
	}
	
	public void hideTileDebugStrings() {
	    mapRenderer.hideTileDebugStrings();
	}
}
