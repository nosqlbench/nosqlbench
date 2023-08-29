/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.api.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import joptsimple.internal.Strings;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.net.*;
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
     * <p>
     * If you are needing an identifier for a node but do not with wish to expose any address data,
     * you can use the {@link #getNodeFingerprint()} which takes this value and hashes
     * it with SHA-1 to produce a hex string.
     *
     * @return A address for the node, likely to be unique and stable for its lifetime
     */
    public static String getNodeId() {
        return getMainInetAddrDirect().map(InetAddress::getHostAddress).orElse("UNKNOWN_HOST_ID");
    }


    private static Optional<InetAddress> getMainInetAddrDirect() {
        List<NetworkInterface> ifaces = getInterfacesDirect();
        Optional<NetworkInterface> first = ifaces.stream()
            .filter(i -> !i.getName().startsWith("docker"))
            .filter(i -> !i.getName().equals("lo"))
            .sorted((o1, o2) -> {
                if (o1.getName().startsWith("e") && o2.getName().startsWith("e")) return 0;
                if (o1.getName().startsWith("e")) return -1;
                if (o2.getName().startsWith("e")) return 1;
                return 0;
            }).findFirst();
        if (first.isEmpty()) return Optional.empty();

        Optional<InetAddress> firstInetAddrForInterface = first.get().getInterfaceAddresses().stream()
            .map(ia -> ia.getAddress())
            .sorted((i1, i2) -> {
                if (i1 instanceof Inet4Address && i2 instanceof Inet4Address) return 0;
                if (i1 instanceof Inet4Address) return -1;
                if (i2 instanceof Inet4Address) return 1;
                return 0;
            }).findFirst();
        return firstInetAddrForInterface;
    }


    /**
     * Using this to bypass OSHI because it calls logger init before we want it.
     * TODO: Maybe remove OSHI altogether if there is a reasonable Java HAL view in current Java editions.
     *
     * @return a list of network interfaces
     */
    private static List<NetworkInterface> getInterfacesDirect() {
        try {
            Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
            return new ArrayList<>(Collections.list(ni));
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Produce a stable string identifier consisting of hexadecimal characters.
     * The internal data used for this value is based on a stable ordering of non-local
     * ip addresses available on the system.
     *
     * @return A stable node identifier
     */
    public static String getNodeFingerprint() {
        String addrId = getNodeId();
        try {
            MessageDigest sha1_digest = MessageDigest.getInstance("SHA-1");
            byte[] addrBytes = sha1_digest.digest(addrId.getBytes(StandardCharsets.UTF_8));
            String fingerprint = "";
            for (int i = 0; i < addrBytes.length; i++) {
                fingerprint +=
                    Integer.toString((addrBytes[i] & 0xff) + 0x100, 16).substring(1);
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

    private final static String radixSymbols = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~-"; // 64 symbols, for 2^6!
    private final int brailleStart = '⠀';
    private final int brailleEnd = '⣿';
    private final int brailleRadix = brailleEnd - brailleStart;

    public static String getBrailleNodeId() {
        String nodeId = getNodeId();
        String[] fields = nodeId.split("\\.");
        byte[] addr;
        try {
            InetAddress inetAddr = Inet4Address.getByName(nodeId);
            addr = inetAddr.getAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return braille((addr[0] << 24) + (addr[1] << 16) + (addr[2] << 8) + addr[3]);
    }

    private static String braille(int value) {
        StringBuilder buf = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int mask = value & 0xF;
            value >>>= 8;
            int charat = '⠀' + mask;
            buf.append((char) charat);
        }
        return buf.toString();
    }

    private static String braille(long value) {
        StringBuilder buf = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int mask = (int) value & 0xF;
            value >>>= 8;
            int charat = '⠀' + mask;
            buf.append((char) charat);
        }
        return buf.toString();
    }

    public static String genSessionCode(long epochMillis) {
        return packLong(epochMillis) + "_" + getPackedNodeId();
    }

    public static String getPackedNodeId() {
        String nodeId = getNodeId();
        String[] fields = nodeId.split("\\.");
        byte[] addr;
        try {
            InetAddress inetAddr = Inet4Address.getByName(nodeId);
            addr = inetAddr.getAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return packLong(
            ((long) (addr[0] & 0xFF) << 24)
                + ((long) (addr[1] & 0xFF) << 16)
                + ((long) (addr[2] & 0xFF) << 8)
                + (addr[3] & 0xFF)

        );
    }

    public static String packLong(long bitfield) {
        String[] symbols =new String[12];
        int i = 11;
        for (; i >0 ; i--) {
            long tail = bitfield & 0b00111111;
            symbols[i]=""+radixSymbols.charAt((int) tail);
            bitfield >>>= 6;
            if (bitfield==0) break;
        }

        String result = Strings.join(Arrays.copyOfRange(symbols, i, symbols.length), "");
        return result;
    }

    public static String genSessionBits() {
        return getBrailleNodeId() + ":" + braille(System.currentTimeMillis());
    }
}
