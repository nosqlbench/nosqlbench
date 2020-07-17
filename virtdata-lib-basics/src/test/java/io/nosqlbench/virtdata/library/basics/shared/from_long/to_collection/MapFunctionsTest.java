package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.junit.Test;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class MapFunctionsTest {

    @Test
    public void testMapFunctions() {
        MapFunctions mf1 = new MapFunctions((LongUnaryOperator) l -> l, (LongUnaryOperator) m -> m);
        Map<Object, Object> mv1 = mf1.apply(3L);
        assertThat(mv1).containsAllEntriesOf(Map.of(3L, 3L));

        MapFunctions mf2 = new MapFunctions(
                (LongFunction<String>) a -> "Ayyy",
                (LongFunction<String>) b -> "Byyy",
                (LongFunction<Double>) c -> 123.456d,
                (LongFunction<Double>) d -> 789.1011d);
        Map<Object, Object> mv2 = mf2.apply(13L);
        assertThat(mv2).containsAllEntriesOf(Map.of("Ayyy", "Byyy", 123.456d, 789.1011d));
    }

    @Test
    public void testMapSized() {
        MapSized mf1 = new MapSized(
                (LongUnaryOperator) s -> s,
                (LongFunction<Double>) c -> 123.456d,
                (LongFunction<Double>) d -> (double) d,
                (LongFunction<String>) String::valueOf,
                (LongFunction<String>) b -> "Byyy"
        );
        Map<Object, Object> mv1 = mf1.apply(5L);
        assertThat(mv1).containsAllEntriesOf(Map.of(
                "5", "Byyy",
                123.456d, 5.0
        ));

        // Notice that the trailing function is used repeatedly, which affects
        // when duplicate values occur, as compared to the above function.
        MapSized mf2 = new MapSized(
                (LongUnaryOperator) s -> s,
                (LongFunction<String>) String::valueOf,
                (LongFunction<String>) b -> "Byyy",
                (LongFunction<Double>) c -> 123.456d,
                (LongFunction<Double>) d -> (double) d);
        Map<Object, Object> mv2 = mf2.apply(5L);
        assertThat(mv2).containsAllEntriesOf(Map.of(
                "5", "Byyy",
                123.456d, 5.0
        ));
    }

    @Test
    public void testMapSizedStepped() {
        MapSizedStepped mf2 = new MapSizedStepped(
                (LongUnaryOperator) s -> s,
                (LongFunction<Double>) a -> 123.456d,
                (LongFunction<String>) b -> "Byyy",
                (LongFunction<String>) String::valueOf,
                (LongFunction<Double>) d -> (double) d);
        Map<Object, Object> mv2 = mf2.apply(5L);
        assertThat(mv2).containsAllEntriesOf(Map.of(
                123.456, "Byyy",
                "6", 6.0d,
                "7", 7.0d,
                "8", 8.0d,
                "9", 9.0d
        ));
    }

    @Test
    public void testMapStepped() {
        MapStepped mf2 = new MapStepped(
                (LongFunction<String>) String::valueOf,
                (LongFunction<Double>) d -> (double) d,
                (LongFunction<String>) String::valueOf,
                (LongFunction<Double>) d -> (double) d
        );
        Map<Object, Object> mv2 = mf2.apply(5L);
        assertThat(mv2).containsAllEntriesOf(Map.of(
                "5", 5.0d,
                "6", 6.0d
        ));

    }

    @Test
    public void testMapHashed() {
        MapHashed mf2 = new MapHashed(
                (LongFunction<String>) String::valueOf,
                (LongFunction<String>) String::valueOf,
                (LongFunction<String>) String::valueOf,
                (LongFunction<String>) String::valueOf
        );
        Map<Object, Object> mv2 = mf2.apply(5L);
        assertThat(mv2).containsAllEntriesOf(Map.of(
                "4464361019114304900","4464361019114304900",
                "44643610191143049001","44643610191143049001"
        ));

    }

    @Test
    public void testMapSizedHashed() {
        MapSizedHashed mf2 = new MapSizedHashed(
                (LongUnaryOperator) s -> s,
                (LongFunction<String>) String::valueOf,
                (LongFunction<String>) String::valueOf);
        Map<Object, Object> mv2 = mf2.apply(5L);
        assertThat(mv2).containsAllEntriesOf(
                Map.of(
                        "4464361019114304900", "4464361019114304900"
                )
        );

    }

}