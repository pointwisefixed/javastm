package com.walmart.stm.transaction;

/**
 * Created by grosal3 on 1/1/16.
 */
public class RefTuple<V, R> {
    V value;
    R revision;

    public RefTuple(V v, R r) {
        value = v;
        revision = r;
    }

    static <V, R> RefTuple get(V v, R r) {
        return new RefTuple(v, r);
    }

    @Override
    public String toString() {
        return String.format("value: %s, revision %s", value, revision);
    }
}
