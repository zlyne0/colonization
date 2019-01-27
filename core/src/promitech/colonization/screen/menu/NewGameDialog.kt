package promitech.colonization.screen.menu

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import net.sf.freecol.common.model.Nation
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.specification.options.OptionGroup
import promitech.colonization.GameCreator
import promitech.colonization.GameResources
import promitech.colonization.gdx.Frame
import promitech.colonization.screen.map.hud.GUIGameController
import promitech.colonization.screen.map.hud.GUIGameModel
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addKeyTypedListener
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate
import promitech.colonization.savegame.SaveGameParser

class NewGameDialog(
	private val guiGameController : GUIGameController,
	private val guiGameModel : GUIGameModel
) : ClosableDialog<SaveGameDialog>(
	ModalDialogSize.width75(), ModalDialogSize.height75()
) {

	class DifficultyLevelSelectItem(val level : OptionGroup) {
		val levelStr = Messages.msgName(level)
		
		override fun toString() : String {
			return levelStr
		}
	}
	
	val difficultyLevelSelectBox : SelectBox<DifficultyLevelSelectItem>
	var nation : Nation? = null
	val playerName = TextField("", skin)
	
	val okButton : TextButton = TextButton(Messages.msg("ok"), skin)
	
	init {
		playerName.addKeyTypedListener { _, _ -> enableConfirmStartGameButton() }
		
		SaveGameParser.loadDefaultSpecification();
		
		val nationBoxLayout = createNationBox()
		getContentTable().add(nationBoxLayout).row()
		
		
		var l1 = Table()
		l1.defaults().padTop(10f)
		l1.add(Label(Messages.msg("defaultPlayerName"), skin))
			.align(Align.right)
			.padRight(10f)
		l1.add(playerName).align(Align.left)
		l1.row()
				
		difficultyLevelSelectBox = createDifficultyLevelSelectBox()
		l1.add(Label(Messages.msg("difficultyLevels.name"), skin))
			.align(Align.right)
			.padRight(10f)
		l1.add(difficultyLevelSelectBox).align(Align.left)
		l1.row()
		
		getContentTable().add(l1)
		
		createConfirmButtons()
		
		buttonTableLayoutExtendX()
		withHidingOnEsc()
	}

	private fun createConfirmButtons() {
		okButton.setDisabled(true)
		
		okButton.addListener { _, _ ->
			System.out.println("start new game")
			
			WaitDialog(Messages.msg("status.startingGame"), {
				GameCreator(guiGameModel).initNewGame(
					nation,
					playerName.getText(),
					difficultyLevelSelectBox.getSelected().level.getId()
				)
			}, {
				guiGameController.resetMapModel()
				guiGameController.showMapScreenAndActiveNextUnit()
			}).show(this@NewGameDialog.dialog.getStage())
		}
		
		val cancelButton = TextButton(Messages.msg("cancel"), skin)
		cancelButton.addListener { _, _ ->
			hideWithFade()
		}
		
		getButtonTable().add(cancelButton).pad(10f).fillX().expandX()
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
		
	private fun createNationBox() : Table {
		val nationLabel = Label("", skin)
		val nationDescriptionLabel = Label("", skin)
		
		val nationsButtons = HorizontalGroup()
		nationsButtons.pad(10f)
		nationsButtons.space(20f)
		for (nation : Nation in Specification.instance.europeanNations.entities()) {
			val coatOfArms : Frame = GameResources.instance.coatOfArms(nation)
			val button = ImageButton(TextureRegionDrawable(coatOfArms.texture))
			button.addListener { _, _ ->
				nationLabel.setText(StringTemplate.template(nation.getId() + ".name").eval())
				nationDescriptionLabel.setText(
					Messages.msgName(nation.nationType) + " - " + Messages.shortDescriptionMsg(nation.nationType)
				)
				onChangeNation(nation)
			}
			nationsButtons.addActor(button)
		}
		val layout = Table()
		layout.pad(10f)
		layout.add(nationsButtons).row()
		layout.add(nationLabel).row()
		layout.add(nationDescriptionLabel).row()
		return layout
	}
	
	private fun onChangeNation(nation : Nation) {
		this.nation = nation
		playerName.setText(Messages.msg(nation.getId() + ".ruler"))
		enableConfirmStartGameButton()
	}
	
	private fun enableConfirmStartGameButton() {
		okButton.setDisabled(false)
		if (this.nation == null) {
			okButton.setDisabled(true)
		}
		if (playerName.getText().isNullOrBlank()) {
			okButton.setDisabled(true)
		}
		if (difficultyLevelSelectBox.getSelected() == null) {
			okButton.setDisabled(true)
		}
	}
	
	private fun createDifficultyLevelSelectBox() : SelectBox<DifficultyLevelSelectItem> {
		val diffLevelItems = Array<DifficultyLevelSelectItem>()
		val levels = Specification.optionGroupEntities.getById("difficultyLevels");
		for (diffLevel in levels.optionsGroup.entities()) {
			if (!diffLevel.isEditable()) {
				diffLevelItems.add(DifficultyLevelSelectItem(diffLevel))
			}
		}
		val selectBox = SelectBox<DifficultyLevelSelectItem>(skin)
		selectBox.setItems(diffLevelItems)
		
		for (l in diffLevelItems) {
			if (l.level.equalsId("model.difficulty.medium")) {
				selectBox.setSelected(l)
			}
		}
		return selectBox
	}	
}