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

var csvlogger = csvmetrics.log("logs/csvmetricstestdir");

activitydef = {
    "alias" : "csvmetrics",
    "driver" : "diag",
    "cycles" : "50000",
    "threads" : "20",
    "op": '{"log":"interval=1000"}',
    "targetrate" : "10000.0"
};
scenario.start(activitydef);
csvlogger.add(metrics.csvmetrics.cycles.servicetime);
csvlogger.start(500,"MILLISECONDS");

scenario.waitMillis(2000);
scenario.stop(activitydef);

csvlogger.report();
