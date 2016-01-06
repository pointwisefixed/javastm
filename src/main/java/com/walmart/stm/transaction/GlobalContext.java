package com.walmart.stm.transaction;

/**
 * Created by grosal3 on 1/1/16.
 */
public class GlobalContext implements Context {

    private static final GlobalContext INSTANCE = new GlobalContext();

    private GlobalContext() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated.");
        }
    }

    public static GlobalContext getInstance() {
        return INSTANCE;
    }

    public <T> T get(Ref<T> ref) {
        return ref.content.value;
    }
}
