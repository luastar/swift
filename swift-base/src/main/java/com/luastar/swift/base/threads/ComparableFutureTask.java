package com.luastar.swift.base.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 扩展{@link FutureTask},
 * 给任务增加取消以及reject操作,增加{@link java.util.concurrent.ThreadPoolExecutor}对优先级队列的支持
 */
public class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<V>>, ItfRejectable {

    private Object innerTask;

    public ComparableFutureTask(Callable<V> callable) {
        super(callable);
        this.innerTask = callable;
    }

    public ComparableFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.innerTask = runnable;
    }

    public int compareTo(ComparableFutureTask<V> o) {
        return ((Comparable) innerTask).compareTo(o.innerTask);
    }

    public void reject() {
        ((ItfRejectable) innerTask).reject();
    }

}