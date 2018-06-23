package promitech.colonization.screen.map.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.orders.move.MoveController;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.ClosableDialogSize;
import promitech.colonization.ui.STable;
import promitech.colonization.ui.STableSelectListener;
import promitech.colonization.ui.resources.Messages;

public class GoToCityDialogList extends ClosableDialog<GoToCityDialogList> {
    
    private final MoveController moveController;
    private STable locationList;
	private final Table dialogLayout = new Table();
	
	private LabelStyle cityNameLabelStyle;
	
	public GoToCityDialogList(final MoveController moveController, ShapeRenderer shape, GUIGameModel guiGameModel) {
		super(ClosableDialogSize.def(), ClosableDialogSize.height75());
		this.moveController = moveController;
		
		locationList = new STable(shape);
		locationList.rowsPad(10, 0, 10, 0);
		populateWithLocations(guiGameModel.game.playingPlayer, guiGameModel.getActiveUnit());
		
        locationList.addSelectListener(onSelectListener);
		
		ScrollPane locationsItemsScrollPane = new ScrollPane(locationList, GameResources.instance.getUiSkin());
		locationsItemsScrollPane.setFlickScroll(false);
		locationsItemsScrollPane.setScrollingDisabled(true, false);
		locationsItemsScrollPane.setForceScroll(false, false);
		locationsItemsScrollPane.setFadeScrollBars(false);
		locationsItemsScrollPane.setOverscroll(true, true);
		locationsItemsScrollPane.setScrollBarPositions(false, true);
		
		buttonTableLayoutExtendX();
		dialogLayout.add(locationsItemsScrollPane).pad(20);
		getContentTable().add(dialogLayout).top().expand();
		dialog.getButtonTable().add(buttonsPanel()).expandX().fillX();
		
		withHidingOnEsc();
	}
	
	private STableSelectListener onSelectListener = new STableSelectListener() {
	    @Override
	    public void onSelect(Object payload) {
	        GoToCityDialogList.this.hide();
	        
	        if (payload instanceof Europe) {
	            moveController.acceptPathToEuropeDestination();
	        }
	        if (payload instanceof Settlement) {
	            Settlement settlement = (Settlement)payload;
	            moveController.acceptPathToDestination(settlement.tile);
	        }
	    }
	};

	private void populateWithLocations(Player player, Unit unit) {
	    int alignment[] = new int[] { Align.center, Align.left };
	    
		if (player.canMoveToEurope() && unit.canMoveToHighSeas()) {
			String europeName = Messages.msg(player.getEuropeNameKey());
			Frame coatOfArms = GameResources.instance.coatOfArms(player.nation());
			locationList.addRow(player.getEurope(),
			    alignment,
			    new Image(coatOfArms.texture),
			    new Label(europeName, cityNameLabelStyle())
			);
		}
		
		for (Settlement settlement : player.settlements.entities()) {
		    Frame colony = GameResources.instance.getFrame(settlement.getImageKey());
		    Label label = new Label(settlement.getName(), cityNameLabelStyle());
		    label.setAlignment(Align.left);
            locationList.addRow(settlement, alignment, new Image(colony.texture), label);
		}
	}

	private Actor buttonsPanel() {
		TextButton okButton = new TextButton(Messages.msg("ok"), GameResources.instance.getUiSkin());
		TextButton cancelButton = new TextButton(Messages.msg("cancel"), GameResources.instance.getUiSkin());
		
		okButton.align(Align.right);
		cancelButton.align(Align.left);

		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
			    Object selectedPayload = locationList.getSelectedPayload();
			    if (selectedPayload != null) {
			        onSelectListener.onSelect(selectedPayload);
			    }
			}
		});
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GoToCityDialogList.this.hide();
			}
		});
		
		Table panel = new Table();
		panel.setFillParent(true);
		panel.add(cancelButton)
			.pad(5, 5, 5, 5)
			.fillX()
			.expandX();
		panel.add(okButton)
			.pad(5, 5, 5, 5)
			.fillX()
			.expandX();
		return panel;
	}
	
	private LabelStyle cityNameLabelStyle() {
		if (cityNameLabelStyle != null) {
			return cityNameLabelStyle;
		}
		BitmapFont font = FontResource.getCityNamesFont();
		font.setColor(Color.WHITE);
		return cityNameLabelStyle = new LabelStyle(font, Color.WHITE);
	}
}
