package io.virtdata.core;

import org.junit.Test;

import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolvedFunctionTest {

    @Test
    public void testToStringWithVarArgs() {
        try {
            TestAdd testAdd = new TestAdd(1, 2, 3);
            Class<?>[] parameterTypes = TestAdd.class.getConstructor(int.class, int[].class).getParameterTypes();
            ResolvedFunction rf = new ResolvedFunction(testAdd, true, parameterTypes, new Object[]{1, 2, 3}, long.class, long.class);
            assertThat(rf.toString()).isEqualTo("long->io.virtdata.core.ResolvedFunctionTest$TestAdd->long [Integer=>int,Integer...=>int...]");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testToStringWithEmptyVarArgs() {
        try {
            TestAdd testAdd = new TestAdd(1);
            Class<?>[] parameterTypes = TestAdd.class.getConstructor(int.class, int[].class).getParameterTypes();
            ResolvedFunction rf = new ResolvedFunction(testAdd, true, parameterTypes, new Object[]{1, 2, 3}, long.class, long.class);
            assertThat(rf.toString()).isEqualTo("long->io.virtdata.core.ResolvedFunctionTest$TestAdd->long [Integer=>int,Integer...=>int...]");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final static class TestAdd implements LongUnaryOperator {

        private final int a;
        private final int[] b;

        public TestAdd(int a, int... b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public long applyAsLong(long operand) {
            return a + operand;
        }
    }
}