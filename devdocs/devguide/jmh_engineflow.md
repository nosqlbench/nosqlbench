# Engine-flow JMH microbenchmarks

There is a JMH suite intended to quantify the steady-state overhead of the new stride/flow context plumbing:

- `OpFlowContext` access + reset
- `OpFlowState` slot selection + begin/reset
- `FlowContextAwareOp` data flow between ops via `CapturingOp` and a simple consumer
- space mapping behavior via `ConcurrentIndexCacheWrapperWithName`
- stride batching via `StrideAction.runStride(...)` and `StrideContext` lifecycle
- a minimal end-to-end `StrideMotor` “single segment” run (with stub `Input`/`StrideAction`)

## Benchmark classes

- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/sysperf/engineflow/OpFlowContextBench.java`
- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/sysperf/engineflow/OpFlowStateBench.java`
- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/sysperf/engineflow/CapturingOpFlowPipelineBench.java`
- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/sysperf/engineflow/SpaceMappingBench.java`
- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/sysperf/engineflow/StrideActionRunStrideBench.java`
- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/sysperf/engineflow/StrideMotorSingleSegmentBench.java`
- `nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/actions/StrideContextJmhBench.java`

## Running

The simplest way is to run the JUnit microbench test so `mvn test` can select it:

- Test: `io.nosqlbench.engine.api.activityapi.sysperf.engineflow.EngineFlowJmhTest`
- Tags: `microbench`, `engine`
- Default include regex: `.*(engineflow\\..*Bench|StrideContextJmhBench).*`

### Maven `test` examples

Run only the engine-flow JMH suite:

```bash
mvn -pl nb-engine/nb-engine-core -Pmicrobench -Dtest=EngineFlowJmhTest test
```

Run all microbench-tagged tests:

```bash
mvn -pl nb-engine/nb-engine-core -Pmicrobench test
```

You can also set `-Dnb.junit.tags=microbench` instead of `-Pmicrobench`.
Offline variants: add `-o` (requires the needed plugin/deps to already be present in your local repo).
