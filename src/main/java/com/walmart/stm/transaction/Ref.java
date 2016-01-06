package com.walmart.stm.transaction;


/**
 * Created by grosal3 on 1/1/16.
 */
public class Ref<T> {
    RefTuple<T, Long> content;

    public Ref(T value) {
        content = RefTuple.get(value, 0l);
    }

    public T getValue(Context ctx) {
        return ctx.get(this);
    }

    public void setValue(T value, Transaction tx) {
        tx.set(this, value);
    }

    @Override
    public String toString() {
        return String.format("Ref content: %s", content);
    }
}
