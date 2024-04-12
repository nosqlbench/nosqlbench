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

package io.nosqlbench.adapters.api.activityimpl;

import com.codahale.metrics.Timer;
import groovy.lang.Binding;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.evalctx.*;
import io.nosqlbench.adapters.api.metrics.ThreadLocalNamedTimers;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * See {@link OpDispenser} for details on how to use this type.
 * <p>
 * Some details are tracked per op template, which aligns to the life-cycle of the op dispenser.
 * Thus, each op dispenser is where the stats for all related operations are kept.
 *
 * @param <T>
 *     The type of operation
 */
public abstract class BaseOpDispenser<T extends Op, S> extends NBBaseComponent implements OpDispenser<T>{
    protected final static Logger logger = LogManager.getLogger(BaseOpDispenser.class);
    public static final String VERIFIER = "verifier";
    public static final String VERIFIER_INIT = "verifier-init";
    public static final String EXPECTED_RESULT = "expected-result";
    public static final String VERIFIER_IMPORTS = "verifier-imports";
    public static final String START_TIMERS = "start-timers";
    public static final String STOP_TIMERS = "stop-timers";

    private final String opName;
    protected final DriverAdapter<? extends T, ? extends S> adapter;
    private final NBLabels labels;
    public final Timer verifierTimer;
    private boolean instrument;
    private Timer successTimer;
    private Timer errorTimer;
    private final String[] timerStarts;
    private final String[] timerStops;

    /**
     * package imports used with "verifiers" or "expected-result" are accumulated here
     */
    private final List<String> verifierImports = new ArrayList<>();
    private final List<Class<?>> verifierStaticImports = new ArrayList<>();
    /**
     * optional invokable functions which throw exceptions when results are not verifiable.
     * This variable is kept here for diagnostics and debugging. The actual instance used within
     * each thread is provided by a {@link ThreadLocal} via {@link #getVerifier()}
     */
    private final CycleFunction<Boolean> _verifier;
    private final ThreadLocal<CycleFunction<Boolean>> tlVerifier;

    protected BaseOpDispenser(final DriverAdapter<? extends T, ? extends S> adapter, final ParsedOp op) {
        super(adapter);
        opName = op.getName();
        this.adapter = adapter;
        labels = op.getLabels();

        this.timerStarts = op.takeOptionalStaticValue(START_TIMERS, String.class)
            .map(s -> s.split(", *"))
            .orElse(null);

        this.timerStops = op.takeOptionalStaticValue(STOP_TIMERS, String.class)
            .map(s -> s.split(", *"))
            .orElse(null);

        if (null != timerStarts)
            for (final String timerStart : this.timerStarts) ThreadLocalNamedTimers.addTimer(op, timerStart);

        this.configureInstrumentation(op);
        this.configureVerifierImports(op);
        List<CycleFunction<Boolean>> verifiers = new ArrayList<>();
        verifiers = configureVerifiers(op);
        this._verifier = CycleFunctions.of((a, b) -> a && b, verifiers, true);
        this.tlVerifier = ThreadLocal.withInitial(_verifier::newInstance);
        this.verifierTimer = create().timer(
            "verifier",
            3,
            MetricCategory.Verification,
            "Time verifier execution, if any."
        );
    }

    private CycleFunction<Boolean> cloneVerifiers() {
        return this._verifier.newInstance();
    }

    public CycleFunction<Boolean> getVerifier() {
        return this.tlVerifier.get();
    }

    private void configureVerifierImports(ParsedOp op) {
        List imports = op.takeOptionalStaticValue(VERIFIER_IMPORTS, List.class)
            .orElse(List.of());
        for (Object element : imports) {
            if (element instanceof CharSequence cs) {
                this.verifierImports.add(cs.toString());
            } else {
                throw new RuntimeException("Imports must be a character sequence.");
            }
        }
    }

    private List<CycleFunction<Boolean>> configureVerifiers(ParsedOp op) {
        Binding variables = new Binding();

        Map<String, ParsedTemplateString> initBlocks = op.getTemplateMap().takeAsNamedTemplates(VERIFIER_INIT);
        try {
            initBlocks.forEach((initName, stringTemplate) -> {
                GroovyCycleFunction<?> initFunction =
                    new GroovyCycleFunction<>(initName,stringTemplate,verifierImports,verifierStaticImports,variables);
                logger.info("configured verifier init:" + initFunction);
                initFunction.setVariable("_parsed_op",op);
                initFunction.apply(0L);
            });
        } catch (Exception e) {
            throw new OpConfigError("error in verifier-init:" + e.getMessage(),e);
        }

        Map<String, ParsedTemplateString> namedVerifiers = op.getTemplateMap().takeAsNamedTemplates(VERIFIER);
        List<CycleFunction<Boolean>> verifierFunctions = new ArrayList<>();
        try {
            namedVerifiers.forEach((verifierName,stringTemplate) -> {
                GroovyBooleanCycleFunction verifier =
                    new GroovyBooleanCycleFunction(verifierName, stringTemplate, verifierImports, verifierStaticImports, variables);
                logger.info("configured verifier:" + verifier);
                verifierFunctions.add(verifier);
            });
        } catch (Exception gre) {
            throw new OpConfigError("error in verifier:" + gre.getMessage(), gre);
        }

        try {
             op.takeAsOptionalStringTemplate(EXPECTED_RESULT)
                .map(tpl -> new GroovyObjectEqualityFunction(op.getName()+"-"+EXPECTED_RESULT, tpl, verifierImports, verifierStaticImports, variables))
                .map(vl -> {
                    logger.info("Configured equality verifier: " + vl);
                    return vl;
                })
                 .ifPresent(verifierFunctions::add);
        } catch (Exception gre) {
            throw new OpConfigError("error in verifier:" + gre.getMessage(), gre);
        }

        return verifierFunctions;
    }

    @Override
    public String getOpName() {
        return this.opName;
    }

    public DriverAdapter<? extends T, ? extends S> getAdapter() {
        return this.adapter;
    }

    private void configureInstrumentation(final ParsedOp pop) {
        instrument = pop.takeStaticConfigOr("instrument", false);
        if (this.instrument) {
            final int hdrDigits = pop.getStaticConfigOr("hdr_digits", 4);

            successTimer = create().timer(
                "successfor_"+getOpName(),
                hdrDigits,
                MetricCategory.Core,
                "Successful result timer for specific operation '" + pop.getName() + "'"
            );
            errorTimer = create().timer(
                "errorsfor_"+getOpName(),
                hdrDigits,
                MetricCategory.Core,
                "Errored result timer for specific operation '" + pop.getName() + "'"
            );
        }
    }

    @Override
    public void onStart(final long cycleValue) {
        if (null != timerStarts) ThreadLocalNamedTimers.TL_INSTANCE.get().start(this.timerStarts);
    }

    @Override
    public void onSuccess(final long cycleValue, final long nanoTime) {
        if (this.instrument) {
            this.successTimer.update(nanoTime, TimeUnit.NANOSECONDS);
        }
        if (null != timerStops) ThreadLocalNamedTimers.TL_INSTANCE.get().stop(this.timerStops);
    }

    @Override
    public void onError(final long cycleValue, final long resultNanos, final Throwable t) {

        if (this.instrument) this.errorTimer.update(resultNanos, TimeUnit.NANOSECONDS);
        if (null != timerStops) ThreadLocalNamedTimers.TL_INSTANCE.get().stop(this.timerStops);
    }

    @Override
    public NBLabels getLabels() {
        return this.labels;
    }

    @Override
    public final T apply(long value) {
        T op = getOp(value);
        return op;
    }
}
