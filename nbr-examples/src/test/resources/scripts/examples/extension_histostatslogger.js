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
    "alias" : "testhistostatslogger",
    "driver" : "diag",
    "cycles" : "50000",
    "threads" : "5",
    "rate" : "100.0",
    "op" : "noop"
};

histostatslogger.logHistoStats("testing extention histostatslogger", ".*", "logs/histostats.csv", "0.5s");
print("started logging to logs/histostats.csv for all metrics at 1/2" +
    " second intervals.");
scenario.start(activitydef);
scenario.waitMillis(4000);
scenario.stop(activitydef);

