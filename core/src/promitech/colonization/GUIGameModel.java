package promitech.colonization;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitIterator;
import net.sf.freecol.common.model.player.EventsNotifications.AddNotificationListener;
import net.sf.freecol.common.model.player.Notification;
import net.sf.freecol.common.model.player.Player;

public class GUIGameModel implements AddNotificationListener {

	public static interface ChangeStateListener {
		public void change(GUIGameModel model);
	}
	
	private ChangeStateListener listener; 
	
	private Unit activeUnit;
	private boolean viewMode = false;
	private boolean createGotoPathMode = false;
	private boolean aiMove = false;

	Player player;
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
	    if (listener != null) {
	        listener.change(this);
	    }
	}
	
	public void addChangeListener(ChangeStateListener listener) {
	    this.listener = listener;
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

	public boolean hasNotifications() {
		return player.eventsNotifications.hasNotifications();
	}

    @Override
    public void onAddNotification(Notification notification) {
        runListeners();
    }
}
