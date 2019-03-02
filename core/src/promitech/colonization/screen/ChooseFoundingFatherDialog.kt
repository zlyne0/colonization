package promitech.colonization.screen

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import net.sf.freecol.common.model.specification.FoundingFather.FoundingFatherType
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.addListener
import promitech.colonization.GameResources
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Image
import net.sf.freecol.common.model.specification.FoundingFather
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.utils.Align

class FoundingFatherInfoPanel(val skin : Skin) : Table() {
	val nameLabel = Label("", skin)
	val ffImage = Image()
	
	val description = object : Label("", skin) {
		override fun getPrefWidth() : Float {
			return ffImage.width * 1.5f
		}
	}
	
	init {
		nameLabel.setWrap(true)
		description.setWrap(true)
		
		add(nameLabel).colspan(1).align(Align.center).pad(10f).row()
		add(ffImage).align(Align.left).pad(10f)
		add(description).top().pad(10f)
	}
	
	fun update(fft : FoundingFatherType, ffId : String) {
		nameLabel.setText(
			Messages.msg(ffId + ".name") + " (" + Messages.msg(fft.msgKey()) + ")"
		)
		
		val frame = GameResources.instance.getFrame(ffId + ".image")
		ffImage.setDrawable(TextureRegionDrawable(frame.texture))
		
		var descriptionStr = Messages.msg(ffId + ".description") +
			"\r\n\r\n" +
			"[" + Messages.msg(ffId + ".birthAndDeath") + "] " + Messages.msg(ffId + ".text")
		description.setText(descriptionStr)
		
		invalidateHierarchy()
	}
	
}

class ChooseFoundingFatherDialog()
	: ModalDialog<ChooseFoundingFatherDialog>(ModalDialogSize.width50(), ModalDialogSize.height75())
{
	private val typeButtons = Table()
	private val ffInfoPanel = FoundingFatherInfoPanel(skin)
	
	init {
		
		val foundingFathers = linkedMapOf(
			FoundingFatherType.TRADE to "model.foundingFather.adamSmith",
			FoundingFatherType.EXPLORATION to "model.foundingFather.ferdinandMagellan",
			FoundingFatherType.MILITARY to "model.foundingFather.hernanCortes", 
			FoundingFatherType.POLITICAL to "model.foundingFather.thomasPaine",
			FoundingFatherType.RELIGIOUS to "model.foundingFather.bartolomeDeLasCasas"
		)
		
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
	}
		
}