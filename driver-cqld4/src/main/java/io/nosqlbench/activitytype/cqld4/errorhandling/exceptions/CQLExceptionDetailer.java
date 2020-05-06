package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.driver.core.exceptions.ReadTimeoutException;
import com.datastax.driver.core.exceptions.WriteTimeoutException;

public class CQLExceptionDetailer {

    public static String messageFor(long cycle, Throwable e) {

        if (e instanceof ReadTimeoutException) {
            ReadTimeoutException rte = (ReadTimeoutException) e;
            return rte.getMessage() +
                    ", coordinator: " + rte.getHost() +
                    ", wasDataRetrieved: " + rte.wasDataRetrieved();
        }

        if (e instanceof WriteTimeoutException) {
            WriteTimeoutException wte = (WriteTimeoutException) e;
            return wte.getMessage() +
                    ", coordinator: " + wte.getHost();
        }

        return e.getMessage();
    }
}
