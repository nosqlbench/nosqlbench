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

// params:
// sample_seconds
// alias
//
// Naming Ideas:
// SLAPlotter, SLACharter, SLAMapper
// SLATracer SLABaser SLAWork, SLAFinder
// Thanks Chelsea and crew

function printf(args) {
    var spec = arguments[0];
    var values = [];
    for (let i = 1; i < arguments.length; i++) {
        values.push(arguments[i]);
    }
    java.lang.System.out.printf(arguments[0], values);
}

var workload = "TEMPLATE(workload,UNSPECIFIED)";
if (workload=="UNSPECIFIED") {
 print("workload was unspecified. Please set workload and try again.");
 exit;
}
printf("workload=%s\n", workload);

var sample_seconds = 600;
sample_seconds = 0 + TEMPLATE(sample_seconds,sample_seconds);
printf("sample_seconds=%d\n", sample_seconds);

var max_rate = 10000;
max_rate = 0 + TEMPLATE(max_rate,max_rate);
print("typeof(max_rate)=" + typeof (max_rate));
printf("max_rate=%d\n", max_rate);

var rate_step = 10000;
rate_step = 0 + TEMPLATE(rate_step,rate_step);
printf("rate_increment=%d\n", rate_step);

var driver = "TEMPLATE(driver,diag)"
printf("driver=%s\n",driver);


print("starting activity for stepup analysis");
var activitydef = params.withDefaults({
    'alias': 'stepup',
    'driver': driver,
    'tags':'any(block:main.*,block:main)',
    'workload' : 'TEMPLATE(workload)',
    'cycles': '1t',
    'stride': '1000',
    'striderate': '1:1.05:restart'
});

var csvlogger = csvoutput.open('stepup_metrics/stepup_rates.csv', 'time', 'workload', 'rate', 'ops')
var csvhistologger=csvmetrics.log("stepup_metrics");
csvlogger.write({'time': java.lang.System.currentTimeMillis()/1000});

scenario.start(activitydef);

printf("waiting 5 seconds for metrics warm-up...\n");
scenario.waitMillis(5000);

for (var rate = rate_step; rate < max_rate + rate_step; rate += rate_step) {
    if (rate > max_rate) {
        rate = max_rate;
    }

    print("rate=" + rate);

    activities.stepup.striderate = "" + (rate / 1000.0)+":1.05:restart";
    print("sampling performance for (sample_seconds=" + params.sample_seconds + ")...");

    var precount = metrics.stepup.cycles.servicetime.count
    var waitmillis = sample_seconds * 1000;
    print("waitmillis=" + waitmillis);
    scenario.waitMillis("" + waitmillis);
    var postcount = metrics.stepup.cycles.servicetime.count
    var count = postcount - precount;
    var ops_per_second = count / params.sample_seconds;
    printf("ops_per_second = %f (%f / %f)\n", ops_per_second, count, sample_seconds);
    csvlogger.write({
        'time': java.lang.System.currentTimeMillis()/1000,
        'workload': activitydef.workload,
        'rate': rate,
        'ops': ops_per_second
    });
    csvhistologger.report();
}

print("stopping activity after stepup analysis\n");
scenario.stop("stepup")

