package promitech.colonization.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import promitech.colonization.screen.ApplicationScreenManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1280, 800);
		config.setForegroundFPS(60);
		config.setTitle("AFreecol");
		new Lwjgl3Application(new ApplicationScreenManager(), config);
	}
}
