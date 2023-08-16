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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.api.config.standard.NBMapConfigurable;
import io.nosqlbench.api.config.params.Element;
import io.nosqlbench.api.config.params.NBParams;

import java.util.*;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBErrorHandler {

    private final Supplier<ErrorMetrics> errorMetricsSupplier;
    private final Supplier<String> configSpecSupplier;
    private final Function<Throwable, String> namer;
    private final Map<String, List<ErrorHandler>> handlerCache = new ConcurrentHashMap<>();
    private final List<HandlerMapping> configs = new ArrayList<>();

    public NBErrorHandler(Supplier<String> configSpecSupplier, Supplier<ErrorMetrics> metricsSupplier) {
        this(configSpecSupplier, metricsSupplier, throwable -> throwable.getClass().getSimpleName());
    }

    public NBErrorHandler(Supplier<String> configSpecSupplier, Supplier<ErrorMetrics> metricsSupplier, Function<Throwable,String> namer) {
        this.errorMetricsSupplier = metricsSupplier;
        this.configSpecSupplier = configSpecSupplier;
        this.namer = namer;

        Arrays.stream(configSpecSupplier.get().split(";"))
            .map(HandlerMapping::new)
            .forEach(configs::add);
    }

    public ErrorDetail handleError(Throwable throwable, long cycle, long nanosIntoOp) {
        String errorName = namer.apply(throwable);
//        String errorName = t.getClass().getSimpleName();
        List<ErrorHandler> handlers = handlerCache.get(errorName);
        ErrorDetail detail = ErrorDetail.ERROR_NONRETRYABLE;

        if (handlers == null) {
            handlers = lookup(errorName);
            handlerCache.put(errorName, handlers);
        }

        boolean retry = false;
        for (ErrorHandler handler : handlers) {
            detail = handler.handleError(errorName, throwable, cycle, nanosIntoOp, detail);
        }
        return detail;
    }

    private synchronized List<ErrorHandler> lookup(String errorName) {
        for (HandlerMapping config : configs) {
            for (Pattern errorPattern : config.matchers) {
                if (errorPattern.matcher(errorName).matches()) {
                    if (config.getResolved() == null) {
                        List<ErrorHandler> handlers = new ArrayList<>();
                        for (Element handlerCfg : config.getHandlerCfgs()) {
                            ErrorHandler handler = getHandler(handlerCfg);
                            handlers.add(handler);
                        }
                        config.setResolved(handlers);
                    }
                    return config.getResolved();
                }
            }
        }
        throw new RuntimeException("Unable to find a configured error handler for error '" + errorName + "'");
    }

    private ErrorHandler getHandler(Element cfg) {
        String name = cfg.get("handler", String.class).orElseThrow();
        LinkedHashMap<String, ServiceLoader.Provider<ErrorHandler>> providers = getProviders();
        Provider<ErrorHandler> provider = providers.get(name);
        if (provider == null) {
            throw new RuntimeException("ErrorHandler named '" + name + "' could not be found in " + providers.keySet());
        }
        ErrorHandler handler = provider.get();
        if (handler instanceof NBMapConfigurable) {
            ((NBMapConfigurable) handler).applyConfig(cfg.getMap());
        }
        if (handler instanceof ErrorMetrics.Aware) {
            ((ErrorMetrics.Aware) handler).setErrorMetricsSupplier(errorMetricsSupplier);
        }
        return handler;
    }

    private synchronized static LinkedHashMap<String, ServiceLoader.Provider<ErrorHandler>> getProviders() {
        ServiceLoader<ErrorHandler> loader = ServiceLoader.load(ErrorHandler.class);

        LinkedHashMap<String, ServiceLoader.Provider<ErrorHandler>> providers;
        providers = new LinkedHashMap<>();

        loader.stream().forEach(provider -> {
            Class<? extends ErrorHandler> type = provider.type();
            if (!type.isAnnotationPresent(Service.class)) {
                throw new RuntimeException(
                    "Annotator services must be annotated with distinct selectors\n" +
                        "such as @Service(Annotator.class,selector=\"myimpl42\")"
                );
            }
            Service service = type.getAnnotation(Service.class);
            if (service.selector().isBlank()) {
                throw new RuntimeException("Services of type ErrorHandler must include the selector property in the Service annotation.");
            }
            providers.put(service.selector(), provider);
        });

        return providers;
    }

    public static class HandlerMapping {

        private final static Pattern pattern = Pattern.compile("((?<matchers>[\\w\\d-_.*+,]+):)?(?<handlers>.*)");
        private final static Pattern leadWord = Pattern.compile(
            "(?<front>([^{},]+)|(\\{[^{}]+?\\}))(,(?<rest>.*))?"
        );
        private final List<Pattern> matchers = new ArrayList<>();
        private final List<Element> params = new ArrayList<>();
        private List<ErrorHandler> resolved;

        public HandlerMapping(String spec) {
            Matcher matcher = pattern.matcher(spec);
            if (!matcher.matches()) {
                throw new RuntimeException("Unable to match pattern for handler spec with '" + spec + "'");
            }
            String gmatchers = matcher.group("matchers");
            String ghandlers = matcher.group("handlers");
            String[] matcherspecs = (gmatchers != null) ? gmatchers.split(",") : new String[]{".*"};
            Arrays.stream(matcherspecs).map(Pattern::compile).forEach(matchers::add);

            while (ghandlers.length() > 0) {
                Matcher leadmatch = leadWord.matcher(ghandlers);
                if (leadmatch.matches()) {
                    String word = leadmatch.group("front");
                    ghandlers = leadmatch.group("rest") == null ? "" : leadmatch.group("rest");
                    Element next = null;
                    if (word.matches("\\d+")) {
                        next = NBParams.one(null,"handler=code code=" + word);
                    } else if (word.matches("[a-zA-Z]+")) {
                        next = NBParams.one(null,"handler=" + word);
                    } else {
                        next = NBParams.one(null,word);
                    }
                    params.add(next);
                } else {
                    throw new RuntimeException("Unable to get initial element from '" + gmatchers + "'");
                }
            }

        }

        public List<Pattern> getErrorMatchers() {
            return this.matchers;
        }

        public List<Element> getHandlerCfgs() {
            return this.params;
        }

        public List<ErrorHandler> getResolved() {
            return resolved;
        }

        public void setResolved(List<ErrorHandler> resolved) {
            this.resolved = resolved;
        }
    }
}
