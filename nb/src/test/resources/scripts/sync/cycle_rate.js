
activitydef = {
    "alias" : "cycle_rate",
    "type" : "diag",
    "cycles" : "5K",
    "threads" : "10",
    "cyclerate" : "1K",
    "phases" : 15
};

scenario.run(activitydef);

print("current cycle = " + metrics.cycle_rate.cycles.servicetime.count);
print("mean cycle rate = " + metrics.cycle_rate.cycles.servicetime.meanRate);



