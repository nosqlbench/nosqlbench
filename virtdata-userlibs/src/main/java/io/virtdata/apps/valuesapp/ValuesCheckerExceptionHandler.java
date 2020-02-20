package io.virtdata.apps.valuesapp;

public class ValuesCheckerExceptionHandler implements Thread.UncaughtExceptionHandler {

    private ValuesCheckerCoordinator coordinator;

    public ValuesCheckerExceptionHandler(ValuesCheckerCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        coordinator.handleException(t, e);
    }
}
