package io.nosqlbench.virtdata.library.basics.shared.util;

import io.nosqlbench.nb.api.errors.BasicError;

import java.math.MathContext;

public class MathContextReader {

    public static MathContext getMathContext(String name) {
        try {

            switch (name.toLowerCase()) {
                case "unlimited":
                    return MathContext.UNLIMITED;
                case "decimal32":
                    return MathContext.DECIMAL32;
                case "decimal64":
                    return MathContext.DECIMAL64;
                case "decimal128":
                    return MathContext.DECIMAL128;
                default:
                    return new MathContext(name);
            }
        } catch (IllegalArgumentException iae) {
            throw new BasicError("'" + name + "' was not a valid format for a new MathContext(String), try something " +
                "like 'precision=17 roundingMode=UP");
        }

    }

}
