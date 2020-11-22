package io.nosqlbench.nb.api;

import oshi.SystemInfo;
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
                .filter(addr -> !(addr.startsWith("127." )))
                .findFirst();
        String systemID = first.orElse("UNKNOWN_SYSTEM_ID" );
        return systemID;
    }

}
