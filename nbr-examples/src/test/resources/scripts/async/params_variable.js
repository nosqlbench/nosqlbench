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

// With the API support for Polyglot, we lost the ability to expose
// map operations (underlying Java Map API) as well as proxy methods
// on ECMA objects. What works now is object semantics + non-map methods.
// More testing and refinement may be needed to clarify the rules here.

// previously:
// print('params.get("one")=\'' + params.get("one") + "'"); // worked with nashorn
// print('params.get("three")=\'' + params.get("three") + "'");
// print('params.size()=' + params.size());

// Called with one=two three=four

print('params["one"]=\'' + params["one"] + "'");
print('params["three"]=\'' + params["three"] + "'");

var overrides = {
  'three': "five"
};

var overridden = params.withOverrides(overrides);

print('overridden["three"] [overridden-three-five]=\'' + overridden["three"] + "'");

var defaults = {
    'four': "niner"
};

var defaulted = params.withDefaults(defaults);

print('defaulted.get["four"] [defaulted-four-niner]=\'' + defaulted["four"] + "'");

