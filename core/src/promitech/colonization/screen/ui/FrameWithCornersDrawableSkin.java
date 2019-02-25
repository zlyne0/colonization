package promitech.colonization.screen.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

public class FrameWithCornersDrawableSkin extends BaseDrawable {

	private static final float TITLE_INTENT = 10f;
	private Sprite leftBottom;
	private Sprite leftTop;
	private Sprite rightBottom;
	private Sprite rightTop;
	private Sprite left;
	private Sprite right;
	private Sprite top;
	private Sprite bottom;
	
	private String title;
	private BitmapFont titleFont;
	private float titleStart;
	private float titleEnd;
	
	private float rx;
	private float ry;
	private float titleDy = 0;
	
	public FrameWithCornersDrawableSkin(GameResources gameResources) {
		loadSprites(gameResources);
	}

	public FrameWithCornersDrawableSkin(String title, BitmapFont titleFont, GameResources gameResources) {
		loadSprites(gameResources);
		if (title != null) {
			this.title = title;
			this.titleFont = titleFont;
			changeTitle(title, titleFont);
		}
	}
	
	public void changeTitle(String title, BitmapFont titleFont) {
		this.title = title;
		this.titleFont = titleFont;
		float titleWidth = FontResource.strWidth(titleFont, title) + 5f;
		titleStart = leftTop.getWidth() + TITLE_INTENT - 5f;
		titleEnd = titleStart + titleWidth + 5f;
		titleDy = titleFont.getXHeight() / 2;
	}
	
	private void loadSprites(GameResources gameResources) {
		Frame f = gameResources.getFrame("frames.corner.image");
		leftBottom = new Sprite(f.texture);
		leftBottom.flip(false, false);
		
		leftTop = new Sprite(f.texture);
		leftTop.flip(false, true);
		
		rightBottom = new Sprite(f.texture);
		rightBottom.flip(true, false);
		
		rightTop = new Sprite(f.texture);
		rightTop.flip(true, true);
		
		f = gameResources.getFrame("frames.left.image");
		left = new Sprite(f.texture);
		left.flip(false, false);
		
		right = new Sprite(f.texture);
		right.flip(true, false);
		
		f = gameResources.getFrame("frames.bottom.image");
		bottom = new Sprite(f.texture);
		bottom.flip(false, false);

		f = gameResources.getFrame("frames.top.image");
		top = new Sprite(f.texture);
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		leftBottom.setPosition(x, y);
		leftBottom.draw(batch);

		leftTop.setPosition(x, y + height - leftTop.getHeight() - titleDy);
		leftTop.draw(batch);
		
		rightBottom.setPosition(x + width - rightBottom.getWidth(), y);
		rightBottom.draw(batch);
		
		rightTop.setPosition(x + width - rightTop.getWidth(), y + height - rightTop.getHeight() - titleDy);
		rightTop.draw(batch);
		
		drawVerticalBorders(batch, x, y, width, height);
		drawHorizontalBorders(batch, x, y, width, height);
	}

	private void drawHorizontalBorders(Batch batch, float x, float y, float width, float height) {
		float borderWidth = width - 2 * leftBottom.getWidth();
		int spritesCount = (int)(borderWidth / left.getHeight()) + 1;
		for (int i=0; i<spritesCount; i++) {
			rx = leftBottom.getWidth() - 1 + x + i*bottom.getWidth();
			ry = y;
			bottom.setPosition(rx, ry);
			bottom.draw(batch);
			
			rx = leftTop.getWidth() - 1 + x + i*top.getWidth();
			ry = y + height - top.getHeight() - titleDy;
			if (title == null || (rx <= titleStart + x || rx >= titleEnd + x)) {
				top.setPosition(rx, ry);
				top.draw(batch);
			}
		}
		
		if (title != null) {
			titleFont.draw(batch, title, 
				x + leftTop.getWidth() + TITLE_INTENT, 
				y + height + (titleFont.getXHeight()/2f) - titleDy
			);
		}
	}

	private void drawVerticalBorders(Batch batch, float x, float y, float width, float height) {
		float borderHeight = height - 2 * leftBottom.getHeight();
		int spritesCount = (int)(borderHeight / left.getHeight()) + 1;
		for (int i=0; i<spritesCount; i++) {
			rx = x;
			ry = leftBottom.getHeight() - 1 + y + i*left.getHeight();
			left.setPosition(rx, ry);
			left.draw(batch);
			
			rx = x + width - right.getWidth();
			ry = leftBottom.getHeight() - 1 + y + i*right.getHeight();
			right.setPosition(rx, ry);
			right.draw(batch);
		}
	}
	
}
