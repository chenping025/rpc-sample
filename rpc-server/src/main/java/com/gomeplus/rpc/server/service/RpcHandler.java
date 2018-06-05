package com.gomeplus.rpc.server.service;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gomeplus.rpc.common.bean.RpcRequest;
import com.gomeplus.rpc.common.bean.RpcResponse;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{

	private static final Logger log = LoggerFactory.getLogger(RpcHandler.class);
	
	private final Map<String, Object> handlerMap;
	
	public RpcHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}
	
	/**
	 * 接受消息，进行业务处理，并返回给客户端
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request)
			throws Exception {
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		
		try {
			//具体业务方法调用
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable e) {
			response.setError(e);
		}
		//写入outBundle(rpcEncoder),进入下一步编码处理后发送到channel中给客户端
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	/**
	 * 根据调用的具体类、方法名、参数类型、参数值通过反射调用实现类的方法
	 * @param request
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private Object handle(RpcRequest request) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		//接口名
		String className = request.getClassName();
		
		//拿到实现类实例
		Object serviceBean = handlerMap.get(className);
		
		//反射调用的方法名、参数类型、参数值
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		
		//接口类
		Class<?> interfaceClass = Class.forName(className);
		
		//调用实例类对象的指定方法并返回结果
		Method method = interfaceClass.getMethod(methodName, parameterTypes);
		return method.invoke(serviceBean, parameters);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("server caught exception", cause);
		ctx.close();
	}

}
