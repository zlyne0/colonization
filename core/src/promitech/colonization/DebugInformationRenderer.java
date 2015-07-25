package promitech.colonization;

import net.sf.freecol.common.model.Map;
import promitech.colonization.actors.MapRenderer;
import promitech.colonization.math.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

class DebugInformationRenderer {

    private ShapeRenderer shape = new ShapeRenderer();
    private ShapeRenderer enemyPathStep = new ShapeRenderer();
    private BitmapFont font = new BitmapFont();
	
    private Point tmpPoint = new Point();
    private OrthographicCamera camera;
    private MapRenderer mapRenderer;
    public GameResources gameResources;
    
	public DebugInformationRenderer(MapRenderer mapRenderer) {
	    this.mapRenderer = mapRenderer;
		
        shape.setColor(Color.RED);
        enemyPathStep.setColor(Color.CYAN);
        font.setColor(Color.RED);
        //font.setScale(0.8f);
        
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
	}
	
	public void render(SpriteBatch batch, Map map) {
    	batch.end();
    	//drawGrid(batch, map, camera);
    	//drawDebugObjects();
    	batch.begin();
    	//xxx(batch, map, camera);
    	//drawGridLabels(batch, map, camera);
	}
	
    public void drawDebugObjects() {
    	enemyPathStep.begin(ShapeType.Line);
    	enemyPathStep.setProjectionMatrix(camera.combined);
    	
        shape.begin(ShapeType.Line);
        shape.setProjectionMatrix(camera.combined);
        shape.end();
        enemyPathStep.end();
    }
    
    public void drawGrid(SpriteBatch batch, Map map, OrthographicCamera camera) {
        int x, y;
        
        shape.setColor(Color.RED);
        shape.begin(ShapeType.Line);
        shape.setProjectionMatrix(camera.combined);
        for (y=0; y<map.height; y++) {
            for (x=0; x<map.width; x++) {
                tmpPoint.x = (MapRenderer.TILE_WIDTH * x) + ((y % 2 == 1) ? MapRenderer.TILE_HEIGHT : 0);
                tmpPoint.y = (MapRenderer.TILE_HEIGHT / 2) * y;
                
                tmpPoint.x += mapRenderer.cameraPosition.x;
                tmpPoint.y += mapRenderer.cameraPosition.y;
                
                tmpPoint.y = 500 - tmpPoint.y;
                // up right
                shape.line(
	                tmpPoint.x + MapRenderer.TILE_WIDTH / 2, 
	                tmpPoint.y, 
	                tmpPoint.x, 
	                tmpPoint.y + MapRenderer.TILE_HEIGHT / 2);
                // up left
                shape.line(
                    tmpPoint.x + MapRenderer.TILE_WIDTH / 2, 
                    tmpPoint.y, 
                    tmpPoint.x + MapRenderer.TILE_WIDTH, 
                    tmpPoint.y + MapRenderer.TILE_HEIGHT / 2);
            }
        }
        shape.end();
    }
    
}
