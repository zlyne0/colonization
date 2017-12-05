package promitech.colonization.actors.cheat

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.reflect.ClassReflection

class PoolsStat {
	class Stat(
		val className: Any,
		val max: Int,
		val initialCapacity: Int,
		val free: Int,
		val peak: Int
	) {
		companion object {
		    fun header() : String {
		        return "free peak  max init class name"
		    }
		}
		
		fun toFormatedString() : String {
			return String.format("%4d %4d %4d %4d %s", free, peak, max, initialCapacity, className);
		}
	}
	
	fun readStats() : List<Stat> {
		var typePoolsField = ClassReflection.getDeclaredField(Pools::class.java, "typePools")
		typePoolsField.setAccessible(true)
		
		//(ObjectMap<Class, Pool>)
		var pools = typePoolsField.get(null) as ObjectMap<Any, Pool<Any>>
		return pools.entries()
			.map { createStatObject(it.key, it.value) }
	}
	
	fun createStatObject(className: Any, pool: Pool<Any>) : Stat {
    	var freeObjectsField = ClassReflection.getDeclaredField(Pool::class.java, "freeObjects")
    	freeObjectsField.setAccessible(true)
    	
    	var poolFreeObject = freeObjectsField.get(pool) as Array<Any>
        
    	return Stat(
            className,
            pool.max, 
            poolFreeObject.items.size, 
            pool.getFree(),
            pool.peak
        )				
	}
}