package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class ClosableDialogSize {

	public static ClosableDialogSize width50() {
		return new ClosableDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getWidth() * 0.5f;
			}
		};
	}

	public static ClosableDialogSize width75() {
		return new ClosableDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getWidth() * 0.75f;
			}
		};
	}

	public static ClosableDialogSize height50() {
		return new ClosableDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getHeight() * 0.5f;
			}
		};
	}
	
	public static ClosableDialogSize height75() {
		return new ClosableDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getHeight() * 0.75f;
			}
		};
	}
	
	
	protected float val;
	public abstract void init(Stage stage);
	
	public float get() {
		return val;
	}
}