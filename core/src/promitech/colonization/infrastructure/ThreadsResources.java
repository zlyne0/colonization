package promitech.colonization.infrastructure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadsResources {

	public static final ThreadsResources instance = new ThreadsResources(); 
	
	private ExecutorService executorService;
	
	private ThreadsResources() {
	}
	
	public void executeMovement(Runnable command) {
		execute(command);
	}
	
	public void execute(Runnable command) {
		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(1);
		}
		executorService.execute(command);
	}
	
	public void dispose() {
		if (executorService != null) {
			executorService.shutdown();
		}
	}
}
