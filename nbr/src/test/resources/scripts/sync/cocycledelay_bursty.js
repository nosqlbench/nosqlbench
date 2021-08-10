co_cycle_delay = {
    "alias": "co_cycle_delay",
    "type": "diag",
    "diagrate": "500",
    "cycles": "0..1000000",
    "threads": "10",
    "cyclerate": "1000,1.5",
    "modulo": "1000"
};

print('starting activity co_cycle_delay');
scenario.start(co_cycle_delay);
for (i = 0; i < 5; i++) {
    scenario.waitMillis(1000);
    if (!scenario.isRunningActivity('co_cycle_delay')) {
        print("scenario exited prematurely, aborting.");
        break;
    }
    print("backlogging, cycles=" + metrics.co_cycle_delay.cycles.servicetime.count +
        " waittime=" + metrics.co_cycle_delay.cycles.waittime.value +
        " diagrate=" + activities.co_cycle_delay.diagrate +
        " cyclerate=" + activities.co_cycle_delay.cyclerate
    );

    // print("config: " +
    //     " cycles.config_cyclerate=" + metrics.co_cycle_delay.cycles.config_cyclerate.value +
    //     " cycles.config_burstrate=" + metrics.co_cycle_delay.cycles.config_burstrate.value +
    //     " cycles.waittime=" + metrics.co_cycle_delay.cycles.waittime.value
    // );
    //
    // print("diag config: " +
    //     " diag.config_cyclerate=" + metrics.co_cycle_delay.diag.config_cyclerate.value +
    //     " diag.config_burstrate=" + metrics.co_cycle_delay.diag.config_burstrate.value +
    //     " diag.waittime=" + metrics.co_cycle_delay.diag.waittime.value
    // );

}
print('step1 metrics.waittime=' + metrics.co_cycle_delay.cycles.waittime.value);
activities.co_cycle_delay.diagrate = "10000";

for (i = 0; i < 10; i++) {
    if (!scenario.isRunningActivity('co_cycle_delay')) {
        print("scenario exited prematurely, aborting.");
        break;
    }
    print("recovering, cycles=" + metrics.co_cycle_delay.cycles.servicetime.count +
        " waittime=" + metrics.co_cycle_delay.cycles.waittime.value +
        " diagrate=" + activities.co_cycle_delay.diagrate +
        " cyclerate=" + activities.co_cycle_delay.cyclerate
    );

    // print("cycles.config: " +
    //     " cycles.config_cyclerate=" + metrics.co_cycle_delay.cycles.config_cyclerate.value +
    //     " cycles.config_burstrate=" + metrics.co_cycle_delay.cycles.config_burstrate.value +
    //     " cycles.waittime=" + metrics.co_cycle_delay.cycles.waittime.value
    // );
    //
    // print("diag config: " +
    //     " diag.config_cyclerate=" + metrics.co_cycle_delay.diag.config_cyclerate.value +
    //     " diag.config_burstrate=" + metrics.co_cycle_delay.diag.config_burstrate.value +
    //     " diag.waittime=" + metrics.co_cycle_delay.diag.waittime.value
    // );


    scenario.waitMillis(1000);
    if (metrics.co_cycle_delay.cycles.waittime.value < 10000000) {
        print("waittime trended back down as expected, exiting on iteration " + i);
        break;
    }
}
//scenario.awaitActivity("co_cycle_delay");
print('step2 metrics.waittime=' + metrics.co_cycle_delay.cycles.waittime.value);
scenario.stop(co_cycle_delay);
print("stopped activity co_cycle_delay");
