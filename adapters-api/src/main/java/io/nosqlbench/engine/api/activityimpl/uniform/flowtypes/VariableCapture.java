package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.Map;

/**
 * If an op implements VariableCapture, then it is known to be able to
 * extract variables from its result. Generally speaking, this should
 * be implemented within an Op according to the standard format
 * of {@link ParsedTemplate#getCaptures()}. Any op implementing
 * this should use the interface below to support interop between adapters
 * and to allow for auto documentation tha the feature is supported for
 * a given adapter.
 */
public interface VariableCapture {
    Map<String,?> capture();
}
