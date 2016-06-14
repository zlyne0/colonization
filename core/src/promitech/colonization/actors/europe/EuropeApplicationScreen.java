package promitech.colonization.actors.europe;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ApplicationScreen;
import promitech.colonization.ApplicationScreenType;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.hud.ButtonActor;

class GoodPriceActor extends Widget {
	
	private int sellPrice = 0;
	private int buyPrice = 0;
	private final GoodsType goodsType;
	private final TextureRegion textureRegion;
	private final float fontHeight;
	private String label;
	
	GoodPriceActor(GoodsType goodsType) {
		this.goodsType = goodsType;
		
		Frame img = GameResources.instance.goodsImage(goodsType);
		this.textureRegion = img.texture;
		this.fontHeight = FontResource.getGoodsQuantityFont().getCapHeight();
	}
	
    @Override
    public float getPrefHeight() {
    	return textureRegion.getRegionHeight() + fontHeight;
    }
    
    @Override
    public float getPrefWidth() {
        BitmapFont font = getLabelFont();
        return Math.max(textureRegion.getRegionWidth(), FontResource.strWidth(font, label));
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	batch.draw(
    	    textureRegion, 
    	    getX() + getWidth() / 2 - textureRegion.getRegionWidth() / 2, 
    	    getY() + fontHeight
    	);
    	BitmapFont font = getLabelFont();
        float priceStrLength = FontResource.strWidth(font, label);
        font.draw(batch, label, 
        		getX() + getWidth()/2 - priceStrLength/2, 
        		getY() + fontHeight
        );
    }
	
    private BitmapFont getLabelFont() {
        BitmapFont font = FontResource.getGoodsQuantityFont();
        font.setColor(Color.WHITE);
        return font;
    }
    
    public void initPrice(int sellPrice, int buyPrice) {
		this.sellPrice = sellPrice;
		this.buyPrice = buyPrice;
		this.label = "" + sellPrice + "/" + buyPrice;
    }
    
    public String toString() {
        return "type[" + goodsType + "], price[" + sellPrice + "/" + buyPrice + "]";
    }
	
}

class MarketPanel extends Table {
	
	private java.util.Map<String, GoodPriceActor> goodActorByType = new HashMap<String, GoodPriceActor>();	
	
	public void init() {
		this.defaults().space(10, 10, 10, 10);
		
        for (GoodsType goodsType : Specification.instance.goodsTypes.sortedEntities()) {
        	if (!goodsType.isStorable()) {
        		continue;
        	}
        	
        	GoodPriceActor goodPriceActor = goodActorByType.get(goodsType.getId());
        	if (goodPriceActor == null) {
        		goodPriceActor = new GoodPriceActor(goodsType);
        		goodActorByType.put(goodsType.getId(), goodPriceActor);
        		add(goodPriceActor);
        	}
        	goodPriceActor.initPrice(18, 19);
        }
	}
}

public class EuropeApplicationScreen extends ApplicationScreen {

	private Stage stage;
	private MarketPanel marketPanel; 
	
	@Override
	public void create() {
		stage = new Stage();
		int bw = (int) (stage.getHeight() * 0.33) / 3;
		
		ButtonActor closeButton = new ButtonActor(this.shape, "ESC");
		closeButton.setWidth(bw);
		closeButton.setHeight(bw);
		closeButton.setX(stage.getWidth() - bw - 10);
		closeButton.setY(stage.getHeight() - bw - 10);
		closeButton.addListener(new InputListener() {
        	@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		screenManager.setScreen(ApplicationScreenType.MAP_VIEW);
        		return true;
        	}
        });
        stage.addActor(closeButton);
		
        marketPanel = new MarketPanel();
        
        Table tableLayout = new Table();
        tableLayout.setFillParent(true);
        tableLayout.add(marketPanel); 
        
        stage.addActor(tableLayout);
        stage.setDebugAll(true);
	}
	
	public void init() {
		marketPanel.init();
	}
	
	@Override
	public void onShow() {
		init();
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void onLeave() {
		Gdx.input.setInputProcessor(null);
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
        stage.act();
        stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);		
	}
}
