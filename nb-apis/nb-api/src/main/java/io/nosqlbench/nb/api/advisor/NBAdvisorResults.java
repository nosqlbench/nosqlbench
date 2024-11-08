package io.nosqlbench.nb.api.advisor;

/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.nb.api.errors.ProcessingEarlyExit;

import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class NBAdvisorResults {
    private final List<NBAdvisorPoint<?>> points = new ArrayList<>();

    public NBAdvisorResults(List<NBAdvisorPoint<?>> points) {
        this.points.addAll(points);
    }

    public List<NBAdvisorPoint.Result<?>> getAdvisorResults() {
        return points.stream().flatMap(a -> a.getResultLog().stream()).toList();
    }

    public int evaluate() {
        List<NBAdvisorPoint.Result<?>> results = getAdvisorResults();
        Iterator<NBAdvisorPoint.Result<?>> iterator = results.iterator();
        int count = 0;
        boolean terminate = false;
        Level level = Level.INFO;
        while (iterator.hasNext()) {
            NBAdvisorPoint.Result<?> result = iterator.next();
            level = result.isError() ? result.conditionLevel() : level.INFO;
            switch (NBAdvisorLevel.get()) {
            case NBAdvisorLevel.none:
            case NBAdvisorLevel.validate:
                if ( level == Level.ERROR ) {
                    System.out.println(result.rendered());
                    count++;
                    terminate = true;
                }
                break;
            case NBAdvisorLevel.enforce:
                if ( level == Level.ERROR || level == Level.WARN ) {
                    System.out.println(result.rendered());
                    count++;
                    terminate = true;
                }
                break;
            }
        }
        if ( terminate ) {
            String message = String.format("Advisor found %d actionable %s.",
                    count,
                    (count < 2 ? "error" : "errors"));
            System.out.println(message);
            throw new ProcessingEarlyExit(message, 2);
        }
        return count;
    }

}
