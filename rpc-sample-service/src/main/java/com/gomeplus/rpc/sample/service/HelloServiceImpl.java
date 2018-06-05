package com.gomeplus.rpc.sample.service;

import com.gomeplus.rpc.sample.interfaces.HelloService;
import com.gomeplus.rpc.sample.interfaces.Person;
import com.gomeplus.rpc.server.annotation.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

	@Override
	public String hello(String name) {
		System.out.println("已经调用服务端接口实现，请求参数：" + name);
		return "hello, " + name;
	}

	@Override
	public String hello(Person person) {
		System.out.println("已经调用服务端接口实现，请求参数：" + person);
		return "hello, " + person.toString();
	}

}
