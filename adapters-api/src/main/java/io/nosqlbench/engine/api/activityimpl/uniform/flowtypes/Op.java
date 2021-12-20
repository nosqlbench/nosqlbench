package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

/**
 * This is the root type of any operation which is used in a NoSQLBench
 * DriverAdapter. It is a tagging interface for incremental type validation
 * in the NB runtime. You probably don't want to use it directly.
 *
 * Instead, use these:
 * <ul>
 *  <li>{@link CycleOp}</li> - An interface that will called if there is nothing to consume
 *  the result type from your operation. In some cases preparing a result body to
 *  hand down the chain is more costly, so implementing this interface allows ...
 * </ul>
 *
 * either {@link CycleOp} or {@link ChainingOp} (but not both!)
 *
 * In the standard flow of an activity, either of the above interfaces is called
 * so long as an Op implements one of them.
 */
public interface Op {
}
