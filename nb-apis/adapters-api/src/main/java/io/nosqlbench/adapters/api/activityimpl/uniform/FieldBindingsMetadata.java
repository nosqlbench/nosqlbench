package io.nosqlbench.adapters.api.activityimpl.uniform;

import java.util.Map;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.nb.api.components.core.NBNamedElement;
import io.nosqlbench.virtdata.core.templates.BindPoint;

/// This optional type allows for [OpDispenser] (or other) implementations to
/// map native field names to their associated binding names. Often, the
/// adapter-native logic is the only place this association can be derived, although
/// it is sometimes needed in core adapter-agnostic logic.
public interface FieldBindingsMetadata<FIELDTYPE> {

    /// Get the map of native fields to bind points.
    /// The bind points don't need to be the same actual object which is used, but both the
    /// field names and the binding points should be equivalent as in [Object#equals].
    /// @return an ordered map of native driver/client fields to their associated bindpoints.
    Map<String, BindPoint> getFieldBindingsMap();

}
