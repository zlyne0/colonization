package promitech.colonization.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class ModalDialogSize {

	public static ModalDialogSize width50() {
		return new ModalDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getWidth() * 0.5f;
			}
		};
	}

	public static ModalDialogSize width75() {
		return new ModalDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getWidth() * 0.75f;
			}
		};
	}

	public static ModalDialogSize height50() {
		return new ModalDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getHeight() * 0.5f;
			}
		};
	}
	
	public static ModalDialogSize height75() {
		return new ModalDialogSize() {
			@Override
			public void init(Stage stage) {
				this.val = stage.getHeight() * 0.75f;
			}
		};
	}

	public static ModalDialogSize def() {
		return null;
	}
	
	protected float val;
	public abstract void init(Stage stage);
	
	public float get() {
		return val;
	}

}