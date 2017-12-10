package promitech.colonization.screen.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

class IsometricTilePolygonSprite {
	public final PolygonSprite polygonSprite;
	private Texture textureSolid;
	
	public IsometricTilePolygonSprite(Color color) {
		int w = MapRenderer.TILE_WIDTH;
		int h = MapRenderer.TILE_HEIGHT;

//         0
//       /   \
//      3-----1
//       \   /
//         2 
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color); 
        pix.fill();
        textureSolid = new Texture(pix);
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
        polygonSprite = new PolygonSprite(polyReg);
        pix.dispose();
        pix = null;
	}
	
	public void dispose() {
		textureSolid.dispose();
		textureSolid = null;
	}
}