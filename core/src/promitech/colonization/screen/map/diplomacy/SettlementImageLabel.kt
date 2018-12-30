package promitech.colonization.screen.map.diplomacy

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import net.sf.freecol.common.model.Settlement
import promitech.colonization.GameResources
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin

public class SettlementImageLabel(val settlement : Settlement) : HorizontalGroup() {
	init {
		val img = Image(TextureRegionDrawable(
			GameResources.instance.getCenterAdjustFrameTexture(settlement.getImageKey()).texture
		), Scaling.none, Align.center)
		
		addActor(img)
		addActor(Label(settlement.getName(), GameResources.instance.getUiSkin()))
	}
}