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

//logger.info("testing dynamically changing threads.");
activitydef = {
    "alias" : "speedcheck",
    "type" : "diag",
    "cycles" : "50000",
    "threads" : "20",
    "interval" : "2000",
    "targetrate" : "10000.0"
};

print("running 5E5 cycles at 1E5 cps ~~ 5 seconds worth");
scenario.start(activitydef);

while (scenario.isRunningActivity(activitydef)) {
    achievedRate = metrics.speedcheck.cycles.servicetime.meanRate;
    currentCycle = metrics.speedcheck.cycles.servicetime.count;
    print("currentCycle = " + currentCycle + ", mean rate = " + achievedRate);
    scenario.waitMillis(1000);
}

achievedRate = metrics.speedcheck.cycles.servicetime.meanRate;
currentCycle = metrics.speedcheck.cycles.servicetime.count;
print("last update - currentCycle = " + currentCycle + ", mean rate = " + achievedRate);


scenario.stop(activitydef);
//print('stopped scenario');


