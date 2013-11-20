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
