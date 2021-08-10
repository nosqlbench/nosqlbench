activitydef = {
    "alias" : "testhistostatslogger",
    "type" : "diag",
    "cycles" : "50000",
    "threads" : "20",
    "interval" : "2000",
    "targetrate" : "10000.0"
};

histostatslogger.logHistoStats("testing extention histostatslogger", ".*", "logs/histostats.csv", "0.5s");
print("started logging to logs/histostats.csv for all metrics at 1/2" +
    " second intervals.");

scenario.start(activitydef);
scenario.waitMillis(2000);
scenario.stop(activitydef);
