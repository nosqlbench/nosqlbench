package io.nosqlbench.engine.docker;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;
import com.sun.security.auth.module.UnixSystem;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.nosqlbench.engine.docker.RestHelper.post;

public class DockerHelper {
    private static final String DOCKER_HOST = "DOCKER_HOST";
    private static final String DOCKER_HOST_ADDR = "unix:///var/run/docker.sock";
//    private Client rsClient = ClientBuilder.newClient();

    private final DockerClientConfig config;
    private final DockerClient dockerClient;
    private final Logger logger = LogManager.getLogger(DockerHelper.class);

    public DockerHelper() {
        System.getProperties().setProperty(DOCKER_HOST, DOCKER_HOST_ADDR);
        this.config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(DOCKER_HOST_ADDR).build();
        DockerCmdExecFactory dockerCmdExecFactory = new OkHttpDockerCmdExecFactory()
                .withReadTimeout(60000)
                .withConnectTimeout(60000);

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }

    public String startDocker(String IMG, String tag, String name, List<Integer> ports, List<String> volumeDescList, List<String> envList, List<String> cmdList, String reload, List<String> linkNames) {
        logger.info("Starting docker with img=" + IMG + ", tag=" + tag + ", name=" + name + ", " +
                "ports=" + ports + ", volumes=" + volumeDescList + ", env=" + envList + ", cmds=" + cmdList + ", reload=" + reload);

        boolean existingContainer = removeExitedContainers(name);

        Container containerId = searchContainer(name, reload, tag);
        if (containerId != null) {
            logger.debug("container is already up with the id: " + containerId.getId());
            return null;
        }

        boolean found;

        List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(IMG).exec();
        if (!found(images, IMG, List.of(tag))) {
            dockerClient.pullImageCmd(IMG)
                    .withTag(tag)
                    .exec(new PullImageResultCallback()).awaitSuccess();

            images = dockerClient.listImagesCmd().withImageNameFilter(IMG).exec();
            if (!found(images, IMG, List.of(tag))) {
                logger.error(String.format("Image %s not found, unable to automatically pull image." +
                                " Check `docker images`",
                        IMG));
                System.exit(1);
            }
        }


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

        List<Link> links = linkNames.stream().map(x -> new Link(x, x)).collect(Collectors.toList());
        CreateContainerCmd builder = dockerClient.createContainerCmd(IMG + ":" + tag);
        if (cmdList != null) {
            builder = builder.withCmd(cmdList);
        }

        builder = builder.withUser(String.valueOf(new UnixSystem().getUid()));
        builder = builder.withExposedPorts(tcpPorts);
        builder = builder.withHostConfig(new HostConfig()
                .withPortBindings(portBindings)
                .withPublishAllPorts(true)
                .withBinds(volumeBindList));
        builder = builder.withName(name);
        builder = builder.withLinks(links);
        if (envList != null) {
            builder = builder.withEnv(envList);
        }
        CreateContainerResponse containerResponse = builder.exec();
        dockerClient.startContainerCmd(containerResponse.getId()).exec();

        if (existingContainer) {
            logger.debug("Started existing container");
            return null;
        }

        return containerResponse.getId();

    }

    private boolean startStoppedContainer(String name) {
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withStatusFilter(List.of("stopped"));
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
            throw e;
        }

        return false;
    }

    private boolean removeExitedContainers(String name) {
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withStatusFilter(List.of("exited"));
        listContainersCmd.getFilters().put("name", Arrays.asList(name));
        List<Container> stoppedContainers = null;
        try {
            stoppedContainers = listContainersCmd.exec();
            for (Container stoppedContainer : stoppedContainers) {
                String id = stoppedContainer.getId();
                logger.info("Removing exited container: " + id);
                dockerClient.removeContainerCmd(id).exec();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to contact docker, make sure docker is up and try again.");
            logger.error("If docker is installed make sure this user has access to the docker group.");
            logger.error("$ sudo gpasswd -a ${USER} docker && newgrp docker");
            throw e;
        }
        return false;
    }

    public Container searchContainer(String name, String reload, String tag) {

        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withStatusFilter(List.of("running"));
        listContainersCmd.getFilters().put("name", Arrays.asList(name));
        List<Container> runningContainers = null;
        try {
            runningContainers = listContainersCmd.exec();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to contact docker, make sure docker is up and try again.");
            throw e;
        }

        if (runningContainers.size() >= 1) {
            //Container test = runningContainers.get(0);
            logger.info(String.format("The container %s is already running", name));

            logger.info(String.format("Hupping config"));

            if (reload != null) {
                try {
                    post(reload, null, false, "reload config");
                } catch (Exception e) {
                    logger.error(String.format("Unexpected config/state for docker container %s, consider removing the container", name));
                    throw e;
                }
            }

            return runningContainers.get(0);
        }
        return null;
    }

    public void pollLog(String containerId, ResultCallbackTemplate<LogContainerResultCallback, Frame> logCallback) {

        LogContainerResultCallback loggingCallback = new
                LogContainerResultCallback();

        LogContainerCmd cmd = dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withFollowStream(true)
                .withTailAll();

        final boolean[] httpStarted = {false};
        cmd.exec(logCallback);

        try {
            loggingCallback.awaitCompletion(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Error getting docker log and detect start for containerId: " + containerId);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private boolean found(List<Image> images, String label, List<String> tags) {
        List<String> validRepoTags = tags.stream().map(s -> label + ":" + s).collect(Collectors.toList());
        for (Image image : images) {
            String[] foundRepoTags = image.getRepoTags();
            for (String foundRepoTag : foundRepoTags) {
                for (String validRepoTag : validRepoTags) {
                    if (foundRepoTag.equals(validRepoTag)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
