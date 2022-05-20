package com.datastax.ebdrivers.dsegraph.errorhandling;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.exceptions.*;
import com.datastax.driver.dse.graph.GraphStatement;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("Duplicates")
public class GraphErrorHandler {
    private final static Logger logger = LogManager.getLogger(GraphErrorHandler.class);

    private final ErrorResponse realErrorResponse;
    // private final ErrorResponse unappliedResponse;
    private final ErrorResponse retryableResponse;
    private final ExceptionMeterMetrics exceptionMeterMetrics;

    public GraphErrorHandler(
            ErrorResponse realErrorResponse,
            // ErrorResponse unappliedResponse,
            ErrorResponse retryableResponse,
            ExceptionMeterMetrics exceptionMeterMetrics) {
        this.realErrorResponse = realErrorResponse;
        //  this.unappliedResponse = unappliedResponse;
        this.retryableResponse = retryableResponse;
        this.exceptionMeterMetrics = exceptionMeterMetrics;
    }

    /**
     * @param e         Exception to be handled
     * @param statement statement that yielded the exception
     * @param cycle     the input cycle that made the statement
     * @return true, if the error handler determines that a retry is needed
     */
    public boolean HandleError(Exception e, GraphStatement statement, long cycle) {
        boolean retry = false;

        try {
            if (e != null) {
                throw e;
            }
        } catch (ExecutionException |
                InvalidQueryException | ReadFailureException | WriteFailureException
                | SyntaxError realerror) {

            if (e instanceof SyntaxError) {
                logger.error("Syntax error:" + GraphQueryStringMapper.getQueryString(statement));
            }

            switch (realErrorResponse) {
                case stop:
                    logger.error("error with cycle " + cycle + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(realerror);
                case warn:
                    logger.warn("error with cycle " + cycle + ": " + e.getMessage());
                case retry:
                    retry = true;
                case count:
                    exceptionMeterMetrics.mark(realerror.getClass().getSimpleName());
                case ignore:
                default:
                    break;
            }

        } catch (NoHostAvailableException | UnavailableException | OperationTimedOutException | OverloadedException
                | WriteTimeoutException | ReadTimeoutException retryable) {
            // retryable errors
            switch (retryableResponse) {
                case stop:
                    logger.error("error with cycle " + cycle + ": " + e.getMessage());
                    e.printStackTrace();
                    throw retryable;
                case warn:
                    logger.warn("error with cycle " + cycle + ": " + e.getMessage());
                case retry:
                    retry = true;
                case count:
                    exceptionMeterMetrics.mark(retryable.getClass().getSimpleName());
                case ignore:
                default:
                    break;
            }
        }
//        catch (ChangeUnappliedException cua) {
//            boolean retry = false;
//            switch (retryableResponse) {
//                case stop:
//                    throw cua;
//                case warn:
//                    logger.warn("error with cycle " + cycle + ": " + e.getMessage());
//                case retry:
//                    retry = true;
//                case count:
//                    exceptionCountMetrics.count(cua);
//                case ignore:
//                default:
//                    break;
//            }
//            return retry;
//        }
        catch (Exception unknown) {
            throw new RuntimeException(
                    "Unrecognized exception in error handler:"
                            + unknown.getClass().getCanonicalName() + ": " + unknown.getMessage(), unknown
            );
        }
        return retry;
    }
}
