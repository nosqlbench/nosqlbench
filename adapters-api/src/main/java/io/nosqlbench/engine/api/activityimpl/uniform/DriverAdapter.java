package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.Map;
import java.util.function.Function;

/**
 * <P>The DriverAdapter interface is expected to be the replacement
 * for ActivityTypes. This interface takes a simpler
 * approach. Specifically, all of the core logic which was being pasted into each
 * driver type is centralized, and only the necessary interfaces
 * needed for construction new operations and shared context are exposed.
 * This means all drivers can now benefit from cross-cutting enhancements
 * in the core implementation.
 * </P>
 *
 * @param <R> The type of Runnable operation which will be used to wrap
 *            all operations for this driver adapter. This allows you to
 *            add context or features common to all operations of this
 *            type.
 * @param <S> The type of context space used by this driver to hold
 *            cached instances of clients, session, or other native driver
 *            esoterica. This is the shared state which might be needed
 *            during construction of R type operations, or even for individual
 *            operations.
 */
public interface DriverAdapter<R extends Op, S> {


    /**
     * <p>
     * <H2>Op Mapping</H2>
     * An Op Mapper is a function which can look at the parsed
     * fields in a {@link ParsedCommand} and create an OpDispenser.
     * An OpDispenser is a function that will produce a special
     * type {@link R} that this DriverAdapter implements as its
     * op implementation.</p>
     *
     * <p>
     * The function that is returned is responsible for creating another function.
     * This might seem counter-intuitive but it is very intentional because
     * of these design constraints:
     * <UL>
     * <LI>Mapping op semantics to a type of operation must be very clear
     * and flexible. Performance is not important at this layer because this is all done
     * during initialization time for an activity.</LI>
     * <LI>Synthesizing executable operations from a known type of operational template
     * must be done very efficiently. This part is done during activity execution, so
     * having the details of how you are going to create an op for execution already
     * sorted out is important.</LI>
     * </UL>
     *
     * To clarify the distinction between these two phases, the first is canonically
     * called <em>op mapping</em> in the documentation. The second is called
     * <em>op synthesis</em>.
     * </p>
     *
     * <p>
     * <H2>A note on implementation strategy:</H2>
     * Generally speaking, implementations of this method should interrogate the op fields
     * in the ParsedCommand and return an OpDispenser that matches the user's intentions.
     * This can be based on something explicit, like the  value of a {@code type} field,
     * or it can be based on whether certain fields are present or not. Advanced implementations
     * might take into account which fields are provided as static values and which are
     * specified as bindings. In any case, the op mapping phase is meant to qualify and
     * pre-check that the fields provided are valid and specific for a given type of operation.
     * What happens within {@link OpDispenser} implementations (the second phase), however, should do
     * as little qualification of field values as possible, focusing simply on constructing
     * the type of operation for which they are designed.
     * </p>
     *
     * @return a synthesizer function for {@link R} op generation
     */
    OpMapper<R> getOpMapper();

    /**
     * The preprocessor function allows the driver adapter to remap
     * the fields in the op template before they are interpreted canonically.
     * At this level, the transform is applied once to the input map
     * (once per op template) to yield the map that is provided to
     * {@link io.nosqlbench.engine.api.activityimpl.OpMapper} implementations.
     *
     * @return A function to pre-process the op template fields.
     */
    default Function<Map<String, Object>, Map<String, Object>> getPreprocessor() {
        return f -> f;
    }

    /**
     * When a driver needs to identify an error uniquely for the purposes of
     * routing it to the correct error handler, or naming it in logs, or naming
     * metrics, override this method in your activity.
     *
     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
     */
    default Function<Throwable, String> getErrorNameMapper() {
        return t -> t.getClass().getSimpleName();
    }

    /**
     * The cache of all objects needed within a single instance
     * of a DriverAdapter which are not operations. These are generally
     * things needed by operations, or things needed during the
     * construction of operations.
     *
     * See {@link DriverSpaceCache} for details on when and how to use this function.

     * <p>During Adapter Initialization, Op Mapping, Op Synthesis, or Op Execution,
     * you may need access to the objects in (the or a) space cache. You can build the
     * type of context needed and then provide this function to provide new instances
     * when needed.</p>
     *
     * @return A cache of named objects
     */
    DriverSpaceCache<? extends S> getSpaceCache();

    /**
     * @return A function which can initialize a new S
     */
    default Function<String, ? extends S> getSpaceInitializer() {
        return n -> null;
    }


    NBConfiguration getConfiguration();
}
