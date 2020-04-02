/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.nondeterministic;

import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches a digit sequence in the current thread name and caches it in a thread local.
 * This allows you to use any intentionally indexed thread factories to provide an analogue for
 * concurrency. Note that once the thread number is cached, it will not be refreshed. This means
 * you can't change the thread name and get an updated value.
 */
@ThreadSafeMapper
@DeprecatedFunction("This is being replaced by ThreadNum() for naming consistency.")
public class ThreadNumToLong implements LongUnaryOperator {

    private static final Pattern pattern = Pattern.compile("^.*?(\\d+).*$");
    private ThreadLocal<Long> threadLocalInt = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            Matcher matcher = pattern.matcher(Thread.currentThread().getName());
            if (matcher.matches()) {
                return Long.valueOf(matcher.group(1));
            } else {
                throw new RuntimeException(
                        "Unable to match a digit sequence in thread name:" + Thread.currentThread().getName()
                );
            }
        }
    };

    @Override
    public long applyAsLong(long input) {
        return threadLocalInt.get();
    }
}
