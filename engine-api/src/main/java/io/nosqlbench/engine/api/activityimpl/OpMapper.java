package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.templating.ParsedCommand;

import java.util.function.Function;

/**
 * <p>
 * <h2>Synopsis</h2>
 * An OpMapper is responsible for converting parsed op templates
 * into dispensers of operations. the intention of the user,
 * Op Templates as expressed as a set of field values, some literal, and
 * some virtualized (to be generated per-cycle). The op template is
 * parsed into a {@link ParsedCommand}.
 * </p>
 *
 * <p>
 * <h2>Concepts</h2>
 * The OpMapper is a function (the op mapper) that returns another function (the op synthesizer).
 * The returned function is then used to create actual operations in some executable form.
 * The difference
 * between the OpMapper and the OpDispenser is this: The OpMapper is responsible for
 * identifying exactly what type of operation the user intends, according to the rules
 * op construction documented by the driver maintainer. The OpDispenser is responsible
 * for efficiently dispensing objects of a given type which can be used to execute an
 * operation. In short, mapping op templates to the users' intention must happen first, and
 * then building an operation efficiently with that specific knowledge can happen after.
 * </p>
 *
 * <p>
 * <h2>Documentation Requirements</h2>
 * The logic which is implemented in the OpMapper must follow closely with the op construction
 * rules provided to the user. Conversely, the driver maintainer should take care to provide
 * rules of construction and examples in the documentation.
 * Each {@link io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter} has a unique
 * name, just as with {@link io.nosqlbench.engine.api.activityapi.core.ActivityType}s. The documentation
 * for each of these should be kept in the bundled resources in a top-level markdown file that
 * matches the driver name.
 * </p>
 *
 * @param <T> The parameter type of the actual operation which will be used
 *            to hold all the details for executing an operation,
 *            generally something that implements {@link Runnable}.
 */
public interface OpMapper<T extends Runnable> extends Function<ParsedCommand, OpDispenser<T>> {

    /**
     * Interrogate the parsed command, and provide a new
     *
     * @param cmd The {@link ParsedCommand} which is the parsed version of the user-provided op template.
     *            This contains all the fields provided by the user, as well as explicit knowledge of
     *            which ones are static and dynamic.
     * @return An OpDispenser which can be used to synthesize real operations.
     */
    @Override
    OpDispenser<T> apply(ParsedCommand cmd);
}
