package promitech.colonization.screen.map.hud

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Player
import promitech.colonization.GameResources
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.STable
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

class ChooseEmigrantToRecruitDialog(
	private val player : Player
)
	: ModalDialog<FirstContactDialog>(ModalDialogSize.def(), ModalDialogSize.def())
{
	
	private val shape = ShapeRenderer()
	private var okButton = TextButton(Messages.msg("ok"), skin)
		
	private var selectedUnitType : UnitType? = null
	
	init {
		okButton.setDisabled(true)
		okButton.addListener { _, _ ->
			recruitImmigrant(selectedUnitType as UnitType)
			hideWithFade()
		}
		
		var unitsTable = createUnitList()
		
		unitsTable.addSelectListener { unitType ->
			recruitImmigrant(unitType as UnitType)
			hideWithFade()
		}
		unitsTable.addSingleClickSelectListener { unitType ->
			selectedUnitType = unitType as UnitType
			okButton.setDisabled(false)
		}
		
        var description = Label(Messages.msg("chooseImmigrant"), skin)
		getContentTable().add(description).pad(20f).row()
		getContentTable().add(unitsTable).pad(20f).row()
		
		buttonTableLayoutExtendX()
		getButtonTable().add(okButton).padTop(20f)
	}

	private fun recruitImmigrant(unitType : UnitType) {
		System.out.println("ui.recruitImmigrant " + unitType.getId())
		player.getEurope().recruitImmigrant(unitType)
	}
	
	private fun createUnitList() : STable {
		val labelAlign = intArrayOf(Align.left, Align.left)
		val unitsTable = STable(shape)
		unitsTable.defaults().space(10f, 0f, 10f, 0f)
		
		for (unitType in player.getEurope().getRecruitables()) {
			val texture : TextureRegion = GameResources.instance.getFrame(unitType.resourceImageKey()).texture
			val unitImg = Image(TextureRegionDrawable(texture), Scaling.none, Align.center)
			
			val labelStr = StringTemplate.template(Messages.nameKey(unitType.getId()))
				.addAmount("%number%", 1)
				.eval()
			val label = Label(labelStr, skin)
			unitsTable.addRow(unitType, labelAlign, unitImg, label)
		}
		return unitsTable
	}	
}