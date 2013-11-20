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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
@RunWith(MockitoJUnitRunner.class)
public class EnforceSetViewTest {
    private static final Long LONG_VALUE_1 = 1L;
    private static final Long LONG_VALUE_2 = 2L;
    private static final Integer COLLECTION_SIZE = 2;
    @Mock
    private AbstractCollectionView<Long> abstractCollectionViewMock;
    private SetView<Long> setView;

    @Before
    public void setUp() {
        setView = SetView.fromCollectionView(abstractCollectionViewMock);
    }

    @Test
    public void size_computed_correctly() {
        when(abstractCollectionViewMock.iterator()).thenReturn(Arrays.asList(LONG_VALUE_1, LONG_VALUE_2, LONG_VALUE_2, LONG_VALUE_1, LONG_VALUE_1, LONG_VALUE_2, LONG_VALUE_1, LONG_VALUE_2, LONG_VALUE_1, LONG_VALUE_1, LONG_VALUE_1).iterator());

        assertThat(setView.size(), is(equalTo(COLLECTION_SIZE)));
    }

    @Test
    public void isEmpty_delegates() {
        when(abstractCollectionViewMock.isEmpty()).thenReturn(true);

        assertThat(setView.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void contains_delegates() {
        when(abstractCollectionViewMock.contains(LONG_VALUE_1)).thenReturn(true);

        assertThat(setView.contains(LONG_VALUE_1), is(equalTo(true)));
    }

    @Test
    public void iterator_delegates() {
        when(abstractCollectionViewMock.iterator()).thenReturn(Arrays.asList(LONG_VALUE_1, LONG_VALUE_2).iterator());

        assertThat(setView, containsInAnyOrder(LONG_VALUE_1, LONG_VALUE_2));
    }

    @Test
    public void if_underlying_collection_is_a_multiset_then_this_collection_is_a_set() {
        when(abstractCollectionViewMock.iterator()).thenReturn(Arrays.asList(LONG_VALUE_1, LONG_VALUE_2, LONG_VALUE_2, LONG_VALUE_1, LONG_VALUE_1, LONG_VALUE_2, LONG_VALUE_1, LONG_VALUE_2, LONG_VALUE_1, LONG_VALUE_1, LONG_VALUE_1).iterator());

        assertThat(setView, containsInAnyOrder(LONG_VALUE_1, LONG_VALUE_2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void add_not_supported() {
        setView.add(LONG_VALUE_1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove_not_supported() {
        setView.remove(LONG_VALUE_2);
    }

    @Test
    public void when_contains_all_delegates() {
        when(abstractCollectionViewMock.containsAll(Arrays.asList(LONG_VALUE_1, LONG_VALUE_2))).thenReturn(true);

        assertThat(setView.containsAll(Arrays.asList(LONG_VALUE_1, LONG_VALUE_2)), is(equalTo(true)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove_all_unsupported() {
        setView.removeAll(Arrays.asList(LONG_VALUE_1, LONG_VALUE_2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void clear_unsupported() {
        setView.clear();
    }

}
