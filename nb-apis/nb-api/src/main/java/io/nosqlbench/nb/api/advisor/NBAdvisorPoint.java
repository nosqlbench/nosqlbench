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

import java.util.*;

public class NBAdvisorPoint<T> extends NBAdvisorPointOrBuilder<T> {

    private final static Logger logger = LogManager.getLogger("ADVISOR");

    private String name;
    private String description;
    private NBAdvisorLevel advisorLevel = NBAdvisorLevel.none;
    private NBAdvisorCondition<T>[] conditions = new NBAdvisorCondition[0];
    private List<Result<?>> resultLog = new ArrayList<Result<?>>();

    public NBAdvisorPoint(String name) {
        this(name, "–");
    }

    public NBAdvisorPoint(String name, String description) {
        this.name = name;
        this.description = description == null ? name : description;
        this.advisorLevel = NBAdvisorLevel.get();
    }

    public NBAdvisorPoint<T> add(NBAdvisorCondition<T> condition) {
        _addArrayCondition(condition);
        return this;
    }

    private void _addArrayCondition(NBAdvisorCondition<T> condition) {
        NBAdvisorCondition<T>[] newConditions = new NBAdvisorCondition[conditions.length + 1];
        System.arraycopy(conditions, 0, newConditions, 0, conditions.length);
        newConditions[newConditions.length - 1] = condition;
        conditions = newConditions;
    }

    public Result<T>[] validateAll(Collection<T> elements) {
        List<Result<T>> buffer = new ArrayList<>();
        for (T element : elements) {
            Result<T>[] oneElementValidation = validate(element);
            for (Result<T> r : oneElementValidation) {
                buffer.add(r);
            }
        }
        return buffer.toArray(new Result[0]);
    }

    public synchronized Result<T>[] validate(T element) {
        Result<T>[] results = new Result[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            results[i] = conditions[i].apply(element);
            resultLog.add(results[i]);
        }
        return results;
    }

    public List<Result<?>> getResultLog() {
        return this.resultLog;
    }

    public NBAdvisorPoint<T> evaluate() {
        NBAdvisorResults advisorResults = new NBAdvisorResults(List.of(this));
        advisorResults.evaluate();
        return this;
    }

    public NBAdvisorPoint<T> clear() {
        this.resultLog.clear();
        return this;
    }

    public NBAdvisorPoint<T> setName(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    public NBAdvisorPoint<T> logName() {
        if (resultLog.size() > 0) {
            logger.info("Advisor: " + name + ": " + description);
        }
        return this;
    }

    public String[] errorMessages(T element) {
        Result<T>[] results = this.validate(element);
        return Arrays.stream(results).filter(Result::isError).map(Result::rendered).toArray(String[]::new);
    }

    public static enum Status {
        OK,
        ERROR
    }

    public static record Result<T>(
        NBAdvisorCondition<T> condition,
        T element,
        Status status
    ) {
        public boolean isError() {
            return status == Status.ERROR;
        }

        public Level conditionLevel() {
            return condition.level();
        }

        public String rendered() {
            return switch (status) {
                case OK -> "OK: " + condition.okMsg().apply(element);
                case ERROR -> conditionLevel() + ": " + condition.errMsg().apply(element);
            };
        }
    }

}
