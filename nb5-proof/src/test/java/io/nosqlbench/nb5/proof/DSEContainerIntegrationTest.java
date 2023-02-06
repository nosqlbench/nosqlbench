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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DSEContainerIntegrationTest {

    public static Logger logger = LogManager.getLogger(DSEContainerIntegrationTest.class);
    private final String java = Optional.ofNullable(System.getenv(
        "JAVA_HOME")).map(v -> v + "/bin/java").orElse("java");

    private final static String JARNAME = "../nb5/target/nb5.jar";
//    private static GenericContainer cass= new CassandraContainer("cassandra").withExposedPorts(9042);

    private static String hostIP = "127.0.0.1";
    private static String datacenter = "datacenter1";
    private static Integer mappedPort9042 = 9042;
    private static ProcessInvoker invoker = new ProcessInvoker();
    private static  CassandraContainer dse;
    static {
        dse  = (CassandraContainer) new CassandraContainer(DockerImageName.parse("datastax/dse-server:6.8.17-ubi7").asCompatibleSubstituteFor("cassandra")).withEnv("DS_LICENSE", "accept").withEnv("CASSANDRA_DC", datacenter).withExposedPorts(9042);
    }
    @BeforeAll
    public static void initContainer() {
        //STEP0:Start the test container and expose the 9042 port on the local host.
        //So that the docker bridge controller exposes the port to our process invoker that will run nb5
        //and target cassandra on that docker container
        dse.start();
        datacenter = dse.getLocalDatacenter();

        //When running with a local Docker daemon, exposed ports will usually be reachable on localhost.
        // However, in some CI environments they may instead be reachable on a different host.
        mappedPort9042 = dse.getMappedPort(9042);
        hostIP = dse.getHost();
    }

    @BeforeEach
    public void setUp() {
        System.out.println("setup");
    }

    @Test
    public void testSimplePutAndGet() {

        invoker.setLogDir("logs/test");
//        //STEP1: Copy the example workload to the local dir
        ProcessResult copyResult = invoker.run("copy-workload", 30,
            "java", "-jar", JARNAME, "--copy=activities/baselines/cql-iot-dse.yaml"
        );
        assertThat(copyResult.exception).isNull();
        String copyOut = String.join("\n", copyResult.getStdoutData());

        //STEP2: Run the example cassandra workload using the schema tag to create the Cass Baselines keyspace
        String[] args = new String[]{
            "java", "-jar", JARNAME, "cql-iot-dse.yaml", "default", "host="+hostIP, "localdc="+datacenter, "port="+ mappedPort9042.toString()
        };
        ProcessResult runSchemaResult = invoker.run("run-workload", 30, args);
        logger.info("The final command line: " + String.join(" ", args));
        assertThat(runSchemaResult.exception).isNull();
        String runSchemaOut = String.join("\n", runSchemaResult.getStdoutData());
        System.out.println(runSchemaOut);

        //STEP3: Run the example cassandra workload using the rampup phase to create the data in a specific number of cycles
//        ProcessResult runRampUpResult = invoker.run("run-workload", 30, "java", "-jar", JARNAME, "run",
//            "driver=cql", "workload=cql-keyvalue", "host="+hostIP, "localdc="+datacenter, "port="+ mappedPort9042.toString(),
//            "tags=blocks:rampup", "cycles=100k"
//        );
//        assertThat(runRampUpResult.exception).isNull();
//        String runRampUpOut = String.join("\n", runRampUpResult.getStdoutData());
//        System.out.println(runRampUpOut);

        System.out.println("end");
    }

}
