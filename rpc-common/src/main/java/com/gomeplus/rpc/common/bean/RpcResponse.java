package com.gomeplus.rpc.common.bean;

/**
 * 封装rpc 响应对象
 * 封装调用后的object对象
 */
public class RpcResponse {

	private String requestId;
	private Object result; //方法调用返回结果
	private Throwable error; //方法异常返回值
	
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public Throwable getError() {
		return error;
	}
	public void setError(Throwable error) {
		this.error = error;
	}
	
	
}
