package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class ObjectsListScore<T> implements Iterable<ObjectsListScore.ObjectScore<T>> {

	private static Comparator<ObjectScore<?>> descendingComparator = new Comparator<ObjectScore<?>>() {
		@Override
		public int compare(ObjectScore<?> o1, ObjectScore<?> o2) {
			return o2.score - o1.score;
		}
	};
	
	public static class ObjectScore<O> {
		private O obj;
		private int score;
		
		public ObjectScore(O obj, int score) {
			this.obj = obj;
			this.score = score;
		}
		
		public String toString() {
			return this.obj + " " + score;
		}

		public O getObj() {
			return obj;
		}

		public int getScore() {
			return score;
		}
	}
	
	private final List<ObjectScore<T>> objs;
	private int scoreSum = 0;
	
	public ObjectsListScore(int capacity) {
		objs = new ArrayList<ObjectsListScore.ObjectScore<T>>(capacity);
	}
	
	public ObjectsListScore<T> add(T obj, int score) {
		this.objs.add(new ObjectScore<T>(obj, score));
		this.scoreSum += score;
		return this;
	}

	public int size() {
		return this.objs.size();
	}
	
	public ObjectScore<T> get(int index) {
		return this.objs.get(index);
	}

	@Override
	public Iterator<ObjectScore<T>> iterator() {
		return this.objs.iterator();
	}
	
	public void sortDescending() {
		Collections.sort(objs, descendingComparator);
	}
	
	public void clear() {
		this.objs.clear();
		this.scoreSum = 0;
	}

	public int scoreSum() {
		return scoreSum;
	}
}