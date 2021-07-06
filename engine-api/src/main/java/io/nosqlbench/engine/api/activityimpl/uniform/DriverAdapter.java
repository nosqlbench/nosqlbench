package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;

import java.util.Map;
import java.util.function.Function;

/**
 * <P>The DriverAdapter interface is expected to be the replacement
 * for the current {@link ActivityType}. This interface takes a simpler
 * approach than the historic NoSQLBench approach. Specifically,
 * all of the core logic which was being pasted into each
 * driver type is centralized, and only the necessary interfaces
 * needed for construction new operations are exposed.
 * </P>
 *
 * This
 *
 * @param <R>
 */
public interface DriverAdapter<R extends Runnable> {


    /**
     * An Op Mapper is a function which can look at the parsed
     * fields in a {@link ParsedCommand} and create an OpDispenser.
     * An OpDispenser is a function that will produce an special
     * type {@link R} that this DriverAdapter implements as its
     * op implementation.
     *
     * @return a synthesizer function for {@link R} op generation
     */
    Function<ParsedCommand, OpDispenser<R>> getOpMapper();

    Function<Map<String,Object>,Map<String,Object>> getPreprocessor();

    /**
     * The op parsers do additional semantic parsing work on behalf
     * of the user, according to the rules set by the driver adapter
     * dev. These rules can, for example, look at a single template
     * formatted field and break it apart into multiple other fields
     * which are used to directly drive the construction of an operation.
     * By doing this, it is possible to mark static text segments
     * and dynamic insertion points separately, and thus allow the
     * developer to create optimal construction patterns depending
     * how much is known. For details on this, see TBD: A section
     * yet to be written on how to do this the easy way.
     *
     * If any of these returns a non-null result, then the map is added
     * to the fields in the op template, and the stmt field is removed.
     *
     * @return optional rewrite rules for op stmt field
     */
}
