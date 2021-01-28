package io.nosqlbench.activitytype.cql.statements.core;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;

class ProxyProtocolHander extends ChannelOutboundHandlerAdapter
{
    private final AtomicBoolean sent = new AtomicBoolean(false);
    private final HAProxyMessage message;

    ProxyProtocolHander(HAProxyMessage message) {
        this.message = message;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (sent.compareAndSet(false, true))
            HAProxyMessageEncoder.INSTANCE.write(ctx, message, ctx.voidPromise());

        super.write(ctx, msg, promise);
    }
}