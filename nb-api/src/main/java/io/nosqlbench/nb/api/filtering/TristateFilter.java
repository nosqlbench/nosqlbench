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

package io.nosqlbench.nb.api.filtering;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <P>A tri-state filter allows for flexible configuration of
 * multi-phase filtering. It effectively allows a conditional behavior
 * for filtering logic that can answer "yes", "no", <em>and</em> "I don't know."
 * </P>
 *
 * <P>This can also be used to build classic bi-state filtering, such as
 * filters that use a boolean predicate to say "keep" or "discard." Where the
 * tri-state filtering pattern shines, however, is in the ability to combine
 * different filtering rules to build a sophisticated filter at run-time
 * that bi-state filtering would prevent.</P>
 *
 * <P>In contrast to the bi-state filter, the default policy that is applied when
 * <em>not</em> matching an item with the predicate is to simply ignore it.
 * This means that in order to implement both matching and discarding
 * policies like a bi-state filter, you must do one of the following:
 * <ul>
 *     <li>Implement a default policy that overrides the "Ignore" action.</li>
 *     <li>Use both "keep" and "discard" predicates together in sequence.</li>
 * </ul>
 * </P>
 *
 * <P>The two techniques above are not mutually exclusive. In practice, tri-state
 * filters are used to build up chains of filters which can delegate down
 * the chain if up-stream filters do not have enough information to make
 * a keep or discard determination. Even in chained filters will have a
 * default policy that will override the "ignore" outcome.</P>
 *
 * <P>Filter chains that
 * have a default "exclude" policy that overrides "ignore" policies are
 * called "exclusive" filters. Their counterparts are "inclusive" filters.
 * In other words, Exclusive tri-state filters include an element if and only
 * if there was a matching include rule before any matching exclude rules
 * or the end of the filter chain, whereas inclusive tri-state filters exclude
 * an element if and only if there was a matching exclude rule before any matching
 * include rules or the end of the filter chain.</P>
 */
public interface TristateFilter<T> extends Function<T, TristateFilter.Policy> {

    @Override
    Policy apply(T cycleResult);

    /**
     * The filter action determines what action is taken for a given
     * of the filter matching. If the filter does not match, then neither
     * element that matches the predicate.
     */
    enum Policy {
        Keep,
        Discard,
        Ignore
    }

    /**
     * Create a predicate that will override any Ignore outcomes with the provided policy.
     *
     * @param defaultPolicy
     *         The policy that will override non-actionable outcomes
     * @return a Predicate that can be used to filter elements
     */
    default Predicate<T> toDefaultingPredicate(Policy defaultPolicy) {
        return new DefaultingPredicate<>(this, defaultPolicy);
    }

    default TristateFilter<T> toDefaultingFilter(Policy defaultPolicy) {
        return new DefaultingTriStateFilter(this,defaultPolicy);
    }

    class DefaultingPredicate<T> implements Predicate<T> {
        private final TristateFilter<T> filter;
        private final Policy defaultPolicy;

        public DefaultingPredicate(TristateFilter<T> filter, Policy defaultPolicy) {
            this.filter = filter;
            this.defaultPolicy = defaultPolicy;
        }

        @Override
        public boolean test(T t) {
            Policy policyResult = filter.apply(t);
            if (policyResult == Policy.Ignore) {
                policyResult = defaultPolicy;
            }
            return policyResult == Policy.Keep;
        }
    }


    /**
     * Create a predicate that will return true if and only if the filter
     * outcome matches the provided policy.
     *
     * @param matchingPolicy
     *         The policy that will signal true in the predicate.
     * @return a Predicate that can be used to filter elements
     */
    default Predicate<T> toMatchingPredicate(Policy matchingPolicy) {
        return new MatchingPredicate<>(this, matchingPolicy);
    }

    class MatchingPredicate<T> implements Predicate<T> {
        private final TristateFilter<T> filter;
        private final Policy matchOn;

        public MatchingPredicate(TristateFilter<T> filter, Policy matchOn) {
            this.filter = filter;
            this.matchOn = matchOn;
        }

        @Override
        public boolean test(T t) {
            return filter.apply(t) == matchOn;
        }
    }


    class DefaultingTriStateFilter<U> implements TristateFilter<U> {
        private final TristateFilter<U> wrappedFilter;
        private final Policy defaultPolicy;

        public DefaultingTriStateFilter(TristateFilter<U> innerFilter, Policy defaultPolicy) {
            this.wrappedFilter = innerFilter;
            this.defaultPolicy = defaultPolicy;
        }

        @Override
        public Policy apply(U cycleResult) {
            Policy policy = wrappedFilter.apply(cycleResult);
            if (policy!=Policy.Ignore) return policy;
            return defaultPolicy;
        }
    }
}
