// Steps to add an independent variable to this algorithm:
// 1. Add it to the test rigging function 'objective'
// 2. Add the bounds
// 3. Add the initial parameters

let TEMPLATE = function () {
};

function printf(args) {
    let spec = arguments[0];
    let values = [];
    for (let i = 1; i < arguments.length; i++) {
        values.push(arguments[i]);
    }
    // print("values:" + JSON.stringify(values,null,2))
    java.lang.System.out.printf(arguments[0], values);
}

function as_js(ref) {
    if (JSON.stringify(ref)) {
        return ref;
    }
    if (ref instanceof java.util.List) {
        return Java.asJSONCompatible(Java.from(ref));
    }
    if (ref instanceof java.util.Map) {
        let newobj = {};
        for each(key in ref.keySet()) {
            newobj[key] = Java.asJSONCompatible(ref.get(key));
        }
        return newobj;
    }
    print("ERROR: Unknown non-js type for conversion:" + ref);
    exit();
}

function summary(ref) {

    let s = "";
    let d = "";

    let obj = as_js(ref);
    Object.keys(obj).forEach(function (key) {
        s += d + key + "=" + obj[key];
        d = ",";
    });
    return s;
}

//ScriptObjectMirror json = (ScriptObjectMirror) engine.eval("JSON");
//function stringify(object) {
// return json.callMember("stringify", object);

//function println(arg) { java.lang.System.out.println(arg); }

if (params.cycles) {
    print("cycles should not be set for this scenario script. " +
        "Other exit condition take precedence.");
    exit();
}

let vars = 'TEMPLATE(vars,vars)';

let sample_time = 10;    // start sampling at 10 second windows
let sample_incr = 1.6181;  // sampling window is fibonacci
let sample_max = 300;    // 5 minutes is the max sampling window
sample_time = TEMPLATE(sample_time, sample_time);
sample_incr = 0.0 + TEMPLATE(sample_incr, sample_incr);
sample_max = TEMPLATE(sample_max, sample_max);

printf("typeof sample_time:%s\n", typeof (sample_time));
printf("typeof sample_incr:%s\n", typeof (sample_incr));
printf("typeof sample_max:%s\n", typeof (sample_max));

printf("  sample_time=%f sample_incr=%.2f sample_max=%f", sample_time, sample_incr, sample_max);

let min_stride = 100;
min_stride = TEMPLATE(min_stride, min_stride);
printf(" min_stride=%d\n", min_stride);

let averageof = 1;
averageof = TEMPLATE(averageof, averageof);
printf("  averageof=%d\n", averageof);

let latency_cutoff = 50.0;
let latency_pctile = 0.99;

latency_cutoff = 0.0 + TEMPLATE(latency_cutoff, latency_cutoff);
latency_pctile = 0.0 + TEMPLATE(latency_pctile, latency_pctile);
printf(" latency_cutoff=%.2f latency_pctile=%.2f\n", latency_cutoff, latency_pctile);

print("```");
print("    Performing BOBYQA analysis with the following parameters:");

print("    Scale sample window between " + sample_time + " and " + sample_max);
print("     scaled by " + sample_incr + " inverse step variance");
print("    Schedule " + min_stride + " operations at a time per thread.");
print("    Report the average result of " + averageof + " searches.");
print("```");

if (latency_pctile > 1.0) {
    latency_pctile = (latency_pctile * 0.01);
}

let activity_type = "TEMPLATE(activity_type,cql)";
let yaml_file = "TEMPLATE(yaml_file,cql-iot)";

//  CREATE SCHEMA
schema_activitydef = params.withDefaults({
    type: activity_type,
    yaml: yaml_file
});
schema_activitydef.alias = "optimo_schema";
schema_activitydef.threads = "1";
schema_activitydef.tags = "TEMPLATE(schematags,phase:schema)";
schema_activitydef.speculative = "none"
print("Creating schema with schematags:" + schema_activitydef.tags);

scenario.run(schema_activitydef);

//  START ITERATING ACTIVITY
activitydef = params.withDefaults({
    type: activity_type,
    yaml: yaml_file,
    threads: "50",
    async: 10,
    speculative: "none"
});
activitydef.alias = "optimo";
activitydef.cycles = "1000000000";
activitydef.recycles = "1000000000";
activitydef.tags = "TEMPLATE(maintags,phase:main)";
activitydef.speculative = "none"

print("Iterating main workload with tags:" + activitydef.tags);


scenario.start(activitydef);
raise_to_min_stride(activities.optimo, min_stride);

//printa("\nwarming up client JIT for 10 seconds... ");
////activities.optimo.cyclerate = "1000:1.1:restart";
//activities.optimo.striderate = ""+(1000.0/activities.optimo.stride)+":1.1:restart";
//scenario.waitMillis(10000);
//activities.optimo.striderate = ""+(100.0/activities.optimo.stride)+":1.1:restart";
print("commencing test");


function filter_params(fp) {
    // print("filtering fp:" + JSON.stringify(fp,null,2))
    fp = as_js(fp);
//  print("PREFILTER summary: " + summary(fp));
    if (fp.async) {
//    print("setting async");
        fp.async = 0.0 + fp.async;
        if (fp.async.toFixed) fp.async = fp.async.toFixed(0);
    }
    if (fp.threads) {
//    print("setting threads");
        fp.threads = 0.0 + fp.threads;
        if (fp.threads.toFixed) fp.threads = fp.threads.toFixed(0);
    }
    if (fp.ccph) {
//    print("setting ccph");
        fp.ccph = 0.0 + fp.ccph;
        if (fp.ccph.toFixed) fp.ccph = fp.ccph.toFixed(0);
    }
//  print("POSTFILTER summary: " + summary(fp));
    return fp;
}

let iteration = 0;

function test_params(params, sample_ms) {

    // print("into testing phase with params:" + JSON.stringify(params,null,2))

    if (params.target_rate) {
        activities.optimo.striderate = "" + (params.target_rate / activities.optimo.stride) + ":1.1:restart";
    } else {
        // activities.optimo.striderate= ""+(1000000/activities.optimo.stride)+":1.1:restart";
    }

    // TODO: This is a CQL-only parametr. It will not affect others, but silently discarding it
    // is not helpful
    if (params.ccph) { // set both core and max to the same value
        activities.optimo.pooling = "" + params.ccph + ":" + params.ccph + ":10000"
    }

    if (params.threads) {
        activities.optimo.threads = params.threads;
    }

    if (params.async) {
        activities.optimo.async = params.async;
    }


    // commence sampling window
    let precount = metrics.optimo.cycles.servicetime.count;
    // print("commencing sampling with precount=" + precount)

    let snapshot_reader = metrics.optimo.result.deltaReader;
    let old_data = snapshot_reader.getDeltaSnapshot(); // reset

    // print("waiting " + sample_ms + " ms")
    scenario.waitMillis(sample_ms);
    let postcount = metrics.optimo.cycles.servicetime.count;

    // print("sampling window over with postcount=" + postcount)

    let count = postcount - precount;
    let ops_per_second = count / (sample_ms / 1000.0);
    let latency_snapshot = snapshot_reader.getDeltaSnapshot(10000);
    let latency_ms = latency_snapshot.getValue(latency_pctile) / 1000000.0;

    result = {
        sample_ms: sample_ms,
        count: count,
        ops_per_second: ops_per_second,
        latency_ms: latency_ms,
//        latency_snapshot: latency_snapshot,
//        achieved_ratio: achieved_ratio,
        latency_pctile: latency_pctile,
        iteration: iteration++
    };
    return result;
}

results_array = [];

function valueof_result(result) {
    let value = result.ops_per_second;

    if (latency_pctile > 0.0) {
        let headroom = latency_cutoff - result.latency_ms;
        let factor1 = (headroom < 0) ? 0.0 : 1.0;

        // if "no dice", we at least indicate that lower latency is better
        if (factor1 == 0) {
            factor1 = -Math.tanh(headroom / 1000)
        }
        value *= factor1;
    }
    return value;
}

function optimo() {
    let startAt = new Date().getTime();

    function objective(p) {
        print("----------\nPARAMS:" + JSON.stringify(p, null, 2));
        p = filter_params(p);
        // print("\n filtered PARAMS:" + JSON.stringify(p,null,2));

        let result = test_params(p, sample_time * 1000);
        let value = valueof_result(result);
        result.value = value;
        results_array.push(result);

        print("RESULT: " + JSON.stringify(result, null, 2));
        // print("VALUE => " + result.value);
        return result.value;
    }

    print("initializing...");
    let optimo = optimos.init();
    parseVars(optimo, vars);
//  optimo.param('threads',1,100);
//  optimo.param('async',1,100000);

    optimo.setMaxPoints();
    optimo.setInitialRadius(1000000.0).setStoppingRadius(0.1);
    optimo.setMaxEval(1000);
    optimo.setObjectiveFunction(objective);

    let result = optimo.optimize();
    print("OPTIMAL RESULT: " + JSON.stringify(result.vars, null, 2));

    let endAt = new Date().getTime();
    let total_seconds = (endAt - startAt) / 1000.0;

    return {
//    target_async: target_async,
        latency_ms: results_array[results_array.length - 1].latency_ms,
        ops_per_second: results_array[results_array.length - 1].ops_per_second,
        total_seconds: total_seconds,
        result: result.map
    };

}

function parseVars(optimo, vars) {
    let varspec = vars.split(",");
    if (varspec[0] == "") {
        print("ERROR: at least one variable is required.");
        print("Consider using one of these:");
        print("vars=async:100:1000 threads=50");
        print("vars=threads:1:100,async:100:1000");
        print("NOTE: any let not under control of optimo will need to be set appropriately.");
        exit();
    }
    varspec.forEach(function (spec) {
        let parts = spec.split(':');
        if (parts.length != 3) {
            print("ERROR name, min, max are required for an optimo variable, but only saw " + spec);
            exit();
        }
        let name = parts[0];
        let min = Number(parts[1])
        let max = Number(parts[2])

        optimo.param(name, min, max);
        printf("added independent variable %s\n", spec)
    });
}


let overallresults = [];

for (let testrun = 1; testrun <= averageof; testrun++) {
    print("starting run " + testrun + " of " + averageof)
    overallresults[testrun] = optimo();
    printf("results for run %f: %s\n", testrun, overallresults[testrun])
    print("completed run " + testrun + " of " + averageof)
}

print("stopping activity: " + JSON.stringify(activitydef, null, 2))
scenario.stop(activitydef);


let total_ops_s = 0.0;
let total_latency = 0.0;
let total_seconds = 0.0;

overallresults.forEach(
    function (x) {
        total_ops_s += x.ops_per_second;
        total_latency += x.latency_ms;
        total_seconds += x.total_seconds;
    }
);
let average_rate = total_ops_s / (averageof + 0.0);
let average_latency = total_latency / (averageof + 0.0);


print("\n ANALYSIS COMPLETE in " + total_seconds + " seconds");
printf("  THRESHOLDS");
printf(" latency:[p" + (latency_pctile * 100.0) + "%%<" + latency_cutoff + "ms]");
print("  AVGOF(" + averageof + ") MAX= " + average_rate.toFixed(2) +
    " ops_s @ p" + (latency_pctile * 100.0) + "% of " + average_latency + "ms");


function add_var(vars, params, varname, min, max) {
    if (params[varname]) {
        vars.push({
            'varname': varname,
            'min': min,
            'max': max
        });
    }
}

function raise_to_min_stride(params, min_stride) {
    let multiplier = (min_stride / params.stride);
    let newstride = (multiplier * params.stride);
    print("increasing stride to " + newstride + ", (" + multiplier + "x initial)");
    params.stride = newstride.toFixed(0);
}

// async, ops_s
//scenario.reportMetrics();
