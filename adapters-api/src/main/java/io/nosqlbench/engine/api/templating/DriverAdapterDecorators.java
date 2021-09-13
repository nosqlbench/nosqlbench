package io.nosqlbench.engine.api.templating;

/**
 * This type simply captures (by extension) any optional decorator
 * interfaces which may be implemented by a {@link io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter}.
 * Thus, it is mostly for documentation.
 *
 * Decorator interfaces are used within NoSQLBench where implementations are truly optional,
 * and thus would cloud the view of a developer implementing strictly to requirements.
 *
 * You can find any such decorator interfaces specific to driver adapters by looking for
 * all implementations of this type.
 */
public interface DriverAdapterDecorators {
}
