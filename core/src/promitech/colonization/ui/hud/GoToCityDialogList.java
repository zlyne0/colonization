package promitech.colonization.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.ui.ClosableDialog;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.SelectableRowTable;
import promitech.colonization.ui.SelectableTableItem;
import promitech.colonization.ui.resources.Messages;

class ImageSelectableTableItem<T> extends SelectableTableItem<T> {
	private final Cell<Image> cell;
	
	public static <T> ImageSelectableTableItem<T> create(T payload, String imgkey) {
		Frame frame = GameResources.instance.getFrame(imgkey);
		return new ImageSelectableTableItem<T>(payload, frame);
	}
	
	public ImageSelectableTableItem(T payload, Frame frame) {
		super(payload);
		TextureRegionDrawable drawableTextureRegion = new TextureRegionDrawable(frame.texture);
		cell = add(new Image(drawableTextureRegion, Scaling.none, Align.center));
		cell.expand().fill();
	}

	public Cell<Image> getImgCell() {
		return cell;
	}
}

class LabelSelectableTableItem<T> extends SelectableTableItem<T> {

	public LabelSelectableTableItem(T payload, String label, LabelStyle labelStyle) {
		super(payload);
		
		Label nameLabel = new Label(label, labelStyle);
		nameLabel.setAlignment(Align.left);
		add(nameLabel)
			.expand().fill()
			.bottom()
			.row();
	}
	
}

public class GoToCityDialogList extends ClosableDialog {

	private SelectableRowTable locationTables;
	private final Table dialogLayout = new Table();
	
	private LabelStyle cityNameLabelStyle;
	
	public GoToCityDialogList(final GUIGameController guiGameController, ShapeRenderer shape, float maxHeight) {
		super("", GameResources.instance.getUiSkin(), maxHeight);
		
		locationTables = new SelectableRowTable(shape);
		
		populateWithLocations(guiGameController.getGame().playingPlayer, guiGameController.getActiveUnit());
		
		locationTables.setDoubleClickedListener(new DoubleClickedListener() {
			@Override
			public void doubleClicked(InputEvent event, float x, float y) {
				if (event.getListenerActor() instanceof SelectableTableItem) {
					GoToCityDialogList.this.hide();
					
					SelectableTableItem<?> item = (SelectableTableItem<?>)event.getListenerActor();
					if (item.payload instanceof Europe) {
						guiGameController.acceptPathToEuropeDestination();
					}
					if (item.payload instanceof Settlement) {
						Settlement settlement = (Settlement)item.payload;
						guiGameController.acceptPathToDestination(settlement.tile);
					}
				}
			}
		});
		
		ScrollPane locationsItemsScrollPane = new ScrollPane(locationTables, GameResources.instance.getUiSkin());
		locationsItemsScrollPane.setFlickScroll(false);
		locationsItemsScrollPane.setScrollingDisabled(true, false);
		locationsItemsScrollPane.setForceScroll(false, false);
		locationsItemsScrollPane.setFadeScrollBars(false);
		locationsItemsScrollPane.setOverscroll(true, true);
		locationsItemsScrollPane.setScrollBarPositions(false, true);
		
		dialogLayout.add(locationsItemsScrollPane).pad(20);
		getContentTable().add(dialogLayout).top().expand();
		getButtonTable().add(buttonsPanel()).expandX();
		
		withHidingOnEsc();
	}

	private void populateWithLocations(Player player, Unit unit) {
		if (player.canMoveToEurope() && unit.canMoveToHighSeas()) {
			String europeName = Messages.msg(player.getEuropeNameKey());
			
			Frame coatOfArms = GameResources.instance.coatOfArms(player.nation());
			ImageSelectableTableItem<Europe> imageItem = new ImageSelectableTableItem<Europe>(player.getEurope(), coatOfArms);
			
			LabelSelectableTableItem<Europe> labelItem = new LabelSelectableTableItem<Europe>(
					player.getEurope(), 
					europeName, 
					cityNameLabelStyle()
			);
			
			locationTables.addCell(imageItem)
				.fill()
				.expand();
			locationTables.addCell(labelItem)
				.expand().fill()
				.align(Align.left);
			locationTables.nextRow();
		}
		
		for (Settlement settlement : player.settlements.entities()) {
			ImageSelectableTableItem<Settlement> imageItem = ImageSelectableTableItem.create(settlement, settlement.getImageKey());
			imageItem.getImgCell().pad(10);
			
			LabelSelectableTableItem<Settlement> labelItem = new LabelSelectableTableItem<Settlement>(
					settlement, 
					settlement.getName(), 
					cityNameLabelStyle()
			);
			
			locationTables.addCell(imageItem)
				.fill()
				.expand();
			locationTables.addCell(labelItem)
				.expand().fill()
				.align(Align.left);
			locationTables.nextRow();
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
				System.out.println("okButton");
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
		panel.add(cancelButton).left().pad(0, 20, 20, 20);
		panel.add(okButton).right().pad(0, 20, 20, 20);
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
