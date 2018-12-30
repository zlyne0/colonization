package promitech.colonization

import com.badlogic.gdx.utils.Array

public fun <T, R> Iterable<T>.toGdxArray() : Array<T> {
	return this.fold(Array<T>(), { arr : Array<T>, item : T ->
		arr.add(item)
		arr
	})
}

public fun <T> Iterable<T>.toGdxArray(initSize : Int) : Array<T> {
	return this.fold(Array<T>(initSize), { arr : Array<T>, item : T ->
		arr.add(item)
		arr
	})
}

public fun Array<*>.isEmpty() : Boolean {
	return this.size == 0
}

