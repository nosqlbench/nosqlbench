/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

activitydef = {
    "alias" : "cycle_rate_change",
    "type" : "diag",
    "cycles" : "0..1000000",
    "threads" : "10",
    "cyclerate" : "2000",
    "interval" : "2000",
    "async" : "1000"
};

print('starting cycle_rate_change');
scenario.start(activitydef);
print('started');
print('cyclerate at 0ms:' + activities.cycle_rate_change.cyclerate);
scenario.waitMillis(1000);
activities.cycle_rate_change.cyclerate='50000';
print("measured cycle increment per second is expected to adjust to 50000");

print('cyclerate now:' + activities.cycle_rate_change.cyclerate);

var lastcount=metrics.cycle_rate_change.cycles.servicetime.count;
for(i=0;i<20;i++) {
    scenario.waitMillis(1000);
    var nextcount=metrics.cycle_rate_change.cycles.servicetime.count;
    var cycles = (nextcount - lastcount);
    print("new this second: " + (nextcount - lastcount));
    print(" waittime: " + metrics.cycle_rate_change.cycles.waittime.value);
    lastcount=nextcount;
    if (cycles>49000 && cycles<51000) {
        print("cycles adjusted, exiting on iteration " + i);
        break;
    }
}
scenario.stop(activitydef);
print('cycle_rate_change activity finished');


