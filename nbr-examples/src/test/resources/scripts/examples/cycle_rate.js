
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
    "alias" : "cycle_rate",
    "driver" : "diag",
    "cycles" : "5K",
    "threads" : "10",
    "cyclerate" : "1K",
    "op" : "noop"
};

scenario.run(activitydef);

print("current cycle = " + metrics.cycle_rate.cycles.servicetime.count);
print("mean cycle rate = " + metrics.cycle_rate.cycles.servicetime.meanRate);



