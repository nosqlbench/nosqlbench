package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.junit.Test;

import java.util.Set;
import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SetFunctionsTest {

    @Test
    public void testSetFunctions() {
        SetFunctions f1 = new SetFunctions((LongUnaryOperator) i -> i, (LongFunction<Double>) j -> (double) j);
        Set<Object> set = f1.apply(3);
        System.out.println(set);
    }

    @Test
    public void testSetHashed() {
        SetHashed f1 = new SetHashed((DoubleUnaryOperator) i -> i, (DoubleToLongFunction) i -> (long) i);
        Set<Object> set = f1.apply(2L);
        assertThat(set).contains(8.2188818279493642E18, 3417914777143645696L);
    }

    @Test
    public void testSetSized() {
        SetSized f1 = new SetSized((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("3"); // This is because there is no stepping on SetSized
    }

    @Test
    public void testSetSizedHashed() {
        SetSizedHashed f1 = new SetSizedHashed((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("860564144","1556714733", "745054359");
    }

    @Test
    public void testSetSizedStepped() {
        SetSizedStepped f1 = new SetSizedStepped((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("3","4","5");
    }

    @Test
    public void testStepped() {
        SetStepped f1 = new SetStepped((LongToIntFunction) i -> (int)i, (IntFunction<String>) String::valueOf);
        Set<Object> set = f1.apply(3L);
        assertThat(set).contains("4",3);
        //This is because there is no sizing function. Both functions are value functions
        //And whatever type they produce is put into the set of objects
    }

}