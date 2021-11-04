shutdown.addShutdownHook('testfunc', function f() {
    print("shutdown hook running");
});
