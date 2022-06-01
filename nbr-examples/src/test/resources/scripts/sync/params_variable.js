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

print('params.get("one")=\'' + params.get("one") + "'");
print('params.get("three")=\'' + params.get("three") + "'");
print('params.size()=' + params.size());

var overrides = {
  'three': "five"
};
print('params.get("three") [overridden-three-five]=\'' + params.withOverrides(overrides).get("three") + "'");
var defaults = {
    'four': "niner"
};
print('params.get("four") [defaulted-four-niner]=\'' + params.withDefaults(defaults).get("four") + "'");

