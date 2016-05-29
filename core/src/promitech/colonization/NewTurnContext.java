package promitech.colonization;

public class NewTurnContext {
	private boolean requireUpdateMapModel = false;

	public void restart() {
		requireUpdateMapModel = false;
	}

	public boolean isRequireUpdateMapModel() {
		return requireUpdateMapModel;
	}

	public void setRequireUpdateMapModel() {
		this.requireUpdateMapModel = true;
	}
}
