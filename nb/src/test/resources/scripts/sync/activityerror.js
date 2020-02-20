activitydef1 = {
    "alias" : "erroring_activity",
    "type" : "diag",
    "cycles" : "0..1500000",
    "threads" : "1",
    "targetrate" : "500"
};

print('starting activity erroring_activity');
scenario.start(activitydef1);
scenario.waitMillis(2000);
activities.erroring_activity.modulo="unparsable";
scenario.awaitActivity("erroring_activity");
print("awaited activity");