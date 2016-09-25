package promitech.colonization.actors;

import net.sf.freecol.common.model.player.Notification;

public interface ChangeColonyStateListener {
    void changeUnitAllocation();
    
    void transfereGoods();
    
    void changeBuildingQueue();
    
    void addNotification(Notification notification);
}

