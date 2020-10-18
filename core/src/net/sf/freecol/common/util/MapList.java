package net.sf.freecol.common.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapList<K, V> {
	private final Map<K, List<V>> map = new LinkedHashMap<K, List<V>>();
	
	public void add(K key, V value) {
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		list.add(value);
	}
	
	public Set<K> keySet() {
		return map.keySet();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapList other = (MapList) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
}