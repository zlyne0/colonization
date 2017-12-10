package promitech.colonization.screen.ui;

import net.sf.freecol.common.model.player.Notification;

public interface ChangeColonyStateListener {
    void changeUnitAllocation();
    
    void transfereGoods();
    
    void changeBuildingQueue();
    
    void addNotification(Notification notification);
}

