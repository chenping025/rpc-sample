package com.gomeplus.rpc.sample.service;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 启动springContext，构造rpcService框架，将标有rpcService注解的
 * 业务类发布到rpcService中
 * @author 
 *
 */
public class RpcBootstrap {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("spring.xml");
	}
}
