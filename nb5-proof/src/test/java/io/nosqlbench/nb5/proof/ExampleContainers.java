/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb5.proof;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleContainers {

    private final String java = Optional.ofNullable(System.getenv(
        "JAVA_HOME")).map(v -> v + "/bin/java").orElse("java");

    private final static String JARNAME = "../nb5/target/nb5.jar";
//    private static GenericContainer cass= new CassandraContainer("cassandra").withExposedPorts(9042);

    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
        .withExposedPorts(6379);
    @BeforeAll
    public static void initContainer() {
        redis.start();

    }

    @BeforeEach
    public void setUp() {
        System.out.println("setup");
    }

    @Test
    public void testSimplePutAndGet() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        ProcessResult result = invoker.run("test-workloads", 30,
            "java", "-jar", JARNAME, "--list-workloads"
        );
        assertThat(result.exception).isNull();
        String stdout = String.join("\n", result.getStdoutData());
        System.out.println(stdout);
        System.out.println("end");
    }

}
