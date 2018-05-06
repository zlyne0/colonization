package promitech.colonization.screen.map.unitanimation;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.screen.map.MapActor;

public class MoveView {

    private MapActor mapActor;
    
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
    
    public void showMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
        centerViewIfRequired(moveContext);
        
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
        centerViewIfRequired(moveContext);
        
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
        centerViewIfRequired(moveContext);
        
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
        centerViewIfRequired(moveContext);
        
		SuccessAttackAnimation animation = Actions.action(SuccessAttackAnimation.class);
		animation.init(
			moveContext.unit, 
			moveContext.sourceTile, 
			moveContext.destTile, 
			loser, endOfAnimation
		);
		mapActor.startUnitTileAnimation(animation);
	}
	
	private void centerViewIfRequired(MoveContext moveContext) {
        if (mapActor.isTileOnScreenEdge(moveContext.destTile)) {
            mapActor.centerCameraOnTile(moveContext.destTile);
        }
	}
}
