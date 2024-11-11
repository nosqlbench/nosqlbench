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


import io.nosqlbench.nb.api.components.core.NBNamedElement;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;

public interface NBAdvisorCondition<T> extends Function<T, NBAdvisorPoint.Result<T>>, Predicate<T> {

    Function<T, String> okMsg();

    Function<T, String> errMsg();

    Level level();

    @Override
    default NBAdvisorPoint.Result<T> apply(T element) {
        boolean hasError = test(element);
        return new NBAdvisorPoint.Result<>(
            this,
            element,
            hasError ? NBAdvisorPoint.Status.ERROR : NBAdvisorPoint.Status.OK
        );
    }

    String getName();
}
