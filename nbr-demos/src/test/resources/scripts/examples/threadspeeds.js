/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

activitydef = {
    "alias" : "threadspeeds",
    "driver" : "diag",
    "cycles" : "0..4000000000000",
    "threads" : "1",
    "interval" : "10000",
    "targetrate" : "1000000",
    "op": "noop"
};
scenario.start(activitydef);

var speeds = [];
var latencies = [];

function aTestCycle(threads, index) {
    activities.threadspeeds.threads = threads; // dynamically adjust the active threads for this activity
    scenario.waitMillis(60000);                // wait for 1 minute to get a clean 1 minute average speed
//     scenario.waitMillis(5000);                // wait for 1 minute to get a clean 1 minute average speed
    speeds[threads]= metrics.threadspeeds.cycles.oneMinuteRate; // record 1 minute avg speed
    print("speeds:" + speeds.toString());

}

var min=1;
var max=256;
var mid=Math.floor((min+max)/2)|0;

[min,mid,max].forEach(aTestCycle);

while (min<mid && mid<max && scenario.isRunningActivity(activitydef)) {
    print("speeds:" + speeds.toString());
    if (speeds[min]<speeds[max]) {
        min=mid;
        mid=Math.floor((mid+max) / 2)|0;
    } else {
        max=mid;
        mid=Math.floor((min+mid) / 2)|0;
    }
    aTestCycle(mid);
}
print("The optimum number of threads is " + mid + ", with " + speeds[mid] + " cps");
var targetrate = (speeds[mid] * .7)|0;
print("Measuring latency with threads=" + mid + " and targetrate=" + targetrate);

activities.threadspeeds.threads = mid;
activities.threadspeeds.targetrate = targetrate;
scenario.waitMillis(60000);


scenario.stop(threadspeeds);


