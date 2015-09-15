package promitech.colonization.actors.map;

import net.sf.freecol.common.model.Player;
import promitech.colonization.actors.map.MapRenderer.TileDrawer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

class FogOfWarDrawer extends TileDrawer {
	public static final Color FOG_OF_WAR_COLOR = new Color(0f, 0f, 0f, 0.3f);

	PolygonSpriteBatch polyBatch;
	private PolygonSprite poly;
	private final Player player;
	
	public FogOfWarDrawer(Player player) {
		this.player = player;
		int w = MapRenderer.TILE_WIDTH;
		int h = MapRenderer.TILE_HEIGHT;

//         0
//       /   \
//      3-----1
//       \   /
//         2 
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(FOG_OF_WAR_COLOR); 
        pix.fill();
        Texture textureSolid = new Texture(pix);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid),
        	new float[] {       
              	w/2,  0,        
              	w,  h/2,        
              	w/2,  h,        
              	0,  h/2         
            }, new short[] {
            	0, 1, 3,
                3, 1, 2         
            }
        );
        poly = new PolygonSprite(polyReg);
        polyBatch = new PolygonSpriteBatch();
	}
	
	@Override
	public void draw() {
		if (player.hasFogOfWar(mapx, mapy)) {
            poly.setPosition(screenPoint.x, screenPoint.y);
            poly.draw(polyBatch);
		}
	}
}
