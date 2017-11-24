package promitech.colonization.gamelogic;

import promitech.colonization.actors.map.MapActor;

public class MoveView {

    private MapActor mapActor;
    
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
    
    public void showMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
        mapActor.startUnitDislocationAnimation(moveContext, endOfAnimation);
    }
    
    public void showFailedAttackMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
        mapActor.startFailedAttackAnimation(moveContext, endOfAnimation);
    }

}
