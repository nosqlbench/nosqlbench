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


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleContainersIntegrationTest {

    public static Logger logger = LogManager.getLogger(ExampleContainersIntegrationTest.class);
    private final String java = Optional.ofNullable(System.getenv(
        "JAVA_HOME")).map(v -> v + "/bin/java").orElse("java");

    private final static String JARNAME = "../nb5/target/nb5.jar";
//    private static GenericContainer cass= new CassandraContainer("cassandra").withExposedPorts(9042);

    private static String hostIP = "127.0.0.1";
    private static String datacenter = "datacenter1";
    private static Integer mappedPort9042 = 9042;
    private static final Integer EXPOSED_PORT = 9042;
    private static final CassandraContainer cass = (CassandraContainer) new CassandraContainer(DockerImageName.parse("cassandra:latest"))
        .withExposedPorts(9042).withAccessToHost(true);
        //.waitingFor(new CassandraWaitStrategy());
    @BeforeAll
    public static void initContainer() {
        //List the tests we would like to run
        ProcessInvoker invoker = new ProcessInvoker();
        //STEP1: Copy the example workload to the local dir
        ProcessResult listResult = invoker.run("list-workloads", 30,
            "java", "-jar", JARNAME, "--list-workloads", "--include=examples"
        );
        assertThat(listResult.exception).isNull();
        String listOut = String.join("\n", listResult.getStdoutData());

        List<String> results = new ArrayList<>();

        // Define the regular expression pattern
        String regex = "/(.+?/)+.+?\\.yaml";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(listOut);
        ArrayList<String> matchedPaths = new ArrayList<>();

        while (matcher.find()) {
            matchedPaths.add(matcher.group());
        }

        System.out.println("Matched paths:");
        for (String path : matchedPaths) {
            System.out.println(path);
        }
    }

    @BeforeEach
    public void setUp() {
        //STEP0:Start the test container and expose the 9042 port on the local host.
        //So that the docker bridge controller exposes the port to our process invoker that will run nb5
        //and target cassandra on that docker container
        cass.start();
        datacenter = cass.getLocalDatacenter();
        //When running with a local Docker daemon, exposed ports will usually be reachable on localhost.
        // However, in some CI environments they may instead be reachable on a different host.
        mappedPort9042 = cass.getMappedPort(9042);
        hostIP = cass.getHost();
        System.out.println("setup");
    }

    @Test
    public void testCqlKeyValueWorkload() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");


        //STEP1: Copy the example workload to the local dir
        ProcessResult copyResult = invoker.run("copy-workload", 30,
            "java", "-jar", JARNAME, "--copy=/activities/baselinesv2/cql-keyvalue2.yaml"
        );
        assertThat(copyResult.exception).isNull();
        String copyOut = String.join("\n", copyResult.getStdoutData());


        //STEP2: Run the example cassandra workload using the schema tag to create the Cass Baselines keyspace
        String[] args = new String[]{
            "java", "-jar", JARNAME, "cql-keyvalue2.yaml", "default", "host="+hostIP, "localdc="+datacenter, "port="+ mappedPort9042.toString(), "rampup-cycles=10", "main-cycles=10"
        };
        logger.info("The final command line: " + String.join(" ", args));
        ProcessResult runSchemaResult = invoker.run("run-workload", 30, args);


        //STEP 3 Check runSchemaOut for errors
        logger.info("Checking if the NB5 command resulted in any errors...");
        assertThat(runSchemaResult.exception).isNull();
        String runSchemaOut = String.join("\n", runSchemaResult.getStdoutData());
        assertThat(runSchemaOut.toLowerCase()).doesNotContain("error");
        logger.info("NB5 command completed with no errors");


        //STEP 4 Check the cluster for created data
        try (CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(hostIP, mappedPort9042)).withLocalDatacenter(datacenter).build()) {
            //->Check for the creation of the keyspace baselines
            logger.info("Checking for the creation of the keyspace \"baselines\"...");
            ResultSet result = session.execute("SELECT keyspace_name FROM system_schema.keyspaces");
            List<Row> rows = result.all();
            boolean keyspaceFound = false;
            for (Row row : rows) {
                if (row.getString("keyspace_name").equals("baselines")) {
                    keyspaceFound = true;
                    break;
                }
            }
            assertTrue(keyspaceFound);
            logger.info("Keyspace \"baselines\" was found, nb5 command had created it successfully");

            //->Check for the creation of the baselines keyvalue table
            logger.info("Checking for the creation of the table \"baselines.keyvalue\"...");
            result = session.execute("SELECT table_name FROM system_schema.tables WHERE keyspace_name='baselines'");
            rows = result.all();
            boolean tableFound = false;
            for (Row row : rows) {
                if (row.getString("table_name").equals("keyvalue")) {
                    tableFound = true;
                    break;
                }
            }
            assertTrue(tableFound);
            logger.info("Table \"baselines.keyvalue\" was found, nb5 command had created it successfully");

            //->Check for the creation of the baselines keyvalue table
            logger.info("Table \"baselines.keyvalue\" has at least 5 rows of key-value pairs, nb5 command had created them successfully");
            result = session.execute("SELECT count(*) FROM baselines.keyvalue");
            int rowCount = result.one().getInt(0);
            assertTrue(rowCount >= 5);
            logger.info("Table \"baselines.keyvalue\" has at least 5 rows of key-value pairs, nb5 command had created them successfully");

        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }



        //STEP5 Create a failing test to make sure that the workload won't work, here we use a random wrong IP
        String[] args2 = new String[]{
            "java", "-jar", JARNAME, "cql-keyvalue2.yaml", "default", "host=0.1.0.1", "localdc="+datacenter, "port="+ mappedPort9042.toString(), "rampup-cycles=10", "main-cycles=10"
        };
        logger.info("The final command line: " + String.join(" ", args2));
        ProcessResult runFailingSchemaResult = invoker.run("run-workload", 30, args2);
        assertThat(runFailingSchemaResult.exception).isNull();
        String runFailingSchemaOut = String.join("\n", runFailingSchemaResult.getStdoutData());
        assertThat(runFailingSchemaOut.toLowerCase()).contains("error");
        System.out.println("end");
    }
    @AfterEach
    public void stopContainers(){
        cass.stop();
    }

    static class CassandraWaitStrategy extends AbstractWaitStrategy {
        public CassandraWaitStrategy() {
            withStartupTimeout(Duration.ofMinutes(2));
        }

        @Override
        protected void waitUntilReady() {
            // Custom wait strategy to determine if Cassandra is ready.
            // For example, we can check the logs or perform a cql query to verify the status of Cassandra.
            String logs = cass.getLogs();
            Unreliables.retryUntilSuccess(120, TimeUnit.SECONDS, () -> {
                if (logs.contains("Listening for thrift clients...")) {
                    return true;
                }
                return false;
            });
        }
    }

}
