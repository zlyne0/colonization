package promitech.colonization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import promitech.colonization.gdx.Frame;
import promitech.colonization.math.Directions;
import promitech.colonization.math.Point;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends ApplicationAdapter {
	private static final int TILE_WIDTH = 128;
	private static final int TILE_HEIGHT = 64;
	
	SpriteBatch batch;
	OrthographicCamera camera;
	
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
		
		initMapTiles();
	}

	
	private void initMapTiles() {
		Texture terainImage;
		String key;
		String keyPrefix;
		
		for (int y=0; y<game.map.height; y++) {
			for (int x=0; x<game.map.width; x++) {
				Tile tile = game.map.getTile(x, y);
				
				key = mapRenderer.tileKey(tile.type, x, y);
				terainImage = gameResources.getImage(key);
				tile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
				
				// add images for beach
				if (tile.type.isWater() && tile.style > 0) {
					int edgeStyle = tile.style >> 4;
					if (edgeStyle > 0) {
						key = mapRenderer.tileEdgeKey(edgeStyle, x, y);
						terainImage = gameResources.getImage(key);
						tile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
					}
					int cornerStyle = tile.style & 15;
					if (cornerStyle > 0) {
						key = mapRenderer.tileCornerKey(cornerStyle, x, y);
						terainImage = gameResources.getImage(key);
						tile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
					}
				}
			}
		}
		
		List<Direction> borderDirections = new ArrayList<Direction>();
		borderDirections.addAll(Direction.longSides);
		borderDirections.addAll(Direction.corners);
		
		for (int y=0; y<game.map.height; y++) {
			for (int x=0; x<game.map.width; x++) {
				Tile tile = game.map.getTile(x, y);
	            for (Direction direction : Direction.values()) {
					Tile borderTile = game.map.getTile(x, y, direction);
					if (borderTile == null) {
						continue;
					}
					if (tile.type.hasTheSameTerain(borderTile.type)) {
						continue;
					}
					if (tile.type.isWater() || borderTile.type.isWater()) {
						if (!tile.type.isWater() && borderTile.type.isWater()) {
							direction = direction.getReverseDirection();
							key = mapRenderer.tileBorder(tile.type, direction, x, y);
							terainImage = gameResources.getImage(key);
							borderTile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
						}
					} else {
						// TODO: optymalization
						//if (borderTile.id > tile.id) {
						//}
						direction = direction.getReverseDirection();
						key = mapRenderer.tileBorder(tile.type, direction, x, y);
						terainImage = gameResources.getImage(key);
						borderTile.addTexture(new SortableTexture(terainImage, tile.type.getOrder()));
					}
					
					if (Direction.longSides.contains(direction) && tile.type.isWater()) {
						TileImprovement riverTileImprovement = borderTile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
						if (riverTileImprovement != null) {
							key = mapRenderer.riverDelta(direction, riverTileImprovement);
							terainImage = gameResources.getImage(key);
							tile.addTexture(new SortableTexture(terainImage, -1));
						}
					}
				}
			}
		}

		for (int y=0; y<game.map.height; y++) {
			for (int x=0; x<game.map.width; x++) {
				Tile tile = game.map.getTile(x, y);
				tile.sort();
			}
		}
		
		Random rand = new Random(System.currentTimeMillis());
		Frame frame;
		
		for (int y=0; y<game.map.height; y++) {
			for (int x=0; x<game.map.width; x++) {
				Tile tile = game.map.getTile(x, y);
				if (tile.type.getTypeStr().equals("model.tile.hills")) {
					keyPrefix = "model.tile.hills.overlay";
					int countForPrefix = gameResources.getCountForPrefix(keyPrefix);
					int i = Math.abs(rand.nextInt()) % countForPrefix;
					key = keyPrefix + Integer.toString(i) + ".image";
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				if (tile.type.getTypeStr().equals("model.tile.mountains")) {
					keyPrefix = "model.tile.mountains.overlay";
					int countForPrefix = gameResources.getCountForPrefix(keyPrefix);
					int i = Math.abs(rand.nextInt()) % countForPrefix;
					key = keyPrefix + Integer.toString(i) + ".image";
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				if (tile.type.isForested()) {
					TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
					
					key = mapRenderer.forestImgKey(tile.type, riverTileImprovement);
					
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				for (TileResource tileResource : tile.tileResources) {
					key = mapRenderer.tileResourceKey(tileResource.getResourceType());
					frame = gameResources.getCenterAdjustFrameTexture(key);
					tile.addOverlayTexture(frame);
				}
				
				for (TileImprovement tileImprovement : tile.tileImprovements) {
					key = "model.tile.river" + tileImprovement.style;
					tile.addOverlayTexture(gameResources.getFrame(key));
				}
				
				
				
				if (tile.lostCityRumour) {
					key = mapRenderer.tileLastCityRumour();
					frame = gameResources.getCenterAdjustFrameTexture(key);
					tile.addOverlayTexture(frame);
				}
			}
		}
		
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
	}
	
}
