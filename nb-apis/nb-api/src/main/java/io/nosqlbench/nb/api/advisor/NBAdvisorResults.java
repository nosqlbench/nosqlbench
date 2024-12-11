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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class NBAdvisorResults {
    private final static Logger logger = LogManager.getLogger("ADVISOR");
    private final List<NBAdvisorPoint<?>> points = new ArrayList<>();

    public NBAdvisorResults(List<NBAdvisorPoint<?>> points) {
        this.points.addAll(points);
    }

    public List<NBAdvisorPoint.Result<?>> getAdvisorResults() {
        return points.stream().flatMap(a -> a.getResultLog().stream()).toList();
    }

    public void render(Level level, String message) {
	    if (level ==  Level.INFO) {
	        logger.info(message);
	    } else if (level == Level.WARN) {
	        logger.warn(message);
	    } else if (level == Level.ERROR) {
	        logger.error(message);
        }
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
                    if ( level == Level.ERROR ) {
                        render(level, result.rendered());
                        count++;
                        terminate = true;
                    } else {
                        render(Level.DEBUG, result.rendered());
                    }
                    break;
                case NBAdvisorLevel.validate:
                    if ( level == Level.ERROR ) {
                        render(level, result.rendered());
                        count++;
                        terminate = true;
                    } else {
                        render(Level.INFO, result.rendered());
                    }
                    break;
                case NBAdvisorLevel.enforce:
                    if ( level == Level.ERROR || level == Level.WARN ) {
                        render(level, result.rendered());
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
            render(Level.ERROR, message);
            throw new NBAdvisorException(message, 2);
        }
        return count;
    }

}
