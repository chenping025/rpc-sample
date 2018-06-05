package com.gomeplus.rpc.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.gomeplus.rpc.common.bean.RpcRequest;
import com.gomeplus.rpc.common.bean.RpcResponse;
import com.gomeplus.rpc.common.coder.RpcDecoder;
import com.gomeplus.rpc.common.coder.RpcEncoder;
import com.gomeplus.rpc.registry.ServiceRegistry;
import com.gomeplus.rpc.server.annotation.RpcService;

/**
 * 框架的rpc服务器，用于将要远程调用的服务为发布为rpc服务
 * 实现了ApplicationContextAware，spring构造该对象的时候会调用setApplicationContext方法，
 * 从而可以在方法中通过自定义获取用户要发布为rpc服务的业务接口和实现类。
 * 实现InitializingBean接口，构造的时候会调用afterPropertiesSet，
 * 在方法中可以启动netty服务器
 */
public class RpcServer implements ApplicationContextAware, InitializingBean{


	private static final Logger log = LoggerFactory.getLogger(RpcServer.class);

	private String serverAddress;
	private ServiceRegistry serviceRegistry;
	
	public RpcServer(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
		this.serverAddress = serverAddress;
		this.serviceRegistry = serviceRegistry;
	}
	
	//存储业务接口名称和实现类的对象
	private volatile Map<String, Object> handlerMap = new HashMap<String, Object>();
	
	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		
		//获取使用了注解的业务类
		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
		if (MapUtils.isNotEmpty(serviceBeanMap)) {
			for(Object serviceBean : serviceBeanMap.values()){
				String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
				handlerMap.put(interfaceName, serviceBean);
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		//构造netty服务端bootstrap
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			//server端引导类
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel channel) throws Exception {
					channel.pipeline().addLast(new RpcDecoder(new RpcRequest().getClass())); //in 解码
					channel.pipeline().addLast(new RpcEncoder(new RpcResponse().getClass())); //out 编码并响应给客户端
					channel.pipeline().addLast(new RpcHandler(handlerMap)); //in 处理业务逻辑，通过动态代理调用实现类的方法
				}
			}).option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
			
			//解析服务地址
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.valueOf(array[1]);
			
			//绑定服务器，sync阻塞直到服务器绑定完成
			ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
			log.debug("rpc-server开始监听，端口为：" + channelFuture.channel().localAddress());
			
			//向zk注册服务
			if (null != serviceRegistry) {
				serviceRegistry.registry(serverAddress);
			}
			//服务器阻塞等待关闭通道
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		
	}

}
