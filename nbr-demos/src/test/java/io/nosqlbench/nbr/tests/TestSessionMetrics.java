package io.nosqlbench.nbr.tests;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.codahale.metrics.Gauge;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivity;
import io.nosqlbench.engine.core.lifecycle.commands.CMD_await;
import io.nosqlbench.engine.core.lifecycle.commands.CMD_start;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.session.NBCommandInvoker;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBComponentTraversal;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSessionMetrics {

  /// This test runs a number of commands which spawn activities and a number of spaces.
  /// When they are all complete, the whole component tree is checked for any remaining activities
  /// or spaces, which should be zero in both cases.
  @Test
  public void testSessionResourceRecycling() {
    int totalActivities = 5;
    NBBufferedContainer context = NBBufferedContainer.builder().name("test_NB_session_metrics")
        .build(TestComponent.EMPTY_COMPONENT);
    String template = """
        ops:
         op1:
          op:
           space: NumberNameToString();
           stmt: "cycle={{Identity()}}\\n"
        """;

    Map<String, String> activitydef1 = Map.of(
        "alias",
        "activity_error",
        "driver",
        "stdout",
        "cycles",
        "0..5",
        "threads",
        "1",
        "rate",
        "1",
        "workload",
        template
    );


    List<String> aliases = new ArrayList<>();
    String targetScenario="default";
    for (int i = 0; i < totalActivities; i++) {
      System.out.println("RUN " + i);
      System.out.flush();
      String alias = "step_" + i;
      aliases.add(alias);
      CMD_start started = new CMD_start(context, alias, targetScenario);
      LinkedHashMap<String, String> activityMap = new LinkedHashMap<>(activitydef1);
      activityMap.put("alias", alias);
      NBCommandParams params = NBCommandParams.of(activityMap);
      NBCommandResult result = NBCommandInvoker.invoke(context, started, params);
      NBMetric metric = context.find().metric("name=spaces,activity=" + alias);
      System.out.println("metric: " + metric);
      if (metric instanceof Gauge<?> gauge) {
        System.out.println("gauge: " + gauge.getValue());
      } else {
        throw new RuntimeException("metric is not a gauge: " + metric);
      }
      assertThat(result.getException()).isNull();
      //      System.out.println("RESULT:\n"+result);
      //      System.out.flush();
    }

    for (String alias : aliases) {
      String awaitName = "await_" + alias;

      System.out.println("awaiting " + alias);
      context.apply(new CMD_await(context, awaitName, targetScenario), NBCommandParams.of(Map.of("alias",
          alias)));
      System.out.println("awaited  " + alias);
      System.out.flush();
    }


    Iterator<NBComponent> all = NBComponentTraversal.traverseDepth(context);
    List<Activity> activities = new ArrayList<>();
    List<Space> spaces = new ArrayList<>();

    while (all.hasNext()) {
      NBComponent component = all.next();
      if (component instanceof StandardActivity<?, ?> activity) {
        activities.add(activity);
      }
      if (component instanceof Space space) {
        spaces.add(space);
      }
    }
    assertThat(activities).hasSize(0);
    assertThat(spaces).hasSize(0);


    context.getChildren().forEach(System.out::println);
    //    assertThat(result.getIOLog()).contains("started activity");
    //    assertThat(result.getIOLog()).contains("stopped activity");

  }
}
