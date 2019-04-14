package promitech.colonization.screen

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import net.sf.freecol.common.model.player.FoundingFather
import net.sf.freecol.common.model.player.FoundingFather.FoundingFatherType
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.MapList
import promitech.colonization.ui.ModalDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.addListener

class ContinentalCongress(val player : Player)
	: ModalDialog<ChooseFoundingFatherDialog>(ModalDialogSize.width50(), ModalDialogSize.height75())
{
	
	private val typeButtons = Table()
	private val ffInfoList = Table()
    private val infoListScrollPane = ScrollPane(ffInfoList, skin)
	
	init {
		val ffs = player.foundingFathers.foundingFathersByType()
		println("ContinentalCongress[${player.id}].typeCount= " + ffs.keySet().size
			+ ", foundingFathersCount=" + player.foundingFathers.size()
		)

        createRecruitingButton()
        createFFTypeButtons(ffs)
        updateRecruitingContent()

        infoListScrollPane.setFlickScroll(false)
        infoListScrollPane.setScrollingDisabled(true, false)
        infoListScrollPane.setForceScroll(false, false)
        infoListScrollPane.setFadeScrollBars(false)
        infoListScrollPane.setOverscroll(true, true)
        infoListScrollPane.setScrollBarPositions(false, true)

		val layout = Table()
		layout.setFillParent(true)
		layout.add(typeButtons).pad(10f).row()
		layout.add(infoListScrollPane).expandX().fillX().row()
		getContentTable().add(layout).expandX().fillX().row()
		
		val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { _, _ ->
			hideWithFade()
		}		
		getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}

    private fun createRecruitingButton() {
        val recruitingButton = TextButton(Messages.msg("report.continentalCongress.recruiting"), skin)
        recruitingButton.addListener { _, _ ->
            updateRecruitingContent()
        }
        typeButtons.add(recruitingButton).padLeft(10f)
    }

    private fun updateRecruitingContent() {
        ffInfoList.clear()

        val current = player.foundingFathers.getCurrentFoundingFather()
        if (current != null) {
			val progressStr = player.foundingFathers.progressStr()
			println("ContinentalCongress[${player.id}].progressStr = ${progressStr}")

            ffInfoList.add(Label(progressStr, skin))
                .align(Align.center)
                .pad(10f)
                .row()

            val ffInfoPanel = FoundingFatherInfoPanel(skin)
            ffInfoPanel.update(current.getType(), current)
            ffInfoList.add(ffInfoPanel).row()
        } else {
            println("ContinentalCongress[${player.id}].noCurrentFoundingFather")

            ffInfoList.add(Label(Messages.msg("report.continentalCongress.none"), skin))
                .align(Align.center)
                .pad(10f)
                .row()
        }

        infoListScrollPane.setScrollPercentY(0f)
        infoListScrollPane.layout()
    }

    private fun createFFTypeButtons(ffs: MapList<FoundingFatherType, FoundingFather>) {
        for (ffType: FoundingFatherType in ffs.keySet()) {
            val ffTypeButton = TextButton(Messages.msg(ffType.msgKey()), skin)
            ffTypeButton.addListener { _, _ ->
                updateInfoList(ffType, ffs.get(ffType))
            }
            typeButtons.add(ffTypeButton).padLeft(10f)
        }
    }

    private fun updateInfoList(ffType : FoundingFatherType, foundingFathers : List<FoundingFather>) {
		ffInfoList.clear()
		for (ff in foundingFathers) {
			val ffInfoPanel = FoundingFatherInfoPanel(skin)
			ffInfoPanel.update(ffType, ff)
			ffInfoList.add(ffInfoPanel).row()
		}
        infoListScrollPane.setScrollPercentY(0f)
        infoListScrollPane.layout()
	}
}