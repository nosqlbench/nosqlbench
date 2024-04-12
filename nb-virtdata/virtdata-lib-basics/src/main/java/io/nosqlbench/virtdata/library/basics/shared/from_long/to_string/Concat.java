/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongBinaryOperator;
import java.util.function.LongFunction;
import java.util.regex.Matcher;


/**
 * <P>This is the core implementation of the Concat style of String
 * binding. It is the newer and recommended version of {@link Template}.</P>
 *
 * <P>Users should use one of these wrappers:
 * <UL>
 * <LI>{@link ConcatCycle} - all inputs are the cycle value</LI>
 * <LI>{@link ConcatHashed} - all inputs are a hash of (cycle+step)</LI>
 * <LI>{@link ConcatStepped} - all inputs are (cycle+step)</LI>
 * <LI>{@link ConcatFixed} - all inputs are always the same value</LI>
 * <LI>{@link ConcatChained} - all inputs are chained hashes of the previous one</LI>
 * </UL></P>
 * <p>
 * <HR/>
 *
 * <P>This implementation is available for specialized use when needed, but the
 * above versions are much more self-explanatory and easy to use.</P>
 *
 * <P>As with previous implementations, the basic input which is fed to the functions
 * is the sum of the input cycle and the step, where the step is simply the index of
 * the insertion point within the template string. These start at 0, so a template string
 * which contains "{}-{}" will have two steps, 0, and 1. For cycle 35, the first
 * function will take input 35, and the second 36. This can create some local neighborhood
 * similarity in test data, so other forms are provided which can hash the values for
 * an added degree of (effective) randomness and one that chains these so that each
 * set of values from a Concat binding are quite distinct from each other.</P>
 * <p>
 * <HR/>
 * <P>Binding functions used to populate each step of the template may have their own bounds
 * of output values like {@link Combinations}. These are easy to use internally since they
 * work well with the hashing. However, some other functions may operate over the whole space
 * of long values, and come with no built-in cardinality constraints. It is recommended to
 * use those with built-in constraints when you want to render a discrete population of values.</P>
 */
@ThreadSafeMapper
@Categories(Category.general)
public class Concat implements LongFunction<String> {

    private final static Logger logger = LogManager.getLogger(Concat.class);
    protected final LongBinaryOperator cycleStepMapper;
    protected final String[] literals;
    protected final LongFunction<String>[] functions;

    public Concat(String template, Object... functions) {
        this(Long::sum, template, functions);
    }

    public Concat(LongBinaryOperator cycleStepMapper, String template, Object... functions) {
        this.cycleStepMapper = cycleStepMapper;
        this.literals = parseTemplate(template);
        if (literals.length > 1 && functions.length < 1) {
            logger.warn("You provided zero functions to go with concat template " + template + ", inserting diagnostic function for \"v:\"(cycle+step)");
            this.functions = new LongFunction[]{l -> "v:" + l};
        } else {
            this.functions = VirtDataConversions.adaptFunctionArray(functions, LongFunction.class, Object.class);
        }
    }

    @Override
    public String apply(long cycle) {
        StringBuilder buffer = new StringBuilder();
        buffer.setLength(0);
        for (int i = 0; i < literals.length - 1; i++) {
            buffer.append(literals[i]);
            long value = cycleStepMapper.applyAsLong(cycle, i);
            int funcIdx = Math.min(functions.length - 1, i);
            LongFunction<String> selectedFunction = functions[funcIdx];
            String string = selectedFunction.apply(value);
            buffer.append(string);
        }
        buffer.append(literals[literals.length - 1]);
        return buffer.toString();
    }


    private String[] parseTemplate(String template) {
        try {
            List<String> literals = new ArrayList<>();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{}");
            Matcher m = p.matcher(template);
            int pos = 0;
            while (m.find()) {
                literals.add(template.substring(pos, m.start()));
                pos = m.end();
            }
            String partial = template.substring(pos);
            literals.add(partial);
            return literals.toArray(new String[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
