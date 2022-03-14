/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb.api.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SystemId {

    /**
     * Return the address of a node which is likely to be unique enough to identify
     * it within a given subnet, after filtering out all local addresses. This is useful
     * when you are managing configuration or results for a set of systems which
     * share a common IP addressing scheme. This identifier should be stable as long
     * as the node's addresses do not change.
     *
     * If you are needing an identifier for a node but wish to expose any address data,
     * you can use the {@link #getNodeFingerprint()} which takes this value and hashes
     * it with SHA-1 to produce a hex string.
     * @return A address for the node, likely to be unique and stable for its lifetime
     */
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

    /**
     * Produce a stable string identifier consisting of hexadecimal characters.
     * The internal data used for this value is based on a stable ordering of non-local
     * ip addresses available on the system.
     * @return A stable node identifier
     */
    public static String getNodeFingerprint() {
        String addrId = getNodeId();
        try {
            MessageDigest sha1_digest = MessageDigest.getInstance("SHA-1");
            byte[] addrBytes = sha1_digest.digest(addrId.getBytes(StandardCharsets.UTF_8));
            String fingerprint = "";
            for (int i=0; i < addrBytes.length; i++) {
                fingerprint +=
                    Integer.toString( ( addrBytes[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return fingerprint.toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
