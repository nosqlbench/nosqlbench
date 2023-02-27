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
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class WorkloadContainerVerifications {
    private enum Driver {
        CQL("cql"),
        HTTP("http"),
        MONGODB("mongodb"),
        TCP ("tcp"),
        PULSAR("pulsar"),
        DYNAMODB("dynamo"),
        KAFKA("kafka");
        private final String name;
        Driver(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
    public static Logger logger = LogManager.getLogger(WorkloadContainerVerifications.class);
    private final String java = Optional.ofNullable(System.getenv(
        "JAVA_HOME")).map(v -> v + "/bin/java").orElse("java");
    private final static String JARNAME = "../nb5/target/nb5.jar";
    private final static String BASIC_CHECK_IDENTIFIER = "basic_check";
    private static final CassandraContainer cass = (CassandraContainer) new CassandraContainer(DockerImageName.parse("cassandra:latest"))
        .withExposedPorts(9042).withAccessToHost(true);
    private static Map<Driver, List<String>> basicWorkloadsMapPerDriver = null;
    @BeforeAll
    public static void listWorkloads() {
        //List the tests we would like to run
        ProcessInvoker invoker = new ProcessInvoker();
        //STEP1: Copy the example workload to the local dir
        ProcessResult listResult = invoker.run("list-workloads", 30,
            "java", "-jar", JARNAME, "--list-workloads", "--include=examples"
        );
        assertThat(listResult.exception).isNull();
        String listOut = String.join("\n", listResult.getStdoutData());


        // Define the regular expression pattern
        String regex = "/(.+?/)+.+?\\.yaml";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(listOut);
        ArrayList<String> matchedPaths = new ArrayList<>();

        while (matcher.find()) {
            matchedPaths.add(matcher.group());
        }
        basicWorkloadsMapPerDriver = new HashMap<>();
        getBasicCheckWorkloadsForEachDriver(matchedPaths, BASIC_CHECK_IDENTIFIER);
    }


    @Test
    public void testCqlKeyValueWorkload() {
        ProcessInvoker invoker = new ProcessInvoker();
        invoker.setLogDir("logs/test");

        if(basicWorkloadsMapPerDriver.get(Driver.CQL) == null)
            return ;
        else if (basicWorkloadsMapPerDriver.get(Driver.CQL).size() == 0)
            return;

        for(String workloadPath : basicWorkloadsMapPerDriver.get(Driver.CQL))
        {
            int lastSlashIndex = workloadPath.lastIndexOf('/');
            String shortName = workloadPath.substring(lastSlashIndex + 1);
            if(shortName.equals("cql-iot-dse.yaml"))
                continue;
            //STEP0:Start the test container and expose the 9042 port on the local host.
            //So that the docker bridge controller exposes the port to our process invoker that will run nb5
            //and target cassandra on that docker container
            cass.start();
            //the default datacenter name
            String datacenter = cass.getLocalDatacenter();
            //When running with a local Docker daemon, exposed ports will usually be reachable on localhost.
            // However, in some CI environments they may instead be reachable on a different host.
            //the port mapped to the original exposed port of the cassandra image
            Integer mappedPort9042 = cass.getMappedPort(9042);
            //the host ip of the cassandra image in the container
            String hostIP = cass.getHost();


            //STEP1: Run the example cassandra workload using the schema tag to create the Cass Baselines keyspace
            String[] args = new String[]{
                "java", "-jar", JARNAME, shortName, BASIC_CHECK_IDENTIFIER, "host="+ hostIP, "localdc="+ datacenter, "port="+ mappedPort9042.toString(), "table=keyvalue", "keyspace=baselines"
            };
            logger.info("The final command line: " + String.join(" ", args));
            ProcessResult runSchemaResult = invoker.run("run-workload", 30, args);


            //STEP 2 Check runSchemaOut for errors
            logger.info("Checking if the NB5 command resulted in any errors...");
            assertThat(runSchemaResult.exception).isNull();
            String runSchemaOut = String.join("\n", runSchemaResult.getStdoutData());
            assertThat(runSchemaOut.toLowerCase()).doesNotContain("error");
            logger.info("NB5 command completed with no errors");


            //STEP 3 Check the cluster for created data
            //THIS PART IS LEFT COMMENTED FOR RUNNING SPECIFIC CQL DATA CREATION CHECKING

//            try (CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(hostIP, mappedPort9042)).withLocalDatacenter(datacenter).build()) {
//                //->Check for the creation of the keyspace baselines
//                logger.info("Checking for the creation of the keyspace \"baselines\"...");
//                ResultSet result = session.execute("SELECT keyspace_name FROM system_schema.keyspaces");
//                List<Row> rows = result.all();
//                boolean keyspaceFound = false;
//                for (Row row : rows) {
//                    if (row.getString("keyspace_name").equals("baselines")) {
//                        keyspaceFound = true;
//                        break;
//                    }
//                }
//                assertTrue(keyspaceFound);
//                logger.info("Keyspace \"baselines\" was found, nb5 command had created it successfully");
//
//                //->Check for the creation of the baselines keyvalue table
//                logger.info("Checking for the creation of the table \"baselines.keyvalue\"...");
//                result = session.execute("SELECT table_name FROM system_schema.tables WHERE keyspace_name='baselines'");
//                rows = result.all();
//                boolean tableFound = false;
//                for (Row row : rows) {
//                    if (row.getString("table_name").equals("keyvalue")) {
//                        tableFound = true;
//                        break;
//                    }
//                }
//                assertTrue(tableFound);
//                logger.info("Table \"baselines.keyvalue\" was found, nb5 command had created it successfully");
//
//                //->Check for the creation of the baselines keyvalue table
//                ResultSet resultSet = session.execute("SELECT count(*) FROM baselines.keyvalue");
//                Row row = resultSet.one();
//                long rowCount = row.getLong(0);
//                logger.info("Number of rows in baselines.keyvalue: " + rowCount);
//                assertTrue(rowCount >= 5);
//                logger.info("Table \"baselines.keyvalue\" has at least 5 rows of key-value pairs, nb5 command had created them successfully");
//
//            } catch (Exception e)
//            {
//                System.out.println(e.getMessage());
//                fail();
//            } finally {
            cass.stop();
//            }
        }
    }

    /*
    This method filters the input list of workloads to output the subset of workloads
    that include a specific scenario (input) and maps all workloads with that scenario to
    a key which is their common driver
    */
    private static void getBasicCheckWorkloadsForEachDriver(List<String> workloadPaths ,String scenarioFilter) {
        for (String workloadPath : workloadPaths) {
            try {
                int lastSlashIndex = workloadPath.lastIndexOf('/');
                String shortName = workloadPath.substring(lastSlashIndex + 1);
                String[] args = new String[]{
                    "java", "-jar", JARNAME, shortName, scenarioFilter, "--show-script"
                };
                ProcessInvoker invoker = new ProcessInvoker();
                ProcessResult runShowScriptResult = invoker.run("run-show-script", 10, args);
                assertThat(runShowScriptResult.exception).isNull();
                String listOut = String.join("\n", runShowScriptResult.getStdoutData());
                Pattern pattern = Pattern.compile("'driver':\\s*'(.+?)'");

                // Use the Matcher class to find the substring in the output script that defines the driver
                Matcher matcher = pattern.matcher(listOut);
                if (matcher.find()) {
                    String scenarioDriverValue = matcher.group(1);
                    for (Driver driverType : Driver.values())
                    {
                        if(driverType.getName().equals(scenarioDriverValue))
                        {
                            if(basicWorkloadsMapPerDriver.containsKey(driverType))
                            {
                                List<String> currentList = basicWorkloadsMapPerDriver.get(driverType);
                                // Modify the list by adding new strings to it
                                currentList.add(workloadPath);
                                // Put the updated list back into the HashMap using the same key
                                basicWorkloadsMapPerDriver.put(driverType, currentList);
                            }
                            else
                            {
                                List<String> pathList = new ArrayList<>();
                                pathList.add(workloadPath);
                                basicWorkloadsMapPerDriver.put(driverType, pathList);
                            }
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("Error reading file " + workloadPath + ": " + e.getMessage());
                break;
            }
        }
    }


}
