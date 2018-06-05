package com.gomeplus.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gomeplus.rpc.common.bean.RpcRequest;
import com.gomeplus.rpc.common.bean.RpcResponse;
import com.gomeplus.rpc.common.coder.RpcDecoder;
import com.gomeplus.rpc.common.coder.RpcEncoder;

public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	
	private String host; //rpc服务端地址
	
	private Integer port; //端口
	
	private RpcResponse response; //服务端返回的调用结果
	
	private final Object obj = new Object();
	
	public RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * 连接rpcServer，并发送消息
	 * @param request
	 * @return
	 * @throws InterruptedException 
	 */
	public RpcResponse send(RpcRequest request) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			//装配客户端启动类
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new RpcEncoder(RpcRequest.class)); //out 编码请求
					channel.pipeline().addLast(new RpcDecoder(RpcResponse.class)); //in 收到响应时解码
					channel.pipeline().addLast(RpcClient.this); //赋值给response对象
				}
			}).option(ChannelOption.SO_KEEPALIVE, true);
			
			//连接服务器
			ChannelFuture future = bootstrap.connect(host, port).sync();
			//将request对象写入outBundle处理后发出（RpcEncoder处理器）
			future.channel().writeAndFlush(request).sync();
			
			//保证处理完服务端的返回结果后再关闭channel网络连接
			synchronized (obj) {
				obj.wait();
			}
			
			if (null != response) {
				future.channel().closeFuture().sync();
			}
			return response;
		} finally {
			group.shutdownGracefully();
		}
		
		
	}

	/**
	 * 读取服务端的返回结果
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response)
			throws Exception {
		this.response = response;
		//保证处理完服务端的返回结果后再关闭channel网络连接
		synchronized (obj) {
			obj.notifyAll();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LOGGER.error("rpcClient caught exception", cause);
	}
	
}
