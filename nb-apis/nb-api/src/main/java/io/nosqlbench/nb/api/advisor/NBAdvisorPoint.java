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
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NBAdvisorPoint<T> extends NBAdvisorPointOrBuilder<T> {

    private final static Logger logger = LogManager.getLogger("ADVISOR");

    private final String name;
    private final String description;
    private NBAdvisorCondition<T>[] conditions = new NBAdvisorCondition[0];

    public NBAdvisorPoint(String name) {
        this(name, null);
    }

    public NBAdvisorPoint(String name, String description) {
        this.name = name;
        this.description = description == null ? name : description;
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

    public Result<T>[] validate(T element) {
        Result<T>[] results = new Result[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            results[i] = conditions[i].apply(element);
        }
        return results;
    }


    public NBAdvisorPoint<T> add(NBAdvisorCondition<T> condition) {
        _addArrayCondition(condition);
        return this;
    }

    public String[] errorMessages(T element) {
        Result<T>[] results = this.validate(element);
        return Arrays.stream(results).filter(Result::isError).map(Result::rendered).toArray(String[]::new);
    }


    private void _addArrayCondition(NBAdvisorCondition<T> condition) {
        NBAdvisorCondition<T>[] newConditions = new NBAdvisorCondition[conditions.length + 1];
        System.arraycopy(conditions, 0, newConditions, 0, conditions.length);
        newConditions[newConditions.length - 1] = condition;
        conditions = newConditions;
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

        public String rendered() {
            return switch (status) {
                case OK -> condition.level() + ": " + condition.okMsg().apply(element);
                case ERROR -> condition.level() + ": " + condition.errMsg().apply(element);
            };
        }
    }

}
