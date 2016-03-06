package promitech.colonization.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import promitech.colonization.ApplicationScreenManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1100;
		config.height = 800;
		new LwjglApplication(new ApplicationScreenManager(), config);
	}
}
