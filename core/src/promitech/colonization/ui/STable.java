package promitech.colonization.ui;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.IntSet;

public class STable extends Table {
    private static HorizontalGroup SEPARATOR_ROW = new HorizontalGroup() {
        @Override
        public float getPrefHeight() {
            return 20f;
        }
    };
    
    private final ShapeRenderer shape;
    private int rowIndexCounter = 0;
    private STableSelectListener onSelectListener;
    private float top, left, bottom, right;
    
    public STable(ShapeRenderer shape) {
        this.shape = shape;
    }
    
    public void addRow(Object payload, int columnAlignment[], Actor ... actors) {
        if (columnAlignment.length != actors.length) {
            throw new IllegalStateException("columns alignment != actor columns");
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

    public void removeSelectedItems() {
        LinkedList<Actor> toRemove = new LinkedList<Actor>();
        for (Actor actor : getChildren()) {
            if (actor instanceof STableItem) {
                STableItem ti = (STableItem)actor;
                if (ti.selected) {
                    toRemove.add(actor);
                }
            }
        }
        for (Actor a : toRemove) {
            removeActor(a);
        }
    }

    public <T> List<T> getUniquePayloads() {
        LinkedList<T> uniqueRowsItems = new LinkedList<T>();
        IntSet set = new IntSet(this.getChildren().size / 2);
        for (Actor actor : getChildren()) {
            if (actor instanceof STableItem) {
                STableItem ti = (STableItem) actor;
                if (set.contains(ti.rowIndex)) {
                    continue;
                }
                set.add(ti.rowIndex);
                uniqueRowsItems.add((T)ti.payload);
            }
        }
        return uniqueRowsItems;
    }
    
    public void addSeparator() {
        super.add(SEPARATOR_ROW).fillX().row();
    }
}

