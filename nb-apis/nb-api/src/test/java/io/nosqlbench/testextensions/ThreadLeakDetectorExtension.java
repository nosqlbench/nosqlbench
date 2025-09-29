// src/test/java/com/example/testing/ThreadLeakDetectorExtension.java
package io.nosqlbench.testextensions;

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


import org.junit.jupiter.api.extension.*;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ThreadLeakDetectorExtension implements BeforeEachCallback, AfterEachCallback {

  // Config via system properties or junit-platform.properties -> systemProperties.*
  private static final Duration GRACE = Duration.ofMillis(
      Long.getLong("threadleak.grace.ms", 300));                    // wait after test
  private static final Duration JOIN_TIMEOUT = Duration.ofMillis(
      Long.getLong("threadleak.join.ms", 200));                     // join timeout per thread
  private static final boolean IGNORE_DAEMONS = Boolean.parseBoolean(
      System.getProperty("threadleak.ignoreDaemons", "true"));
  private static final Pattern IGNORE_NAME = Pattern.compile(
      System.getProperty("threadleak.ignoreNameRegex",
          // common infra/IDE/GC threads; tune for your stack
          "^(Finalizer|Reference Handler|Signal Dispatcher|Common-Cleaner|.*ForkJoinPool.*|.*Timer.*|.*Attach Listener.*|.*Process reaper.*)$"));
  private static final Pattern IGNORE_GROUP = Pattern.compile(
      System.getProperty("threadleak.ignoreGroupRegex", "^(system|InnocuousThreadGroup)$"));

  private final ThreadMXBeanShim mx = new ThreadMXBeanShim();

  private Set<Long> beforeIds;

  @Override
  public void beforeEach(ExtensionContext ctx) {
      beforeIds = liveThreadIds();
  }

  @Override
  public void afterEach(ExtensionContext ctx) throws Exception {
      // Small grace to let pools/tasks shut down
      Thread.sleep(GRACE.toMillis());

      Set<Thread> after = liveThreads();

      // Try to join stragglers briefly (best-effort)
      for (Thread t : after) {
          if (t.isAlive() && !t.isDaemon()) {
              try { t.join(JOIN_TIMEOUT.toMillis()); } catch (InterruptedException ignored) {}
          }
      }

      // Recompute after join attempt
      after = liveThreads();

      List<Thread> leaked = after.stream()
          // newly appeared since beforeEach
          .filter(t -> beforeIds == null || !beforeIds.contains(t.getId()))
          // filter typical noise
          .filter(t -> !(IGNORE_DAEMONS && t.isDaemon()))
          .filter(t -> !IGNORE_NAME.matcher(t.getName()).matches())
          .filter(t -> !IGNORE_GROUP.matcher(t.getThreadGroup() != null ? t.getThreadGroup().getName() : "").matches())
          .collect(Collectors.toList());

      if (!leaked.isEmpty()) {
          String details = leaked.stream()
              .map(t -> String.format("#%d '%s' group=%s daemon=%s state=%s",
                      t.getId(), t.getName(),
                      (t.getThreadGroup() != null ? t.getThreadGroup().getName() : "<none>"),
                      t.isDaemon(), t.getState()))
              .collect(Collectors.joining("\n"));

          throw new AssertionError("Detected leaked threads after test:\n" + details);
      }
  }

  private Set<Long> liveThreadIds() {
      long[] ids = mx.getAllThreadIds();
      Set<Long> set = new HashSet<>(ids.length);
      for (long id : ids) set.add(id);
      return set;
  }

  private Set<Thread> liveThreads() {
      // getAllStackTraces() is a handy snapshot of live Thread objects
      return Thread.getAllStackTraces().keySet();
  }

  // Minimal shim to avoid depending on com.sun.* at compile time
  static final class ThreadMXBeanShim {
      final java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
      long[] getAllThreadIds() { return bean.getAllThreadIds(); }
  }
}
