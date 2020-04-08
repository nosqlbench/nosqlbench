package io.nosqlbench.engine.docker;

/*
 *
 * @author Sebastián Estévez on 4/4/19.
 *
 */


import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import io.nosqlbench.nb.api.content.NBIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static io.nosqlbench.engine.docker.RestHelper.post;

public class DockerMetricsManager {

    private final DockerHelper dh;

    String userHome = System.getProperty("user.home");

    private Logger logger = LoggerFactory.getLogger(DockerMetricsManager.class);

    public DockerMetricsManager() {
        dh = new DockerHelper();
   }

    public void startMetrics() {

        String ip = startGraphite();

        startPrometheus(ip);

        startGrafana(ip);

    }

    private void startGrafana(String ip) {

        String GRAFANA_IMG = "grafana/grafana";
        String tag = "5.3.2";
        String name = "grafana";
        List<Integer> port = Arrays.asList(3000);

        setupGrafanaFiles(ip);

        List<String> volumeDescList = Arrays.asList(
            userHome + "/.nosqlbench/grafana:/var/lib/grafana:rw"
            //cwd+"/docker-metrics/grafana:/grafana",
            //cwd+"/docker-metrics/grafana/datasources:/etc/grafana/provisioning/datasources",
            //cwd+"/docker-metrics/grafana/dashboardconf:/etc/grafana/provisioning/dashboards"
            //,cwd+"/docker-metrics/grafana/dashboards:/var/lib/grafana/dashboards:ro"
        );
        List<String> envList = Arrays.asList(
            "GF_SECURITY_ADMIN_PASSWORD=admin",
            "GF_AUTH_ANONYMOUS_ENABLED=\"true\"",
            "GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_URL=https://assethub.datastax.com:3001",
            "GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_NAME=\"Upload to DataStax\""
        );

        String reload = null;
        String containerId = dh.startDocker(GRAFANA_IMG, tag, name, port, volumeDescList, envList, null, reload);
        if (containerId == null){
            return;
        }

        dh.pollLog(containerId, new LogCallback());

        logger.info("grafana container started, http listening");

        configureGrafana();
    }

    private void startPrometheus(String ip) {

        logger.info("preparing to start docker metrics");
        String PROMETHEUS_IMG = "prom/prometheus";
        String tag = "v2.4.3";
        String name = "prom";
        List<Integer> port = Arrays.asList(9090);

        setupPromFiles(ip);

        List<String> volumeDescList = Arrays.asList(
            //cwd+"/docker-metrics/prometheus:/prometheus",
            userHome + "/.nosqlbench/prometheus-conf:/etc/prometheus",
            userHome + "/.nosqlbench/prometheus:/prometheus"
            //"./prometheus/tg_dse.json:/etc/prometheus/tg_dse.json"
        );

        List<String> envList = null;

        List<String> cmdList = Arrays.asList(
            "--config.file=/etc/prometheus/prometheus.yml",
            "--storage.tsdb.path=/prometheus",
            "--storage.tsdb.retention=183d",
            "--web.enable-lifecycle"

        );

        String reload = "http://localhost:9090/-/reload";
        dh.startDocker(PROMETHEUS_IMG, tag, name, port, volumeDescList, envList, cmdList, reload);

        logger.info("prometheus started and listenning");
    }

    private String startGraphite() {

        logger.info("preparing to start graphite exporter container...");

        //docker run -d -p 9108:9108 -p 9109:9109 -p 9109:9109/udp prom/graphite-exporter
        String GRAPHITE_EXPORTER_IMG = "prom/graphite-exporter";
        String tag = "latest";
        String name = "graphite-exporter";
        //TODO: look into UDP
        List<Integer> port = Arrays.asList(9108, 9109);
        List<String> volumeDescList = Arrays.asList();
        List<String> envList = Arrays.asList();

        String reload = null;
        dh.startDocker(GRAPHITE_EXPORTER_IMG, tag, name, port, volumeDescList, envList, null, reload);

        logger.info("graphite exporter container started");

        logger.info("searching for graphite exporter container ip");
        ContainerNetworkSettings settings = dh.searchContainer(name, null).getNetworkSettings();
        Map<String, ContainerNetwork> networks = settings.getNetworks();
        String ip = null;
        for (String key : networks.keySet()) {
            ContainerNetwork network = networks.get(key);
            ip = network.getIpAddress();
        }

        return ip;
    }

    private void setupPromFiles(String ip) {
        String datasource = NBIO.readCharBuffer("docker/prometheus/prometheus.yml").toString();

        if (ip == null) {
            logger.error("IP for graphite container not found");
            System.exit(1);
        }

        datasource = datasource.replace("!!!GRAPHITE_IP!!!", ip);

        File nosqlbenchDir = new File(userHome, "/.nosqlbench/");
        mkdir(nosqlbenchDir);


        File prometheusDir = new File(userHome, "/.nosqlbench/prometheus");
        mkdir(prometheusDir);

        File promConfDir = new File(userHome, "/.nosqlbench/prometheus-conf");
        mkdir(promConfDir);

        Path prometheusDirPath = Paths.get(userHome, "/.nosqlbench" +
                "/prometheus");

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        try {
            Files.setPosixFilePermissions(prometheusDirPath, perms);
        } catch (IOException e) {
            logger.error("failed to set permissions on prom backup " +
                    "directory " + userHome + "/.nosqlbench/prometheus)");
            e.printStackTrace();
            System.exit(1);
        }

        try (PrintWriter out = new PrintWriter(
                new FileWriter(userHome + "/.nosqlbench/prometheus-conf" +
                        "/prometheus.yml", false))) {
            out.println(datasource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("error writing prometheus yaml file to ~/.prometheus");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("creating file in ~/.prometheus");
            System.exit(1);
        }
    }

    private void mkdir(File dir) {
        if(dir.exists()){
            return;
        }
        if(! dir.mkdir()){
            if( dir.canWrite()){
                System.out.println("no write access");
            }
            if( dir.canRead()){
                System.out.println("no read access");
            }
            System.out.println("Could not create directory " + dir.getPath());
            System.out.println("fix directory permissions to run --docker-metrics");
            System.exit(1);
        }
    }


    private void setupGrafanaFiles(String ip) {

        File grafanaDir = new File(userHome, "/.nosqlbench/grafana");
        mkdir(grafanaDir);

        Path grafanaDirPath = Paths.get(userHome, "/.nosqlbench/grafana");

        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        try {
            Files.setPosixFilePermissions(grafanaDirPath, perms);
        } catch (IOException e) {
            logger.error("failed to set permissions on grafana directory " +
                "directory " + userHome + "/.nosqlbench/grafana)");
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void configureGrafana() {
        post("http://localhost:3000/api/dashboards/db", "docker/dashboards/analysis.json", true, "load analysis dashboard");
        post("http://localhost:3000/api/datasources", "docker/datasources/prometheus-datasource.yaml", true, "configure data source");
    }


    public void stopMetrics() {
        //TODO: maybe implement
    }

    private class LogCallback extends ResultCallbackTemplate<LogContainerResultCallback, Frame> {
        @Override
        public void onNext(Frame item) {
            if (item.toString().contains("HTTP Server Listen")) {
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
