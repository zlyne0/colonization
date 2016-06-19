package promitech.colonization.actors;

public interface ChangeColonyStateListener {
    void changeUnitAllocation();
    
    void transfereGoods();
    
    void changeBuildingQueue();
}

