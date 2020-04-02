package io.nosqlbench.virtdata.api.bindings;

/**
 * A public class which holds global values. This is used for holding
 * an intentional type of unset which is different than null.
 * For downstream consumer libraries which need to distinguish between
 * unset and null, this is that value.
 */
public enum VALUE {
    unset // unset does not mean Null.
}
