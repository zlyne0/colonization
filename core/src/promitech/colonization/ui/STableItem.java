package promitech.colonization.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

class STableItem extends Table {
    ShapeRenderer shape;
    boolean selected = false;
    int rowIndex;
    Object payload;
    
    public STableItem() {
        setTouchable(Touchable.enabled);
    }
    
    public Cell<Actor> addItem(Actor actor) {
        return add(actor).expand();
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
    
    void setUnselected() {
        selected = false;
    }

    void setSelected() {
        selected = true;
    }
}
