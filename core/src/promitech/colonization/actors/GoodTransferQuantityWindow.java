package promitech.colonization.actors;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.GameResources;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.ui.resources.Messages;

public class GoodTransferQuantityWindow extends Dialog {

	private final Skin skin;
	private TextField numberTextField;
	private final int minVal;
	private final int maxVal;
	private final String goodTypeId;

	private final DragAndDropSourceContainer<AbstractGoods> sourceContainer;
	private final DragAndDropTargetContainer<AbstractGoods> targetContainer;
	
	public GoodTransferQuantityWindow(
			AbstractGoods abstractGoods, 
			DragAndDropSourceContainer<AbstractGoods> sourceContainer, 
			DragAndDropTargetContainer<AbstractGoods> targetContainer
	) {
		super("", GameResources.instance.getUiSkin());
		this.sourceContainer = sourceContainer;
		this.targetContainer = targetContainer;
		this.goodTypeId = abstractGoods.getTypeId();
		skin = GameResources.instance.getUiSkin();
		
		this.minVal = 1;
		this.maxVal = abstractGoods.getQuantity();
		
		numberTextField = new TextField(Integer.toString(abstractGoods.getQuantity()), skin);
		numberTextField.setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
		
		getContentTable().add(new Label(Messages.msg("goodsTransfer.text"), skin)).row();
		getContentTable().add(numberTextField).row();
		getContentTable().add(new Image(GameResources.instance.resource(abstractGoods.getTypeId()).texture));
		
		TextButton okButton = new TextButton(Messages.msg("ok"), skin);
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				pressedOk();
			}
		});
		getButtonTable().add(okButton);
		
		TextButton cancelButton = new TextButton(Messages.msg("cancel"), skin);
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				pressedCancel();
			}
		});
		getButtonTable().add(cancelButton);
		
		addListener(new InputListener() {
			public boolean keyDown (InputEvent event, int keycode2) {
				if (Keys.ENTER == keycode2) {
					pressedOk();
				}
				if (Keys.ESCAPE == keycode2) {
					pressedCancel();
				}
				return false;
			}
		});
	}

	private void pressedOk() {
		int cur = Integer.parseInt(numberTextField.getText());
		if (cur < minVal || cur > maxVal) {
			System.out.println("can not move good " + goodTypeId + " in quantity " + cur);
			return;
		}
		AbstractGoods transferGood = new AbstractGoods(goodTypeId, cur);
		if (targetContainer.canPutPayload(transferGood, -1, -1)) {
        	sourceContainer.takePayload(transferGood, -1, -1);
        	targetContainer.putPayload(transferGood, -1, -1);
			hide();
			return;
		} 
		System.out.println("target container do not accept good " + goodTypeId + " in quantity " + cur);
	}
	
	private void pressedCancel() {
		hide();
	}
}
