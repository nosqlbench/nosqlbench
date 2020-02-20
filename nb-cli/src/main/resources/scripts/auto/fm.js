
function printa(arg) { java.lang.System.out.print(arg); }
//function println(arg) { java.lang.System.out.println(arg); }


if ("TEMPLATE(showhelp,false)" === "true") {
    var helpdata = files.read("docs/findmax.md");
    print(helpdata);
    exit();
}

if (params.cycles) {
    print("cycles should not be set for this scenario script. Other exit condition take precedence.");
    exit();
}

print("Performing FindMax analysis with the following parameters:");

var sample_seconds = TEMPLATE(sample_seconds,10);
print(" sample_seconds="+sample_seconds);

var latency_percentile = TEMPLATE(latency_percentile,0.99);
print(" latency_percentile="+latency_percentile);

var latency_threshold =TEMPLATE(latency_threshold,50.0);
print(" latency_threshold="+latency_threshold);

var minimum_ratio =TEMPLATE(minimum_ratio,0.8);
print(" minimum_ratio="+minimum_ratio);

var averageof =TEMPLATE(averageof,3);
print(" averageof="+averageof);


var initial_base = 0;     // ops_s baseline
var initial_step = 1000;  // how much to start adding to baseline initially or when baseline moves
var step_growth = 2;      // how quickly to increase the step from the previous step

var baserate_gauge = scriptingmetrics.newGauge("findmax.base_rate", initial_base + 0.0);

var rategauge = scriptingmetrics.newGauge("findmax.achieved_rate", 0.0);
var baserate = scriptingmetrics.newGauge("findmax.base_rate", 0.0);

//  CREATE SCHEMA

schema_activitydef = params.withDefaults({
    type: "diag"
});
schema_activitydef.alias="findmax_schema";
schema_activitydef.threads="1";
schema_activitydef.tags="TEMPLATE(schematags,phase:schema)";
print("Creating schema with schematags:" + schema_activitydef.tags);

scenario.run(schema_activitydef);

//  START ITERATING ACTIVITY


activitydef = params.withDefaults({
    type: "diag",
    threads: "50"
});
activitydef.alias="findmax";
activitydef.cycles="1000000000";
activitydef.recycles="1000000000";
activitydef.tags="TEMPLATE(maintags,phase:main)";
print("Iterating main workload with tags:" + activitydef.tags);


function ops_s(iteration, results) {
    return results[iteration].ops_per_second.toFixed(2);
}
function achieved_ratio(iteration, results) {
    return results[iteration].achieved_ratio;
}
function p_ms(iteration, results, pctile) {
    return results[iteration].latency_snapshot.getValue(pctile) / 1000000.0;
}
// function p99ms(iteration, results) {
//     return results[iteration].latency_snapshot.p99ms
// }
function target_rate(iteration, results) {
    return results[iteration].target_rate;
}

function lowest_unacceptable_of(result1, result2) {
    return result1.target_rate < result2.target_rate ? result1 : result2;
}

scenario.start(activitydef);

printa("\nwarming up client JIT for 10 seconds... ");
activities.findmax.cyclerate = "1000:1.1:restart";
scenario.waitMillis(10000);
activities.findmax.cyclerate = "100:1.1:restart";
print("commencing test");

function testCycleFun(params) {

    var rate_summary = "targeting " + params.target_rate + " ops_s (" + params.base + "+" + params.step +")";
    print("\n >-- iteration " + params.iteration + " ---> " + rate_summary + " for " + params.sample_seconds + "s");
    var result = {};

    // print(" target rate is " + params.target_rate + " ops_s");
    activities.findmax.cyclerate = ""+params.target_rate+":1.1:restart";

    precount = metrics.findmax.cycles.servicetime.count;

    var snapshot_reader = metrics.findmax.result.deltaReader;
    var old_data = snapshot_reader.getDeltaSnapshot(); // reset

    // print(" sampling performance for " + params.sample_seconds + " seconds...");
    scenario.waitMillis(params.sample_seconds * 1000);

    postcount = metrics.findmax.cycles.servicetime.count;

    var count = postcount - precount;
    var ops_per_second = count / params.sample_seconds;
    rategauge.update(ops_per_second);
    var latency_snapshot = snapshot_reader.getDeltaSnapshot(10000);

    return {
        target_rate: params.target_rate,
        count: postcount - precount,
        ops_per_second: ops_per_second,
        latency_snapshot: latency_snapshot,
        achieved_ratio: (count / params.sample_seconds) / params.target_rate,
        latency_ms: latency_snapshot.getValue(latency_percentile) / 1000000.0
    };

}

function highest_acceptable_of(results, iteration1, iteration2) {
    if (iteration1 == 0) {
        return iteration2;
    }
    return results[iteration1].ops_per_second > results[iteration2].ops_per_second ? iteration1 : iteration2;
}

function find_max() {

    var results = [];

    var iteration = 0;
    var highest_acceptable_iteration = 0;
    var lowest_unacceptable_iteration = 0;

    var base = initial_base;
    baserate_gauge.update(base);
    var step = initial_step;

    while (true) {
        iteration++;
        // print("\niteration #" + iteration);

        var params = {
            iteration: iteration,
            target_rate: (base + step),
            base: base,
            step: step,
            sample_seconds: sample_seconds
        };

        step = step * step_growth;
        var result = results[iteration] = testCycleFun(params);

        var failed_checks = 0;

        // latency check
        if (result.percentile_ms > latency_threshold) {
            failed_checks++;
            printa(" ✘(TOO HIGH)")
        } else {
            printa(" ✔(OK)")
        }
        print(" p" + (latency_percentile * 100.0) + " " + result.latency_ms + "ms [<max " + latency_threshold + "ms]");

        // throughput check
        if (result.achieved_ratio < minimum_ratio) {
            failed_checks++;
            printa(" ✘(TOO LOW)");
        } else {
            printa(" ✔(OK)")
        }
        print(" " + result.ops_per_second + " ops_s / " + params.target_rate + " = " + (result.achieved_ratio * 100.0).toFixed(2) + "%"
            +" [>min " + (minimum_ratio * 100.0).toFixed(2) + "%]"
        );

        // speedup check
        var speedup = (iteration==1 ? 0.0 : result.ops_per_second - results[highest_acceptable_iteration].ops_per_second);

        if (speedup < 0.0) {
            failed_checks++;
            printa(" ✘(SLOWER)")
        } else {
            printa(" ✔(OK)")
        }
        print(" speedup " + speedup.toFixed(2) + " [>min 0.0]");

        if (failed_checks == 0) {
            highest_acceptable_iteration = highest_acceptable_of(results, highest_acceptable_iteration, iteration);
            print(" ---> accepting iteration " + highest_acceptable_iteration);
            continue;
        } else {
            print("\n !!!   rejecting iteration " + iteration);

            if (lowest_unacceptable_iteration == 0) {
                lowest_unacceptable_iteration = iteration;
            } else {
                // keep the more conservative target rate, if both this iteration and another one was not acceptable
                lowest_unacceptable_iteration = result.target_rate < results[lowest_unacceptable_iteration].target_rate ?
                    iteration : lowest_unacceptable_iteration;
            }
            highest_acceptable_iteration = highest_acceptable_iteration == 0 ? iteration : highest_acceptable_iteration;

            print(" target search range is [[ ✔ "
                + results[highest_acceptable_iteration].target_rate
                + "(i"+highest_acceptable_iteration+ ") , ✘ "
                + results[lowest_unacceptable_iteration].target_rate
                + "(i"+lowest_unacceptable_iteration+")"
            + " ))");

            // print(
            //     " ✔ i" + highest_acceptable_iteration + "(" +
            //     results[highest_acceptable_iteration].ops_per_second + " of target=" +
            //     results[highest_acceptable_iteration].target_rate + ") <- highest acceptable iteration");
            //
            // print(" ✘ i" + lowest_unacceptable_iteration + "(" +
            //     results[lowest_unacceptable_iteration].ops_per_second + " of target=" +
            //     results[lowest_unacceptable_iteration].target_rate +") <- lowest unacceptable iteration");

            var too_fast = results[lowest_unacceptable_iteration].target_rate;

            if (base + step >= too_fast && results[highest_acceptable_iteration].target_rate + initial_step < too_fast) {
                base = results[highest_acceptable_iteration].target_rate;
                baserate_gauge.update(base);
                step = initial_step;

                print(" continuing search at base:" + base + " step:" + step);
                activities.findmax.cyclerate = ""+base+":1.1:restart";

                print("\n settling load at base for + " + sample_seconds + "s before active sampling");
                scenario.waitMillis(sample_seconds * 1000);


            } else {
                print("\n search complete");
                print(" MAX= " + ops_s(highest_acceptable_iteration, results) + " ops_s"
                    + " @ " +
                    (p_ms(highest_acceptable_iteration, results, latency_percentile)*100.0)
                    + "ms p"+
                    (latency_percentile*100.0)+" latency");
                break;
            }
        }
    }
    return {
        ops_s: ops_s(highest_acceptable_iteration, results),
        target_rate: target_rate(highest_acceptable_iteration, results),
        latency_ms: p_ms(highest_acceptable_iteration, results,latency_percentile)
    };

}

overallresults = [];
for (var testrun = 0; testrun < averageof; testrun++) {
    overallresults[testrun] = find_max();
}

scenario.stop(activitydef);

var total_ops_s = 0.0;
var total_latency = 0.0;

overallresults.forEach(
    function (x) {
        print("\ntest run:");
        print("ops_s:       " + x.ops_s);
        print("p"+latency_percentile+"ms:       " + x.latency_ms);
        print("target_rate: " + x.target_rate);
        total_ops_s += x.ops_s;
        total_latency += x.latency_ms;

    }
);
var average_rate = total_ops_s / (averageof + 0.0);
var average_latency = total_latency / (averageof + 0.0);

print("RESULTS:");
print("averaging " + averageof + " runs, the maximum throughput of this workload");
print("at or below " + latency_threshold + "ms and");
print("at or above " + minimum_ratio + " of target rate achieved, is");
print(average_rate + " ops_s, with an average latency of " + average_latency);
//scenario.reportMetrics();
