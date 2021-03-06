/**
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS
 * OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under
 * the License.
 */
package com.yammer.collections.azure;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/* package */
class RowMapView<R, C, V> implements Map<R, Map<C, V>> {
    private final Table<R, C, V> backingTable;
    private final Function<R, Map<C, V>> valueCreator;
    private final Function<R, Entry<R, Map<C, V>>> entryCreator;

    RowMapView(final Table<R, C, V> backingTable) {
        this.backingTable = backingTable;
        valueCreator = new Function<R, Map<C, V>>() {
            @Override
            public Map<C, V> apply(R key) {
                return backingTable.row(key);
            }
        };
        entryCreator = new

                Function<R, Entry<R, Map<C, V>>>() {
                    @Override
                    public Entry<R, Map<C, V>> apply(R input) {
                        return new RowMapViewEntry<>(backingTable, RowMapView.this, input);
                    }
                };
    }

    @Override
    public int size() {
        return backingTable.rowKeySet().size();
    }

    @Override
    public boolean isEmpty() {
        return backingTable.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return backingTable.containsRow(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Entry)) {
            return false;
        }

        Entry<?, ?> entry = (Entry<?, ?>) value;
        try {
            return backingTable.column((C) entry.getKey()).containsValue(entry.getValue());
        } catch (ClassCastException ignored) {
            return false;
        }
    }

    @Override
    public Map<C, V> get(Object key) {
        if(key == null) {
            return null;
        }
        try {
            Map<C, V> mapForRow = backingTable.row((R) key);
            return mapForRow.isEmpty() ? null : mapForRow;
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    /**
     * This methods breaks the contract in that the returned value is not the previous
     * value, but the current value. This is because this class is only a view of a remote
     * data store.
     */
    @Override
    public Map<C, V> put(R key, Map<C, V> value) {
        checkNotNull(key);
        checkNotNull(value);
        Map<C, V> oldValue = get(key);
        oldValue.clear();
        if (!value.isEmpty()) {
            backingTable.row(key).putAll(value);
        }
        return oldValue;
    }

    /**
     * This methods breaks the contract in that the returned value is not the previous
     * value, but the current value. This is because this class is only a view of a remote
     * data store.
     */
    @Override
    public Map<C, V> remove(Object key) {
        if(key == null) {
            return null;
        }

        Map<C, V> oldValue = get(key);
        oldValue.clear();
        return oldValue;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void putAll(Map<? extends R, ? extends Map<C, V>> m) {
        checkNotNull(m);
        for (Entry<? extends R, ? extends Map<C, V>> entry : m.entrySet()) {
            backingTable.row(entry.getKey()).putAll(entry.getValue());
        }
    }

    @Override
    public void clear() {
        backingTable.clear();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<R> keySet() {
        return backingTable.rowKeySet();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Collection<Map<C, V>> values() {
        return Collections2.transform(
                keySet(),
                valueCreator
        );
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Entry<R, Map<C, V>>> entrySet() {
        return SetView.fromSetCollectionView(
                Collections2.transform(
                        keySet(),
                        entryCreator
                ));
    }

    private static final class RowMapViewEntry<R, C, V> implements Entry<R, Map<C, V>> {
        private final Table<R, C, V> backingTable;
        private final Map<R, Map<C, V>> backingMap;
        private final R key;


        private RowMapViewEntry(Table<R, C, V> backingTable, Map<R, Map<C, V>> backingMap, R key) {
            this.backingTable = backingTable;
            this.backingMap = backingMap;
            this.key = key;
        }

        @Override
        public R getKey() {
            return key;
        }

        @Override
        public Map<C, V> getValue() {
            return backingTable.row(key);
        }

        @Override
        public Map<C, V> setValue(Map<C, V> value) {
            return backingMap.put(key, value);
        }
    }
}
