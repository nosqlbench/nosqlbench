package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;

import java.util.Map;
import java.util.function.Function;

/**
 * <P>The DriverAdapter interface is expected to be the replacement
 * for the current {@link ActivityType}. This interface takes a simpler
 * approach. Specifically, all of the core logic which was being pasted into each
 * driver type is centralized, and only the necessary interfaces
 * needed for construction new operations and shared context are exposed.
 * This means all drivers can now benefit from cross-cutting enhancements
 * in the core implementation.
 * </P>
 *
 * @param <R> The type of Runnable operation which will be used to wrap
 *           all operations for this driver adapter. This allows you to
 *           add context or features common to all operations of this
 *           type.
 * @param <S> The type of context space used by this driver to hold
 *           cached instances of clients, session, or other native driver
 *           esoterica. This is the shared state which might be needed
 *           during construction of R type operations, or even for individual
 *           operations.
 */
public interface DriverAdapter<R extends Runnable, S> {


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

    /**
     * The preprocessor function allows the driver adapter to remap
     * the fields in the op template before they are interpreted canonically.
     * At this level, the transform is applied once to the input map
     * (once per op template) to yield the map that is provided to
     * {@link io.nosqlbench.engine.api.activityimpl.OpMapper} implementations.
     * @return A function to pre-process the op template fields.
     */
    default Function<Map<String,Object>,Map<String,Object>> getPreprocessor() {
        return f->f;
    }

    /**
     * When a driver needs to identify an error uniquely for the purposes of
     * routing it to the correct error handler, or naming it in logs, or naming
     * metrics, override this method in your activity.
     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
     */
    default Function<Throwable,String> getErrorNameMapper() {
        return t -> t.getClass().getSimpleName();
    }

    /**
     * During Adapter Initialization, Op Mapping, Op Synthesis, or
     * Op Execution, you may need access to some shared context that
     * could change over time. You can build the type of context
     * needed and then provide this function to provide new instances
     * when needed.
     *
     * These instance are generally called <em>spaces</em> and are
     * cached in a {@link DriverSpaceCache<S>}.
     *
     * @return A function which can initialize a new S
     */
    default Function<String,? extends S> getSpaceInitializer() {
        return n -> null;
    }

    /**
     * The cache of all objects needed within a single instance
     * of a DriverAdapter which are not operations. These are generally
     * things needed by operations, or things needed during the
     * construction of operations.
     * @return A cache of named objects
     */
    DriverSpaceCache<? extends S> getSpaceCache();

}
