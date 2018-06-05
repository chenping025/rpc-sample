package com.gomeplus.rpc.common.bean;

/**
 * 封装rpc请求对象
 * （封装需要通过反射创建对象的属性）
 */
public class RpcRequest {

	private String requestId;
	private String className; //接口名
	private String methodName; //方法名
	private Class<?>[] parameterTypes; //参数类型
	private Object[] parameters; //参数具体值
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public Object[] getParameters() {
		return parameters;
	}
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	
}
