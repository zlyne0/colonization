package promitech.colonization.ui.resources;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

public class Messages {

	private static final Messages messagesResources = new Messages();
	
	private I18NBundle msgBundle;
	
	private Messages() {
	}

	public static Messages instance() {
		return messagesResources;
	}
	
	public void load() {
		FileHandle baseFileHandle = Gdx.files.internal("i18n/FreeColMessages");
		Locale locale = new Locale("pl");
		msgBundle = I18NBundle.createBundle(baseFileHandle, locale);		
	}
	
	public static String msg(String key) {
		return messagesResources.msgBundle.get(key);
	}
	
	public void dispose() {
	}

	public static String nameKey(String id) {
		return id + ".name";
	}

	public static String descriptionKey(String id) {
		return id + ".description";
	}
	
}
