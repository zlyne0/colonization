package promitech.colonization.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class STable extends Table {
    private final ShapeRenderer shape;
    private int rowIndexCounter = 0;
    private STableSelectListener onSelectListener;
    private float top, left, bottom, right;
    
    public STable(ShapeRenderer shape) {
        this.shape = shape;
    }
    
    public void addRow(Object payload, int columnAlignment[], Actor ... actors) {
        if (columnAlignment.length != actors.length) {
            throw new IllegalStateException("col alignment != actor columns");
        }
        for (int i=0; i<actors.length; i++) {
            Actor actor = actors[i];
            
            STableItem sTableItem = new STableItem();
            sTableItem.addItem(actor).align(columnAlignment[i]).pad(top, left, bottom, right);
            sTableItem.shape = shape;
            sTableItem.rowIndex = rowIndexCounter;
            sTableItem.payload = payload;
            sTableItem.addListener(itemClickedListener);
            
            add(sTableItem).fill().expand();
        }
        rowIndexCounter++;
        row();
    }

    public void rowsPad(float top, float left, float bottom, float right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }
    
    private DoubleClickedListener itemClickedListener = new DoubleClickedListener() {
        public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            
            int selectedRow = -1;
            if (event.getListenerActor() instanceof STableItem) {
                STableItem item = (STableItem)event.getListenerActor();
                selectedRow = item.rowIndex;
            }
            
            for (Actor a : STable.this.getChildren()) {
                if (a instanceof STableItem) {
                    STableItem item = (STableItem)a;
                    if (item.rowIndex == selectedRow) {
                        item.setSelected();
                    } else {
                        item.setUnselected();
                    }
                }
            }
        };
        
        public void doubleClicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
            if (event.getListenerActor() instanceof STableItem) {
                STableItem item = (STableItem)event.getListenerActor();
                if (onSelectListener != null) {
                    onSelectListener.onSelect(item.payload);
                }
            }
        };
    };
    
    public void addSelectListener(STableSelectListener selectListener) {
        this.onSelectListener = selectListener;
    }

    public Object getSelectedPayload() {
        for (Actor a : STable.this.getChildren()) {
            if (a instanceof STableItem) {
                STableItem item = (STableItem)a;
                if (item.selected) {
                    return item.payload;
                }
            }
        }
        return null;
    }
}

