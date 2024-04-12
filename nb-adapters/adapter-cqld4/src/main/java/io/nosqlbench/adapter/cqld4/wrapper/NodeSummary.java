/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.cqld4.wrapper;

import com.datastax.oss.driver.api.core.metadata.Node;

import java.net.InetSocketAddress;

public enum NodeSummary {
    none, addr, mid, all;

    public String summarize(Node node) {
        StringBuilder sb = new StringBuilder();
        sb.append(" bcaddr:").append(node.getBroadcastAddress().map(InetSocketAddress::toString).orElse(""));
        if (this == addr) return sb.toString();

        sb.append(" lsaddr:").append(node.getListenAddress().map(InetSocketAddress::toString).orElse(""));
        sb.append(" rpcaddr:").append(node.getBroadcastRpcAddress().map(InetSocketAddress::toString).orElse(""));
        sb.append(" DC:").append(node.getDatacenter());
        sb.append(" R:").append(node.getRack());
        sb.append(" SmV:").append(node.getSchemaVersion());
        sb.append(" dist:").append(node.getDistance());
        sb.append(" ").append(node.getState());
        sb.append(" conn:").append(node.getOpenConnections());

        if (this == mid) return sb.toString();

        sb.append(" up:").append(String.format("%.3fS", ((double) node.getUpSinceMillis()) / 1000.0d));
        sb.append(" reconn:").append(node.isReconnecting() ? "yes" : "no");
        sb.append(" extras:").append(node.getExtras());

        sb.append(" endpoit:").append(node.getEndPoint());
        sb.append(" host_id:").append(node.getHostId());

        return sb.toString();
    }
}
