package net.sf.freecol.common.util;

public final class PredicateUtil {

    public static <T> Predicate<T> and(final Predicate<T> a, final Predicate<T> b) {
        return new Predicate<T>() {
            @Override
            public boolean test(T obj) {
                return a.test(obj) && b.test(obj);
            }
        };
    }

    public static <T> Predicate<T> and(final Predicate<T> a, final Predicate<T> b, final Predicate<T> c) {
        return new Predicate<T>() {
            @Override
            public boolean test(T obj) {
                return a.test(obj) && b.test(obj) && c.test(obj);
            }
        };
    }
}