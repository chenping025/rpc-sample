package com.gomeplus.rpc.sample.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gomeplus.rpc.client.RpcProxy;
import com.gomeplus.rpc.sample.interfaces.HelloService;
import com.gomeplus.rpc.sample.interfaces.Person;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:spring.xml")
public class HelloServiceTest {
	
	@Autowired
	private RpcProxy rpcProxy;
	
	@Test
	public void testHelloService1() {
		HelloService helloService = rpcProxy.create(HelloService.class);
		String result = helloService.hello("world");
		System.out.println("rpc调用返回结果：" + result);
	}
	
	@Test
	public void testHelloService2() {
		HelloService helloService = rpcProxy.create(HelloService.class);
		Person person = new Person();
		person.setName("james");
		person.setGender("male");
		person.setAge(32);
		String result = helloService.hello(person);
		System.out.println("rpc调用返回结果：" + result);
	}

}
