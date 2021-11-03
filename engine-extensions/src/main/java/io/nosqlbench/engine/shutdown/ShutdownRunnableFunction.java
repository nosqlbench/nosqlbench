package io.nosqlbench.engine.shutdown;

import org.apache.logging.log4j.Logger;

import java.util.function.Function;


public class ShutdownRunnableFunction extends Thread {
    private final String name;
    private final Function<Object[],Object> function;
    private final Logger logger;

    public ShutdownRunnableFunction(Logger logger, String name, Function<?, ?> function) {
        this.logger = logger;
        this.name = name;
        this.function = (Function<Object[],Object>)function;
    }

    @Override
    public void run() {
        logger.info("Running shutdown hook '" + name + "'...");
        try {
            Object result = function.apply(new Object[0]);
            if (result instanceof CharSequence) {
                logger.info("shutdown hook returned output:\n" + ((CharSequence) result));
            }
            logger.info("Completed shutdown hook '" + name + "'...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
