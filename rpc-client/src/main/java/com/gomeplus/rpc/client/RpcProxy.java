package com.gomeplus.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gomeplus.rpc.common.bean.RpcRequest;
import com.gomeplus.rpc.common.bean.RpcResponse;
import com.gomeplus.rpc.registry.ServiceDiscovery;

/**
 * rpc代理，创建rpc服务代理
 * 启动rpcClient和rpcServer通信，通过动态代理，调用业务实现类
 */
public class RpcProxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);
	
	private String serverAddress;
	
	private ServiceDiscovery serviceDiscovery;
	
	public RpcProxy(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public RpcProxy(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}
	
	/**
	 * 创建代理
	 * @param interfaceClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass){
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, 
				new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						//创建rpcRequest，封装被代理类的属性
						RpcRequest request = new RpcRequest();
						request.setRequestId(UUID.randomUUID().toString());
						//声明这个方法的业务接口名称
						request.setClassName(method.getDeclaringClass().getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);
						
						//从zk获取服务地址
						if (null != serviceDiscovery) {
							serverAddress = serviceDiscovery.discovery();
						}
						String[] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.valueOf(array[1]);
						//启动rpcClient并与服务端进行通信
						RpcClient client = new RpcClient(host, port);
						RpcResponse response = client.send(request);
						if (null != response.getError()) {
							throw response.getError();
						} else {
							return response.getResult();
						}
					}
				});
	}
}
