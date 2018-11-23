package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.GameResources
import promitech.colonization.ui.ClosableDialog
import promitech.colonization.ui.ModalDialogSize
import promitech.colonization.ui.addListener
import promitech.colonization.ui.resources.Messages
import promitech.colonization.ui.resources.StringTemplate
import com.badlogic.gdx.scenes.scene2d.ui.Table

class IndianSettlementInformationDialog(val game: Game, val settlement: IndianSettlement, val player : Player)
	: ClosableDialog<IndianSettlementInformationDialog>(ModalDialogSize.width75(), ModalDialogSize.def())
{
	val visited : Boolean
	val contacted : Boolean
	val layout = Table()
	
	init {
		visited = settlement.isVisitedBy(player)
		contacted = settlement.hasContact(player)

		layout.defaults().align(Align.left)
		layout.columnDefaults(1).padLeft(10f)		
		
		addSettlementLabel()
		addLearnableSkillRow()
		addMostHatedNationRow()
		addHighlyWantedGoodsRow()
		addOtherWantedGoodsRow()

        val okButton = TextButton(Messages.msg("ok"), skin)
		okButton.addListener { _, _ ->
			hideWithFade()
		}
		
		getContentTable().add(layout).expandX().fillX().row()
	    getButtonTable().add(okButton).pad(10f).fillX().expandX()
	}
	
	private fun addSettlementLabel() {
		val a1 = HorizontalGroup()
		var a2 : Actor? = null
		
		val img = Image(TextureRegionDrawable(
			GameResources.instance.getCenterAdjustFrameTexture(settlement.getImageKey()).texture
		), Scaling.none, Align.center)
		
		var str : String
		if (contacted) {
			str = settlement.getName() + ", "
			str += 
				StringTemplate.template( if (settlement.settlementType.isCapital) "indianCapital" else "indianSettlement" )
					.addStringTemplate("%nation%", settlement.getOwner().getNationName())
					.eval() 
			str += "(" + Messages.msg(settlement.getTension(player).getKey()) + ")"
		} else {
			str = Messages.msg("indianSettlement.nameUnknown") + ", (" + Messages.msg("tension.unknown") + ")"
		}
		a1.addActor(img)
		a1.addActor(Label(str, skin)) 

		if (settlement.getMissionary() != null) {
			var mimg = Image(TextureRegionDrawable(
				GameResources.instance.getFrame(settlement.getMissionary().unit.resourceImageKey()).texture
			), Scaling.none, Align.center)
			a2 = mimg
		}
		
		layout.add(a1)
		if (a2 != null) {
			layout.add(a2)
		}
		layout.row()
	}
	
	private fun addLearnableSkillRow() {
		val l1 = Label(Messages.msg("indianSettlement.learnableSkill"), skin)
		val a2 : Actor
		if (visited) {
			if (settlement.learnableSkill == null) {
				a2 = Label(Messages.msg("indianSettlement.skillNone"), skin)
			} else {
				a2 = HorizontalGroup()

				val unitType = Specification.instance.unitTypes.getById(settlement.learnableSkill.id)
				val texture = GameResources.instance.getFrame(unitType.resourceImageKey()).texture;
				val image = Image(TextureRegionDrawable(texture), Scaling.none, Align.center);
				a2.addActor(image)
				
				var str = StringTemplate.template(Messages.nameKey(settlement.learnableSkill.id))
					.addAmount("%number%", 1)
					.eval()
				a2.addActor(Label(str, skin))
			}
		} else {
			a2 = Label(Messages.msg("indianSettlement.skillUnknown"), skin)
		}
		
    	layout.add(l1)
	    layout.add(a2)
    	layout.row()
	}
	
	private fun addMostHatedNationRow() {
		val l1 = Label(Messages.msg("indianSettlement.mostHated"), skin)
		val a2 : Actor
		
		if (contacted) {
			val mostHatedPlayer = mostHatedPlayer()
			if (mostHatedPlayer == null) {
				a2 = Label(Messages.msg("indianSettlement.mostHatedNone"), skin)
			} else {
				a2 = HorizontalGroup()
				val img = Image(TextureRegionDrawable(
					GameResources.instance.coatOfArms(mostHatedPlayer.nation()).texture
				), Scaling.none, Align.center)
				img.setScale(0.75f)
				a2.addActor(img)
				a2.addActor(Label(Messages.message(mostHatedPlayer.nationName), skin))
			}
		} else {
			a2 = Label(Messages.msg("indianSettlement.mostHatedUnknown"), skin)
		}
		
    	layout.add(l1)
	    layout.add(a2)
    	layout.row()
	}
	
	private fun mostHatedPlayer() : Player? {
		var mostHatedPlayerWithTension = settlement.mostHatedPlayer(game.players)
		if (mostHatedPlayerWithTension == null) {
			return null
		}
		return game.players.getById(mostHatedPlayerWithTension.key)
	}
	
	private fun addHighlyWantedGoodsRow() {
		val l1 = Label(Messages.msg("indianSettlement.highlyWanted"), skin)
		val a2 : Actor
		
		if (visited) {
			if (settlement.wantedGoods.isNotEmpty()) {
				val goodType = settlement.wantedGoods.get(0)
				
				a2 = HorizontalGroup()
				addGoods(a2, goodType)
			} else {
				a2 = Label(Messages.msg("indianSettlement.wantedGoodsNone"), skin)
			}
		} else {
			a2 = Label(Messages.msg("indianSettlement.wantedGoodsUnknown"), skin)
		}
		
    	layout.add(l1)
	    layout.add(a2)
    	layout.row()
	}
	
	private fun addOtherWantedGoodsRow() {
		val l1 = Label(Messages.msg("indianSettlement.otherWanted"), skin)
		val a2 : Actor
		
		if (visited) {
			if (settlement.wantedGoods.size > 1) {
				a2 = HorizontalGroup()
				for (i in 1 .. settlement.wantedGoods.size - 1) {
					addGoods(a2, settlement.wantedGoods.get(i))
				}
			} else {
				a2 = Label(Messages.msg("indianSettlement.wantedGoodsNone"), skin)
			}
		} else {
			a2 = Label(Messages.msg("indianSettlement.wantedGoodsUnknown"), skin)
		}
		
    	layout.add(l1)
	    layout.add(a2)
    	layout.row()
	}
	
	private fun addGoods(group : HorizontalGroup, goodsType : GoodsType) {
		val goodsImg = Image(TextureRegionDrawable(
			GameResources.instance.goodsImage(goodsType).texture
		), Scaling.none, Align.center)
		
		var str = StringTemplate.template(Messages.nameKey(goodsType.id))
			.addAmount("%amount%", 1)
			.eval()
		group.addActor(goodsImg)
		group.addActor(Label(str, skin))
	}	
}