function printf(args) {
    var spec = arguments[0];
    var values = [];
    for (let i = 1; i < arguments.length; i++) {
        values.push(arguments[i]);
    }
    java.lang.System.out.printf(arguments[0],values);
}

if ("TEMPLATE(showhelp,false)" === "true") {
    var helpdata = files.read("docs/findmax.md");
    printf(helpdata);
    exit();
}

if (params.cycles) {
    printf("cycles should not be set for this scenario script. " +
        "Other exit condition take precedence.");
    exit();
}


var sample_time = 10;    // start sampling at 10 second windows
var sample_incr = 1.6181;  // sampling window is fibonacci
var sample_max = 300;    // 5 minutes is the max sampling window

var rate_base = 0;
var rate_step = 100;
var rate_incr = 2.0

var min_stride = 100;
var averageof = 2;

var latency_cutoff = 50.0;
var testrate_cutoff = 0.8;
var bestrate_cutoff = 0.90;
var latency_pctile = 0.99;

var profile = "TEMPLATE(profile,default)";
printf(" profile=%s\n",profile);

if (profile == "default") {
    printf(" :NOTE: This profile is neither very fast nor very accurate.\n");
    printf(" :NOTE: Consider using profile=fast or profile=accurate.\n");
} else if (profile == "fast") {
    sample_time = 5;
    sample_incr = 1.6181;
    sample_max = 60;
    averageof = 1;
} else if (profile == "accurate") {
    sample_time = 10;
    sample_incr = 2;
    sample_max = 300;
    averageof = 3;
}

sample_time = TEMPLATE(sample_time, sample_time);
sample_incr = 0.0 + TEMPLATE(sample_incr, sample_incr);
sample_max = TEMPLATE(sample_max, sample_max);

printf('  sample_time=%d',sample_time);
printf('  sample_incr=%f',sample_incr);
printf('  sample_max=%d\n',sample_max);

averageof = TEMPLATE(averageof, averageof);
printf('  averageof=%d',averageof);

min_stride = TEMPLATE(min_stride, min_stride);
printf(' min_stride=%d\n',min_stride);

rate_base = TEMPLATE(rate_base, rate_base);
rate_step = TEMPLATE(rate_step, rate_step);
rate_incr = 0.0 + (TEMPLATE(rate_incr, rate_incr));      // how quickly to increase the step from the previous step

printf('  rate_base=%d',rate_base)
printf(' rate_step=%d',rate_step)
printf(' rate_incr=%d\n',rate_incr);

testrate_cutoff = 0.0 + TEMPLATE(testrate_cutoff, testrate_cutoff);
bestrate_cutoff = 0.0 + TEMPLATE(bestrate_cutoff, bestrate_cutoff);
latency_cutoff = 0.0 + TEMPLATE(latency_cutoff, latency_cutoff);
latency_pctile = 0.0 + TEMPLATE(latency_pctile, latency_pctile);

printf(' testrate_cutoff=%s',as_pct(testrate_cutoff));
printf(' bestrate_cutoff=%s',as_pct(bestrate_cutoff));
printf(' latency_cutoff=%dms',latency_cutoff);
printf(' latency_pctile=%s',as_pctile(latency_pctile));

printf("\n```\n");
printf("    Performing FindMax analysis with the following parameters:\n");

printf("    Scale sample window between %d and %d\n",sample_time,sample_max);
printf("     increasing by %.3fX on each rejected iteration.\n", sample_incr);
printf("    Set target rate to %d + %d  * ( %d ^iter) \n", rate_base, rate_step, rate_incr);
printf("     for each iteration accepted in a row.\n");
printf("    Schedule %s operations at a time per thread.\n", min_stride);
printf("    Report the average result of running the findmax search algorithm %d times.\n",averageof);
printf("    Reject iterations which fail to achieve %2.0f%% of the target rate.\n", testrate_cutoff*100);
printf("    Reject iterations which fail to achieve %2.0f%% of the best rate.\n",bestrate_cutoff*100);
printf("    Reject iterations which fail to achieve better than %dms response\n",latency_cutoff);
printf("     at percentile p%f\n",latency_pctile*100);
printf("```\n");

if (latency_pctile > 1.0) {
    latency_pctile = (latency_pctile * 0.01);
}

var reporter_rate_base = scriptingmetrics.newGauge("findmax.params.rate_base", rate_base + 0.0);
var reporter_achieved_rate = scriptingmetrics.newGauge("findmax.sampling.achieved_rate", 0.0);
var reporter_target_rate = scriptingmetrics.newGauge("findmax.target.rate_base", 0.0);


var activity_type = "TEMPLATE(activity_type,cql)";
var yaml_file = "TEMPLATE(yaml_file,cql-iot)";

//  CREATE SCHEMA
schema_activitydef = params.withDefaults({
    type: activity_type,
    yaml: yaml_file
});

schema_activitydef.alias = "findmax_schema";
schema_activitydef.threads = "1";
schema_activitydef.tags = "TEMPLATE(schematags,phase:schema)";
printf("Creating schema with schematags: %s\n",schema_activitydef.tags.toString());

scenario.run(schema_activitydef);

//  START ITERATING ACTIVITY
activitydef = params.withDefaults({
    type: activity_type,
    yaml: yaml_file,
    threads: "50"
});

activitydef.alias = "findmax";
activitydef.cycles = "1000000000";
activitydef.recycles = "1000000000";
activitydef.tags = "TEMPLATE(maintags,phase:main)";

function ops_s(iteration, results) {
    return results[iteration].ops_per_second;
}

function achieved_ratio(iteration, results) {
    return results[iteration].achieved_ratio;
}

function p_ms(iteration, results, pctile) {
    return results[iteration].latency_snapshot.getValue(pctile) / 1000000.0;
}

function target_rate(iteration, results) {
    return results[iteration].target_rate;
}

function lowest_unacceptable_of(result1, result2) {
    return result1.target_rate < result2.target_rate ? result1 : result2;
}

function as_pct(val) {
    return (val * 100.0).toFixed(0)+"%";
}
function as_pctile(val) {
    return "p" + (val * 100.0).toFixed(0);
}

scenario.start(activitydef);
raise_to_min_stride(activities.findmax, min_stride);

printf("\nwarming up client JIT for 10 seconds... \n");
activities.findmax.striderate = "" + (1000.0 / activities.findmax.stride) + ":1.1:restart";
scenario.waitMillis(10000);
activities.findmax.striderate = "" + (100.0 / activities.findmax.stride) + ":1.1:restart";
printf("commencing test\n");

function raise_to_min_stride(params, min_stride) {
    var multipler = (min_stride / params.stride);
    var newstride = (multipler * params.stride);
    printf("increasing stride to %d ( %d x initial)\n",newstride, multipler);
    params.stride = newstride.toFixed(0);
}

function testCycleFun(params) {

    var rate_summary = "targeting " + params.target_rate + " ops_s (" + params.base + "+" + params.step + ")";
    printf("\n >-- iteration %d ---> %s for %ss\n",
        params.iteration,
        rate_summary,
        params.sample_seconds
    );

    var result = {};

    // printf(" target rate is " + params.target_rate + " ops_s");
    activities.findmax.striderate = "" + (params.target_rate / activities.findmax.stride) + ":1.1:restart";

    precount = metrics.findmax.cycles.servicetime.count;

    var snapshot_reader = metrics.findmax.result.deltaReader;
    var old_data = snapshot_reader.getDeltaSnapshot(); // reset

    // printf(" sampling performance for " + params.sample_seconds + " seconds...");
    scenario.waitMillis(params.sample_seconds * 1000);

    postcount = metrics.findmax.cycles.servicetime.count;

    var count = postcount - precount;
    var ops_per_second = count / params.sample_seconds;
    reporter_achieved_rate.update(ops_per_second);
    var latency_snapshot = snapshot_reader.getDeltaSnapshot(10000);

    return {
        target_rate: params.target_rate,
        count: postcount - precount,
        ops_per_second: ops_per_second,
        latency_snapshot: latency_snapshot,
        achieved_ratio: (count / params.sample_seconds) / params.target_rate,
        latency_ms: latency_snapshot.getValue(latency_pctile) / 1000000.0
    };

}

function highest_acceptable_of(results, iteration1, iteration2) {
    if (iteration1 == 0) {
        return iteration2;
    }
    return results[iteration1].ops_per_second > results[iteration2].ops_per_second ? iteration1 : iteration2;
}

function find_max() {

    var startAt = new Date().getTime();
    var results = [];

    var iteration = 0;
    var highest_acceptable_iteration = 0;
    var lowest_unacceptable_iteration = 0;
    var rejected_count = 0;

    var base = rate_base;
    reporter_target_rate.update(base);
    var step = rate_step;

    var accepted_count = 0;
    var rejected_count = 0;

    while (true) {
        iteration++;

        var zoom_lvl = Math.pow(sample_incr, rejected_count);
        var zoomed_out = Math.max(sample_time * zoom_lvl, sample_time).toFixed();
        actual_sample_time = Math.min(zoomed_out, sample_max);

        var params = {
            iteration: iteration,
            target_rate: (base + step),
            base: base,
            step: step,
            sample_seconds: actual_sample_time
        };

        step = step * rate_incr;
        var result = results[iteration] = testCycleFun(params);
        var failed_checks = 0;

        // latency check
        printf(" LATENCY        ");

        var latency_summary = result.latency_ms.toFixed(2)+"ms "+as_pctile(latency_pctile);
        if (result.latency_ms > latency_cutoff) {
            failed_checks++;
            printf(" FAIL(TOO HIGH) [ %s >", latency_summary)
        } else {
            printf(" PASS(OK)       [ %s <", latency_summary)
        }
        printf(" max %dms ]\n", latency_cutoff);

        // throughput achieved of target goal
        printf(" OPRATE/TARGET  ");
        var throughput_summary  = as_pct(result.achieved_ratio) + " of target";

        if (result.achieved_ratio < testrate_cutoff) {
            failed_checks++;
            printf(" FAIL(TOO LOW)  [ %s <", throughput_summary);
        } else {
            printf(" PASS(OK)       [ %s >", throughput_summary)
        }
        printf(" min %s ]", as_pct(testrate_cutoff));
        printf(" ( %s/%s )\n", result.ops_per_second.toFixed(0), params.target_rate.toFixed(0));
        // printf(" (" + result.ops_per_second.toFixed(0) + "/" + params.target_rate.toFixed(0) + ")");

        // throughput achieved of best known throughput
        printf(" OPRATE/BEST    ");
        // If this iteration is lower than relrate threshold as compared to the best iteration, then discard it
        // The first iteration is always "100%"
        var highest_rate_known = (iteration == 1 ? result.ops_per_second : results[highest_acceptable_iteration].ops_per_second);
        var rel_rate = (result.ops_per_second / highest_rate_known);
        var rel_rate_summary = as_pct(rel_rate) + " of best known";

        var rel_rate_pct = (rel_rate * 100.0).toFixed(2) + "%";
        if (rel_rate < bestrate_cutoff) {
            failed_checks++;
            printf(" FAIL(SLOWER)   [ %s <", rel_rate_summary)
        } else {
            printf(" PASS(OK)       [ %s >", rel_rate_summary)
        }

        printf(" min %s ] ( %s/%s ) \n",
            as_pct(bestrate_cutoff),
            result.ops_per_second.toFixed(0),
            highest_rate_known.toFixed(0)
            );

        if (failed_checks == 0) {
            accepted_count++;
            highest_acceptable_iteration = highest_acceptable_of(results, highest_acceptable_iteration, iteration);
            printf(" ---> accepting iteration %d\n",iteration);

        } else {
            rejected_count++;
            printf(" !!!  rejecting iteration %d\n",iteration);

            if (lowest_unacceptable_iteration == 0) {
                lowest_unacceptable_iteration = iteration;
            } else {
                // keep the more conservative target rate, if both this iteration and another one was not acceptable
                lowest_unacceptable_iteration = result.target_rate < results[lowest_unacceptable_iteration].target_rate ?
                    iteration : lowest_unacceptable_iteration;
            }
            highest_acceptable_iteration = highest_acceptable_iteration == 0 ? iteration : highest_acceptable_iteration;

            var too_fast = results[lowest_unacceptable_iteration].target_rate;

            if (base + step >= too_fast && results[highest_acceptable_iteration].target_rate + rate_step < too_fast) {
                base = results[highest_acceptable_iteration].target_rate;
                reporter_target_rate.update(base);
                step = rate_step;

                var search_summary = "[[ PASS " +
                    results[highest_acceptable_iteration].ops_per_second.toFixed(2) + "/"+
                    results[highest_acceptable_iteration].target_rate +
                    " @" + results[highest_acceptable_iteration].latency_ms.toFixed(2) +
                    "ms " + as_pctile(latency_pctile) +
                    " (i" +
                    highest_acceptable_iteration +
                    ") , FAIL " +
                    results[lowest_unacceptable_iteration].ops_per_second.toFixed(2) + "/"+
                    results[lowest_unacceptable_iteration].target_rate +
                    " @" + results[lowest_unacceptable_iteration].latency_ms.toFixed(2) +
                    "ms " + as_pctile(latency_pctile) +
                    " (i" +
                    lowest_unacceptable_iteration +
                    ") )).";

                printf("\n new search range is %s\n", search_summary);
                printf(" continuing search at base: %d step: %d", base, step);
                activities.findmax.striderate = "" + (base / activities.findmax.stride) + ":1.1:restart";

                printf("\n settling load at base for %ds before active sampling.\n", sample_time);
                scenario.waitMillis(sample_time * 1000);

            } else {
                printf("\n search completed: selected iteration %d", highest_acceptable_iteration);
                printf(" with %s ops_s @ %sms %s latency\n",
                    ops_s(highest_acceptable_iteration, results).toFixed(2),
                    (p_ms(highest_acceptable_iteration, results, latency_pctile)).toFixed(2),
                    as_pctile(latency_pctile)
                );
                break;
            }
        }
    }
    var endAt = new Date().getTime();
    var total_seconds = (endAt - startAt) / 1000.0;

    return {
        iteration: highest_acceptable_iteration,
        ops_s: ops_s(highest_acceptable_iteration, results),
        target_rate: target_rate(highest_acceptable_iteration, results),
        latency_ms: p_ms(highest_acceptable_iteration, results, latency_pctile),
        total_seconds: total_seconds
    };

}

overallresults = [];
for (var testrun = 0; testrun < averageof; testrun++) {
    overallresults[testrun] = find_max();
    overallresults[testrun].testrun = testrun;
}

scenario.stop(activitydef);

var total_ops_s = 0.0;
var total_latency = 0.0;
var total_seconds = 0.0;

overallresults.forEach(
    function (x) {
        // printf("run as JSON:%s\n",JSON.stringify(x));
        total_ops_s += x.ops_s;
        total_latency += x.latency_ms;
        total_seconds += x.total_seconds;
        // printf("summary of result: testrun="+x.testrun+" : x.ops_s="+x.ops_s+", latency_ms="+x.latency_ms+", total_seconds="+x.total_seconds+"\n");
    }
);

var average_rate = total_ops_s / averageof;
var average_latency = total_latency / (averageof + 0.0);

printf("\n");
printf(" ANALYSIS COMPLETE in %s seconds\n", total_seconds.toFixed(2));
printf(" THRESHOLDS:\n");
printf("  latency:[%s < %sms]\n",as_pctile(latency_pctile), latency_cutoff.toFixed(0));
printf("  throughput:[> %s of target]\n",as_pct(testrate_cutoff));
printf("  throughput:[> %s of best]\n",as_pct(bestrate_cutoff));
printf(" AVERAGE OF %d RESULTS:\n", averageof);
printf("  %s ops_s @ %s of %sms\n\n",
    average_rate.toFixed(2),
    as_pctile(latency_pctile),
    average_latency.toFixed(2)
);

//scenario.reportMetrics();

