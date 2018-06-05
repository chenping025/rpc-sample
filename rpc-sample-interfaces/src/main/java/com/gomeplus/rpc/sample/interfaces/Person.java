package com.gomeplus.rpc.sample.interfaces;

import java.io.Serializable;

public class Person implements Serializable{

	private static final long serialVersionUID = -668430097073814304L;
	
	private String name;
	
	private String gender;
	
	private Integer age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", gender=" + gender + ", age=" + age
				+ "]";
	}
	
	

}
