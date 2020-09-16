package io.nosqlbench.virtdata.userlibs.streams.fillers;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * A fillable object needs to be filled with data. It may also have owned properties or objects that also need to be
 * filled with data. A type must be fillable if it is to be traversed to find other fillable elements.
 *
 * Fillable elements should expect to have whatever data they contain be replaced when they are filled.
 */
public interface Fillable {
    default List<Fillable> getFillables() {
        return List.of();
    }

    static void fill(Fillable fillable, ByteBufferSource source) {
        if (fillable instanceof ByteBufferFillable) {
            ((ByteBufferFillable) fillable).fill(source);
        } else {
            throw new RuntimeException("Unknown fillable type " + fillable.getClass().getCanonicalName());
        }
        for (Fillable fillableFillable : fillable.getFillables()) {
            fill(fillableFillable, source);
        }
    }
}
