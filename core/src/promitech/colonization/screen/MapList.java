package promitech.colonization.screen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class MapList<K, V> {
	private Map<K, List<V>> map = new LinkedHashMap<K, List<V>>();
	
	public void add(K key, V value) {
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		list.add(value);
	}
	
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}
	
	public boolean isNotEmpty(K key) {
		List<V> l = map.get(key);
		return l != null && l.size() > 0;
	}
	
	public List<V> get(K key) {
		return map.get(key);
	}
}