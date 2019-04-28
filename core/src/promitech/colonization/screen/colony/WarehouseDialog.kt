package promitech.colonization.screen.colony

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener
import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.GameResources
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate

class WarehouseDialog(
	val shape : ShapeRenderer,
	val colony : Colony
)
	: ModalDialog<WarehouseDialog>(ModalDialogSize.def(), ModalDialogSize.height75())
{
	private val goodsListTable = Table()
	
	init {
		
		for (goodsType in Specification.instance.goodsTypes.sortedEntities()) {
			if (!goodsType.isStorable()) {
				continue
			}
			addGoodsRow(goodsType)
		}
		
		val scrollPane = ScrollPane(goodsListTable, skin)
		scrollPane.setFlickScroll(false)
		scrollPane.setForceScroll(false, true)
		scrollPane.setFadeScrollBars(false)
		scrollPane.setOverscroll(true, true)
		scrollPane.setScrollBarPositions(false, true)
		scrollPane.setScrollingDisabled(true, false)
		
		getContentTable().add(scrollPane).pad(20f).expandX().fillX().row()		
		
		buttonTableLayoutExtendX()
		getButtonTable().add(okButton()).padTop(20f)
	}
	
	fun addGoodsRow(goodsType : GoodsType) {
		val exportInfo = colony.exportInfo(goodsType)
		
		val img = GameResources.instance.goodsImage(goodsType)
		val label = StringTemplate.template(Messages.nameKey(goodsType.getId()))
			.addAmount("%amount%", 1)
			.eval()
		
		val exportCheckBox = CheckBox(Messages.msg("warehouseDialog.export"), skin)
		exportCheckBox.setChecked(exportInfo.isExport())
		exportCheckBox.addListener { ->
			exportInfo.setExport(exportCheckBox.isChecked())
		}
				
		val exportLevel = TextField(
			exportInfo.getExportLevel().toString(),
			GameResources.instance.getUiSkin()
		)
		exportLevel.setTextFieldFilter(TextFieldFilter.DigitsOnlyFilter())
		exportLevel.setTextFieldListener( object : TextFieldListener {
			override fun keyTyped(textField: TextField, c: Char) {
				if (exportLevel.getText().equals("")) {
					exportLevel.setText("0")
				}
				exportInfo.setExportLevel(exportLevel.getText().toInt())
			}
		})
		
		goodsListTable.defaults().pad(5f, 0f, 5f, 10f)
		goodsListTable.add(Image(img.texture))
		goodsListTable.add(Label(label, skin))
		goodsListTable.add(exportCheckBox)
		goodsListTable.add(exportLevel)
		goodsListTable.row()
	}
	
	fun okButton() : TextButton {
		val okButton = TextButton(Messages.msg("ok"), skin)
			okButton.addListener { ->
			this@WarehouseDialog.hideWithFade()
		}
		return okButton
	}
}