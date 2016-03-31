package promitech.colonization.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class SelectableTableItem<T> extends Table {
	ShapeRenderer shape;
	int rowIndex;
    boolean selected = false;
    
    public final T payload;
    
    public SelectableTableItem(T payload) {
    	this.payload = payload;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (selected) {
            shape.setProjectionMatrix(batch.getProjectionMatrix());
            shape.setTransformMatrix(batch.getTransformMatrix());
            batch.end();
            
            shape.begin(ShapeType.Filled);
            shape.setColor(Color.YELLOW);
            shape.rect(getX(), getY(), getWidth(), getHeight());
            shape.end();
            
            batch.begin();
        }
        super.draw(batch, parentAlpha);
    }
    
    public void setUnselected() {
        selected = false;
    }

    public void setSelected() {
        selected = true;
    }
	
}
