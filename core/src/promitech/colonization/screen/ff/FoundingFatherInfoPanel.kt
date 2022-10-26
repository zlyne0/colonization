package promitech.colonization.screen.ff

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.player.FoundingFather
import promitech.colonization.GameResources
import promitech.colonization.ui.resources.Messages

internal class FoundingFatherInfoPanel(val panelSkin : Skin) : Table() {
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

		add(nameLabel).colspan(2).align(Align.center).pad(10f).expandX().fillX().row()
		add(ffImage).align(Align.left or Align.top).pad(10f)
		add(description).top().pad(10f)
	}

	fun update(ff : FoundingFather) {
		foundingFather = ff

		nameLabel.setText(
			Messages.msgName(ff) + " (" + Messages.msg(ff.type.msgKey()) + ")"
		)

		val frame = GameResources.instance.getFrame(ff.getId() + ".image")
		ffImage.setDrawable(TextureRegionDrawable(frame.texture))

		val descriptionStr = Messages.msgDescription(ff) +
			"\r\n\r\n" +
			"[" + Messages.msg(ff.getId() + ".birthAndDeath") + "] " + Messages.msg(ff.getId() + ".text")
		description.setText(descriptionStr)

		invalidateHierarchy()
	}

}