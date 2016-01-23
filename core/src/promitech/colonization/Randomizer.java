package promitech.colonization;

import java.util.Random;

public final class Randomizer {

	private static final Randomizer instance = new Randomizer();
	
	private final Random rand;
	
	
	private Randomizer() {
		rand = new Random(System.currentTimeMillis());
	}

	public static Randomizer getInstance() {
		return instance;
	}
	
	public int randomInt(int max) {
		return Math.abs(rand.nextInt()) % max;
	}

	public int randomInt(int min, int max) {
		return rand.nextInt(max - min) + min;
	}
	
}
