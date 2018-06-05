package com.gomeplus.rpc.sample.interfaces;


public interface HelloService {

	String hello(String name);
	
	String hello(Person person);
}
