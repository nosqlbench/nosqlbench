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

var optimo = optimos.init();

optimo.param('pa', 0.0, 200000.0);
optimo.param('pb', 0.0, 200000.0);

optimo.setInitialRadius(10000.0).setStoppingRadius(0.001).setMaxEval(1000);

optimo.setObjectiveFunction(
    function (values) {
        // var arraydata = Java.from(ary);
        print("ary:" + JSON.stringify(values));

        var a = 0.0 + values.pa;
        var b = 0.0 + values.pb;

        var result = 1000000 - ((Math.abs(100 - a) + Math.abs(100 - b)));
        print("a=" + a + ",b=" + b + ", r=" + result);
        return result;
    }
);

var result = optimo.optimize();

print("optimized result was " + result);
print("map of result was " + result.getMap());

