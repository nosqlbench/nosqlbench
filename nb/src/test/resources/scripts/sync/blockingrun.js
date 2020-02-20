activitydef1 = {
    "alias" : "blockingactivity1",
    "type" : "diag",
    "cycles" : "0..100000",
    "threads" : "1",
    "interval" : "2000"
};
activitydef2 = {
    "alias" : "blockingactivity2",
    "type" : "diag",
    "cycles" : "0..100000",
    "threads" : "1",
    "interval" : "2000"
};


print('running blockingactivity1');
scenario.run(10000,activitydef1);
print('blockingactivity1 finished');
print('running blockingactivity2');
scenario.run(10000,activitydef2);
print('blockingactivity2 finished');
