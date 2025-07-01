package io.nosqlbench.adapters.api.activityimpl.uniform;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.checkerframework.checker.units.qual.K;

public interface SpaceCache<S extends Space> extends Iterable<S>, NBComponent {
  S get(Object key);
}
