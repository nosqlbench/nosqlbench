// driver
// workload
// sample_seconds
// min_rate
// rate_step
// max_rate

function printf(args) {
    var spec = arguments[0];
    var values = [];
    for (let i = 1; i < arguments.length; i++) {
        values.push(arguments[i]);
    }
    java.lang.System.out.printf(arguments[0], values);
}

var sample_seconds = 600;
sample_seconds = 0 + TEMPLATE(sample_seconds,sample_seconds);
printf("sample_seconds=%d\n", sample_seconds);

var min_rate = 1;
min_rate = 0 + TEMPLATE(min_rate,min_rate);
var max_rate = 10000;
max_rate = 0 + TEMPLATE(max_rate,max_rate);
var rate_step = 10000;
rate_step = 0 + TEMPLATE(rate_step,rate_step);
printf("rate (min,step,max) = (%d,%d,%d)\n");

var driver = "TEMPLATE(driver,diag)"
var workload = "TEMPLATE(workload,none)"
printf("driver=%s workload=%s\n",driver,workload);

printf("starting activity for stepup analysis...\n");
var activitydef = params.withDefaults({
    'alias': 'stepup',
    'driver': driver,
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
