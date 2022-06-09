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

co_cycle_delay = {
    "alias" : "co_cycle_delay",
    "driver" : "diag",
    "cycles" : "0..10000",
    "threads" : "1",
    "cyclerate" : "1000,1.0",
    "op" : "diagrate:diagrate=800"
};

print('starting activity co_cycle_delay');
scenario.start(co_cycle_delay);
scenario.waitMillis(4000);
print('step1 cycles.waittime=' + metrics.co_cycle_delay.cycles.waittime.value);
activities.co_cycle_delay.diagrate="10000";
for(i=0;i<5;i++) {
    if (! scenario.isRunningActivity('co_cycle_delay')) {
        print("scenario exited prematurely, aborting.");
        break;
    }
    print("iteration " + i + " waittime now " + metrics.co_cycle_delay.cycles.waittime.value);
    scenario.waitMillis(1000);
}


//scenario.awaitActivity("co_cycle_delay");
print('step2 cycles.waittime=' + metrics.co_cycle_delay.cycles.waittime.value);
print("awaited activity");
