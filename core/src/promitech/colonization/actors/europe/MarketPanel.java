package promitech.colonization.actors.europe;

import java.util.HashMap;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.MarketData;
import net.sf.freecol.common.model.player.MessageNotification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.TransactionEffectOnMarket;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.GoodTransferActorBridge;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.actors.colony.DragAndDropTargetContainer;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.KnobResizeableScrollPane;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.resources.StringTemplate;

public class MarketPanel extends Container<ScrollPane> implements DragAndDropTargetContainer<AbstractGoods>, DragAndDropSourceContainer<AbstractGoods> {
	private final java.util.Map<String, MarketGoodsActor> goodActorByType = new HashMap<String, MarketGoodsActor>();
	private final DragAndDrop goodsDragAndDrop;
	private final ShapeRenderer shapeRenderer;
	private final MarketLog marketLog;
	private final ChangeColonyStateListener changeColonyStateListener;
	
	private Player player;
	private Game game;
	private final Table scrollPaneContent = new Table();
	
	private final GoodTransferActorBridge goodTransferActorBridge;
	
    private final DoubleClickedListener marketGoodsDoubleClickListener = new DoubleClickedListener() {
        public void doubleClicked(InputEvent event, float x, float y) {
        	final MarketGoodsActor marketGoodsActor = (MarketGoodsActor)event.getListenerActor();
        	
        	MarketData marketData = player.market().marketGoods.getById(marketGoodsActor.getGoodsType().getId());
        	if (marketData.hasArrears()) {
        		payArrearsDoubleClickListener(marketData, marketGoodsActor);
        	} else {
        		goodTransferActorBridge.transferFromMarket(marketGoodsActor.getGoodsType().getId());
        	}
        }
    };
    
    private void payArrearsDoubleClickListener(final MarketData marketData, final MarketGoodsActor marketGoodsActor) {
		if (player.hasNotGold(marketData.getArrears())) {
			SimpleMessageDialog okDialog = new SimpleMessageDialog("", GameResources.instance.getUiSkin());
			okDialog.withContent(StringTemplate.template("model.europe.cantPayArrears")
				.addAmount("%amount%", marketData.getArrears())
			);
			okDialog.withButton("ok");
			okDialog.show(getStage());
		} else {
			QuestionDialog payConfirmationDialog = new QuestionDialog();
			payConfirmationDialog.addQuestion(StringTemplate.template("model.europe.payArrears")
				.addAmount("%amount%", marketData.getArrears())
			);
			
			OptionAction<MarketGoodsActor> paidYesAnswer = new OptionAction<MarketGoodsActor>() {
				@Override
				public void executeAction(final MarketGoodsActor marketGoodsActorPayload) {
					MarketData md = player.market().payArrears(player, marketGoodsActor.getGoodsType());
					marketGoodsActor.initData(md);
				}
			};
			
			payConfirmationDialog.addAnswer("ok", paidYesAnswer, marketGoodsActor);
			payConfirmationDialog.addOnlyCloseAnswer("cancel");
			payConfirmationDialog.show(getStage());
		}
    }
	
	MarketPanel(ShapeRenderer shapeRenderer, 
			DragAndDrop goodsDragAndDrop, 
			ChangeColonyStateListener changeColonyStateListener, 
			MarketLog marketLog, 
			GoodTransferActorBridge goodTransferActorBridge
	) {
		this.goodsDragAndDrop = goodsDragAndDrop;
		this.marketLog = marketLog;
		this.changeColonyStateListener = changeColonyStateListener;
		this.shapeRenderer = shapeRenderer;
		this.goodTransferActorBridge = goodTransferActorBridge;
		
		goodsDragAndDrop.addTarget(new MarketGoodsActor.GoodsDragAndDropTarget(this, this));
		
		ScrollPane scrollPane = new KnobResizeableScrollPane(scrollPaneContent, GameResources.instance.getUiSkin());
		scrollPane.setForceScroll(false, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setOverscroll(true, true);
		scrollPane.setScrollBarPositions(true, true);
		scrollPane.setScrollingDisabled(false, true);
		setActor(scrollPane);
	}
	
	public void init(Player player, Game game) {
		this.player = player;
		this.game = game;
		
		updatePrices();
	}
	
	private void updatePrices() {
        for (GoodsType goodsType : Specification.instance.goodsTypes.sortedEntities()) {
        	if (!goodsType.isStorable()) {
        		continue;
        	}
        	
        	MarketGoodsActor marketGoodsActor = goodActorByType.get(goodsType.getId());
        	if (marketGoodsActor == null) {
        		marketGoodsActor = new MarketGoodsActor(goodsType, shapeRenderer, this);
        		marketGoodsActor.addListener(marketGoodsDoubleClickListener);
        		
        		goodActorByType.put(goodsType.getId(), marketGoodsActor);
        		scrollPaneContent.add(marketGoodsActor)
        			.width(marketGoodsActor.getPrefWidth() + 20)
        			.height(marketGoodsActor.getPrefHeight() + 20);
        		
        		goodsDragAndDrop.addSource(new MarketGoodsActor.GoodsDragAndDropSource(marketGoodsActor));
        	}
        	MarketData marketData = player.market().marketGoods.getById(goodsType.getId());
        	marketGoodsActor.initData(marketData);
        }
	}

	@Override
	public void putPayload(AbstractGoods payload, float x, float y) {
		System.out.println("sell goods " + payload);

		GoodsType goodsType = Specification.instance.goodsTypes.getById(payload.getTypeId());
		TransactionEffectOnMarket transaction = player.market().sellGoods(game, player, goodsType, payload.getQuantity());
		marketLog.logSale(transaction);
		
		if (transaction.isMarketPriceChanged()) {
			System.out.println(
					"market price changed for " + transaction.goodsTypeId + 
					" from " + transaction.sellPriceBeforeTransaction + 
					" to " + transaction.sellPriceAfterTransaction
			);
			changeColonyStateListener.addNotification(MessageNotification.createGoodsPriceChangeNotification(player, transaction));
			updatePrices();
		}
	}

	@Override
	public boolean canPutPayload(AbstractGoods goods, float x, float y) {
		return player.market().canTradeInEurope(goods.getTypeId());
	}

	@Override
	public void takePayload(AbstractGoods payload, float x, float y) {
		System.out.println("Buy goods " + payload);
		

		int price = player.market().getBidPrice(payload.getTypeId(), payload.getQuantity());
		if (player.hasNotGold(price)) {
			marketLog.logMessage("notEnoughGold");
			payload.makeEmpty();
		} else {
		    GoodsType goodsType = Specification.instance.goodsTypes.getById(payload.getTypeId());
			TransactionEffectOnMarket transaction = player.market().buyGoods(game, player, goodsType, payload.getQuantity());
			marketLog.logPurchase(transaction);
			if (transaction.isMarketPriceChanged()) {
				System.out.println(
						"market price changed for " + transaction.goodsTypeId + 
						" from " + transaction.buyPriceBeforeTransaction + 
						" to " + transaction.buyPriceAfterTransaction
						);
				changeColonyStateListener.addNotification(MessageNotification.createGoodsPriceChangeNotification(player, transaction));
				updatePrices();
			}
		}
	}
}
