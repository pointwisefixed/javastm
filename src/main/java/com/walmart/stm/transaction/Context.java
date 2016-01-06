package com.walmart.stm.transaction;

/**
 * Created by grosal3 on 1/1/16.
 */
public interface Context {
    <T> T get(Ref<T> tRef);
}
