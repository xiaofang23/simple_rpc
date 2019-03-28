package demo.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import demo.netty.NettyClient;
import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import demo.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@Component
public class RpcFactory implements InvocationHandler {

    @Autowired
    NettyClient nettyClient;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(method.getDeclaringClass().getName());
       // rpcRequest.setServiceName("demo.service.interfaces.UserService");
        rpcRequest.setMethodName(method.getName());
        logger.info("方法名为:{}",JSON.toJSONString(method.getName()));
        rpcRequest.setParametersType(method.getParameterTypes());
        logger.info("方法类型为:{}",JSON.toJSONString(method.getParameterTypes()));
        rpcRequest.setParameters(objects);
        logger.info("参数为:{}",JSON.toJSONString(objects));
        rpcRequest.setID(IdUtil.getId());
        Object result = nettyClient.send(rpcRequest);
        logger.info("发送对象:{}",JSON.toJSONString(result));
        Class<?> returnType = method.getReturnType();

        RpcResponse rpcResponse = JSON.parseObject(result.toString(),RpcResponse.class);
        logger.info("接受对象:{}",JSON.toJSONString(rpcResponse));
        if(rpcResponse.getCode()==1)
            throw new RuntimeException(rpcResponse.getError_msg());
        //判断是否为原始类型boolean、char、byte、short、int、long、float、double
        //以及是不是String的子类
        if(returnType.isPrimitive()|| String.class.isAssignableFrom(returnType)){
            return rpcResponse.getData();
        }
        //集合或者集合的子集
        else if(Collection.class.isAssignableFrom(returnType)){
            return JSONArray.parseArray(rpcResponse.getData().toString(),Object.class);
        }
        //Map或者Map的子集
        else  if(Map.class.isAssignableFrom(returnType)){
            return JSON.parseObject(rpcResponse.getData().toString(),Map.class);
        }else {
            Object data = rpcResponse.getData();
            return JSONObject.parseObject(data.toString(),returnType);
        }
    }


}
