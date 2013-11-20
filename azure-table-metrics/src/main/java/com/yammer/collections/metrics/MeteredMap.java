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
package com.yammer.collections.metrics;

import com.google.common.collect.ForwardingMap;
import com.yammer.metrics.core.TimerContext;

import java.util.Map;

import static com.yammer.collections.metrics.Timers.GET_TIMER;
import static com.yammer.collections.metrics.Timers.PUT_TIMER;
import static com.yammer.collections.metrics.Timers.REMOVE_TIMER;

class MeteredMap<K, V> extends ForwardingMap<K, V> {
    private final Map<K, V> backingMap;

    MeteredMap(Map<K, V> backingMap) {
        this.backingMap = backingMap;
    }

    @Override
    protected Map<K, V> delegate() {
        return backingMap;
    }

    @Override
    public V remove(Object object) {
        TimerContext ctx = REMOVE_TIMER.time();
        try {
            return backingMap.remove(object);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public V get(Object key) {
        TimerContext ctx = GET_TIMER.time();
        try {
            return backingMap.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public V put(K key, V value) {
        TimerContext ctx = PUT_TIMER.time();
        try {
            return backingMap.put(key, value);
        } finally {
            ctx.stop();
        }
    }
}
