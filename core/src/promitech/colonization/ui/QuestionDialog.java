package promitech.colonization.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import promitech.colonization.GameResources;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class QuestionDialog extends Dialog {
	public static final QuestionDialog.OptionAction<Object> DO_NOTHING_ACTION = null;
	
    public static interface OptionAction<T> {
        public void executeAction(T payload);
    }
    
    private final Table dialogLayout = new Table();
    private final Map<TextButton, OptionAction<? extends Object>> actionByButton = new HashMap<TextButton, OptionAction<? extends Object>>();
    private final Map<TextButton, Object> payloadByButton = new HashMap<TextButton, Object>();
    private final List<Runnable> onCloseListeners = new ArrayList<Runnable>();
    
    private final ChangeListener buttonChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            OptionAction<Object> optionAction = (OptionAction<Object>) actionByButton.get(event.getListenerActor());
            hide();
            if (optionAction != null) {
                optionAction.executeAction(payloadByButton.get(event.getListenerActor()));
            }
            for (Runnable listener : onCloseListeners) {
            	listener.run();
            }
        }
    };    
    
    public QuestionDialog() {
        super("", GameResources.instance.getUiSkin());
    }
    
    public QuestionDialog(StringTemplate question) {
    	super("", GameResources.instance.getUiSkin());
    	
    	addQuestion(question);
    }
    
    public final QuestionDialog addQuestion(StringTemplate st) {
        String question = Messages.message(st);
        addQuestion(question);
        return this;
    }

    public Cell<Actor> addDialogActor(Actor actor) {
    	return dialogLayout.add(actor);
    }
    
    public void addQuestion(String question) {
        Label lable = new Label(question, GameResources.instance.getUiSkin());
        lable.setWrap(true);
        dialogLayout.add(lable).fillX().pad(20).space(10).width(500).row();
    }

    public <T> void addAnswer(StringTemplate strTemplate, OptionAction<T> optionAction, T payload) {
    	addAnswerText(Messages.message(strTemplate), optionAction, payload);
    }
    
    public <T> QuestionDialog addAnswer(String msgKey, OptionAction<T> optionAction, T payload) {
    	addAnswerText(Messages.msg(msgKey), optionAction, payload);
    	return this;
    }

    public <T> void addAnswerText(String text, OptionAction<T> optionAction, T payload) {
    	TextButton button = new TextButton(text, GameResources.instance.getUiSkin());
    	button.addListener(buttonChangeListener);
    	
    	dialogLayout.add(button).fillX().space(10).row();
    	actionByButton.put(button, optionAction);
    	payloadByButton.put(button, payload);
    }
    
    public void addOnlyCloseAnswer(String msgKey) {
    	TextButton button = new TextButton(Messages.msg(msgKey), GameResources.instance.getUiSkin());
    	button.addListener(buttonChangeListener);
    	
        dialogLayout.add(button).fillX().space(10).row();
    }
    
    public void addOnCloseListener(Runnable listener) {
    	onCloseListeners.add(listener);
    }
    
    @Override
    public Dialog show(Stage stage) {
        getContentTable().add(dialogLayout).fillX();
        return super.show(stage);
    }
    
}
