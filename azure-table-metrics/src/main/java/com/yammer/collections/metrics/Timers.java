package com.yammer.collections.metrics;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

class Timers {
    static final Timer GET_TIMER = createTimerFor("get");
    static final Timer PUT_TIMER = createTimerFor("put");
    static final Timer REMOVE_TIMER = createTimerFor("remove");

    private static Timer createTimerFor(String name) {
        return Metrics.newTimer(MeteredTable.class, name);
    }

}
