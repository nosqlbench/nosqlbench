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

package io.nosqlbench.engine.api.activityapi.errorhandling;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Allow error handlers to be registered for any exception type, with an explicit handler
 * that will be called if no specific other handler is matched.
 *
 * This error handler will automatically cascade up the error type hierarchy of the reported
 * error to find any matching handlers. If none are found between the reported error type
 * and the upper type bound, inclusive, then the default error handler is called, which
 * simply rethrows an error indicating that no more suitable error handler was provided.
 *
 * If you need to have the default error handler trigger before a certain supertype of
 * allowable errors is traversed, then set the upper bound with {@link #setUpperBound(Class)}.
 *
 * You may also register named groups of exceptions for which you can set the handler in
 * a single call.
 *
 * <H2>Type Parameters</H2>
 *
 * The type parameter R represents a generic return type that is returned by a handler.
 * This is borrowed directly from the base error handler type {@link CycleErrorHandler}
 * from which this aggregating handler is built. R can be any type that makes sense
 * for your particular error handling logic. For example, a Boolean return type can
 * be used to signal whether down-chain handlers should be executed or not.
 *
 * <H2>Patterns</H2>
 * Some of the methods support matching exceptions by substring or regex. The patterns
 * are applied to the {@link Class#getSimpleName()} version of the classname. Further,
 * simple substrings consisting only of word characters are considered shortcuts for
 * single Throwable types and thus throw an exception if more than one is matched.
 * Patterns that contain non-word characters allow for bulk management.
 *
 * @param <T> The subtype bound of exception to allow exception handlers for.
 * @param <R> The result type that will be produced by these error handlers.
 */
public class HashedErrorHandler<T extends Throwable, R> implements CycleErrorHandler<T, R> {
    private final static Logger logger = LogManager.getLogger(HashedErrorHandler.class);

    private final CycleErrorHandler<T, R> DEFAULT_defaultHandler = (cycle, error, errMsg) -> {
        throw new RuntimeException("no handler defined for type " + error.getClass() + " in cycle " + cycle + ", " + errMsg);
    };
    private Class<? extends Throwable> upperBound = Throwable.class;
    private final Map<String, Set<Class<? extends T>>> errorGroups = new ConcurrentHashMap<>();
    private final Map<Class<? extends T>, CycleErrorHandler<T, R>> handlers = new ConcurrentHashMap<>();
    private final Set<Class<? extends T>> validClasses = new HashSet<>();
    private CycleErrorHandler<T, R> defaultHandler = DEFAULT_defaultHandler;

    /**
     * Set a group name for a set of classes. If the classes in the
     * group are not already in the list of recognized classes, then
     * they are added as well.
     *
     * @param groupName  the name that the group will be referred to as
     * @param exceptions the set of exceptions to include in the group
     */
    @java.lang.SafeVarargs
    public final void setGroup(String groupName, Class<? extends T>... exceptions) {
        this.errorGroups.put(groupName, new HashSet<>(Arrays.asList(exceptions)));
        this.addValidClasses(exceptions);
    }

    public Set<Class<? extends T>> getGroup(String groupName) {
        return errorGroups.get(groupName);
    }

    /**
     * Set the error handler for the specified class, and any subclasses of it.
     *
     * @param errorHandler The error handler to be called when this class or any subclasses
     *                     that do not have their own more specific handler.
     * @param errorClasses The set of classes to set the handler for
     */
    @java.lang.SafeVarargs
    public final synchronized void setHandlerForClasses(
            CycleErrorHandler<T, R> errorHandler,
            Class<? extends T>... errorClasses
    ) {
        for (Class<? extends T> errorClass : errorClasses) {
            logger.debug("handling " + errorClass.getSimpleName() + " with " + errorHandler);
            handlers.put(errorClass, errorHandler);
        }
    }

    /**
     * Set the error handler for a named group of exception classes.
     *
     * @param errorHandler The error handler to be called when this class or any
     *                     subclasses that do not have their own more specific handler.
     * @param groupName    The named group of exception classes
     * @throws RuntimeException if the group name is not found
     */
    public final synchronized void setHandlerForGroup(String groupName, CycleErrorHandler<T, R> errorHandler) {
        Set<Class<? extends T>> classes = errorGroups.get(groupName);
        if (classes == null) {
            throw new RuntimeException("Group name '" + groupName + "' was not found.");
        }
        for (Class<? extends T> errorClass : classes) {
            setHandlerForClasses(errorHandler, errorClass);
        }
    }

    /**
     * Find the matching classes from the recognized classes, and then
     * set the handler for all of them. If the pattern includes only
     * word characters, then it is assumed to be a substring, and only
     * one match is allowed. If the pattern includes any non-word
     * characters, then it is presumed to be a regex, for which multiple
     * classes are allowed to be matched.
     *
     * @param pattern      A substring or regex for the class names
     * @param errorHandler the error handler to be registered for the classes
     */
    public final synchronized void setHandlerForPattern(
            String pattern,
            CycleErrorHandler<T, R> errorHandler
    ) {
        this.findValidClasses(pattern).forEach(
                c -> setHandlerForClasses(errorHandler, c)
        );
    }

    /**
     * Unset all class handlers. This does not reset the default handler.
     */
    public final synchronized void resetAllClassHandlers() {
        handlers.clear();
    }

    /**
     * Return the current list of active handler assignments.
     * @return an unmodifiable {@link Map} of {@link Class} to {@link CycleErrorHandler}.
     */
    public final synchronized Map<Class<? extends T>, CycleErrorHandler<T, R>> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    /**
     * Add to the set of valid classes that will be used when searching for a class
     * by name.
     *
     * @param validClasses The classes that this error handler will search
     */
    @java.lang.SafeVarargs
    public final synchronized void addValidClasses(Class<? extends T>... validClasses) {
        this.validClasses.addAll(Arrays.asList(validClasses));
    }

    public Set<Class<? extends T>> getValidClasses() {
        return Collections.unmodifiableSet(validClasses);
    }

    /**
     * Set the default handler that gets called on any exceptions that do not match a class
     * or super-class specific handler.
     *
     * @param errorHandler The error handler to be called as a last resort.
     * @return this HashedErrorHandler, for method chaining
     */
    public HashedErrorHandler<T, R> setDefaultHandler(CycleErrorHandler<T, R> errorHandler) {
        Objects.requireNonNull(errorHandler);
        defaultHandler = errorHandler;
        return this;
    }

    /**
     * Sets the uppper bound on the Throwable type that you want to consider when
     * walking up the class hierarchy to find a handled supertype. By default,
     * this is simply {@link Throwable}. If the set of types that should be
     * handled directly are more limited than this, you can cause the default handler
     * to trigger when the upper bound type is found if the traversal gets that far.
     *
     * @param upperBound The Throwable subtype which is the lowest subtype to onAfterOpStop
     * @return this, for method chaining.
     */
    public HashedErrorHandler<T, R> setUpperBound(Class<? extends T> upperBound) {
        this.upperBound = upperBound;
        return this;
    }

    private List<Class<? extends T>> findValidClasses(String partialClassName) {
        boolean requireOne = true;
        Pattern p = null;
        if (partialClassName.matches("^\\w+$")) {
            p = Pattern.compile("^.*" + partialClassName + ".*$");
        } else {
            p = Pattern.compile(partialClassName);
            requireOne = false;
        }

        List<Class<? extends T>> matchedClasses = this.validClasses.stream()
                .filter(cn -> cn.getSimpleName().matches("^.*" + partialClassName + ".*$"))
                .collect(Collectors.toList());

        if (matchedClasses.size() == 0) {
            throw new RuntimeException(
                    "Found no matching classes for class name pattern " + partialClassName
                            + ". Valid class names are:\n" + validClasses.stream()
                            .map(Class::getSimpleName)
                            .collect(Collectors.joining("\n", " ", ""))
            );
        }
        if (requireOne && matchedClasses.size() > 1) {
            throw new RuntimeException(
                    "Found " + matchedClasses.size() + " matching classes for class name shortcut '"
                            + partialClassName + "':\n"
                            + matchedClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(",")));
        }
        return matchedClasses;
    }


    /**
     * Handle the error according to the matching error handler for the supplied
     * {@link Throwable} subtype. Handlers of supertypes are used when a specific
     * handler is not found for the reported error class. This means that you
     * can install a default handler for a throwable that is a common parent to
     * your exceptions and have it onAfterOpStop all reported errors by default.
     *
     * <p>
     * The return type is contextual to how this handler class is used. If it is
     * important to use error handler implementations to control flow or other
     * optional execution, then the return type can be used as a form of signaling
     * for that. If you have no need for this, then simply use these classes with
     * a Void result type in the R parameter.
     *
     * @param cycle     The activity cycle for which the error is being handled
     * @param throwable The exception that was thrown or that needs to be handled
     * @param errMsg    A detailed message explaining the error
     * @return the handler result type, depending on the usage context
     */
    @Override
    public R handleError(long cycle, T throwable, String errMsg) {
        Class<?> errorClass = throwable.getClass();
        CycleErrorHandler<T, R> errorHandler = null;
        while (errorHandler == null) {
            errorHandler = handlers.get(errorClass);
            errorClass = errorClass.getSuperclass();
            if (!upperBound.isAssignableFrom(errorClass)) {
                break;
            }
        }
        errorHandler = (errorHandler == null) ? defaultHandler : errorHandler;
        return errorHandler.handleError(cycle, throwable, errMsg);
    }

    public List<String> getGroupNames() {
        return new ArrayList<String>(this.errorGroups.keySet());
    }

}
