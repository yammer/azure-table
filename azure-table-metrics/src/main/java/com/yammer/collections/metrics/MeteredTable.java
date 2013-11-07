package com.yammer.collections.metrics;

import com.google.common.collect.ForwardingTable;
import com.google.common.collect.Table;
import com.yammer.metrics.core.TimerContext;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yammer.collections.metrics.Timers.GET_TIMER;
import static com.yammer.collections.metrics.Timers.PUT_TIMER;
import static com.yammer.collections.metrics.Timers.REMOVE_TIMER;

/**
 * Provides metrics for the core operations, but not for the collections view operations.
 * Operations include: get, put, remove, size, clear
 *
 * @param <R>
 * @param <C>
 * @param <V>
 */
@SuppressWarnings("ClassWithTooManyMethods")
public class MeteredTable<R, C, V> extends ForwardingTable<R, C, V> {
    private final Table<R, C, V> backingTable;

    private MeteredTable(Table<R, C, V> backingTable) {
        this.backingTable = checkNotNull(backingTable);
    }

    public static <R, C, V> Table<R, C, V> create(Table<R, C, V> backingTable) {
        return new MeteredTable<>(backingTable);
    }

    @Override
    public V get(Object o, Object o2) {
        TimerContext ctx = GET_TIMER.time();
        try {
            return backingTable.get(o, o2);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public V put(R r, C c, V v) {
        TimerContext ctx = PUT_TIMER.time();
        try {
            return backingTable.put(r, c, v);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public V remove(Object o, Object o2) {
        TimerContext ctx = REMOVE_TIMER.time();
        try {
            return backingTable.remove(o, o2);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Map<C, V> row(R r) {
        return new MeteredMap<>(backingTable.row(r));
    }

    @Override
    public Map<R, V> column(C c) {
        return new MeteredMap<>(backingTable.column(c));
    }

    @Override
    protected Table<R, C, V> delegate() {
        return backingTable;
    }
}
