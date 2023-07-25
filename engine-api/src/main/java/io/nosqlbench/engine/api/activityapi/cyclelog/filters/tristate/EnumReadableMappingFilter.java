/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.regex.Pattern;

/**
 * A result reading filter which uses an Enum as a set of elements to filter.
 * If you have an Enum that implements ResultReadable, then you can easily create
 * an enum-aware filter for it with this class.
 *
 * @param <E> The type of the Enum which implements TristateFilter
 */
public class EnumReadableMappingFilter<E extends Enum<E> & ResultReadable> implements TristateFilter<ResultReadable> {
    private final static Logger logger = LogManager.getLogger(EnumReadableMappingFilter.class);

    private final E[] enumValues;
    private final ResultMappingArrayFilter arrayFilter = new ResultMappingArrayFilter();

    public EnumReadableMappingFilter(E[] enumValues, Policy defaultPolicy) {
        this.enumValues = enumValues;
        for (E enumValue : enumValues) {
            arrayFilter.addPolicy(enumValue,defaultPolicy);
        }
    }

    public void addPolicy(String s, Policy policy) {
        Pattern p = null;
        int matched=0;
        if (s.matches("\\w+")) {
            p = Pattern.compile("^" + s + "$");
        } else {
            p = Pattern.compile(s);
        }
        for (E enumValue : enumValues) {
            if (enumValue.toString().matches(p.pattern())) {
                matched++;
                logger.debug(() -> "Setting policy for " + enumValue + " to " + policy);
                int resultCode = enumValue.getResult();
                arrayFilter.addPolicy(enumValue,policy);
            }
        }
        if (matched==0) {
            StringBuilder sb = new StringBuilder();
            for (E enumValue : this.enumValues) {
                sb.append(enumValue.toString()).append(",");
            }
            logger.warn("Unable to match any known type with pattern '" + s + "', available names: " + sb);
        }
    }

    @Override
    public Policy apply(ResultReadable cycleResult) {
        return arrayFilter.apply(cycleResult);
    }

    public String toString () {
            StringBuilder sb = new StringBuilder();

            for (E enumValue : enumValues) {
                int result = enumValue.getResult();
                sb.append(enumValue)
                        .append("->")
                        .append(result)
                        .append("->")
                        .append(arrayFilter.getPolicy(result)).append("\n");
            }
            return sb.toString();

        }
    }
