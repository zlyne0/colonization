package promitech.colonization.infrastructure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ObjectFloatMap;

public class FontResource {

	private static final FontResource fontResource = new FontResource();
	
	private BitmapFont cityNamesFont;
	private BitmapFont citySizeFont;
	private BitmapFont unitBoxFont = new BitmapFont(false);
	
	private GlyphLayout glyphLayout = new GlyphLayout();
	
	private ObjectFloatMap<String> stringWidth = new ObjectFloatMap<String>(); 
	
	private FontResource() {
	}
	
	public static FontResource instance() {
		return fontResource;
	}
	
	public void load() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("base/resources/fonts/LiberationSerif-Regular.ttf"));
		
		FreeTypeFontParameter params = new FreeTypeFontParameter();
		params.size = (int)(32 * Gdx.graphics.getDensity());
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		cityNamesFont = generator.generateFont(params);
		
		params.size = (int)(32 * Gdx.graphics.getDensity());
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		citySizeFont = generator.generateFont(params);
		
		generator.dispose();
		
	}
	
	public float strWidth(BitmapFont strFont, String str) {
		if (stringWidth.containsKey(str)) {
			return stringWidth.get(str, 0);
		}
		glyphLayout.setText(strFont, str);
		float w = glyphLayout.width;
		stringWidth.put(str, w);
		return w;
	}
	
	public void dispose() {
		cityNamesFont.dispose();
		citySizeFont.dispose();
		unitBoxFont.dispose();
	}

	public BitmapFont getCityNamesFont() {
		return cityNamesFont;
	}

	public BitmapFont getCitySizeFont() {
		return citySizeFont;
	}

	public BitmapFont getUnitBoxFont() {
		return unitBoxFont;
	}
	
}
