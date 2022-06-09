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

activitydef1 = {
    "alias" : "erroring_activity_init",
    "driver" : "diag",
    "cycles" : "0..1500000",
    "threads" : "1",
    "targetrate" : "500",
    "unknown_config" : "unparsable",
    "op" : "noop"
};

print('starting activity erroring_activity_init');
scenario.start(activitydef1);
scenario.waitMillis(2000);
scenario.awaitActivity("erroring_activity_init");
print("awaited activity");
