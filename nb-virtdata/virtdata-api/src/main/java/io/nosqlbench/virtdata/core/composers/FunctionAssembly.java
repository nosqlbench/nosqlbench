/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.core.composers;

import io.nosqlbench.virtdata.core.bindings.FunctionType;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.function.*;

public class FunctionAssembly implements FunctionComposer {
    private final static Logger logger  = LogManager.getLogger(FunctionAssembly.class);
    private FunctionComposer<?> composer = null;

    @Override
    public Object getFunctionObject() {
        if (composer != null) {
            return composer.getFunctionObject();
        } else {
            throw new RuntimeException("No function have been passed for assembly.");
        }
    }

    @Override
    public FunctionComposer andThen(Object outer) {
        try {
            if (composer != null) {
                composer = composer.andThen(outer);
            } else {
                composer = andThenInitial(outer);
            }
            return composer;
        } catch (Exception e) {
            logger.error("Error while composing functions:\n");
            if (composer != null) {
                logger.error("composer: class:" + composer.getClass().getSimpleName() + ", toString:" + composer);
            }
            logger.error("outer: class:" + outer.getClass() + ", toString:" + outer);
            throw e;
        }
    }

    private FunctionComposer<?> andThenInitial(Object o) {
        try {
            FunctionType functionType = FunctionType.valueOf(o);
            switch (functionType) {
                case long_long:
                    return new ComposerForLongUnaryOperator((LongUnaryOperator) o);
                case long_int:
                    return new ComposerForLongToIntFunction((LongToIntFunction) o);
                case long_double:
                    return new ComposerForLongToDoubleFunction((LongToDoubleFunction) o);
                case long_T:
                    return new ComposerForLongFunction((LongFunction<?>) o);
                case int_int:
                    return new ComposerForIntUnaryOperator((IntUnaryOperator) o);
                case int_long:
                    return new ComposerForIntToLongFunction((IntToLongFunction) o);
                case int_double:
                    return new ComposerForIntToDoubleFunction((IntToDoubleFunction) o);
                case int_T:
                    return new ComposerForIntFunction((IntFunction<?>) o);
                case double_double:
                    return new ComposerForDoubleUnaryOperator((DoubleUnaryOperator) o);
                case double_long:
                    return new ComposerForDoubleToLongFunction((DoubleToLongFunction) o);
                case double_int:
                    return new ComposerForDoubleToIntFunction((DoubleToIntFunction) o);
                case double_T:
                    return new ComposerForDoubleFunction((DoubleFunction<?>) o);
                case R_T:
                    return new ComposerForFunction((Function<?, ?>) o);
                default:
                    throw new RuntimeException("Unrecognized function type:" + functionType);
            }
        } catch (Exception e) {
            logger.error("Error while setting up initial composer state for function class:" +
            o.getClass().getSimpleName() + ", toString:" + o);
            throw e;
        }
    }

    @Override
    public String toString() {
        return "composer:" + this.composer;
    }
}
