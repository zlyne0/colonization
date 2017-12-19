package promitech.colonization;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.RandomXS128;

import net.sf.freecol.common.model.specification.WithProbability;

public final class Randomizer {

	private static final Randomizer instance = new Randomizer();
	
	private Random rand;
	
	public static void changeRandomObject(Random random) {
	    instance.rand = random;
	}
	
	private Randomizer() {
		rand = new RandomXS128(System.currentTimeMillis());
	}
	
	public static Randomizer instance() {
		return instance;
	}
	
	public float realProbability() {
	    return rand.nextFloat();
	}
	
	public int randomInt(int max) {
		return Math.abs(rand.nextInt()) % max;
	}

	public int randomInt(int min, int max) {
		if (min == -1 || max == -1) {
			return -1;
		}
		return rand.nextInt(max - min) + min;
	}

	public boolean isHappen(int percentProbability) {
		if (percentProbability <= 0) {
			return false;
		}
		return randomInt(100) < percentProbability;
	}
	
	public boolean isHappen(WithProbability event) {
		if (event.getOccureProbability() <= 0) {
			return false;
		}
		return randomInt(100) < event.getOccureProbability();
	}

	public <T extends WithProbability> T randomOne(Collection<T> events) {
		if (events.isEmpty()) {
			return null;
		}
		int total = 0;
		for (T wp : events) {
			total += wp.getOccureProbability();
		}
		
		int r = randomInt(total);
		int totalSelect = 0;
		for (T wp : events) {
			totalSelect += wp.getOccureProbability();
			if (r < totalSelect) {
				return wp;
			}
		}
		return null;
	}

	public <T> T randomMember(List<T> col) {
		if (col.isEmpty()) {
			return null;
		}
		if (col.size() == 1) {
			return col.get(0);
		}
		int index = randomInt(col.size());
		return col.get(index);
	}

	public <LIST_ELEMENT_TYPE> void shuffle(List<LIST_ELEMENT_TYPE> list) {
		Collections.shuffle(list, rand);
	}
}
