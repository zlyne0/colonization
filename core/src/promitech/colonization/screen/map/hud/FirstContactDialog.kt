package promitech.colonization.screen.map.hud

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ClosableDialogSize
import promitech.colonization.ui.resources.StringTemplate
import net.sf.freecol.common.model.player.Player
import com.badlogic.gdx.scenes.scene2d.ui.Label
import promitech.colonization.ui.resources.Messages
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Image
import promitech.colonization.ui.addListener
import promitech.colonization.ui.kAddOnCloseListener
import net.sf.freecol.common.model.player.Stance
import net.sf.freecol.common.model.player.Tension
import promitech.colonization.orders.move.HumanPlayerInteractionSemaphore

class FirstContactDialog(
	val player : Player,
	val contactPlayer : Player,
	val humanPlayerInteractionSemaphore : HumanPlayerInteractionSemaphore = HumanPlayerInteractionSemaphore()
)
	: ClosableDialog<NewLandNameDialog>(ClosableDialogSize.width50(), ClosableDialogSize.def())
{
	
	val skin : Skin
	val yesButton : TextButton
	val noButton : TextButton
	
	init {
		skin = GameResources.instance.getUiSkin()
		yesButton = TextButton(Messages.msg("welcome.yes"), skin)
		noButton = TextButton(Messages.msg("welcome.no"), skin)
		
		// TODO: jak rozwiazac ze gdy zamknie okno przez klik poza, trzeba zrobic aby nie bylo to closable dialog, 
		yesButton.addListener { _, _ ->
			player.changeStance(contactPlayer, Stance.PEACE)
			hideWithFade()
		}
		
		noButton.addListener { _, _ ->
			player.changeStance(contactPlayer, Stance.PEACE)
			player.modifyTension(contactPlayer, Tension.TENSION_ADD_MAJOR)
			player.addMissionBan(contactPlayer)
			hideWithFade()
		}
		
		kAddOnCloseListener {
		    humanPlayerInteractionSemaphore.release()
		}
	}
	
	private fun createLayout() {
	    var headerKey = "event.meeting." + contactPlayer.nation().getNationNameKey()
        var imageKey = "EventImage.meeting." + contactPlayer.nation().getNationNameKey()
        if (!Messages.containsKey(headerKey)) {
            headerKey = "event.meeting.natives"
            imageKey = "EventImage.meeting.natives"
        }
	    
	    val headerLabel = Label(Messages.msg(headerKey), skin)
        var imgFrame = GameResources.instance.getFrame(imageKey)
        val contactImage = Image(imgFrame.texture)
        
        val settlementType = contactPlayer.nationType().getSettlementRegularType().getId() + ".plural"
        val nativeMsg = StringTemplate.template("welcomeSimple.text")
            .addStringTemplate("%nation%", contactPlayer.getNationName())
            .add("%camps%", Integer.toString(contactPlayer.settlements.size()))
            .addKey("%settlementType%", settlementType)
        val contactLabel = Label(Messages.message(nativeMsg), skin)
        contactLabel.setWrap(true)
        
        getContentTable().add(headerLabel).pad(20f).row()
        getContentTable().add(contactImage).row()
        getContentTable().add(contactLabel).pad(20f).fillX().expandX().row()
		
        buttonTableLayoutExtendX()
        getButtonTable().add(noButton).pad(10f).fillX().expandX()
        getButtonTable().add(yesButton).pad(10f).fillX().expandX()
	}
	
	override fun init(shapeRenderer : ShapeRenderer) {
		createLayout()
	}
	
	override fun show(stage : Stage) {
		super.show(stage)
		resetPositionToCenter()
	}
	
}