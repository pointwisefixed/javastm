package com.walmart.stm.transaction;

/**
 * Created by grosal3 on 1/1/16.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class Transaction implements Context {

    private HashMap<Ref, Object> inTxMap = new HashMap<>();
    private HashSet<Ref> toUpdate = new HashSet<>();
    private long revision;
    private static AtomicLong transactionNum = new AtomicLong(0);
    private Map<Ref, Long> version = new HashMap<>();

    public Transaction() {
        revision = transactionNum.incrementAndGet();
    }

    public <T> T get(Ref<T> ref) {
        if (!inTxMap.containsKey(ref)) {
            RefTuple<T, Long> tuple = ref.content;
            inTxMap.put(ref, tuple.value);
            if (!version.containsKey(ref)) {
                version.put(ref, tuple.revision);
            }
        }
        return (T) inTxMap.get(ref);
    }

    public <T> void set(Ref<T> ref, T value) {
        inTxMap.put(ref, value);
        toUpdate.add(ref);
        if (!version.containsKey(ref)) {
            RefTuple<T, Long> tuple = ref.content;
            version.put(ref, tuple.revision);
        }
    }

    public boolean commit() {
        synchronized (STM.commitLock) {
            boolean isValid = true;
            for (Ref ref : inTxMap.keySet()) {
                if (ref.content.revision != version.get(ref)) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                for (Ref ref : toUpdate) {
                    ref.content = RefTuple.get(inTxMap.get(ref), revision);
                }
            }
            return isValid;
        }
    }

}
