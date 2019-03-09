package promitech.colonization.screen

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.Turn
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.FoundingFather
import net.sf.freecol.common.model.specification.FoundingFather.FoundingFatherType
import promitech.colonization.GameResources
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages

class FoundingFatherInfoPanel(val panelSkin : Skin) : Table() {
	val nameLabel = Label("", panelSkin)
	val ffImage = Image()
	
	val description = object : Label("", panelSkin) {
		override fun getPrefWidth() : Float {
			return ffImage.width * 1.5f
		}
	}
	
	var foundingFather : FoundingFather? = null
	
	init {
		nameLabel.setWrap(true)
		description.setWrap(true)
		
		add(nameLabel).colspan(1).align(Align.center).pad(10f).row()
		add(ffImage).align(Align.left or Align.top).pad(10f)
		add(description).top().pad(10f)
	}
	
	fun update(fft : FoundingFatherType, ff : FoundingFather) {
		foundingFather = ff
		
		nameLabel.setText(
			Messages.msgName(ff) + " (" + Messages.msg(fft.msgKey()) + ")"
		)
		
		val frame = GameResources.instance.getFrame(ff.getId() + ".image")
		ffImage.setDrawable(TextureRegionDrawable(frame.texture))
		
		var descriptionStr = Messages.msgDescription(ff) +
			"\r\n\r\n" +
			"[" + Messages.msg(ff.getId() + ".birthAndDeath") + "] " + Messages.msg(ff.getId() + ".text")
		description.setText(descriptionStr)
		
		invalidateHierarchy()
	}
	
}

class ChooseFoundingFatherDialog(
	player : Player,
	turn : Turn
)
	: ModalDialog<ChooseFoundingFatherDialog>(ModalDialogSize.width50(), ModalDialogSize.height75())
{
	private val typeButtons = Table()
	private val ffInfoPanel = FoundingFatherInfoPanel(skin)
	
	init {
		val foundingFathers = player.foundingFathers.generateRandomFoundingFathers(turn)
		
		foundingFathers.forEach { (fft, ff) ->
			val button = TextButton(Messages.msg(fft.msgKey()), skin)
			button.addListener { _, _ ->
				ffInfoPanel.update(fft, ff)
			}
			typeButtons.add(button).padLeft(10f)
		}
		
		if (foundingFathers.isNotEmpty()) {
			val first = foundingFathers.iterator().next()
			ffInfoPanel.update(first.key, first.value)
		}
		
		val layout = Table()
		layout.setFillParent(true)
		layout.add(typeButtons).pad(10f).row()
		layout.add(ffInfoPanel).left().row()
		
		getContentTable().add(layout).expandX().fillX().row()

		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { _, _ ->
			player.foundingFathers.setCurrentFoundingFather(ffInfoPanel.foundingFather)
			hideWithFade()
		}		
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
		
}