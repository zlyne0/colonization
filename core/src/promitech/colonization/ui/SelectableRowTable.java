package promitech.colonization.ui;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.IntSet;

public class SelectableRowTable extends Table {

    private static HorizontalGroup SEPARATOR_ROW = new HorizontalGroup() {
        @Override
        public float getPrefHeight() {
            return 20f;
        }
    };
    
	private DoubleClickedListener doubleClickedListener;
	private final ShapeRenderer shape;
	private int rowIndexCounter = 0;
	
    private DoubleClickedListener itemClickedListener = new DoubleClickedListener() {
    	public void clicked(InputEvent event, float x, float y) {
    		super.clicked(event, x, y);

    		int selectedRow = -1;
    		if (event.getListenerActor() instanceof SelectableTableItem) {
    			SelectableTableItem<?> item = (SelectableTableItem<?>)event.getListenerActor();
    			selectedRow = item.rowIndex;
    		}
    		
    		for (Actor a : SelectableRowTable.this.getChildren()) {
    		    if (a instanceof SelectableTableItem) {
    		    	SelectableTableItem<?> item = (SelectableTableItem<?>)a;
    		    	if (item.rowIndex == selectedRow) {
    		    		item.setSelected();
    		    	} else {
    		    		item.setUnselected();
    		    	}
    		    }
    		}
    	};
    	
    	public void doubleClicked(InputEvent event, float x, float y) {
    		if (event.getListenerActor() instanceof SelectableTableItem) {
    			SelectableTableItem<?> item = (SelectableTableItem<?>)event.getListenerActor();
    			item.setUnselected();
    			
    			if (doubleClickedListener != null) {
    				doubleClickedListener.doubleClicked(event, x, y);
    			}
    		}
    	};
    };
	
	
	public SelectableRowTable(ShapeRenderer shape) {
		this.shape = shape;
	}
	
	public void nextRow() {
		rowIndexCounter++;
		row();
	}
	
	public <T extends SelectableTableItem<?>> Cell<T> addCell(T actor) {
		actor.shape = shape;
		actor.rowIndex = rowIndexCounter;
		actor.addListener(itemClickedListener);
		return super.add(actor);
	}

	public <T> void removeSelectableItems(SelectableTableItem<T> tableItem) {
		LinkedList<Actor> toRemove = new LinkedList<Actor>();
		
		for (Actor actor : getChildren()) {
			if (actor instanceof SelectableTableItem) {
				SelectableTableItem<?> ti = (SelectableTableItem<?>)actor;
				if (ti.rowIndex == tableItem.rowIndex) {
					toRemove.add(actor);
				}
			}
		}
		for (Actor a : toRemove) {
			removeActor(a);
		}
	}

	public void addSeparator() {
	    super.add(SEPARATOR_ROW).fillX().row();
	}
	
	public void setDoubleClickedListener(DoubleClickedListener doubleClickedListener) {
		this.doubleClickedListener = doubleClickedListener;
	}

	public <T> List<T> getItemsObjectsFromUniqueRows() {
		LinkedList<T> uniqueRowsItems = new LinkedList<T>();
		IntSet set = new IntSet(this.getChildren().size / 2);
		for (Actor actor : getChildren()) {
			if (actor instanceof SelectableTableItem) {
				SelectableTableItem<T> ti = (SelectableTableItem<T>) actor;
				if (set.contains(ti.rowIndex)) {
					continue;
				}
				set.add(ti.rowIndex);
				uniqueRowsItems.add(ti.payload);
			}
		}
		return uniqueRowsItems;
	}
}
