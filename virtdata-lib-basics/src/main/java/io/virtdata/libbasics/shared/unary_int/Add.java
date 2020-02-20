package io.virtdata.libbasics.shared.unary_int;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Adds a value to the input.
 */
@ThreadSafeMapper
public class Add implements IntUnaryOperator {

    private int addend;

    @Example({"Add(23)", "adds integer 23 to the input integer value"})
    public Add(int addend) {
        this.addend = addend;
    }

    @Override
    public int applyAsInt(int operand) {
        return operand + addend;
    }
}
