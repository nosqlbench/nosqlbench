package com.datastax.driver.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;

public class TokenRangeUtil {

    private final Metadata clusterMetadata;
    private final ProtocolVersion protocolVersion;
    private final CodecRegistry codecRegistry;
    private final Token.Factory factory;
    private final Cluster cluster;

    public TokenRangeUtil(Cluster cluster) {
        this.cluster= cluster;
        clusterMetadata = cluster.getMetadata();
        protocolVersion = cluster.getConfiguration().getProtocolOptions().getProtocolVersion();
        codecRegistry = cluster.getConfiguration().getCodecRegistry();
        factory = Token.getFactory(clusterMetadata.partitioner);
    }

    public Set<TokenRange> getTokenRangesFor(String keyspace, String hostaddress) {
        Host host=null;
        if (hostaddress.matches("\\d+")) {
            int hostenum = Integer.parseInt(hostaddress);
            host = clusterMetadata.getAllHosts().stream()
                    .sorted(Comparator.comparing(h -> h.getAddress().toString()))
                    .skip(hostenum)
                    .findFirst()
                    .orElseThrow();
        } else if (!hostaddress.isEmpty()) {
            host = clusterMetadata.getAllHosts().stream()
                    .filter(h -> h.getAddress().toString().replaceAll("/","").equals(hostaddress))
                    .findFirst()
                    .orElseThrow();
        } else {
            throw new RuntimeException("You must specify a host enum in order or a host address.");
        }
        return clusterMetadata.getTokenRanges(keyspace,host);
    }


    public void printRanges(String tokensks) {
        Set<Host> hosts = clusterMetadata.getAllHosts();

        for (Host host : hosts) {
            String address = host.getAddress().toString().substring(1);
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("ranges-"+address));
                String ranges = getTokenRangesFor(tokensks, address).toString();
                writer.write(ranges);

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Can't write  token range files");
            }
        }

    }


    public M3PTokenFilter getFilterFor(Set<TokenRange> ranges) {
        return new M3PTokenFilter(ranges, this.cluster);
    }

}
