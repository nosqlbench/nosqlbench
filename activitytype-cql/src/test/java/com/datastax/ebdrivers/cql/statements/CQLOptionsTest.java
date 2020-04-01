package com.datastax.ebdrivers.cql.statements;

import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.SpeculativeExecutionPolicy;
import io.nosqlbench.activitytype.cql.core.CQLOptions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CQLOptionsTest {

    @Test
    public void testSpeculative() {
        SpeculativeExecutionPolicy p1 = CQLOptions.speculativeFor("p99:5");
        assertThat(p1).isNotNull();
        SpeculativeExecutionPolicy p2 = CQLOptions.speculativeFor("p99:5:5000ms");
        assertThat(p2).isNotNull();
    }

    @Test
    public void testConstant() {
        SpeculativeExecutionPolicy p1 = CQLOptions.speculativeFor("5000ms:5");
        assertThat(p1).isNotNull();
    }

    @Test
    public void testWhitelist() {
        LoadBalancingPolicy lbp = CQLOptions.whitelistFor("127.0.0.1,127.0.0.2:123", null);
        assertThat(lbp).isNotNull();
    }

    @Test
    public void testSocketOptionPatterns() {
        SocketOptions so = CQLOptions.socketOptionsFor("read_timeout_ms=23423,connect_timeout_ms=2344;keep_alive:true,reuse_address:true;so_linger:323;tcp_no_delay=true;receive_buffer_size:100,send_buffer_size=1000");
        assertThat(so.getConnectTimeoutMillis()).isEqualTo(2344);
        assertThat(so.getKeepAlive()).isEqualTo(true);
        assertThat(so.getReadTimeoutMillis()).isEqualTo(23423);
        assertThat(so.getReceiveBufferSize()).isEqualTo(100);
        assertThat(so.getReuseAddress()).isEqualTo(true);
        assertThat(so.getSendBufferSize()).isEqualTo(1000);
        assertThat(so.getSoLinger()).isEqualTo(323);
        assertThat(so.getTcpNoDelay()).isEqualTo(true);

    }

    @Test
    public void testConnectionsPatterns() {
        PoolingOptions po = CQLOptions.poolingOptionsFor("2345");
        assertThat(po.getCoreConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(2345);
        assertThat(po.getMaxConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(Integer.MIN_VALUE);
        assertThat(po.getMaxRequestsPerConnection(HostDistance.LOCAL)).isEqualTo(Integer.MIN_VALUE);

        PoolingOptions po2 = CQLOptions.poolingOptionsFor("1:2:3,4:5:6");
        assertThat(po2.getCoreConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(1);
        assertThat(po2.getMaxConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(2);
        assertThat(po2.getMaxRequestsPerConnection(HostDistance.LOCAL)).isEqualTo(3);
        assertThat(po2.getCoreConnectionsPerHost(HostDistance.REMOTE)).isEqualTo(4);
        assertThat(po2.getMaxConnectionsPerHost(HostDistance.REMOTE)).isEqualTo(5);
        assertThat(po2.getMaxRequestsPerConnection(HostDistance.REMOTE)).isEqualTo(6);

        PoolingOptions po3 = CQLOptions.poolingOptionsFor("1:2:3,4:5:6,heartbeat_interval_s:100,idle_timeout_s:123,pool_timeout_ms:234");
        assertThat(po3.getIdleTimeoutSeconds()).isEqualTo(123);
        assertThat(po3.getPoolTimeoutMillis()).isEqualTo(234);
        assertThat(po3.getHeartbeatIntervalSeconds()).isEqualTo(100);

    }
}
