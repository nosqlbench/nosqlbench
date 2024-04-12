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

print("params from command line:");
print(params);
print('before: params["three"]:' + params["three"]);
print('before: params.three:' + params.three);

var overrides = {
  'three': "undef",
};

print("params.three after overriding with three:UNDEF");
params = params.withOverrides({'three':'UNDEF'});
print(params);
print('after: params["three"]:' + params["three"]);
print('after: params.three:' + params.three);


