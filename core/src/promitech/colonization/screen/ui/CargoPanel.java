package promitech.colonization.screen.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;

import promitech.colonization.screen.colony.DragAndDropPreHandlerTargetContainer;
import promitech.colonization.screen.colony.DragAndDropSourceContainer;
import promitech.colonization.screen.colony.DragAndDropTargetContainer;

class CargoPanel extends Table implements
    DragAndDropSourceContainer<AbstractGoods>,
    DragAndDropTargetContainer<AbstractGoods>,
    DragAndDropPreHandlerTargetContainer<AbstractGoods>
{
	private static final float ITEMS_SPACE = 15;

	private final ChangeColonyStateListener changeColonyStateListener;
	private float cargoWidthWidth;
	private final DragAndDrop goodsDragAndDrop;
	private UnitActor unitActor;
	private final float prefHeight;
	private final ShapeRenderer shapeRenderer = new ShapeRenderer();

	CargoPanel(DragAndDrop goodsDragAndDrop, ChangeColonyStateListener changeColonyStateListener) {
		setTouchable(Touchable.enabled);

		this.goodsDragAndDrop = goodsDragAndDrop;
		this.changeColonyStateListener = changeColonyStateListener;

		prefHeight = LabelGoodActor.goodsImgHeight() + ITEMS_SPACE;

		align(Align.left);
		defaults().align(Align.left);
	}

	void initCargoForUnit(UnitActor unitActor) {
		this.unitActor = unitActor;
		cargoWidthWidth = unitActor.unit.unitType.getSpace() * (LabelGoodActor.goodsImgWidth() + ITEMS_SPACE);

		updateCargoPanelData();
	}

	protected void updateCargoPanelData() {
		clear();

		GoodsContainer goodsContainer = unitActor.unit.getGoodsContainer();
		for (AbstractGoods carrierGood : goodsContainer.slotedGoods()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(carrierGood.getTypeId());

			QuantityGoodActor goodActor = new QuantityGoodActor(goodsType, carrierGood.getQuantity());
			goodActor.dragAndDropSourceContainer = this;
			goodsDragAndDrop.addSource(new QuantityGoodActor.GoodsDragAndDropSource(goodActor));
			add(goodActor)
				.width(goodActor.getPrefWidth() + ITEMS_SPACE)
				.height(goodActor.getPrefHeight() + ITEMS_SPACE);
		}
	}

	@Override
	public float getPrefWidth() {
		return cargoWidthWidth;
	}

	@Override
	public float getPrefHeight() {
		return prefHeight;
	}

	@Override
	public void putPayload(AbstractGoods anAbstractGood, float x, float y) {
		unitActor.putPayload(anAbstractGood, x, y);
	}

	@Override
	public boolean canPutPayload(AbstractGoods anAbstractGood, float x, float y) {
		return unitActor.canPutPayload(anAbstractGood, x, y);
	}

	@Override
	public void takePayload(AbstractGoods anAbstractGood, float x, float y) {
		System.out.println("carrierPanel: carrierId[" + unitActor.unit.getId() + "] take goods " + anAbstractGood);

		unitActor.unit.unloadCargo(anAbstractGood);
		updateCargoPanelData();
		changeColonyStateListener.transfereGoods();
	}

	@Override
	public void onDragPayload(float x, float y) {
	}

	@Override
	public void onLeaveDragPayload() {
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		drawBorders(batch);
		super.draw(batch, parentAlpha);
	}

	private void drawBorders(Batch batch) {
		batch.end();

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);

		float oneBox = LabelGoodActor.goodsImgWidth() + ITEMS_SPACE;
		for (int i=0; i<unitActor.unit.unitType.getSpace(); i++) {
			shapeRenderer.rect(getX() + (oneBox * i), getY(), oneBox, prefHeight);
		}
		shapeRenderer.end();

		batch.begin();
	}

	@Override
	public boolean isPrePutPayload(AbstractGoods payload, float x, float y) {
		return unitActor.isPrePutPayload(payload, x, y);
	}

	@Override
	public void prePutPayload(AbstractGoods payload, float x, float y,
			DragAndDropSourceContainer<AbstractGoods> sourceContainer)
	{
		unitActor.prePutPayload(payload, x, y, sourceContainer);
	}
}
