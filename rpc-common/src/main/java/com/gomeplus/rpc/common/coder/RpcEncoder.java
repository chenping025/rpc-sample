package com.gomeplus.rpc.common.coder;

import com.gomeplus.rpc.common.util.SerializationUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * rpc编码器（将对象转换为ByteBuf）
 */
@SuppressWarnings("rawtypes")
public class RpcEncoder extends MessageToByteEncoder {

	private Class<?> genericClass; //要编码的对象
	
	public RpcEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object inObj, ByteBuf out)
			throws Exception {
		//序列化
		if (genericClass.isInstance(inObj)) {
			byte[] data = SerializationUtil.serialize(inObj);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}

	
}
