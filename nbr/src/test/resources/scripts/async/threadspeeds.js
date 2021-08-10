activitydef = {
    "alias" : "threadspeeds",
    "type" : "diag",
    "cycles" : "0..4000000000000",
    "threads" : "1",
    "interval" : "10000",
    "targetrate" : "1000000"
};
scenario.start(activitydef);

var speeds = [];
var latencies = [];

function aTestCycle(threads, index) {
    activities.threadspeeds.threads = threads; // dynamically adjust the active threads for this activity
    scenario.waitMillis(60000);                // wait for 1 minute to get a clean 1 minute average speed
//     scenario.waitMillis(5000);                // wait for 1 minute to get a clean 1 minute average speed
    speeds[threads]= metrics.threadspeeds.cycles.oneMinuteRate; // record 1 minute avg speed
    print("speeds:" + speeds.toString());

}

var min=1;
var max=256;
var mid=Math.floor((min+max)/2)|0;

[min,mid,max].forEach(aTestCycle);

while (min<mid && mid<max && scenario.isRunningActivity(activitydef)) {
    print("speeds:" + speeds.toString());
    if (speeds[min]<speeds[max]) {
        min=mid;
        mid=Math.floor((mid+max) / 2)|0;
    } else {
        max=mid;
        mid=Math.floor((min+mid) / 2)|0;
    }
    aTestCycle(mid);
}
print("The optimum number of threads is " + mid + ", with " + speeds[mid] + " cps");
var targetrate = (speeds[mid] * .7)|0;
print("Measuring latency with threads=" + mid + " and targetrate=" + targetrate);

activities.threadspeeds.threads = mid;
activities.threadspeeds.targetrate = targetrate;
scenario.waitMillis(60000);


scenario.stop(threadspeeds);


