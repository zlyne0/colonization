package promitech.colonization.screen.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.FloatArray
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.specification.AbstractGoods
import promitech.colonization.GameResources
import promitech.colonization.gdx.Frame

internal class ReadonlyCargoSummaryPanel(
    private val unit: Unit,
    private val gameResources: GameResources
) : Widget() {

    private val imagesXPos: FloatArray
    private val images: Array<Frame>

    private val width: Float

    init {
        var imgCount = 0
        var goodsList: List<AbstractGoods> = emptyList()
        if (unit.goodsContainer != null) {
            goodsList = unit.goodsContainer.slotedGoods()
            imgCount += goodsList.size
        }
        if (unit.unitContainer != null) {
            imgCount += unit.unitContainer.units.size()
        }

        imagesXPos = FloatArray(imgCount)
        images = Array<Frame>(imgCount)

        var x = 0f
        var imgWidth = 0
        for (goods in goodsList) {
            val goodsImage = gameResources.goodsImage(goods.typeId)
            imgWidth = goodsImage.texture.regionWidth

            images.add(goodsImage)
            imagesXPos.add(x)

            x += goodsImage.texture.regionWidth / 2
        }
        x += imgWidth / 2

        if (unit.unitContainer != null) {
            for (unit in unit.unitContainer.units) {
                val unitImage = gameResources.getFrame(unit.resourceImageKey())
                imgWidth = unitImage.texture.regionWidth

                images.add(unitImage)
                imagesXPos.add(x)

                x += unitImage.texture.regionWidth / 2
            }
        }
        width = (x + imgWidth / 2).toFloat()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        for (index in 0 .. imagesXPos.size-1) {
            val image = images.get(index)
            batch.draw(image.texture, getX() + imagesXPos.get(index), getY() - image.texture.regionHeight / 2)
        }
    }

    override fun getWidth(): Float {
        return prefWidth
    }

    override fun getHeight(): Float {
        return prefHeight
    }

    override fun getPrefHeight(): Float {
        return super.getPrefHeight()
    }

    override fun getPrefWidth(): Float {
        return width
    }
}