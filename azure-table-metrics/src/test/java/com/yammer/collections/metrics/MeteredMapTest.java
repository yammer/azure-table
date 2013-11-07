package com.yammer.collections.metrics;

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
        meteredMap = new MeteredMap<>(backingMapMock);
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
