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

stride_rate = {
    "alias" : "stride_rate",
    "type" : "diag",
    "cycles" : "0..12500",
    "stride" : "1",
    "threads" : "5",
    "striderate" : "2500",
    "interval" : "1000",
    "async" : 1000
};

print('running stride_rate');
scenario.run(10000,stride_rate);
print('stride_rate finished');

print("metrics.stride_rate.strides.servicetime.meanRate = " + metrics.stride_rate.strides.servicetime.meanRate);

