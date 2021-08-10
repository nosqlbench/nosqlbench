package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

/**
 * <p>If an Op implements OpGenerator, then it will be asked for chained
 * operations that are secondary unless or until {@link #getNextOp()}}
 * returns null.</p>
 *
 * <p>If an Op *might* generate a secondary operation, then it should implement
 * this interface and simply return null in the case that there is none.</p>
 *
 * <p>If you need to run many operations after this, then you can keep the
 * state of these in the same Op implementation and simply keep returning
 * it until the work list is done. The same applies for structured op
 * generation, such as lists of lists or similar.</p>
 *
 */
public interface OpGenerator {
    Op getNextOp();
}
