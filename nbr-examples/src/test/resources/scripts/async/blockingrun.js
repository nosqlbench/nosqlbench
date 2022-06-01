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
    "alias" : "blockingactivity1",
    "type" : "diag",
    "cycles" : "0..100000",
    "threads" : "1",
    "interval" : "2000",
    "async" : "1000"
};
activitydef2 = {
    "alias" : "blockingactivity2",
    "type" : "diag",
    "cycles" : "0..100000",
    "threads" : "1",
    "interval" : "2000",
    "async" : "1000"
};


print('running blockingactivity1');
scenario.run(10000,activitydef1);
print('blockingactivity1 finished');
print('running blockingactivity2');
scenario.run(10000,activitydef2);
print('blockingactivity2 finished');
