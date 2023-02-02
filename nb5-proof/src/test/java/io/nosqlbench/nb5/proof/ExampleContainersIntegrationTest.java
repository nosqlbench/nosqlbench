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
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleContainersIntegrationTest {

    private final String java = Optional.ofNullable(System.getenv(
        "JAVA_HOME")).map(v -> v + "/bin/java").orElse("java");

    private final static String JARNAME = "../nb5/target/nb5.jar";
//    private static GenericContainer cass= new CassandraContainer("cassandra").withExposedPorts(9042);

    private static String hostIP = "127.0.0.1";
    private static final Integer EXPOSED_PORT = 9042;
    public static GenericContainer<?> cass = new CassandraContainer<>(DockerImageName.parse("cassandra:latest"))
        .withExposedPorts(EXPOSED_PORT).withAccessToHost(true).waitingFor(new CassandraQueryWaitStrategy());
    @BeforeAll
    public static void initContainer() {
        //STEP0:Start the test container and expose the 9042 port on the local host.
        //So that the docker bridge controller exposes the port to our process invoker that will run nb5
        //and target cassandra on that docker container
        cass.start();

        //When running with a local Docker daemon, exposed ports will usually be reachable on localhost.
        // However, in some CI environments they may instead be reachable on a different host.
        hostIP = cass.getHost();

    }

    @BeforeEach
    public void setUp() {
        System.out.println("setup");
    }

    @Test
    public void testSimplePutAndGet() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");
        //STEP1: Copy the example workload to the local dir
        ProcessResult copyResult = invoker.run("copy-workload", 30,
            "java", "-jar", JARNAME, "--copy=activities/baselines/cql-keyvalue.yaml"
        );
        assertThat(copyResult.exception).isNull();
        String copyOut = String.join("\n", copyResult.getStdoutData());

        //STEP2: Run the example cassandra workload using the default params (
        ProcessResult runResult = invoker.run("run-workload", 30, "java", "-jar", JARNAME, "run",
            "driver=cql", "workload=cql-keyvalue", "host="+hostIP, "localdc=datacenter1"
            );
        assertThat(runResult.exception).isNull();
        String runOut = String.join("\n", runResult.getStdoutData());
        System.out.println(runOut);
        System.out.println("end");
    }

}
