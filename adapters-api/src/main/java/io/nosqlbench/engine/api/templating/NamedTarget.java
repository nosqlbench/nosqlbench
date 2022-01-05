package io.nosqlbench.engine.api.templating;

import java.util.function.LongFunction;

/**
 * <p>The result type from calling {@link ParsedOp#getRequiredTypeFromEnum(Class)}, which
 * captures the matching enum type as well as the field name and a value function.</p>
 *
 * <p>The <em>enumId</em> is type-safe enum value from the provided enum to the above method.
 * The <em>field</em> is the field name which was passed. The <em>targetFunction</em> is
 * a {@link LongFunction} of Object which can be called to return an associated target value.</p>
 *
 * For example, with an emum like <pre>{@code
 * public enum LandMovers {
 *     BullDozer,
 *     DumpTruck
 * }
 * }</pre>
 *
 * and a parsed op like <pre>{@code
 * (json)
 * {
 *  "op": {
 *   "bulldozer": "{dozerid}"
 *   }
 * }
 *
 * (yaml)
 * op:
 *  bulldozer: "{dozerid}
 * }</pre>
 * the result will be returned with the following: <pre>{@code
 *  enumId: BullDozer
 *  field: bulldozer
 *  targetFunction: (long l) -> ...
 * }</pre>
 * @param <E>
 */
public class NamedTarget<E extends Enum<E>> {
    public final E enumId;
    public final String field;
    public final LongFunction<?> targetFunction;

    public NamedTarget(E enumId, String matchingOpFieldName, LongFunction<?> value) {
        this.enumId = enumId;
        this.field = matchingOpFieldName;
        this.targetFunction = value;
    }
}
