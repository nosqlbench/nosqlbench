activitydef1 = {
    "alias" : "activity_to_await",
    "type" : "diag",
    "cycles" : "0..1500",
    "threads" : "1",
    "targetrate" : "500"
};

print('starting activity teststartstopdiag');
scenario.start(activitydef1);
scenario.awaitActivity("activity_to_await");
print("awaited activity");