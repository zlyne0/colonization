package promitech.colonization.screen.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

public class FrameWithCornersDrawableSkin extends BaseDrawable {

	private Sprite leftBottom;
	private Sprite leftTop;
	private Sprite rightBottom;
	private Sprite rightTop;
	private Sprite left;
	private Sprite right;
	private Sprite top;
	private Sprite bottom;
	
	public FrameWithCornersDrawableSkin(GameResources gameResources) {
		loadSprites(gameResources);
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
		
		top = new Sprite(f.texture);
		top.flip(false, true);
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		leftBottom.setPosition(x, y);
		leftBottom.draw(batch);

		leftTop.setPosition(x, y + height - leftTop.getHeight());
		leftTop.draw(batch);
		
		rightBottom.setPosition(x + width - rightBottom.getWidth(), y);
		rightBottom.draw(batch);
		
		rightTop.setPosition(x + width - rightTop.getWidth(), y + height - rightTop.getHeight());
		rightTop.draw(batch);
		
		drawVerticalBorders(batch, x, y, width, height);
		drawHorizontalBorders(batch, x, y, width, height);
	}

	private void drawHorizontalBorders(Batch batch, float x, float y, float width, float height) {
		float borderWidth = width - 2 * leftBottom.getWidth();
		int spritesCount = (int)(borderWidth / left.getHeight()) + 1;
		for (int i=0; i<spritesCount; i++) {
			bottom.setPosition(
				leftBottom.getWidth() - 1 + x + i*bottom.getWidth(), 
				y
			);
			bottom.draw(batch);
			
			top.setPosition(
				leftBottom.getWidth() - 1 + x + i*top.getWidth(), 
				y + height - top.getHeight()
			);
			top.draw(batch);
		}
	}

	private void drawVerticalBorders(Batch batch, float x, float y, float width, float height) {
		float borderHeight = height - 2 * leftBottom.getHeight();
		int spritesCount = (int)(borderHeight / left.getHeight()) + 1;
		for (int i=0; i<spritesCount; i++) {
			left.setPosition(
				x, 
				leftBottom.getHeight() - 1 + y + i*left.getHeight()
			);
			left.draw(batch);
			right.setPosition(
				x + width - right.getWidth(), 
				leftBottom.getHeight() - 1 + y + i*right.getHeight()
			);
			right.draw(batch);
		}
	}
	
}
