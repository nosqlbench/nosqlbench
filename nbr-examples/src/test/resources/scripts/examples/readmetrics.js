/*
 * Copyright (c) 2022-2023 nosqlbench
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
    "alias" : "testactivity",
    "driver" : "diag",
    "cycles" : "0..1000000000",
    "threads" : "25",
    "interval" : "2000",
    "op" : "noop"
};

activity = controller.start(activitydef);
controller.waitMillis(500);
var service_timer = activity.find().topMetric("activity=testactivity,name=cycles_servicetime", Packages.io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer.class);
while (service_timer.getCount() < 1000) {
    stdout.println('waiting 10ms because cycles<10000 : ' + service_timer.getCount());
    controller.waitMillis(10);

}
controller.stop(activitydef);
stdout.println("count: " + service_timer.getCount());
