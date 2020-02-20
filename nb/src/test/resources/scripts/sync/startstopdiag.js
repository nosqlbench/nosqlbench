activitydef = {
    "alias" : "teststartstopdiag",
    "type" : "diag",
    "cycles" : "0..1000000000",
    "threads" : "25",
    "interval" : "2000"
};

print('starting activity teststartstopdiag');
scenario.start(activitydef);

print('waiting 500 ms');

scenario.waitMillis(500);
print('waited, stopping activity teststartstopdiag');
scenario.stop(activitydef);

print('stopped activity teststartstopdiag');
