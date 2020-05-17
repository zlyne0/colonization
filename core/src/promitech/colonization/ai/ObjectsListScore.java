package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ObjectsListScore<T> {

	private static Comparator<ObjectScore<?>> descendingComparator = new Comparator<ObjectScore<?>>() {
		@Override
		public int compare(ObjectScore<?> o1, ObjectScore<?> o2) {
			return o2.score - o1.score;
		}
	};
	
	public static class ObjectScore<O> {
		O obj;
		int score;
		
		public ObjectScore(O obj, int score) {
			this.obj = obj;
			this.score = score;
		}
		
		public String toString() {
			return this.obj + " " + score;
		}
	}
	
	private List<ObjectScore<T>> objs = new ArrayList<ObjectScore<T>>();
	private int scoreSum = 0;
	
	public ObjectsListScore<T> add(T obj, int score) {
		this.objs.add(new ObjectScore<T>(obj, score));
		this.scoreSum += score;
		return this;
	}

	public int size() {
		return this.objs.size();
	}
	
	public T obj(int index) {
		return this.objs.get(index).obj;
	}
	
	public int score(int index) {
		return this.objs.get(index).score;
	}
	
	public ObjectScore<T> objScore(int index) {
		return this.objs.get(index);
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