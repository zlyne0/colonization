package promitech.colonization;

import java.util.LinkedList;
import java.util.List;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitIterator;

public class GUIGameModel {

	public static interface ChangeStateListener {
		public void change(GUIGameModel model);
	}
	
	private List<ChangeStateListener> listeners = new LinkedList<GUIGameModel.ChangeStateListener>(); 
	
	private Unit activeUnit;
	private boolean viewMode = false;
	private boolean createGotoPathMode = false;
	private boolean aiMove = false;
	
	Unit previewViewModeUnit = null;
	UnitIterator unitIterator = null;

	public boolean isActiveUnitSet() {
		return activeUnit != null;
	}
	
	public boolean isActiveUnitNotSet() {
		return activeUnit == null;
	}
	
	public Unit getActiveUnit() {
		return activeUnit;
	}

	public void setActiveUnit(Unit activeUnit) {
		this.activeUnit = activeUnit;
		runListeners();
	}
	
	void runListeners() {
		for (ChangeStateListener l : listeners) {
			l.change(this);
		}
	}
	
	public void addChangeListener(ChangeStateListener listener) {
		this.listeners.add(listener);
	}

	public boolean isViewMode() {
		return viewMode;
	}

	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
		runListeners();
	}

	public boolean isCreateGotoPathMode() {
		return createGotoPathMode;
	}

	public void setCreateGotoPathMode(boolean createGotoPathMode) {
		this.createGotoPathMode = createGotoPathMode;
		runListeners();
	}

	public boolean isAiMove() {
		return aiMove;
	}

	public void setAiMove(boolean aiMove) {
		this.aiMove = aiMove;
		runListeners();
	}

	public boolean hasUnitsToMove() {
		return unitIterator.hasNext();
	}
}
