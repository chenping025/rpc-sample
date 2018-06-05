package com.gomeplus.rpc.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * rpc注解 （标注在服务实现上）
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME) //VM在运行期保留注解，通过注解可以读取到标识直接的接口，通过反射实例化服务类
@Component
public @interface RpcService {

	Class<?> value(); 
}
