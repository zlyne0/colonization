package promitech.colonization.actors.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.move.MoveContext;

public class MoveView {

    private MapActor mapActor;
    
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
    
    public void showMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
    	UnitDislocationAnimation animation = Actions.action(UnitDislocationAnimation.class);
    	animation.init( 
            moveContext.unit,
            moveContext.sourceTile,
            moveContext.destTile,
            endOfAnimation
        );
        mapActor.startUnitTileAnimation(animation);
    }
    
    public void showFailedAttackMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
    	UnitFailAttackAnimation animation = Actions.action(UnitFailAttackAnimation.class);
        animation.init(
	        moveContext.unit,
	        moveContext.sourceTile,
	        moveContext.destTile,
	        endOfAnimation
		);
        mapActor.startUnitTileAnimation(animation);
    }

	public void showAttackRetreat(MoveContext moveContext, Runnable endOfAnimation) {
		UnitAttackRetreatAnimation animation = Actions.action(UnitAttackRetreatAnimation.class);
		animation.init(
            moveContext.unit,
            moveContext.sourceTile,
            moveContext.destTile,
            endOfAnimation
		);
		mapActor.startUnitTileAnimation(animation);		
	}
	
	public void showSuccessfulAttackWithMove(MoveContext moveContext, Unit loser, Runnable endOfAnimation) {
		SuccessAttackAnimation animation = Actions.action(SuccessAttackAnimation.class);
		animation.init(
			moveContext.unit, 
			moveContext.sourceTile, 
			moveContext.destTile, 
			loser, endOfAnimation
		);
		mapActor.startUnitTileAnimation(animation);
	}
}
