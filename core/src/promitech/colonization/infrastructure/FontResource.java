package promitech.colonization.infrastructure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ObjectFloatMap;

public class FontResource {

	// ó and Ó is already added
	public static final String POLISH_CHARS = "ęąśłżźćńĘĄŚŁŻŹĆŃ";
	
	private static final FontResource instance = new FontResource();
	
	private BitmapFont cityNamesFont;
	private BitmapFont citySizeFont;
	private BitmapFont unitBoxFont = new BitmapFont(false);
	private BitmapFont infoPanelTileFont = new BitmapFont();
	private BitmapFont goodsQuantityFont; 
	private BitmapFont warehouseGoodsQuantityFont;
	private BitmapFont populationPanelFont = new BitmapFont(false);
	private BitmapFont hudButtonsFont;
	
	private GlyphLayout glyphLayout = new GlyphLayout();
	
	private ObjectFloatMap<String> stringWidth = new ObjectFloatMap<String>();
	
	private FontResource() {
	}
	
	public static void load() {
		instance.internalLoad();
	}
	
	private void internalLoad() {
		initFromFont1();
		initFromFont2();
	}

	private void initFromFont1() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("base/resources/fonts/LiberationSerif-Regular.ttf"));
		
		FreeTypeFontParameter params = new FreeTypeFontParameter();
		params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARS;
		params.size = 19;
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		cityNamesFont = generator.generateFont(params);
		
		params = new FreeTypeFontParameter();
		params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARS;
		params.size = (int)(32 * Gdx.graphics.getDensity());
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		citySizeFont = generator.generateFont(params);

		
		params = new FreeTypeFontParameter(); 
		params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARS;
		params.size = 19;
		params.borderColor = Color.BLACK;
		params.borderWidth = 1;
		params.color = Color.WHITE;
		goodsQuantityFont = generator.generateFont(params);

        params = new FreeTypeFontParameter(); 
		params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARS;
        params.size = (int)(32 * Gdx.graphics.getDensity());
        params.borderColor = Color.BLACK;
        params.borderWidth = 1;
        params.color = Color.WHITE;
		warehouseGoodsQuantityFont = generator.generateFont(params);
		
		hudButtonsFont = new BitmapFont(false);
		//hudButtonsFont.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
		hudButtonsFont.setColor(Color.WHITE);
		
		unitBoxFont.setColor(Color.BLACK);
		
		generator.dispose();
	}
	
	private void initFromFont2() {
		FreeTypeFontParameter params = null;
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("base/resources/fonts/LiberationSerif-Bold.ttf"));
		params = new FreeTypeFontParameter();
		params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARS;
		params.size = 16;
		params.borderColor = Color.BLACK;
		params.borderWidth = 0;
		infoPanelTileFont = generator.generateFont(params);
		infoPanelTileFont.setColor(Color.BLACK);
		
		generator.dispose();
	}
	
	public static float strWidth(BitmapFont strFont, String str) {
		if (instance.stringWidth.containsKey(str)) {
			return instance.stringWidth.get(str, 0);
		}
		instance.glyphLayout.setText(strFont, str);
		float w = instance.glyphLayout.width;
		instance.stringWidth.put(str, w);
		return w;
	}
	
    public static float strIntWidth(BitmapFont font, int i) {
    	StringBuilder str = new StringBuilder();
		if (i < 0) {
    		str.append("-");
    	}
    	i = Math.abs(i);
    	if (i >= 100) {
    		str.append("555");
    	} else {
    		if (i < 10) {
    			str.append("5");
    		} else {
    			str.append("55");
    		}
    	}
    	return strWidth(font, str.toString());
    }
	
    public static float fontHeight(BitmapFont font) {
    	return font.getCapHeight();
    }
    
	public static void dispose() {
		instance.cityNamesFont.dispose();
		instance.citySizeFont.dispose();
		instance.unitBoxFont.dispose();
		instance.infoPanelTileFont.dispose();
		instance.goodsQuantityFont.dispose();
	}

	public static BitmapFont getPathTurnsFont() {
		instance.cityNamesFont.setColor(Color.WHITE);
		return instance.cityNamesFont;
	}
	
	public static BitmapFont getCityNamesFont() {
		return instance.cityNamesFont;
	}

	public static BitmapFont getCitySizeFont() {
		return instance.citySizeFont;
	}

	public static BitmapFont getUnitBoxFont() {
		return instance.unitBoxFont;
	}

	public static BitmapFont getInfoPanelTitleFont() {
		return instance.infoPanelTileFont;
	}
	
	public static BitmapFont getGoodsQuantityFont() {
		instance.goodsQuantityFont.setColor(Color.WHITE);
		return instance.goodsQuantityFont;
	}
	
	public static BitmapFont getWarehouseGoodsQuantityFont() {
	    return instance.warehouseGoodsQuantityFont;
	}
	
    public static BitmapFont getPopulationPanelFont() {
        return instance.populationPanelFont;
    }

	public static BitmapFont getHudButtonsFont() {
		return instance.hudButtonsFont;
	}
}
