package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class MapIdEntitiesAssert extends AbstractAssert<MapIdEntitiesAssert, MapIdEntities> {

	public MapIdEntitiesAssert(MapIdEntities<?> actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static MapIdEntitiesAssert assertThat(MapIdEntities<?> map) {
		return new MapIdEntitiesAssert(map, MapIdEntitiesAssert.class);
	}
	
	public MapIdEntitiesAssert containsId(String id) {
		isNotNull();
		if (!actual.containsId(id)) {
			failWithMessage("expected contain entity id <%s>", id);
		}
		return this;
	}
	
}
