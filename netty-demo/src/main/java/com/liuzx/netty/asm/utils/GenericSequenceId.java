package com.liuzx.netty.asm.utils;

import java.util.concurrent.atomic.LongAdder;


public class GenericSequenceId {
    private static final LongAdder COUNTER = new LongAdder();

    public static int nextId() {
        int value = COUNTER.intValue();
        COUNTER.increment();
        return value;
    }
}
