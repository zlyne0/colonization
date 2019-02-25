package net.sf.freecol.common.util;

public interface Predicate<T> {
    
    public static final Predicate<?> AlwaysTrue = new Predicate() {
        @Override
        public boolean test(Object obj) {
            return true;
        }
    }; 
    
    boolean test(T obj);
}