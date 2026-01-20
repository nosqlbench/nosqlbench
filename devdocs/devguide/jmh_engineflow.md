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

The simplest way is to run the runner from your IDE after building the module:

- Runner: `io.nosqlbench.engine.api.activityapi.sysperf.engineflow.EngineFlowPerfBaseliner`
- Optional arg0: a JMH include regex (defaults to `.*(engineflow\\..*Bench|StrideContextJmhBench).*`)
- Optional arg1: a JMH results file (does not suppress console output)

Offline build (no downloads) to compile the benchmarks:

```bash
mvn -o -pl nb-engine/nb-engine-core -am test -DskipTests
```

### Maven `exec:java` examples

In a multi-module reactor, `exec:java` runs once per module; use a build step + a single-module exec step (chained with `&&`) so the exec only happens for `nb-engine-core`:

```bash
mvn -pl nb-engine/nb-engine-core -am -DskipTests test-compile \
  && mvn -pl nb-engine/nb-engine-core -DskipTests exec:java \
    -Dexec.mainClass=io.nosqlbench.engine.api.activityapi.sysperf.engineflow.EngineFlowPerfBaseliner
```

Run the baseliner with an explicit include regex and a JMH results file:

```bash
mvn -pl nb-engine/nb-engine-core -am -DskipTests test-compile \
  && mvn -pl nb-engine/nb-engine-core -DskipTests exec:java \
    -Dexec.mainClass=io.nosqlbench.engine.api.activityapi.sysperf.engineflow.EngineFlowPerfBaseliner \
    -Dexec.args='\".*engineflow\\\\..*Bench.*\" /tmp/jmh_engineflow.json'
```

List available benchmarks (JMH `-l`):

```bash
mvn -pl nb-engine/nb-engine-core -am -DskipTests test-compile \
  && mvn -pl nb-engine/nb-engine-core -DskipTests exec:java \
    -Dexec.mainClass=org.openjdk.jmh.Main -Dexec.args='-l'
```

Run only the engine-flow benchmarks (JMH include regex):

```bash
mvn -pl nb-engine/nb-engine-core -am -DskipTests test-compile \
  && mvn -pl nb-engine/nb-engine-core -DskipTests exec:java \
    -Dexec.mainClass=org.openjdk.jmh.Main \
    -Dexec.args='-wi 2 -i 3 -f 1 \".*(engineflow\\\\..*Bench|StrideContextJmhBench).*\"'
```

Offline variants: add `-o` to both Maven invocations (requires the needed plugin/deps to already be present in your local repo).
