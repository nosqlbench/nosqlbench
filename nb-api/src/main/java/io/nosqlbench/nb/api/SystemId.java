package io.nosqlbench.nb.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.*;

public class SystemId {

    public static String getNodeId() {
        SystemInfo sysinfo = new SystemInfo();
        HardwareAbstractionLayer hal = sysinfo.getHardware();
        List<NetworkIF> interfaces = hal.getNetworkIFs();

        Optional<String> first = interfaces.stream()
                .filter(i -> !i.getName().startsWith("docker" ))
                .filter(i -> !i.getName().equals("lo" ))
                .sorted((o1, o2) -> {
                    if (o1.getName().startsWith("e" ) && o2.getName().startsWith("e" )) {
                        return 0;
                    }
                    if (o1.getName().startsWith("e" )) {
                        return -1;
                    }
                    if (o2.getName().startsWith("e" )) {
                        return 1;
                    }
                    return 0;
                })
            .flatMap(iface -> Arrays.stream(iface.getIPv4addr().clone()))
            .filter(addr -> !(addr.startsWith("127.")))
            .findFirst();
        String systemID = first.orElse("UNKNOWN_SYSTEM_ID");
        return systemID;
    }

    public static String getHostSummary() {
        SystemInfo sysinfo = new SystemInfo();
        HardwareAbstractionLayer hal = sysinfo.getHardware();
        CentralProcessor p = hal.getProcessor();

        Gson gson = new GsonBuilder().create();

        Set<String> ifspeeds = new HashSet<>();
        hal.getNetworkIFs().forEach(
            x -> {
                long spd = x.getSpeed();
                if (spd < (1024 * 1024 * 1000)) {
                    ifspeeds.add(String.format("%.0fMib", (double) (spd / (1024 * 1024))));
                } else {
                    ifspeeds.add(String.format("%.0fGib", (double) (spd / (1024 * 1024 * 1000))));
                }
            }
        );

        Map<String, Object> details = Map.of(
            "physical-cores", String.valueOf(p.getPhysicalProcessorCount()),
            "logical-cores", String.valueOf(p.getLogicalProcessors().size()),
            "max-frequency-ghz", String.format("%.2f", (p.getMaxFreq() / 1_000_000_000_000.0d)),
            "sockets", String.valueOf(p.getPhysicalPackageCount()),
            "processor-name", String.valueOf(p.getProcessorIdentifier().getName()),
            "memory-GiB", String.format("%.2f", hal.getMemory().getTotal() / (1024.0 * 1024.0 * 1024.0)),
            "heap-max-GiB", String.format("%.2f", Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0 * 1024.0)),
            "if-speeds", ifspeeds

        );

        return gson.toJson(details);

    }
}
