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

import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
@RunWith(MockitoJUnitRunner.class)
public class MeteredMapTest {
    private static final Float KEY = 1.1f;
    private static final Integer VALUE = 22;
    private static final Integer OTHER_VALUE = 33;
    @Mock
    private Map<Float,Integer> backingMapMock;
    private MeteredMap<Float, Integer> meteredMap;

    @Before
    public void setUp() {
        meteredMap = new MeteredMap<>(backingMapMock, new MetricRegistry());
    }

    @Test
    public void remove_delegates_to_backing_table() {
        when(backingMapMock.remove(KEY)).thenReturn(VALUE);

        assertThat(meteredMap.remove(KEY), is(equalTo(VALUE)));
    }

    @Test
    public void put_delegates_to_backing_table() {
        when(backingMapMock.put(KEY, VALUE)).thenReturn(OTHER_VALUE);

        assertThat(meteredMap.put(KEY, VALUE), is(equalTo(OTHER_VALUE)));
    }

    @Test
    public void removed_delegates_to_backing_table() {
        when(backingMapMock.get(KEY)).thenReturn(VALUE);

        assertThat(meteredMap.get(KEY), is(equalTo(VALUE)));
    }

}
