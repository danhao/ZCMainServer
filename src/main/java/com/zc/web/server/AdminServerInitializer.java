package com.zc.web.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class AdminServerInitializer extends ChannelInitializer<SocketChannel> {
//    private ChannelHandler handler = null;

//    public HttpServerInitializer(ChannelHandler handler){
//    	this.handler = handler;
//    }
    
	@Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new AdminServerHandler());
    }
}