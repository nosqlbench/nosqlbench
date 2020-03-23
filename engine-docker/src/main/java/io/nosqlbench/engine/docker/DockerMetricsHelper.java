package io.nosqlbench.engine.docker;

/*
 *
 * @author Sebastián Estévez on 4/4/19.
 *
 */


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
//import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;
import com.sun.security.auth.module.UnixSystem;
import io.nosqlbench.engine.api.util.NosqlBenchFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import io.nosqlbench.util.nosqlbenchFiles;


public class DockerMetricsHelper {

    private static final String DOCKER_HOST = "DOCKER_HOST";
    private static final String DOCKER_HOST_ADDR = "unix:///var/run/docker.sock";
    String userHome = System.getProperty("user.home");
//    private Client rsClient = ClientBuilder.newClient();

    private DockerClientConfig config;
    private DockerClient dockerClient;
    private Logger logger = LoggerFactory.getLogger(DockerMetricsHelper.class);

    public DockerMetricsHelper() {
        System.getProperties().setProperty(DOCKER_HOST, DOCKER_HOST_ADDR);
        this.config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(DOCKER_HOST_ADDR).build();
        DockerCmdExecFactory dockerCmdExecFactory = new OkHttpDockerCmdExecFactory()
            .withReadTimeout(1000)
            .withConnectTimeout(1000);

//        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
//                .withReadTimeout(1000)
//                .withConnectTimeout(1000);

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }

    public void startMetrics() {

        logger.info("preparing to startg graphite exporter container");

        //docker run -d -p 9108:9108 -p 9109:9109 -p 9109:9109/udp prom/graphite-exporter
        String GRAPHITE_EXPORTER_IMG = "prom/graphite-exporter";
        String tag = "latest";
        String name = "graphite-exporter";
        //TODO: look into UDP
        List<Integer> port = Arrays.asList(9108, 9109);
        List<String> volumeDescList = Arrays.asList();
        List<String> envList = Arrays.asList();

        String reload = null;
        startDocker(GRAPHITE_EXPORTER_IMG, tag, name, port, volumeDescList, envList, null, reload);

        logger.info("graphite exporter container started");

        logger.info("searching for graphite exporter container ip");
        ContainerNetworkSettings settings = searchContainer(name, null).getNetworkSettings();
        Map<String, ContainerNetwork> networks = settings.getNetworks();
        String ip = null;
        for (String key : networks.keySet()) {
            ContainerNetwork network = networks.get(key);
            ip = network.getIpAddress();
        }

        logger.info("preparing to start docker metrics");
        String PROMETHEUS_IMG = "prom/prometheus";
        tag = "v2.4.3";
        name = "prom";
        port = Arrays.asList(9090);

        setupPromFiles(ip);

        volumeDescList = Arrays.asList(
                //cwd+"/docker-metrics/prometheus:/prometheus",
                userHome + "/.nosqlbench/prometheus-conf:/etc/prometheus",
                userHome + "/.nosqlbench/prometheus:/prometheus"
                //"./prometheus/tg_dse.json:/etc/prometheus/tg_dse.json"
        );

        envList = null;

        List<String> cmdList = Arrays.asList(
                "--config.file=/etc/prometheus/prometheus.yml",
                "--storage.tsdb.path=/prometheus",
                "--storage.tsdb.retention=183d",
                "--web.enable-lifecycle"

        );

        reload = "http://localhost:9090/-/reload";
        startDocker(PROMETHEUS_IMG, tag, name, port, volumeDescList, envList, cmdList, reload);

        String GRAFANA_IMG = "grafana/grafana";
        tag = "5.3.2";
        name = "grafana";
        port = Arrays.asList(3000);

        setupGrafanaFiles(ip);

        volumeDescList = Arrays.asList(
                userHome+"/.nosqlbench/grafana:/var/lib/grafana:rw"
                //cwd+"/docker-metrics/grafana:/grafana",
                //cwd+"/docker-metrics/grafana/datasources:/etc/grafana/provisioning/datasources",
                //cwd+"/docker-metrics/grafana/dashboardconf:/etc/grafana/provisioning/dashboards"
                //,cwd+"/docker-metrics/grafana/dashboards:/var/lib/grafana/dashboards:ro"
        );
        envList = Arrays.asList(
                "GF_SECURITY_ADMIN_PASSWORD=admin",
                "GF_AUTH_ANONYMOUS_ENABLED=\"true\"",
                "GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_URL=https://assethub.datastax.com:3001",
                "GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_NAME=\"Upload to DataStax\""
        );

        reload = null;
        String containerId = startDocker(GRAFANA_IMG, tag, name, port, volumeDescList, envList, null, reload);


        LogContainerResultCallback loggingCallback = new
                LogContainerResultCallback();

        try {
            LogContainerCmd cmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withFollowStream(true)
                    .withTailAll();

            final boolean[] httpStarted = {false};
            cmd.exec(new LogCallback());

            loggingCallback.awaitCompletion(10, TimeUnit.SECONDS);

            logger.info("grafana container started, http listenning");

            configureGrafana();


        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("unable to detect grafana start");
        }
    }

    private void setupPromFiles(String ip) {
        String datasource = NosqlBenchFiles.readFile("docker/prometheus/prometheus.yml");

        if (ip == null) {
            logger.error("IP for graphite container not found");
            System.exit(1);
        }

        datasource = datasource.replace("!!!GRAPHITE_IP!!!", ip);

        File prometheusDir = new File(userHome, ".nosqlbench/prometheus");
        prometheusDir.mkdir();

        new File(userHome, ".nosqlbench/prometheus-conf").mkdir();

        Path prometheusDirPath = Paths.get(userHome, ".nosqlbench" +
                "/prometheus");

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        try {
            Files.setPosixFilePermissions(prometheusDirPath, perms);
        } catch (IOException e) {
            logger.error("failed to set permissions on prom backup " +
                    "directory (~/.nosqlbench/prometheus)");
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


    private void setupGrafanaFiles(String ip) {

        File grafanaDir = new File(userHome, ".nosqlbench/grafana");
        grafanaDir.mkdir();

        Path grafanaDirPath = Paths.get(userHome, ".nosqlbench/grafana");

        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        try {
            Files.setPosixFilePermissions(grafanaDirPath, perms);
        } catch (IOException e) {
            logger.error("failed to set permissions on prom backup " +
                    "directory (~/.nosqlbench/prometheus)");
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void configureGrafana() {
        post("http://localhost:3000/api/dashboards/db", "docker/dashboards/analysis.json", true);
        post("http://localhost:3000/api/datasources", "docker/datasources/prometheus-datasource.yaml", true);
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }


    private HttpResponse<String> post(String url, String path, boolean auth) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        HttpClient httpClient = clientBuilder.build();

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder = builder.uri(URI.create(url));
        if (auth) {
            builder = builder.header("Authorization", basicAuth("username", "password"));
        }

        if (path !=null) {
            String dashboard = NosqlBenchFiles.readFile(path);
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(dashboard));
        } else {
            builder = builder.GET();
        }

        HttpRequest request = builder.build();

        try {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("http response for configuring grafana:\n" + resp);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String startDocker(String IMG, String tag, String name, List<Integer> ports, List<String> volumeDescList, List<String> envList, List<String> cmdList, String reload) {
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withStatusFilter(List.of("exited"));
        listContainersCmd.getFilters().put("name", Arrays.asList(name));
        List<Container> stoppedContainers = null;
        try {
            stoppedContainers = listContainersCmd.exec();
            for (Container stoppedContainer : stoppedContainers) {
                String id = stoppedContainer.getId();
                logger.info("Removing exited container: " + id);
                dockerClient.removeContainerCmd(id).exec();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to contact docker, make sure docker is up and try again.");
            logger.error("If docker is installed make sure this user has access to the docker group.");
            logger.error("$ sudo gpasswd -a ${USER} docker && newgrp docker");
            System.exit(1);
        }

        Container containerId = searchContainer(name, reload);
        if (containerId != null) {
            return containerId.getId();
        }

        Info info = dockerClient.infoCmd().exec();
        dockerClient.buildImageCmd();

        String term = IMG.split("/")[1];
        //List<SearchItem> dockerSearch = dockerClient.searchImagesCmd(term).exec();
        List<Image> dockerList = dockerClient.listImagesCmd().withImageNameFilter(IMG).exec();
        if (dockerList.size() == 0) {
            dockerClient.pullImageCmd(IMG)
                    .withTag(tag)
                    .exec(new PullImageResultCallback()).awaitSuccess();

            dockerList = dockerClient.listImagesCmd().withImageNameFilter(IMG).exec();
            if (dockerList.size() == 0) {
                logger.error(String.format("Image %s not found, unable to automatically pull image." +
                                " Check `docker images`",
                        IMG));
                System.exit(1);
            }
        }
        logger.info("Search returned" + dockerList.toString());


        List<ExposedPort> tcpPorts = new ArrayList<>();
        List<PortBinding> portBindings = new ArrayList<>();
        for (Integer port : ports) {
            ExposedPort tcpPort = ExposedPort.tcp(port);
            Ports.Binding binding = new Ports.Binding("0.0.0.0", String.valueOf(port));
            PortBinding pb = new PortBinding(binding, tcpPort);

            tcpPorts.add(tcpPort);
            portBindings.add(pb);
        }

        List<Volume> volumeList = new ArrayList<>();
        List<Bind> volumeBindList = new ArrayList<>();
        for (String volumeDesc : volumeDescList) {
            String volFrom = volumeDesc.split(":")[0];
            String volTo = volumeDesc.split(":")[1];
            Volume vol = new Volume(volTo);
            volumeList.add(vol);
            volumeBindList.add(new Bind(volFrom, vol));
        }


        CreateContainerResponse containerResponse;
        if (envList == null) {
            containerResponse = dockerClient.createContainerCmd(IMG + ":" + tag)
                    .withCmd(cmdList)
                    .withExposedPorts(tcpPorts)
                    .withHostConfig(
                            new HostConfig()
                                    .withPortBindings(portBindings)
                                    .withPublishAllPorts(true)
                                    .withBinds(volumeBindList)
                    )
                    .withName(name)
                    //.withVolumes(volumeList)
                    .exec();
        } else {
            long user = new UnixSystem().getUid();
            containerResponse = dockerClient.createContainerCmd(IMG + ":" + tag)
                    .withEnv(envList)
                    .withExposedPorts(tcpPorts)
                    .withHostConfig(
                            new HostConfig()
                                    .withPortBindings(portBindings)
                                    .withPublishAllPorts(true)
                                    .withBinds(volumeBindList)
                    )
                    .withName(name)
                    .withUser(""+user)
                    //.withVolumes(volumeList)
                    .exec();
        }

        dockerClient.startContainerCmd(containerResponse.getId()).exec();

        return containerResponse.getId();

    }

    private Container searchContainer(String name, String reload) {

        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withStatusFilter(List.of("running"));
        listContainersCmd.getFilters().put("name", Arrays.asList(name));
        List<Container> runningContainers = null;
        try {
            runningContainers = listContainersCmd.exec();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to contact docker, make sure docker is up and try again.");
            System.exit(1);
        }

        if (runningContainers.size() >= 1) {
            //Container test = runningContainers.get(0);
            logger.info(String.format("The container %s is already running", name));

            logger.info(String.format("Hupping config"));

            if (reload != null) {
                post(reload, null, false);
            }

            return runningContainers.get(0);
        }
        return null;
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
